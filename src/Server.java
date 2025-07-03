import com.sun.net.httpserver.HttpServer;
import config.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public final class Server {
    /**
     * todo: replace Fixed Thread with Virtual Thread
     */
    private static final Server instance = new Server();
    private final HttpServer httpServer;

    private final Logger logger = Config.getInstance().getLogger();

    private Server() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(Config.getInstance().getPort()), 0);
        } catch (IOException e) {
            logger.severe(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        httpServer.setExecutor(Executors.newFixedThreadPool(4));
    }

    public static Server getServer() {
        return instance;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }


    public void start() {
        httpServer.start();
        System.out.println("HTTP server started");
        logger.fine("Server started");
    }

    public void serveStaticContent() {
        String staticDir = Config.STATIC_DIR;
        Path staticPath = Paths.get(staticDir).toAbsolutePath();

        if (!Files.exists(staticPath)) {
            logger.warning("Static directory does not exist: " + staticDir);
            return;
        }

        httpServer.createContext("/static", exchange -> {
//            String filePath = exchange.getRequestURI().getPath().replace("/static", "");
//            Path file = staticPath.resolve(filePath).normalize();

//            if (!Files.exists(file) || !file.startsWith(staticPath)) {
//                exchange.sendResponseHeaders(404, -1);
//                return;
//            }

            byte[] content = Files.readAllBytes(staticPath);
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, content.length);

            var os = exchange.getResponseBody();
            os.write(content);
            os.close();
        });

        System.out.println(staticPath);
    }
}
