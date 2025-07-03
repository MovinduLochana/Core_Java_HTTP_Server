package config;

import annotation.PathVariable;
import annotation.RequestBody;
import annotation.RequestParam;
import annotation.WebRoute;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteBinder {
    private final Map<String, Map<String, Route>> staticRoutes = new HashMap<>();
    private final List<Route> dynamicRoutes = new ArrayList<>();
    private final Object controller;
    public RouteBinder(Object controller) {
        this.controller = controller;
        registerRoutes();
    }

    private void registerRoutes() {
        for (var method : controller.getClass().getDeclaredMethods()) {

            if (method.isAnnotationPresent(WebRoute.class)) {

                var routePath = method.getAnnotation(WebRoute.class);

                String path = routePath.path();
                String httpMethod = routePath.method().toUpperCase();

                var route = new Route(method, path, httpMethod);

                if (route.isStatic) {
                    staticRoutes.computeIfAbsent(path, key -> new HashMap<>())
                            .put(httpMethod, route);
                } else {
                    dynamicRoutes.add(route);
                }
            }
        }
    }

    public void registerWithServer(HttpServer server) {

        // static
        for (var entry : staticRoutes.entrySet()) {

            var path = entry.getKey();

            server.createContext(path, ex -> {

                var reqMeth = ex.getRequestMethod().toUpperCase();
                var route = staticRoutes.get(path).get(reqMeth);

                if (route != null) {
                    try {
                        var args = buildMethodArguments(ex, route, null);
                        route.method.invoke(controller, args);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        sendResponse(ex, 500, "text/plain", "Server Error" + e.getMessage());
                        System.out.println(e.getMessage());
                    }
                } else {
                    sendResponse(ex, 405, "text/plain", "Method Not Allowed");
                }

            });

        }

        var dynamicRouteGroups = new HashMap<String, List<Route>>();
        for (var route : dynamicRoutes) {
            dynamicRouteGroups.computeIfAbsent(
                            route.path.startsWith("/") ? route.path.split("\\{")[0] : "/" + route.path.split("\\{")[0],
                            key -> new ArrayList<>()
                    )
                    .add(route);
        }

        for(var entry : dynamicRouteGroups.entrySet()) {

            server.createContext(entry.getKey(), ex -> {
                var reqPath = ex.getRequestURI().getPath();
                var method = ex.getRequestMethod().toUpperCase();

                for(var route : entry.getValue()) {
                    if(route.httpMethod.equals(method)) {

                        var matcher = route.pathPattern.matcher(reqPath);
                        if(matcher.matches()) {
                            try {
                                var args = buildMethodArguments(ex, route, matcher);
                                route.method.invoke(controller, args);
                                return;
                            } catch (InvocationTargetException | IllegalAccessException e){
                                System.out.println(e.getMessage());
                                sendResponse(ex, 500, "text/plain", "Server Error" + e.getMessage());
                                return;
                            }
                        }
                    }
                }
                sendResponse(ex, 404, "text/plain", "Not Found");
            });

        }
    }

    private Object[] buildMethodArguments(HttpExchange exchange, Route route, Matcher matcher) throws IOException {
        var parameters = route.method.getParameters();
        var args = new Object[parameters.length];
        var queryParams = parseQuery(exchange.getRequestURI().getQuery());

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = parameters[i].getAnnotation(PathVariable.class);
                String varName = pv.value();
                args[i] = matcher != null ? matcher.group(varName) : null;
            } else if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                InputStream is = exchange.getRequestBody();
                args[i] = new String(is.readAllBytes());
            } else if (parameters[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = parameters[i].getAnnotation(RequestParam.class);
                String paramName = rp.value();
                args[i] = queryParams.getOrDefault(paramName, rp.defaultValue());
            } else if (parameters[i].getType().equals(HttpExchange.class)) {
                args[i] = exchange;
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length >= 2) {
                    params.put(pair[0], pair[1]);
                } else if (pair.length == 1) {
                    params.put(pair[0], "");
                }
            }
        }
        return params;
    }

//    private Map<String, String> parseQuery(String query) {
//        return Arrays.stream(query.split("&"))
//                .map(param -> param.split("=", 2))
//                .collect(Collectors.toMap(
//                        param -> URLDecoder.decode(param[0], StandardCharsets.UTF_8),
//                        param -> URLDecoder.decode(param[1], StandardCharsets.UTF_8),
//                        (v1, v2) -> v2 // only take last if duplicate
//                ));
//    }

    private void sendResponse(HttpExchange exchange, int status, String contentType, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, response.getBytes().length);
        try (var os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static class Route {
        Method method;
        Pattern pathPattern;
        String httpMethod;
        boolean isStatic;
        String path;
        List<String> pathVariableNames;

        Route(Method method, String path, String httpMethod) {
            this.method = method;
            this.httpMethod = httpMethod.toUpperCase();
            this.pathVariableNames = new ArrayList<>();
            this.path = path;
            this.isStatic = !path.contains("{");
            this.pathPattern = Pattern.compile(convertPathToRegex(path));
        }

        private String convertPathToRegex(String path) {
            if (path.contains("{")) {
                String regex = path.replaceAll("\\{([^}]+)\\}", "(?<$1>[^/]+)");
                return "^" + regex + "$";
            }
            return "^" + Pattern.quote(path) + "$";
        }
    }
}