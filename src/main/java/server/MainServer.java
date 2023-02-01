package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dijkstra.DijkstraAlgorithm;
import dijkstra.DijkstraResult;
import loader.GraphReader;
import org.json.JSONObject;
import struct.AdjacencyGraph;
import struct.SortedAdjacencyGraph;

import java.io.*;
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

		// If receives request & graph structure is currently not set up, a error response is sent
		if (this.adjacencyGraph == null) {
			System.out.println("ERROR:\tServer received request while setting up AdjacencyGraph");
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);

			OutputStream outputStream = exchange.getResponseBody();
			byte[] responseMessage = "The server has not finished the setup of the underlying graph file. Please wait some time".getBytes();
			outputStream.write(responseMessage);
			outputStream.close();

		} else {
			// Get request body string content
			String requestBodyMessage = null;
			try (InputStream inputStream = exchange.getRequestBody()) {
				byte[] requestBody = inputStream.readAllBytes();
				requestBodyMessage = new String(requestBody);
			}

			// Create wrapper object which parses the nested JSON input to further JSON objects
			JSONObject requestJSON = new JSONObject(requestBodyMessage);

			// Unnesting JSON structure by creating object containing starting long,lat from the nested one
			JSONObject startChoords = requestJSON.getJSONObject("start");
			int startNodeId = this.getNearestNodeIdFrom(startChoords);

			JSONObject targetChoords = requestJSON.getJSONObject("target");
			int targetNodeId = this.getNearestNodeIdFrom(targetChoords);

			System.out.println("INFO:\tEmitted start node ID: " + startNodeId + " & targetNodeID: " + targetNodeId);

			// FIXME: Somehow won't proceed right here...
			DijkstraResult result =
					DijkstraAlgorithm.dijkstra(this.adjacencyGraph, startNodeId, targetNodeId);
			System.out.println("test");
			//ArrayDeque<Integer> path =  result.getPath();
			//System.out.println(path);

			exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, 0);

			try (OutputStream outputStream = exchange.getResponseBody()) {
				outputStream.write("lol".getBytes());
			}
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
	 * Simply calls start on the server & prints out a info message :)
	 */
	public void start()
	{
		System.out.println("INFO:\tStarting Java HttpServer...");
		this.httpServer.start();
	}

	/**
	 * Creates a background thread executing the GraphReader.read() method. The generated adjacencyGraph is set to this classes {@code this.adjacencyGraph} reference
	 */
	private void setUpGraph()
	{
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
	 * Returns the node id of the nearest node to the specified {@link JSONObject}, which must contain a coordinate formatted
	 * as "long:" and "lat:".
	 * Uses the local classes' {@link SortedAdjacencyGraph} instance for this.
	 *
	 * @param jsonChoords Containint "long:" and "lat:" with following double primitives
	 * @return The ID of the closest node
	 */
	private int getNearestNodeIdFrom(final JSONObject jsonChoords)
	{
		double longitude = jsonChoords.getDouble("long");
		double latitude = jsonChoords.getDouble("lat");

		System.out.println("INFO:\t\tParsed (" + longitude + ',' + latitude + ") from JSNO Object");

		SortedAdjacencyGraph.IndexNode closestNode =
				this.sortedAdjacencyGraph.getClosestNode(longitude, latitude);

		return closestNode.nodeId();
	}

	public static void main(String... args)
	{
		MainServer server = new MainServer();

		server.setUpGraph();
		server.start();
	}
}
