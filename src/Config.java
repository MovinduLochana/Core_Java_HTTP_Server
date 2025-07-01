import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.*;

public final class Config {

    /**
     *  todo: fix static naming
     *  todo: cleanup operations
     *  todo: check console handler
     *  todo: replace fis with nio
     * **/

    private static final String CONFIG = "F:\\Java\\NewProjects\\http_server\\src\\server.properties";
    private static final Properties serverProps = new Properties();

    private static final Logger LOGGER = Logger.getLogger(Config.class.getName());

    public static String STATIC_DIR = "static";
    public static int PORT = 9000;

    private static final Config instance = new Config();

    private Config() {
        System.out.println("Server properties loading");
        try (var fis = new FileInputStream(CONFIG)) {

            serverProps.load(fis);
            PORT = Integer.parseInt(serverProps.getProperty("port", "9000"));
            STATIC_DIR = serverProps.getProperty("static.dir", "static_content");

            var logFile = new FileHandler("server.log", true);
            logFile.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(logFile);

        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.severe(e.getMessage());
        } finally {
            System.out.println("Server properties loading ended");
            LOGGER.info("Server Configuration loaded");
        }
    }

    public static Config getInstance() {
        return instance;
    }

    public Properties getServerProperties() {
        return serverProps;
    }

    public Logger getLogger() {
        return LOGGER;
    }

}
