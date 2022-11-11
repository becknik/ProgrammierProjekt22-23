package ProgrammingProject.src.main.java.struct;

import java.util.Arrays;

public class AdjacencyGraph extends Graph{

    private double[] longitudes;
    private double[] latitudes;

    private int[] sources;
    private int[] targets;
    private int[] offset;
    private int cachedSourceNode;
    private double[] distances;

    public AdjacencyGraph(int nodeCount, int edgeCount){

        longitudes = new double[nodeCount];
        latitudes = new double[nodeCount];
        offset = new int[nodeCount];
        sources = new int[edgeCount];
        targets = new int[edgeCount];
        distances = new double[edgeCount];
    }

    public void addNode(int nodeId, double longitude, double latitude){
        longitudes[nodeId] = longitude;
        latitudes[nodeId] = latitude;
    }

    public void addEdge(int edgeId, int source, int target){
        sources[edgeId] = source;
        targets[edgeId] = target;

        if (cachedSourceNode + 1 < source) {    // Nodes without outgoing nodes, are detected here
            /*
            Adding offset value of the last node with outgoing nodes to the nodes without outgoing nodes.
            Due to offset being set on the next nodes offset entry, the for loop starts with 1 for the next node and adds
            1 because the offset value of the next node matters.
             */
            for (int i = 1; i <= source - cachedSourceNode; i++) {
                offset[cachedSourceNode + i + 1] = offset[cachedSourceNode + 1];
            }
            this.cachedSourceNode = source;
        }
        if (cachedSourceNode == source) {
            offset[source + 1] = offset[source];
            cachedSourceNode = source;
        }
        offset[source+1]++;
    }
}
