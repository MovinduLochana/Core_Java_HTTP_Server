
import config.Config;
import config.RouteBinder;
import controller.BasicController;
import database.Database;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * todo: create annotations for contexts and add Media type option
 * */
public class Main {

    private static final Logger logger = Config.getInstance().getLogger();

    public static void main(String[] args) throws IOException, ScriptException {

        var server = Server.getServer();

        var routeBinder = new RouteBinder(new BasicController());
        routeBinder.registerWithServer(server.getHttpServer());

        server.serveStaticContent();
        server.start();

    }
}