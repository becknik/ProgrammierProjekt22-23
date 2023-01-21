package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;

public class MainServer {
	private final HttpHandler websiteHandler = exchange -> {
		//System.out.println(exchange.getRequestHeaders()); // Let's see, what this prints out
		// The heck is getRequestBody() for?
		//exchange.getRequestBody().readAllBytes() seems to be empty

		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0); // Variable length response is to be sent

		File website = new File(System.getProperty("user.dir") + "/website/website.html");
		FileInputStream websiteStream = new FileInputStream(website);

		OutputStream responseStream = exchange.getResponseBody();
		responseStream.write(websiteStream.readAllBytes());

		responseStream.close();
		websiteStream.close();
	};

	private final HttpHandler peripheryFileHandler = exchange -> {
		exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

		URI request = exchange.getRequestURI();

		File peripheryFile = new File(System.getProperty("user.dir") + "/website" + request.getPath());
		FileInputStream websiteStream = new FileInputStream(peripheryFile);

		OutputStream responseStream = exchange.getResponseBody();
		responseStream.write(websiteStream.readAllBytes());

		responseStream.close();
		websiteStream.close();
	};

	private final HttpHandler requestHandler = exchange -> {

		byte[] requestBody = exchange.getRequestBody().readAllBytes();
		String message = new String(requestBody);
		System.out.println(message);

		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED,-1);
	};

	final private HttpServer httpServer;

	public MainServer()
	{
        /*
        All simultaneous requests will be queued by the operating system.
        However, the operating system will decide how many of these requests can be queued at any given point in time.
        This value represents back logging.
         */

		HttpServer httpServerButMaybeNot; // For final attribute initialization

		try {
			InetSocketAddress socketAddress = new InetSocketAddress(8080);
			httpServerButMaybeNot = HttpServer.create(socketAddress, 0); // OS default backlogging queue length
		} catch (IOException e) {
			System.err.println("Failed to create new HttpServer instance listening on localhost");
			httpServerButMaybeNot = null;
			e.printStackTrace();
		}
		this.httpServer = httpServerButMaybeNot;

		System.out.println("INFO:\tSetting website context handler...");
		httpServer.createContext("/", this.websiteHandler);
		httpServer.createContext("/map-setup.js", this.peripheryFileHandler);
		httpServer.createContext("/style.css", this.peripheryFileHandler);
		httpServer.createContext("/src/main/java/server/MainServer.java", this.requestHandler);
	}

	/**
	 * SImply calls start on the server :)
	 */
	public void start()
	{
		this.httpServer.start();
	}

	public static void main(String... args)
	{
		MainServer server = new MainServer();

		System.out.println("INFO:\tStarting Java HttpServer...");
		server.start();
	}
}
