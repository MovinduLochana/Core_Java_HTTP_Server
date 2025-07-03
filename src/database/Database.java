package database;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;
import config.Config;

public class Database {

    private static Database instance;

    private final Logger logger = Config.getInstance().getLogger();
    private final UserTable userTable;

    private Database() throws IOException, ClassNotFoundException {

        var dbPath = Paths.get(Config.DATABASE_FILE);

        if (!dbPath.toFile().exists()) {
            userTable = new UserTable(new HashMap<>());
        }
        else {

            try(var ois = new ObjectInputStream(Files.newInputStream(dbPath))) {
                userTable = (UserTable) ois.readObject();
                logger.info("Database loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                logger.severe("Failed to load database: " + e.getMessage());
                throw e;
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try(var oos = new ObjectOutputStream(Files.newOutputStream(dbPath))) {
                oos.writeObject(userTable);
                logger.info("Database saved successfully.");
            } catch (IOException e) {
                logger.severe("Failed to save database: " + e.getMessage());
            }
        }));

        userTable.allUsers();
    }

    public static Database getInstance() {
        if (instance == null) {
            try {
                instance = new Database();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Database file not found: " + Config.DATABASE_FILE);
                throw new RuntimeException("Database file not found: " + Config.DATABASE_FILE, e);
            }
        }
        return instance;
    }

    public boolean insert(String name, String email) {
        return userTable.addUser(name, email);
    }

    public boolean update(String name, String email) {
        return !userTable.addUser(name, email);
    }

    public String[] getUserById(int id) {
        var x = userTable.getUserById(id);

        return new String[]{x.name(), x.email()};
    }

}
