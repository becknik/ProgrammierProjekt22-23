package struct;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Logger;

public class AdjacencyGraph extends Graph{
    private static final Logger logger = Logger.getLogger(AdjacencyGraph.class.getName());
    public static boolean enableLogging;

    // Node stuff
    private final double[] longitudes;
    private final double[] latitudes;

    // Edge stuff
    private final int[] sources;
    private final int[] targets;
    private final int[] offset;
    private int cachedSourceNodeID;

    // Stuff to be calculated:
    private final double[] distances;

    public AdjacencyGraph(int nodeCount, int edgeCount){
        longitudes = new double[nodeCount];
        latitudes = new double[nodeCount];
        offset = new int[nodeCount + 1];  // Gotcha!
        sources = new int[edgeCount];
        targets = new int[edgeCount];
        distances = new double[edgeCount];
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
     * TODO
     * @param edgeId
     * @param source
     * @param target
     */
    public void addEdge(int edgeId, int source, int target){
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
    }

    /**
     * Prints out the current objects structure with all interesting values in a formatted tabular
     * @param out - PrintStream to be printed to
     */
    public void printOutStructs(final PrintStream out) {
        out.print(" Node ID/Index:\t| Latitude:\t| Longitude:\t| Offset: \t| Targets: \n");
        for (int i = 0; i < this.longitudes.length; i++) {
            // TODO Add distance value to outgoing nodes
            out.printf("  %d\t\t|  %f\t|  %f\t| %d\t|  %s%n", i, latitudes[i], longitudes[i], offset[i], Arrays.toString(getOutgoingNodes(i)));
        }
    }

    /**
     * Returns the outgoing nodes of a node specified via node ID alias the latitude/ longitudes corresponding index
     * @param node - The nodes ID/ index of which the outgoing nodes are requested
     * @return - The outgoing nodes typed as int[]
     */
    private int[] getOutgoingNodes(final int node) {
        int startOutgoingNodeID = offset[node];
        int exclusiveOutgoingNodeID = offset[node + 1];
        return Arrays.copyOfRange(targets, startOutgoingNodeID, exclusiveOutgoingNodeID);
    }

    public void calculateDistances() { //TODO
        if (distances[(int) (Math.random() * distances.length)] != 0) {
            throw new RuntimeException("Trying to calculate the distances again? HOW DARE YOU?!");
        }
    }
}
