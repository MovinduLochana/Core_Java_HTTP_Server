import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class Server {

    /**
     * todo: replace Fixed Thread with Virtual Thread
     * */

    private static final Server instance =  new Server();
    private final HttpServer httpServer;

    private final Logger logger = Config.getInstance().getLogger();

    private Server() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(Config.PORT),0);
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        httpServer.setExecutor(Executors.newFixedThreadPool(4));
    }

    public static Server getServer() {
        return instance;
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP server started");
        logger.fine("Server started");
    }

    public void serveStatic(String path, String msg) {
        addRoute(path, (exchange) -> {
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, msg.getBytes().length);

            var os  = exchange.getResponseBody();
            os.write(msg.getBytes());
            os.close();
        });
    }

    public void handleGet(String path, String msg) {
        addRoute(path, (exchange) -> {
            String query = exchange.getRequestURI().getQuery();
            System.out.println(query);

            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, msg.getBytes().length);

            var os  = exchange.getResponseBody();
            os.write(msg.getBytes());
            os.close();
        });
    }

    public void handlePost(String path, String msg) {
    }

    public void addRoute(String path, HttpHandler handler) {
        httpServer.createContext(path, handler);
        logger.fine("Added route: " + path);
    }

    private Map<String, String> parseQuery(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        param -> URLDecoder.decode(param[0], StandardCharsets.UTF_8),
                        param -> URLDecoder.decode(param[1], StandardCharsets.UTF_8),
                        (v1, v2) -> v2 // only take last if duplicate
                ));
    }
}
