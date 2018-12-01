package cn.lightfish.proxy;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Scanner;
/**
 * cjw
 * 294712221@qq.com
 */
public class MySQLD {
    ProxyMonitor ProxyMonitor;
    private static final Logger logger = LoggerFactory.getLogger(MySQLD.class);
    public MySQLD(ProxyMonitor ProxyMonitor) {
        this.ProxyMonitor = ProxyMonitor;
    }

    public void inactive() {
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.useDelimiter(";");
            while (scanner.hasNextLine()) {
                String cmd = scanner.next();
                if (ProxyMonitor.connection==null){
                    logger.error("请使用源客户端建立mysql客户端连接!!!!");
                    continue;
                }
                switch (cmd) {
                    default:
                        ProxyMonitor.connection.setClient(this);
                        sendSQL(cmd);
                        ProxyMonitor. connection.setClient(null);
                }

            }
        }
    }

    public void revResp(Buffer buffer){

    }
    public void sendSQL(String sql) {
        sql += ";";
        byte[] bytes = sql.getBytes();
        io.vertx.core.buffer.Buffer buffer = io.vertx.core.buffer.Buffer.buffer(bytes.length + 4);
        writeFixInt(buffer, 3, bytes.length + 1);
        byte packetId = 0;
        byte com_query = 3;
        buffer.appendUnsignedByte(packetId);
        buffer.appendUnsignedByte(com_query);
        buffer.appendBytes(bytes);
        ProxyMonitor.connection.sendBuffer2Server(buffer);
    }

    public void writeFixInt(io.vertx.core.buffer.Buffer buffer, int length, long val) {
        for (int i = 0; i < length; i++) {
            byte b = (byte) ((val >> (i * 8)) & 0xFF);
            buffer.appendUnsignedByte(b);
        }
    }
}
