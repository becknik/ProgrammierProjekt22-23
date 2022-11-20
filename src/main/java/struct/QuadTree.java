package struct;

import java.util.logging.Logger;

public class QuadTree implements Graph {
    private interface Node extends Graph {}

    private class InnerNode implements Node {
        // Values to determine area of node
        private final double rightmostLongitude;   // X position
        private final double rightmostLatitude;    // Y position
        private final double leftmostLongitude;
        private final double leftmostLatitude;
        // Center coordinates of node, calculated in constructor, used determine child node tile for passed by new node
        private final double centersLongitude;
        private final double centersLatitude;

        // The fields/ tiles of node
        private Node northeasternNode;  // 0 <- imaginary filed
        private Node southeasternNode;  // 1
        private Node southwesternNode;  // 2
        private Node northwesternNode;  // 3

        /**
         * Constructor for creating first InnerNode of the quadTree, which contains a path to all possible nodes
         * @param rightmostLongitude - The right upper edge longitude the quadtree view should cover
         * @param rightmostLatitude - " latitude "
         * @param leftmostLongitude - The bottom left edge longitude the quadtree view should cover
         * @param leftmostLatitude - " latitude "
         */
        private InnerNode(double rightmostLongitude, final double rightmostLatitude,
                          final double leftmostLongitude, final double leftmostLatitude){
            this.rightmostLongitude = rightmostLongitude;
            this.rightmostLatitude = rightmostLatitude;
            this.leftmostLongitude = leftmostLongitude;
            this.leftmostLatitude = leftmostLatitude;

            // Calculating the center node
            this.centersLongitude = (leftmostLongitude + rightmostLongitude) / 2;
            this.centersLatitude = (leftmostLatitude + rightmostLatitude) / 2;
        }

        /**
         * Constructor for creating new InnerNode objects due to a {@code LeafNode} getting resolved due to a capacity
         * limit of 4 graph nodes and a new graph node to be added
         * @param rightmostLongitude - The right upper corner longitude of new InnerNode, which is derived by its parents area and center
         * @param rightmostLatitude - " latitude "
         * @param leftmostLongitude - The bottom left corner longitude of new InnerNode, which is derived by its parents area and center
         * @param leftmostLatitude - " latitude "
         * @param leafNode - An LeafNode object containing 4 graph nodes to be separated into this inner nodes child LeafNodes.
         *                 Last is done by calling the addNode method
         */
        private InnerNode(double rightmostLongitude, final double rightmostLatitude,
                          final double leftmostLongitude, final double leftmostLatitude,
                          final LeafNode leafNode) {
            this(rightmostLongitude, rightmostLatitude, leftmostLongitude, leftmostLatitude);
            assert leafNode.nodeCount == 4 : "The nodeCount of the leafNode passed the constructor of a replacing new inner node is not 5!";

            // Optimization: prevent creation of new LeafNode object by "recycling" the old one to northEasternNode
            int[] nodeIds = leafNode.nodeIds;
            double[] longitudes = leafNode.longitudes;
            double[] latitudes = leafNode.latitudes;

            leafNode.nodeIds = new int[4];
            leafNode.longitudes = new double[4];
            leafNode.latitudes= new double[4];
            leafNode.nodeCount = 0;
            this.northeasternNode = leafNode;

            this.addNode(nodeIds[0], longitudes[0], latitudes[0]);
            this.addNode(nodeIds[1], longitudes[1], latitudes[1]);
            this.addNode(nodeIds[2], longitudes[2], latitudes[2]);
            this.addNode(nodeIds[3], longitudes[3], latitudes[3]);
        }

        /**
         * Adds the specified node "recursively" to one of the directional sub-nodes by calculating the relative positioning
         * to this nodes center coordinates. Initializes with the node as a LeafNode containing the graph node, if the
         * determined child node is null at the moment of calling.
         * @param nodeId - graph nodes Id
         * @param longitude - graph nodes longitude
         * @param latitude - graph nodes latitude
         */
        @Override
        public void addNode(final int nodeId, final double longitude, final double latitude) {
            if (enableLogging) logger.info(String.format("Adding node\t%d (%f, %f)\tto InnerNode\t%s",nodeId, longitude, latitude, this));

            // Calculating the relative difference to center of this node
            double differenceLongitude = this.centersLongitude - longitude;
            double differenceLatitude = this.centersLatitude - latitude;

            /*
            * Determines which field to which the graph node is passed to by comparing relative longitude and latitude with 0.
            * If the graph nodes coords lay on one of this nodes center coords, the bottom nodes are selected
            * Optimization: Redundant boolean operations; refactoring
            */
            if (differenceLongitude > 0) {
               if (differenceLatitude > 0) {    // case: northeastern tile
                    if (northeasternNode != null) {
                        this.substituteLeafNodeIfNecessary(this.northeasternNode);
                        northeasternNode.addNode(nodeId, longitude, latitude);
                    } else {
                        northeasternNode = new LeafNode(nodeId, longitude, latitude);
                    }
               } else {     // case: southwestern tile || somewhere on this centers latitude coordinate ---x---
                   if (southeasternNode != null) {
                       this.substituteLeafNodeIfNecessary(this.southeasternNode);
                       southeasternNode.addNode(nodeId, longitude, latitude);
                   } else {
                       southeasternNode = new LeafNode(nodeId, longitude, latitude);
                   }
               }
            } else {
                if (differenceLongitude <= 0) {     // case: southwestern tile || somewhere on this centers longitude coordinate
                    if (southwesternNode != null) {
                        this.substituteLeafNodeIfNecessary(this.southwesternNode);
                        southwesternNode.addNode(nodeId, longitude, latitude);
                    } else {
                        southwesternNode = new LeafNode(nodeId, longitude, latitude);
                    }
                } else {    // case northwest tile
                    if (northwesternNode != null) {
                        this.substituteLeafNodeIfNecessary(this.northwesternNode);
                        northwesternNode.addNode(nodeId, longitude, latitude);
                    } else {
                        northwesternNode = new LeafNode(nodeId, longitude, latitude);
                    }
                }
            }
        }

