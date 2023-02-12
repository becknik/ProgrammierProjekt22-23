package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dijkstra.DijkstraAlgorithm;
import dijkstra.OneToAllResult;
import dijkstra.OneToOnePath;
import loader.GraphReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import struct.AdjacencyGraph;
import struct.SortedAdjacencyGraph;

import javax.naming.OperationNotSupportedException;
import java.awt.geom.Point2D;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

public class DijkstraServer {
	private final HttpServer httpServer;

	final File graphFile;
	private AdjacencyGraph adjacencyGraph;
	private SortedAdjacencyGraph sortedAdjacencyGraph;
	private Optional<OneToAllResult> oneToAllResult;
	private boolean graphResourcesReady;
	private File websiteRootDirectory;

	public DijkstraServer(final File graphFile)
	{
		Objects.requireNonNull(graphFile, "Provided file path is invalid");
		this.graphFile = graphFile;

		HttpServer httpServerButMaybeNot; // For final attribute initialization
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(8080);
			httpServerButMaybeNot = HttpServer.create(socketAddress, 0); // OS default backlogging queue length
		} catch (IOException e) {
			httpServerButMaybeNot = null;

			System.err.println("Failed to create new HttpServer instance listening on localhost");
			e.printStackTrace();
		}
		this.httpServer = httpServerButMaybeNot; // Sometimes I hate Java...

