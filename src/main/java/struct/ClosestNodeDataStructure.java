package struct;

import java.util.Arrays;

public class ClosestNodeDataStructure {

    public record Node(double longitude, double latitude, int nodeId) implements Comparable{
        @Override
        public String toString() {
            String node = longitude + " " + latitude;
            return node ;
        }

        @Override
        public int compareTo(Object o) {

            if (o.getClass() != Node.class){
                throw new IllegalArgumentException("Compare to only for instances of Node!");
            }
            Node node = (Node) o;

            if (this.latitude - node.latitude < 0) {
                return -1;
            } else if (this.latitude - node.latitude > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        /**
         * This method returns the distance as double to the given coordinates
         * @param - A second node record to which the distance shall be calculated
         * @return the distance from the node on which this method is called to the given coordinates
         */
        public double getDistanceTo(Node node) {
            double distance = Math.sqrt(Math.pow((this.longitude - node.longitude), 2) + Math.pow((this.latitude - node.latitude), 2));
            return distance;
        }
    };

    Node[] sortedAdjacencyGraph;

    public ClosestNodeDataStructure(AdjacencyGraph adjacencyGraph){

        sortedAdjacencyGraph = new Node[adjacencyGraph.getSize()];
        for (int i = 0; i < adjacencyGraph.getSize(); i++){
            sortedAdjacencyGraph[i] = new Node(adjacencyGraph.getLongitude(i), adjacencyGraph.getLatitude(i), i);
        }
        Arrays.sort(sortedAdjacencyGraph);
    }

    /**
     * This method returns the closest node to the given coordinates
     * @param longitude the x-value of the given coordinate
     * @param latitude the y-value of the given coordinate
     * @return the closest node to the given coordinates which is contained by sortedAdjacencyGraph
     */
    public Node getClosestNode(double longitude, double latitude) {
        Node imaginaryNode = new Node(longitude, latitude, -1);    // TODO Dont create new record instance right here
        int pivotIndex = getPivotIndex(sortedAdjacencyGraph, imaginaryNode);
        Node curClosestNode = sortedAdjacencyGraph[pivotIndex];
        double curDistance = imaginaryNode.getDistanceTo(curClosestNode);

        for (int i = 0;curDistance >= Math.abs(sortedAdjacencyGraph[pivotIndex + i].latitude - imaginaryNode.latitude); i++) {
            if (pivotIndex + i >= sortedAdjacencyGraph.length - 1) {
                break;
            }
            if (sortedAdjacencyGraph[pivotIndex + i].getDistanceTo(imaginaryNode) < curDistance) {
                curClosestNode = sortedAdjacencyGraph[pivotIndex + i];
                curDistance = curClosestNode.getDistanceTo(imaginaryNode);
            }
        }
        for (int i = 1; curDistance >= Math.abs(sortedAdjacencyGraph[pivotIndex - i].latitude - imaginaryNode.latitude); i++) {
            if (pivotIndex - i <= 0) {
                break;
            }
            if (sortedAdjacencyGraph[pivotIndex - i].getDistanceTo(imaginaryNode) < curDistance) {
                curClosestNode = sortedAdjacencyGraph[pivotIndex - i];
                curDistance = curClosestNode.getDistanceTo(imaginaryNode);
            }
        }
        return curClosestNode;
    }


    /**
     * This method returns the index of the given Node[] which may be good for starting to look for the closest node :)
     * @param nodeArray the Array to be searched for the pivot element
     * @param node contains the coordinates of the position to look for the closest node
     * @return the index where the node should be in the node Array
     */
    /*
    @requires nodeArray has to be sorted by latitudes
     */
    public static int getPivotIndex(Node[] nodeArray,Node node) {
        int indexOfSearchPoint = Arrays.binarySearch(nodeArray, node);
        if (indexOfSearchPoint < 0) {
            indexOfSearchPoint = -indexOfSearchPoint;
        }
        if (indexOfSearchPoint >= nodeArray.length) {
            return nodeArray.length - 1;
        }
        return indexOfSearchPoint;
    }
}
