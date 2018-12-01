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
    ProxyConnection connection;
    private static final Logger logger = LoggerFactory.getLogger(MySQLD.class);
    public MySQLD(ProxyConnection connection) {
        this.connection = connection;
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.useDelimiter(";");
            while (scanner.hasNextLine()) {
                String cmd = scanner.next();
                switch (cmd) {
                    default:
                        connection.setClient(this);
                        sendSQL(cmd);
                        connection.setClient(null);
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
        connection.sendBuffer2Server(buffer);
    }

    public void writeFixInt(io.vertx.core.buffer.Buffer buffer, int length, long val) {
        for (int i = 0; i < length; i++) {
            byte b = (byte) ((val >> (i * 8)) & 0xFF);
            buffer.appendUnsignedByte(b);
        }
    }
}
