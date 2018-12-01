package cn.lightfish.proxy;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetSocket;


public class ProxyConnection {
    private final NetSocket clientSocket;
    private final NetSocket serverSocket;
    private NetworkTrafficRecorder record;
    private static final Logger logger = LoggerFactory.getLogger(ProxyConnection.class);
    private volatile boolean isClient = false;

    public void sendSQL(String sql) {
        sql += ";";
        System.out.format("send: %s\n", sql);
        byte[] bytes = sql.getBytes();
        Buffer buffer = Buffer.buffer(bytes.length + 4);
        writeFixInt(buffer, 3, bytes.length + 1);
        byte packetId = 0;
        byte com_query = 3;
        buffer.appendUnsignedByte(packetId);
        buffer.appendUnsignedByte(com_query);
        buffer.appendBytes(bytes);
        //System.out.format("%s -> %s\n",sql,Arrays.toString(buffer.getBytes()));
        record(Direction.CLIENT_2_SERVER, buffer);
        serverSocket.write(buffer);
    }

    public void writeFixInt(Buffer buffer, int length, long val) {
        for (int i = 0; i < length; i++) {
            byte b = (byte) ((val >> (i * 8)) & 0xFF);
            buffer.appendUnsignedByte(b);
        }
    }

    public ProxyConnection(NetSocket clientSocket, NetSocket serverSocket, NetworkTrafficRecorder record) {
        this.clientSocket = clientSocket;
        this.serverSocket = serverSocket;
        this.record = record;
        serverSocket.closeHandler(v -> clientSocket.close());
        clientSocket.closeHandler(v -> serverSocket.close());
        serverSocket.exceptionHandler(this::close);
        clientSocket.exceptionHandler(this::close);
        clientSocket.handler(buffer -> {
            record(Direction.CLIENT_2_SERVER, buffer);
            this.setClient(false);
            serverSocket.write(buffer);
        });
        serverSocket.handler(buffer -> {
            record(Direction.SERVER_2_CLUENT, buffer);
            if (!isClient)
                clientSocket.write(buffer);
        });
    }

    private void record(Direction direction, Buffer buffer) {
        if (record != null) {
            record.record(direction, buffer);
        }
    }

    private void close(Throwable e) {
        logger.error(e.getMessage(), e);
        clientSocket.close();
        serverSocket.close();
        if (record != null) {
            record.close();
        }
    }

    public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean client) {
        isClient = client;
        System.out.format("set client %s\n",Boolean.valueOf(isClient).toString());
    }
}
