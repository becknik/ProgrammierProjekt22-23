package Server;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private HttpServer server;

    public Server() {
        // TODO what is backlog
        /*
        Back Logging
        When a server accepts a client request, this request first will be queued by the operating system.
        Later, it will be given to the server to process the request. All of these simultaneous requests will be queued
        by the operating system. However, the operating system will decide how many of these requests can be queued at
        any given point in time. This value represents back logging. In our example, this value is 0, which means that
        we do not queue any requests.
         */
        try {
            this.server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        } catch (IOException e) {
            System.err.println("Failed to create new HttpServer instance");
            e.printStackTrace();
        }

        HttpContext startContext = this.server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String requestedMethod = exchange.getRequestMethod();
                if (!"GET".equals(requestedMethod)) exchange.sendResponseHeaders(505,3);

            }
        });

        //HttpExchange test =
        //HttpHandler handler = Htt

        this.server.start();
    }

    public static void main(String... args) {
        Server server = new Server();
    }
}
