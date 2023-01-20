package server;

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

public class OurServer {
	private final HttpHandler websiteHandler;
	private final HttpHandler peripheryFileHandler;

	public OurServer () {
        /*
        All simultaneous requests will be queued by the operating system.
        However, the operating system will decide how many of these requests can be queued at any given point in time.
        This value represents back logging.
         */

		HttpServer httpServer = null;
		try {
			httpServer = HttpServer.create(new InetSocketAddress(8080), 0); // OS default backlogging queue length
		} catch (IOException e) {
			System.err.println("Failed to create new HttpServer instance listening on localhost");
			e.printStackTrace();
		}


		this.websiteHandler = exchange -> {
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

		this.peripheryFileHandler = exchange -> {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);

			Headers httpHeaders = exchange.getRequestHeaders();
			List<String> acceptedMediaTypes = httpHeaders.get("Accept");

			for (String s : acceptedMediaTypes) {
				System.out.println(s);
			}

			File website = new File(System.getProperty("user.dir") + "/website/map-setup.js");
			FileInputStream websiteStream = new FileInputStream(website);

			OutputStream responseStream = exchange.getResponseBody();
			responseStream.write(websiteStream.readAllBytes());

			responseStream.close();
			websiteStream.close();
		};

		httpServer.createContext("/", this.websiteHandler);
		httpServer.createContext("map-setup.js", this.peripheryFileHandler);
		httpServer.createContext("style.css", this.peripheryFileHandler);

		System.out.println("INFO: Starting java HttpServer...");
		httpServer.start();
	}

	public static void main (String... args) {
		OurServer server = new OurServer();
	}
}
