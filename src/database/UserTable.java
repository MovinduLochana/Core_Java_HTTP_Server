package database;

import java.io.Serializable;
import java.util.HashMap;

public record UserTable(HashMap<Integer, User> users) implements Serializable {

    record User(String name, String email) implements Serializable {

    }

    static int currentRow;

    boolean addUser(String name, String email) {
        return users.put(currentRow++, new User(name, email)) == null;
    }

    User removeUser(int id) {
        return users.remove(id);
    }

    User getUserById(int id) {
        return users.get(id);
    }

    void allUsers() {
        users.forEach((id, user) -> System.out.println("ID: " + id + ", Name: " + user.name + ", Email: " + user.email));
    }

}