        /**
         * Checks if the maybeALeafNode is a LeafNode and then checks for its capacity. If the capacity is 4, it creates
         * a new InnerNode by setting up the right values.
         * @param maybeALeafNode - Maybe an instance of LeafNode
         */
        private void substituteLeafNodeIfNecessary(final Node maybeALeafNode) {
            LeafNode leafNode = (maybeALeafNode instanceof LeafNode aLeafNode && aLeafNode.getNodeCount() == 4) ? (LeafNode) maybeALeafNode: null;  // TODO Maybe expensive & code smell, all in one line...

            if (leafNode != null) {     // May look stupid, but I think its optimization
                // Creates the new Inner Node by selecting the right values for their coords
                if (maybeALeafNode == this.northeasternNode) {
                    this.northeasternNode = new InnerNode(this.rightmostLongitude, this.rightmostLatitude,
                            this.centersLongitude, this.centersLatitude, leafNode);

                } else if (maybeALeafNode == this.southeasternNode) {
                    double rightCornersX = this.rightmostLongitude;
                    double centersY = this.centersLatitude;
                    double centersX = this.centersLongitude;
                    double leftCornersY = this.leftmostLatitude;
                    northeasternNode = new InnerNode(rightCornersX, centersY, centersX, leftCornersY, leafNode);

                } else if (maybeALeafNode == this.southwesternNode) {
                    this.southwesternNode= new InnerNode(this.centersLongitude, this.centersLatitude,
                            this.leftmostLongitude, this.leftmostLatitude, leafNode);

                } else {
                    double centersX = this.centersLongitude;
                    double rightCornersY = this.rightmostLatitude;
                    double leftCornersX = this.leftmostLongitude;
                    double centersY = this.centersLongitude;
                    northeasternNode = new InnerNode(centersX, rightCornersY, leftCornersX, centersY, leafNode);
                }
            }
        }
    }

    private class LeafNode implements Node {
        private int nodeCount;

        // Values are not final due to recycling optimization when capacity of this node is full and the node gets passed
        // to an empty InnerNode instance
        int[] nodeIds;
        double[] longitudes;  // TODO longitude & latitude values may be unnecessary due to lookup in struct?
        double[] latitudes;

        /**
         * Creates an empty LeafNode only containing arrays in the length of 4.
         * It's called on the creation of the QuadTree object
         */
        private LeafNode() {
            this.nodeIds = new int[4];
            this.longitudes = new double[4];
            this.latitudes = new double[4];
        }

        /**
         * Created a new LeafNode with a parameter graph node by calling the standard constructor & adding the node
         * @param nodeId - Graph nodes ID
         * @param longitude - Graph nodes longitude
         * @param latitude - Graph nodes latitude
         */
        private LeafNode(final int nodeId, final double longitude, final double latitude) {
            this();
            this.addNode(nodeId, longitude, latitude);
        }

        @Override
        public void addNode(final int nodeId, final double longitude, final double latitude) {
            if (enableLogging) logger.info(String.format("Adding node\t%d (%f, %f)\tto LeafNode\t%s",nodeId, longitude, latitude, this));

            this.nodeIds[nodeCount] = nodeId;
            this.longitudes[nodeCount] = longitude;
            this.latitudes[nodeCount] = latitude;
            this.nodeCount++;
        }

        public int getNodeCount() {
            return this.nodeCount;
        }

    }

    public static final Logger logger = Logger.getLogger(AdjacencyGraph.class.getName());
    public static boolean enableLogging;

    private Node root;
    private int totalNodeCount;
    private final AdjacencyGraph adjacencyGraph;

    /**
     * Initializes the QuadTree graph structure from values of the {@code AdjacencyGraph} object provided.
     * @param adjacencyGraph - The graph the tree will be build from
     */
    public QuadTree(final AdjacencyGraph adjacencyGraph) {
        this.adjacencyGraph = adjacencyGraph;
        this.root = new LeafNode();

       for (int nodeId = 0; nodeId < adjacencyGraph.longitudes.length; nodeId++) {
           this.addNode(nodeId, adjacencyGraph.longitudes[nodeId], adjacencyGraph.latitudes[nodeId]);
       }
    }

    public void addNode(int nodeId, double longitude, double latitude) {
        if (this.totalNodeCount == 4) {
            double[] outerCoordinates = adjacencyGraph.getOutestCoordinates();
            this.root = new InnerNode(outerCoordinates[0], outerCoordinates[1], outerCoordinates[2], outerCoordinates[3]);
        }
        this.root.addNode(nodeId, longitude, latitude);
        totalNodeCount++;
    }
}