		this.websiteRootDirectory = new File(System.getProperty("user.dir") + "/website");
		/*
		 * Distributes the exchange Http-method wise to sub-handlers.
		 * Because me being sick of making a subdomain for every single request made by the website, I decided to go for a
		 * "subdomain per Http method" approach. This just makes more sense & is more appealing to me, personally...
		 */
		HttpHandler metaHandler = exchange -> {

			switch (exchange.getRequestMethod()) {
				case "GET" -> this.retrieveHandler.handle(exchange);
				case "PUT" -> this.updateHandler.handle(exchange);
				default -> {
					System.err.println("ERROR:\tClient used http protocol method \"" + exchange.getRequestMethod() +
							"\" which semantic was not meant to be implemented into this server...");
					exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_IMPLEMENTED, -1);
				}
			}
		};
		assert httpServer != null;
		httpServer.createContext("/", metaHandler);

		this.graphResourcesReady = false;

		System.out.println("INFO:\tFinished constructing server");
	}

	private final HttpHandler retrieveHandler = exchange -> {
		assert "GET".equals(exchange.getRequestMethod());

		String URI = exchange.getRequestURI().toString();
		if (URI.equals("/ServerStatus")) {
			this.statusHandler.handle(exchange);
		} else {
			this.websiteFilesHandler.handle(exchange);
		}
	};

	private final HttpHandler statusHandler = exchange -> {
		assert exchange.getRequestURI().toString().equals("/ServerStatus");

		String statusMessage;
		if (this.graphResourcesReady) {
			statusMessage = "Server resources ready";
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, statusMessage.length());
		} else {
			statusMessage = "Server is busy setting up the graph entities for later shortest path computation. Please wait...";
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAVAILABLE, statusMessage.length());
		}

		try (OutputStream responseBody = exchange.getResponseBody()) {
			responseBody.write(statusMessage.getBytes());
		}
	};

	/**
	 * Handles all website related Http GET file request. Maps the requests to the server root's "website" subdirectory.
	 * If the root itself is requested on connection to the server, the website.html is returned. Handles errors.
	 * Makes use of {@code websiteRootDirectory}.
	 */
	private final HttpHandler websiteFilesHandler = exchange -> {

		String requestedURI = exchange.getRequestURI().toString();
		String targetFileName = ("/".equals(requestedURI)) ? "website.html" : requestedURI;

		File fileToHandle = new File(this.websiteRootDirectory, targetFileName);

		try (
				FileInputStream websiteStream = new FileInputStream(fileToHandle);
				OutputStream responseBody = exchange.getResponseBody()
		) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0); // Variable response length
			responseBody.write(websiteStream.readAllBytes());
		} catch (FileNotFoundException e) {
			exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);

			System.err.println("ERROR:\tThe by client specified file \"" + targetFileName + "\" was not found on the webservers root");
			// Do not terminate this thread
		} catch (IOException e) {
			System.err.println("ERROR:\tFailed to open output stream from file stream \"" + fileToHandle);
			e.printStackTrace();
		}
	};


	/**
	 * This handler is meant to manage update requests to website content & error handling, if the internal state is
	 * problematic. If this server receives a computation request on the  graph structures & it's currently not set up,
	 * this handler minds the business.
	 */
	private final HttpHandler updateHandler = exchange -> {
		if (!this.graphResourcesReady) {
			System.out.println("WARNING:The server received request computation during setup process of graph files");

			String response = "ERROR: Could not process request due to server graph setup not being finished.";

			exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.length());
			try (OutputStream responseBody = exchange.getResponseBody()) {
				responseBody.write(response.getBytes());
			}
		} else {
			this.dijkstraHandler.handle(exchange);
		}
	};

	/**
	 * Parses the requests JSON, calculates the nearest nodes form it, then throws on the dijkstra algorithm just to
	 * send the GeoJSON-encoded list of coordinate-tuples back to the front end.
	 * The requests JSON string must contain a wrapper object containing two tuple objects. Every tuple object must
	 * declare "lat" & "long" double member.
	 */
	private final HttpHandler dijkstraHandler = exchange -> {

		String requestBodyMessage;
		try (InputStream inputStream = exchange.getRequestBody()) {
			byte[] requestBody = inputStream.readAllBytes();
			requestBodyMessage = new String(requestBody);
		}

		// Create wrapper object which parses the nested JSON string input (containing two 2-tuples) to a JSON objects
		JSONObject requestJSON = new JSONObject(requestBodyMessage);

		String dijkstraExecutionMode = requestJSON.getString("mode");

		if ("OneToOne".equals(dijkstraExecutionMode)) {
			this.handleOneToOne(exchange,requestJSON);
		} else {
			this.handleOneToAll(exchange, requestJSON);
		}
	};

	private void handleOneToOne(final HttpExchange exchange, final JSONObject requestJSON) throws IOException
	{
		// Un-nesting JSON structure by creating object containing starting long,lat from the nested one
		JSONObject startCoords = requestJSON.getJSONObject("start");
		int startNodeId = this.getNearestNodeIdFrom(startCoords);

		JSONObject targetCoords = requestJSON.getJSONObject("target");
		int targetNodeId = this.getNearestNodeIdFrom(targetCoords);

		OneToOnePath result = (OneToOnePath) DijkstraAlgorithm.dijkstra(this.adjacencyGraph, startNodeId, targetNodeId);

		// Convert the path consisting of edge ids to a path containing coordinates

		ArrayList<Point2D.Double> pathInCoordinates;
		try {
			pathInCoordinates = result.getPathInCoordinates();
		} catch (OperationNotSupportedException e) {
			throw new RuntimeException(e);
		}

		this.sendGEOJsonFormattedPath(exchange, pathInCoordinates);

		System.out.println("INFO:\tOneToOne Path is " + pathInCoordinates.size() + " edges long");
	}

	private void handleOneToAll(final HttpExchange exchange, final JSONObject requestJSON) throws IOException
	{
		JSONObject coords;
		OneToAllExecutionMode executionMode;

		try {
			coords = requestJSON.getJSONObject("target");
			executionMode = OneToAllExecutionMode.TARGET;
		}
		catch (JSONException e) {
			coords = requestJSON.getJSONObject("start");
			executionMode = OneToAllExecutionMode.SOURCE;
		}

		int nodeId = this.getNearestNodeIdFrom(coords);

		if (executionMode == OneToAllExecutionMode.SOURCE) {
			OneToAllResult dijkstraOneToAllResult =
					(OneToAllResult) DijkstraAlgorithm.dijkstra(this.adjacencyGraph, nodeId);
			this.oneToAllResult = Optional.of(dijkstraOneToAllResult);

			exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, -1);

			System.out.println("INFO:\t Finished calculating OneToAll result for source node " + nodeId);
		} else {
			assert this.oneToAllResult.isPresent();

			OneToAllResult oneToAllResult = this.oneToAllResult.get();
			Deque<Integer> pathToTargetNode = oneToAllResult.getPathTo(nodeId);
			ArrayList<Point2D.Double> pathToTargetInCoordinates;
			try {
				 pathToTargetInCoordinates = oneToAllResult.getPathInCoordinates(pathToTargetNode);
			} catch (OperationNotSupportedException e) {
				throw new RuntimeException(e);
			}

			this.sendGEOJsonFormattedPath(exchange, pathToTargetInCoordinates);

			System.out.println("INFO:\tOneToAll Path to target node " + nodeId + " is " + pathToTargetInCoordinates.size() + " edges long");
		}
	}

	private enum OneToAllExecutionMode {
		SOURCE,
		TARGET
	}

	private void sendGEOJsonFormattedPath(final HttpExchange exchange, final ArrayList<Point2D.Double> pathInCoordinates) throws IOException
	{
		JSONObject geoJSON = this.buildGeoJSONFrom(pathInCoordinates);
		byte[] geoJSONString = geoJSON.toString().getBytes(StandardCharsets.UTF_8);

		exchange.sendResponseHeaders(HttpURLConnection.HTTP_ACCEPTED, geoJSONString.length);
		try (OutputStream responseBody = exchange.getResponseBody()) {
			responseBody.write(geoJSONString);
		}
	}

	/**
	 * Returns the node id of the nearest node to the specified {@link JSONObject}, which must contain a coordinate
	 * formatted as "long:" and "lat:".
	 * Uses the local classes' {@link SortedAdjacencyGraph} instance for this.
	 *
	 * @param jsonCoords Containing "long:" and "lat:" with following double
	 * @return The ID of the closest node
	 */
	private int getNearestNodeIdFrom(final JSONObject jsonCoords)
	{
		double longitude = jsonCoords.getDouble("long");
		double latitude = jsonCoords.getDouble("lat");

		System.out.println("INFO:\t\tParsed (" + longitude + ',' + latitude + ") from JSON Object");

		SortedAdjacencyGraph.IndexNode closestNode = this.sortedAdjacencyGraph.getClosestNode(longitude, latitude);

		System.out.println("INFO:\t\tClosest node is " + closestNode.nodeId());

		return closestNode.nodeId();
	}

	// string bisher
	private JSONObject buildGeoJSONFrom(final ArrayList<Point2D.Double> coordsPath)
	{
		// See https://www.rfc-editor.org/rfc/rfc7946#section-3.1.4
		JSONObject geoJSON = new JSONObject();
		JSONArray coordinatesArray = new JSONArray();

		for (var coordinate : coordsPath) {
			JSONArray pointAsArray = new JSONArray();
			pointAsArray.put(coordinate.getX());
			pointAsArray.put(coordinate.getY());

			coordinatesArray.put(pointAsArray);
		}
		geoJSON.put("type", "LineString");
		geoJSON.put("coordinates", coordinatesArray);

		return geoJSON;
	}


	/**
	 * Simply calls start on the server, starts the graph structure setup process & prints out an info message :)
	 */
	public void start()
	{
		System.out.println("INFO:\tStarting Java HttpServer...");
		this.httpServer.start();

		this.setUpGraph();
	}

	/**
	 * Creates a background thread executing the GraphReader.read() method. The
	 * generated adjacencyGraph is set to this classes {@code this.adjacencyGraph}
	 * reference
	 */
	private void setUpGraph()
	{
		Runnable setUpGraph = () -> {
			System.out.println("INFO:\tStarting graph setup...");
			this.adjacencyGraph = GraphReader.createAdjacencyGraphOf(this.graphFile);
			this.sortedAdjacencyGraph = new SortedAdjacencyGraph(this.adjacencyGraph);

			this.graphResourcesReady = true;
			System.out.println("INFO:\tFinished graph setup");
		};

		new Thread(setUpGraph).start();
	}

	/**
	 * Starts a DijkstraServer using the .fmi graph file specified as first argument of this method.
	 * The graph file string is converted to the {@code File} Type & passed to the server's constructor.
	 *
	 * @param args First argument must be the graph file path
	 */
	public static void main(String... args)
	{
		String graphPath = args[0];
		Objects.requireNonNull(args, "No path specified. This server needs a path to a .fmi file for starting.");
		File graphFile = new File(graphPath);

		DijkstraServer dijkstraServer = new DijkstraServer(graphFile);
		dijkstraServer.start();
	}
}
