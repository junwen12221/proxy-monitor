package cn.lightfish.proxy;

import java.util.Scanner;

public class MySQLD {
    ProxyConnection connection;

    public MySQLD(ProxyConnection connection) {
        this.connection = connection;
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.useDelimiter(";");
            while (scanner.hasNextLine()) {
                String cmd = scanner.next();
                switch (cmd) {
                    default:
                        connection.setClient(true);
                        connection.sendSQL(cmd);
                }

            }
        }
    }
}
