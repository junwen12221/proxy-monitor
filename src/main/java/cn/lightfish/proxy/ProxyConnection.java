package cn.lightfish.proxy;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetSocket;

/**
 * cjw
 * 294712221@qq.com
 */
public class ProxyConnection {
    public final NetSocket clientSocket;
    public final NetSocket serverSocket;
    private static final Logger logger = LoggerFactory.getLogger(ProxyConnection.class);
    public MySQLD mySQLD;
    public ProxyMonitor monitor;



    public ProxyConnection(NetSocket clientSocket, NetSocket serverSocket,ProxyMonitor monitor) {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
        this.monitor = monitor;
        serverSocket.closeHandler(v -> clientSocket.close());
        clientSocket.closeHandler(v -> serverSocket.close());
        serverSocket.exceptionHandler(this::close);
        clientSocket.exceptionHandler(this::close);
        clientSocket.handler(buffer -> {
            try {
                this.setClient(null);
                sendBuffer2Server(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        serverSocket.handler(buffer -> {
            try {
                record(Direction.SERVER_2_CLIENT, buffer);
                if (mySQLD == null)
                    clientSocket.write(buffer);
                else
                    mySQLD.revResp(buffer);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    public void sendBuffer2Server(Buffer buffer){
        record(Direction.CLIENT_2_SERVER, buffer);
        serverSocket.write(buffer);
    }

    private void record(Direction direction, Buffer buffer) {
        if (monitor.recorder != null) {
            monitor.recorder.record(direction, buffer);
        }
    }

    private void close(Throwable e) {
        logger.error(e.getMessage(), e);
        clientSocket.close();
        serverSocket.close();
        if (monitor.recorder != null) {
            monitor.recorder.close();
        }
    }


    public void setClient(MySQLD mySQLD) {
       boolean  isClient = mySQLD!=null;
        System.out.format("set client %s\n", Boolean.valueOf(isClient).toString());
    }
}
