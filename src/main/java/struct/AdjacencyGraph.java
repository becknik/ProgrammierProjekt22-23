package struct;

import java.awt.*;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class AdjacencyGraph implements Graph {
    private static final Logger logger = Logger.getLogger(AdjacencyGraph.class.getName());
    public static boolean enableLogging;

    // Node stuff
    final double[] longitudes;
    final double[] latitudes;

    // Edge stuff
    private final int[] sources;
    private final int[] targets;
    private final int[] offset;
    private int cachedSourceNodeID;

    // Stuff to be calculated:
    private final double[] distances;
    private double[] distanceOneToAll;

    public AdjacencyGraph(int nodeCount, int edgeCount){
        longitudes = new double[nodeCount];
        latitudes = new double[nodeCount];
        offset = new int[nodeCount + 1];  // Gotcha!
        sources = new int[edgeCount];
        targets = new int[edgeCount];
        distances = new double[edgeCount];
        distanceOneToAll = new double[nodeCount];
    }

    /**
     * Simply adds some node by specifying the nodes ID, longitude & latitude
     * @param nodeId - Number of occurrence in the graph file
     * @param longitude
     * @param latitude
     */
    public void addNode(int nodeId, double longitude, double latitude){
        longitudes[nodeId] = longitude;
        latitudes[nodeId] = latitude;
    }

    /**
     * Adds an edge to the source & target array and calculates the distance by calling the {@code calculateDistances}
     * and saving the result to the distances array.
     *
     * @param edgeId - The id of the edge (one probably habe more edges)
     * @param source - The source node the edge is outgoing
     * @param target - The target node the edge aims to
     */
    public void addEdgeAndCalculateDistance(final int edgeId, final int source, final int target){
        sources[edgeId] = source;
        targets[edgeId] = target;

        /* When there is a sequence of nodes without outgoing edges in between the last observed node and the current,
         the value of the offset[last observed node+1] is copied inductively into the offset value gap until offset[current node]
         is set to the calue of the last observed node
        */
        while (cachedSourceNodeID < source) {
            cachedSourceNodeID++;
            offset[cachedSourceNodeID + 1] = offset[cachedSourceNodeID];
        }
        // Offset value of the next row increases
        offset[source + 1]++;
        // Adds distance value for this edge
        distances[edgeId] = this.calculateDistanceOf(source, target);
    }

    /**
     * calculates the distance between 2 nodes
     * @param source
     * @param target
     * @return - the distance between source and target as double value
     */
    private double calculateDistanceOf(final int source, final int target) {
        double sourceLon, sourceLat, targetLon, targetLat, distance;

        sourceLon = longitudes[source];
        sourceLat = latitudes[source];
        targetLon = longitudes[target];
        targetLat = latitudes[target];
        distance = 0;

        distance = Math.sqrt(Math.pow((sourceLon - targetLon), 2) + Math.pow((sourceLat - targetLat), 2));

        return distance;
    }

    /**
     * Returns the lowest latitude & longitude, used by the {@code QuadTree} class to determine the placement of the greatest "tile"
     * @return - A quadrupel (longitude highest, latitude, Longitude lowest, latitude)
     */
    public double[] getOutestCoordinates() {
        double highestLatitude = 0d, lowestLatitude = 0d, highestLongitude = 0d, lowestLongitude = 0d;

        // Saving the highest & lowest longitude & latitude from the arrays into the initialized variables
        for (double longitude : this.longitudes) {
            if (longitude > highestLongitude) highestLongitude = longitude;
            else if (longitude < lowestLongitude) lowestLongitude = longitude;
        }
        for (double latitude : this.latitudes) {
            if (latitude > highestLatitude) highestLongitude = latitude;
            else if (latitude < lowestLatitude) lowestLongitude = latitude;
        }

        return new double[]{ highestLongitude, highestLatitude, lowestLongitude, lowestLatitude};
    }

    /**
     * Prints out the current objects structure with all interesting values in a formatted tabular
     * @param out - PrintStream to be printed to
     */
    public void printOutStructs(final PrintStream out) {
        out.print(" Node ID/Index:\t| Latitude:\t| Longitude:\t| Offset: \t| Targets: \n");
        for (int i = 0; i < this.longitudes.length; i++) {
            // TODO Add distance value to outgoing nodes
            out.printf("  %d\t\t|  %f\t|  %f\t| %d\t|  %s%n", i, latitudes[i], longitudes[i], offset[i], Arrays.toString(getOutgoingTargetNodes(i)));
        }
    }

    /**
     * Returns the outgoing nodes of a node specified via node ID alias the latitude/ longitudes corresponding index
     * @param node - The nodes ID/ index of which the outgoing nodes are requested
     * @return - The outgoing nodes typed as int[]
     */
    private int[] getOutgoingTargetNodes(final int node) {
        int startOutgoingNodeID = offset[node];
        int exclusiveOutgoingNodeID = offset[node + 1];
        return Arrays.copyOfRange(targets, startOutgoingNodeID, exclusiveOutgoingNodeID);
    }

    /**
     * Returns an Array of the indexes for the outgoing edges from sourceNode.
     * @param sourceNode the given source node
     * @return array with the indexes of those edges which are outgoing edges from sourceNode
     */
    private int[] getIndexOfOutgoingEdges(final int sourceNode) {
        int startOutgoingNodeID = offset[sourceNode];
        int exclusiveOutgoingNodeID = offset[sourceNode + 1];
        int indexRange = exclusiveOutgoingNodeID - startOutgoingNodeID;

        int[] indizes = new int[indexRange];

        for(int i = 0; i < indexRange; i++) {
            indizes[i] = startOutgoingNodeID + i;
        }
        return indizes;
    }

    public int getSize() {
        return longitudes.length;
    }

    public double getLongitude(int index) {
        return longitudes[index];
    }
    public double getLatitude(int index) {
        return latitudes[index];
    }
    public double getDistance(int index) {
        return distances[index];
    }

    public record DijkstraNode(int idNode, double distance) implements Comparable{

        @Override
        public int compareTo(Object o) {
            if (o.getClass() != DijkstraNode.class){
                throw new IllegalArgumentException("Compare to only for instances of DijkstraNode!");
            }
            AdjacencyGraph.DijkstraNode node = (AdjacencyGraph.DijkstraNode) o;

            if (this.distance - node.distance < 0) {
                return -1;
            } else if (this.distance - node.distance > 0) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * calculates the oneToAllDijkstra
     * @param nodeID the source node for the dijkstra algorithm
     */
    public void oneToAllDijkstra(int nodeID) {

        PriorityQueue<DijkstraNode> prioNodes = new PriorityQueue();

        double[] currentDistances = new double[this.getSize()];
        Arrays.fill(currentDistances, -1);
        double[] lockedDistances = new double[this.getSize()];
        Arrays.fill(lockedDistances, -1);

        DijkstraNode currentNode;
        int currentID = nodeID;
        double currentDistance = 0;

        int[] connectedNodes = getOutgoingTargetNodes(nodeID);
        int[] connectedEdges = getIndexOfOutgoingEdges(nodeID);

        // Setup for first loop iteration BANANA
        DijkstraNode startNode = new DijkstraNode(nodeID, 0);
        prioNodes.add(startNode);

        currentDistances[nodeID] = 0;

        while(!prioNodes.isEmpty()){

            currentNode = prioNodes.poll();
            lockedDistances[currentNode.idNode] = currentNode.distance;
            currentID = currentNode.idNode;

            connectedNodes = getOutgoingTargetNodes(currentID);
            connectedEdges = getIndexOfOutgoingEdges(currentID);

            // Add neighbournodes to priorityqueue (skips the nodes which are allready included in lockedDistances)
            for(int i = 0; i < connectedNodes.length; i++){
                final int deleteID = connectedNodes[i];

                currentDistance = currentNode.distance + distances[connectedEdges[i]];

                if(currentDistances[connectedNodes[i]] < 0.0){
                    // Generates a dijkstra Object with the distance of source node + edge distance
                    prioNodes.add(new DijkstraNode(connectedNodes[i], currentDistance));
                    currentDistances[connectedNodes[i]] = currentDistance;
                }
                else if(currentDistances[connectedNodes[i]] > currentDistance){
                    currentDistances[connectedNodes[i]] = currentDistance;
                    prioNodes.removeIf(o1 -> o1.idNode == deleteID);
                    prioNodes.add(new DijkstraNode(connectedNodes[i], currentDistance));
                }
            }

            // Sort PrioNodes - check this
        }
        currentID = currentID;
    }

}
