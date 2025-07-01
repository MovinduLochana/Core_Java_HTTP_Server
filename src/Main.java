import java.io.IOException;
import java.util.logging.Logger;

/**
 * todo: create annotations for contexts and add Media type option
 * */
public class Main {

    private static final Logger logger = Config.getInstance().getLogger();

    public static void main(String[] args) throws IOException {

        var server = Server.getServer();

        server.serveStatic("/",
                """
                     <html>
                     <body> Hello </body>
                     </html>
                    """);

        server.handleGet("/api/data", "data");
//        server.handlePost("/api/user", "user");

        server.start();
    }
}