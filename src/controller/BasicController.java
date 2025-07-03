package controller;

import annotation.PathVariable;
import annotation.RequestBody;
import annotation.RequestParam;
import annotation.WebRoute;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.logging.Logger;

public class BasicController {
    private static final Logger LOGGER = Logger.getLogger(BasicController.class.getName());

    @WebRoute(path = "/")
    public void handleRoot(HttpExchange exchange) throws IOException {
        String response = "<html><body><h1>Welcome to Advanced HTTP Server!</h1>" +
                "<p>Try <a href=\"/api/greet?name=User\">/api/greet?name=User</a> or " +
                "<a href=\"/api/user/123\">/api/user/123</a></p></body></html>";
        sendResponse(exchange, 200, "text/html", response);
        LOGGER.info("Served root page to " + exchange.getRemoteAddress());
    }

    @WebRoute(path = "/api/greet")
    public void handleGreet(HttpExchange exchange, @RequestParam(value = "name", defaultValue = "Guest") String name) throws IOException {
        String response = "{\"message\": Hello, " + name + "!}";
        sendResponse(exchange, 200, "application/json", response);
        LOGGER.info("Greeted " + name + " from " + exchange.getRemoteAddress());
    }

    @WebRoute(path = "/api/user/{id}")
    public void handleUser(HttpExchange exchange, @PathVariable("id") String userId) throws IOException {
        String response = "{\"userId\": \"" + userId + "\", \"message\": \"User details for ID " + userId + "\"}";
        sendResponse(exchange, 200, "application/json", response);
        LOGGER.info("Served user details for ID " + userId + " to " + exchange.getRemoteAddress());
    }

    @WebRoute(path = "/api/data", method = "POST")
    public void handleData(HttpExchange exchange, @RequestBody String body) throws IOException {
        String response = "{\"received\": " + body + "}";
        sendResponse(exchange, 200, "application/json", response);
        LOGGER.info("Processed POST data from " + exchange.getRemoteAddress());
    }

    private void sendResponse(HttpExchange exchange, int status, String contentType, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, response.getBytes().length);
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}