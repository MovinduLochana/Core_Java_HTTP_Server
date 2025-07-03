# HTTP Server

A lightweight, custom HTTP server built in Java using `com.sun.net.httpserver`. 
This project demonstrates a robust server with features like routing, static file serving, JSON handling, and concurrent request processing.

## Features

- **Multiple Endpoints**:
    - `/`: Serves a welcome HTML page.
    - `/api/greet?name=<name>`: Returns a JSON greeting (GET).
    - `/api/data`: Accepts JSON data (POST) and echoes it back.
    - `/static/*`: Serves static files (e.g., HTML, CSS) from the `static` directory.
- **Configuration**: Loads port and static directory from `server.properties`.
- **Concurrency**: Uses a thread pool to handle multiple requests efficiently.
- **Logging**: Logs requests and errors to `server.log`.
- **Error Handling**: Returns appropriate HTTP status codes (e.g., 404, 405).
- **Query Parsing**: Supports URL query parameters.
- **JSON Support**: Handles JSON responses and POST request bodies.


## Configuration

Edit `server.properties` to change:

- `port`: Server port (default: 9000).
- `static.dir`: Directory for static files (default: `static_content`).

## Logging

Logs are written to `server.log` with timestamps, request details, and errors.

## Future Improvements

- Add authentication middleware.
- Support HTTPS with SSL/TLS.
- Implement RESTful CRUD operations.
- Add unit tests with JUnit.

## Why This Project?

This server demonstrates core backend development skills, including HTTP protocol handling, concurrency, configuration management, and logging;
Tryout virtual threads and new java features.