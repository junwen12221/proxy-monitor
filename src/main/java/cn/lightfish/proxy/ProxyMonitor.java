package cn.lightfish.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ProxyMonitor extends AbstractVerticle {
    private int sourcePort = 8066;
    private int targetPort = 3306;

    public ProxyMonitor(int sourcePort, int targetPort, String targetHost) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.targetHost = targetHost;
        this.connection = connection;
    }

    private String targetHost = "localhost";
    private static final Logger logger = LoggerFactory.getLogger(ProxyMonitor.class);
    ProxyConnection connection;

    public static void main(String[] args) throws Exception{
        String s = new String(Files.readAllBytes(Paths.get("config.json").toAbsolutePath()));
        JsonObject jsonObject = new JsonObject(s);
        Vertx.vertx().deployVerticle(new ProxyMonitor(
                jsonObject.getInteger("sourcePort"),
                jsonObject.getInteger("targetPort"),
                jsonObject.getString("targetHost")
        ));
    }

    public void afterStart() {
        new Thread(() -> {
            new MySQLD(connection);
        }).start();
    }

    @Override
    public void start() throws Exception {
        NetServer netServer = vertx.createNetServer();
        NetClient netClient = vertx.createNetClient();
        NetworkTrafficRecorder recorder = new NetworkTrafficRecorder();
        netServer.connectHandler(socket -> netClient.connect(targetPort, targetHost, result -> {
            if (result.succeeded()) {
                connection = new ProxyConnection(socket, result.result(), recorder);
                afterStart();
            } else {
                logger.error(result.cause().getMessage(), result.cause());
                socket.close();
            }
        }));
        netServer.listen(sourcePort, listenResult -> {
            if (listenResult.succeeded()) {
                logger.info("ProxyMonitor is running!");
            } else {
                logger.error("ProxyMonitor exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
                System.exit(1);
            }
        });
    }
}
