package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.net.ServerSocket;
import java.net.Socket;


public class HttpServer {
	private final int port;

	// Two level map: first level is HTTP Method (GET, POST, OPTION, etc.), second level is the
	// request paths.
	private final Map<String, Map<String, Handler>> handlers = new HashMap<>();

	// TODO SSL support

	/**
	 * Initializes a server on the specified port number & initializes a empty map to the HTTP methods a client could call as keys
	 * @param port The port the server sohuld listen to
	 */
	public HttpServer (final int port) {
		this.port = port;

		// Initialize HTTP Methods TODO check if all methods covered/ the methods are even usable
		this.handlers.put("GET", new HashMap<>());
		this.handlers.put("OPTIONS", new HashMap<>());
		this.handlers.put("HEAD", new HashMap<>());
		this.handlers.put("POST", new HashMap<>());
		this.handlers.put("PUT", new HashMap<>());
		this.handlers.put("DELETE", new HashMap<>());
		this.handlers.put("TRACE", new HashMap<>());
		this.handlers.put("CONNECT", new HashMap<>());
	}

	/**
	 * Adds a handler to this servers map, using the HTTP method name to get the second level map, which contains path & handler.
	 * If the handler to a path is already set, the handler is substituted & an error message is print to this servers log.
	 * Else the path & handler form key & value pair in the map.
	 */
	public void addHandler (String method, String path, Handler handler) {

		// Gets the map of path & corresponding handler to the given HTTP method
		Map<String, Handler> HttpMethodHandlers = this.handlers.get(method);
		// If the handler gets replaced, previousHandler is != null
		Handler previousHandler = HttpMethodHandlers.put(path, handler);

		if (previousHandler != null)
			System.err.println("Added a new handler for the path " + path + " and dropped the old one.");

		//this.handlers.put(method, HttpMethodHandlers); // This should be oboslete due to references (wtf?)
	}

	/**
	 * Starts the server on a socket & listens for client sockets to accept.
	 * If a client socket connects, a new Thread containing a handler form {@code this.handlers} & socket is started.
	 */
	public void start () {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			System.err.println("Failed to establish server connection on port " + this.port);
			e.printStackTrace();
		}

		while (true) {
			System.out.println("INFO:\tListening on port " + this.port);

			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Failed to establish socket to client " + this.port);
			}
			System.out.println("INFO:\tReceived connection from " + clientSocket.getRemoteSocketAddress().toString());

			SocketHandler handler = new SocketHandler(clientSocket, this.handlers);

			new Thread(handler).start();
		}
	}

	public static void main (String... args) {
		HttpServer server = new HttpServer(8080);

		// Adding the "Hello World" test response to this servers Resolving map
		server.addHandler("GET", "/hello", (request, response) -> {
			response.setResponseCode(200, "OK");
			response.addHeader("Content-Type", "text/html");

			String htmlString = "<h1>Hello World!</h1><br>It seems to work!<br>URL: " + request.getFullUrl() +
					"HTTP Method:" + request.getMethod();
			response.addBody(htmlString);
		});

		//server.addHandler("GET", "/*", new Handler());  // Default handler
		server.addHandler("GET", "/*", new SimpleFileHandler());  // original line
		server.start();
	}
}
