package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import loader.GraphReader;
import struct.AdjacencyGraph;
import struct.SortedAdjacencyGraph;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;

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

		// H
		File peripheryFile = new File(System.getProperty("user.dir") + "/website" + request.getPath());
		FileInputStream websiteStream = new FileInputStream(peripheryFile);

		OutputStream responseStream = exchange.getResponseBody();
		responseStream.write(websiteStream.readAllBytes());

		responseStream.close();
		websiteStream.close();
	};

	private final HttpHandler requestHandler = exchange -> {
		System.out.println("INFO:\tReceived request by client:" + exchange.getRequestMethod());

		if (this.adjacencyGraph == null) {
			System.out.println("ERROR:\tServer received request while setting up AdjacencyGraph");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST,0);

			OutputStream outputStream = exchange.getResponseBody();
			byte[] responseMessage = "The server has not finished the setup of the underlying graph file. Please wait some time".getBytes();
			outputStream.write(responseMessage);
			outputStream.close();
		}
		else {
			byte[] requestBody = exchange.getRequestBody().readAllBytes();
			exchange.getRequestBody().close();

			String message = new String(requestBody);
			System.out.println(message);



			ByteBuffer byteBuffer = ByteBuffer.wrap(requestBody);
			double bufferCoordinate;
			double[] coordinates = new double[4];
			for (int arrayIndex = 0; (bufferCoordinate = byteBuffer.getDouble()) != 0; arrayIndex++) {
				coordinates[arrayIndex] = bufferCoordinate;
			}

			for (double d : coordinates) System.out.println(d);


/*
			String[] coordinates2 = new String[4];
			coordinates2 = message.split(", ");

			SortedAdjacencyGraph.IndexNode closestStartNode = this.sortedAdjacencyGraph.getClosestNode(Double.parseDouble(coordinates2[0]), Double.parseDouble(coordinates2[1]));
			Double[] startNodeCoords = new Double[2];
			startNodeCoords[0] = closestStartNode.longitude();
			startNodeCoords[1] = closestStartNode.latitude();

			SortedAdjacencyGraph.IndexNode closestTargetNode = this.sortedAdjacencyGraph.getClosestNode(Double.parseDouble(coordinates2[2]), Double.parseDouble(coordinates2[3]));
			Double[] targetNodeCoords = new Double[2];
			targetNodeCoords[0] = closestTargetNode.longitude();
			targetNodeCoords[1] = closestTargetNode.latitude();

			System.out.println(startNodeCoords);


 */


			exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, -1);
		}
	};

	private final HttpServer httpServer;
	private AdjacencyGraph adjacencyGraph;
	private SortedAdjacencyGraph sortedAdjacencyGraph;

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
	 * Creates a background thread executing the GraphReader.read() method. The generated adjacencyGraph is set to this classes {@code this.adjacencyGraph} reference
	 */
	private void setUpGraph() {

		Runnable setUpGraph = () -> {
			System.out.println("INFO:\tStarting graph setup");
			File graphSourceFile = new File(System.getProperty("user.dir") + System.getProperty("file.separator") + "stgtregbz.fmi");
			this.adjacencyGraph = GraphReader.createAdjacencyGraphOf(graphSourceFile);
			this.sortedAdjacencyGraph = new SortedAdjacencyGraph(this.adjacencyGraph);
			System.out.println("INFO:\tFinished graph setup");

			assert this.adjacencyGraph != null;
			assert this.sortedAdjacencyGraph != null;
		};
		new Thread(setUpGraph).start();
	}

	/**
	 * Simply calls start on the server & prints out a info message :)
	 */
	public void start()
	{
		System.out.println("INFO:\tStarting Java HttpServer...");
		this.httpServer.start();
	}

	public static void main(String... args)
	{
		MainServer server = new MainServer();

		server.setUpGraph();
		server.start();
	}
}
