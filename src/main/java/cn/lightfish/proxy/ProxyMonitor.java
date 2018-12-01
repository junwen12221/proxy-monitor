package cn.lightfish.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * cjw
 * 294712221@qq.com
 */
public class ProxyMonitor extends AbstractVerticle {
    private int sourcePort = 8066;
    private int targetPort = 3306;
    public NetworkTrafficRecorder recorder;

    public ProxyMonitor(int sourcePort, int targetPort, String targetHost,
                        NetworkTrafficRecorder recorder) {
        this.sourcePort = sourcePort;
        this.targetPort = targetPort;
        this.targetHost = targetHost;
        this.connection = connection;
        this.recorder = recorder;
    }

    private String targetHost = "localhost";
    private static final Logger logger = LoggerFactory.getLogger(ProxyMonitor.class);
    ProxyConnection connection;

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("config.json").toAbsolutePath();
        logger.info("读取配置路径:"+path);
        String s = new String(Files.readAllBytes(path));
        logger.info(s);
        JsonObject jsonObject = new JsonObject(s);
        NetworkTrafficRecorder recorder = new NetworkTrafficRecorder();
        ProxyMonitor proxyMonitor = new ProxyMonitor(
                jsonObject.getInteger("sourcePort"),
                jsonObject.getInteger("targetPort"),
                jsonObject.getString("targetHost"), recorder
        );
        Vertx.vertx().deployVerticle(proxyMonitor);

    }

    public void afterStart() {
        //new MySQLD(connection);
    }

    @Override
    public void start() throws Exception {
        NetServer netServer = vertx.createNetServer();
        NetClient netClient = vertx.createNetClient();
        netServer.connectHandler(socket -> netClient.connect(targetPort, targetHost, result -> {
            if (result.succeeded()) {
                connection = new ProxyConnection(socket, result.result(), this);
                afterStart();
            } else {
                logger.error(result.cause().getMessage(), result.cause());
                socket.close();
            }
        }));
        netServer.listen(sourcePort, listenResult -> {
            if (listenResult.succeeded()) {
                new Thread(()->{
                    MySQLD mySQLD = new MySQLD(this);
                    mySQLD.inactive();
                    logger.info("ProxyMonitor is Running!!!");
                }).start();
                logger.info("listening post:"+sourcePort+" successfully!");
            } else {
                logger.error("ProxyMonitor exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
                System.exit(1);
            }
        });
    }
}
