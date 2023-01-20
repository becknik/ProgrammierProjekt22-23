package Server;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.net.ServerSocket;
import java.net.Socket;



public class HttpServer  {
    private final int port;
    private Handler defaultHandler = null;
    // Two level map: first level is HTTP Method (GET, POST, OPTION, etc.), second level is the
    // request paths.
    private Map<String, Map<String, Handler>> handlers = new HashMap<>();

    // TODO SSL support
    public HttpServer(int port)  {
        this.port = port;
    }

    /**
     * @param path if this is the special string "/*", this is the default handler if
     *   no other handler matches.
     */
    public void addHandler(String method, String path, Handler handler)  {
        Map<String, Handler> methodHandlers = this.handlers.get(method);
        if (methodHandlers == null)  {
            methodHandlers = new HashMap<String, Handler>();
            this.handlers.put(method, methodHandlers);
        }
        methodHandlers.put(path, handler);
    }

    public void start() {
        try (ServerSocket socket = new ServerSocket(this.port)) {
            System.out.println("Listening on port " + this.port);
            Socket clientSocket;

            while ((clientSocket = socket.accept()) != null) {
                System.out.println("Received connection from " + clientSocket.getRemoteSocketAddress().toString());

                SocketHandler handler = new SocketHandler(clientSocket, this.handlers);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Failed to establish socket on port");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException  {
        HttpServer server = new HttpServer(8080);

        // Hello world response
        server.addHandler("GET", "/hello", new Handler()  {
            @Override
            public void handle(Request request, Response response) throws IOException  {
                String html = "It works, " + request.getParameter("name") + "";
                response.setResponseCode(200, "OK");
                response.addHeader("Content-Type", "text/html");
                response.addBody(html);
            }
        });

        //server.addHandler("GET", "/*", new Handler());  // Default handler
        server.addHandler("GET", "/*", new FileHandler());  // original line
        server.start();
    }
}
