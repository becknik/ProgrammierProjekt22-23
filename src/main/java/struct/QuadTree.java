package struct;

import java.util.logging.Logger;

public class QuadTree implements Graph {
    private interface Node extends Graph {}

    private class InnerNode implements Node {
        // A reference to this Nodes parent node, which in case of the root InnerNode also can be the QuadTree itself
        private final Graph parent;

        // Values to determine area of node
        private final double rightmostLongitude;   // X position
        private final double rightmostLatitude;    // Y position
        private final double leftmostLongitude;
        private final double leftmostLatitude;

        // The fields/ tiles of node
        private Node northeasternNode;  // 0 <- imaginary filed
        private Node southeasternNode;  // 1
        private Node southwesternNode;  // 2
        private Node northwesternNode;  // 3

        /**
         * Constructor for creating first InnerNode of the QuadTree, which contains a path to all possible nodes
         * @param parent - A reference to the nodes parent node or the Quadtree instance itself
         * @param rightmostLongitude - The right upper edge longitude the quadtree view should cover
         * @param rightmostLatitude - " latitude "
         * @param leftmostLongitude - The bottom left edge longitude the quadtree view should cover
         * @param leftmostLatitude - " latitude "
         */
        private InnerNode(final Graph parent,
                          double rightmostLongitude, final double rightmostLatitude,
                          final double leftmostLongitude, final double leftmostLatitude){
            this.parent = parent;
            this.rightmostLongitude = rightmostLongitude;
            this.rightmostLatitude = rightmostLatitude;
            this.leftmostLongitude = leftmostLongitude;
            this.leftmostLatitude = leftmostLatitude;
        }

        /**
         * Constructor for creating new InnerNode objects due to a {@code LeafNode} getting resolved due to a capacity
         * limit of 4 graph nodes and a new graph node to be added
         * @param parent - A reference to the nodes parent node or the Quadtree instance itself
         * @param rightmostLongitude - The right upper corner longitude of new InnerNode, which is derived by its parents area and center
         * @param rightmostLatitude - " latitude "
         * @param leftmostLongitude - The bottom left corner longitude of new InnerNode, which is derived by its parents area and center
         * @param leftmostLatitude - " latitude "
         * @param leafNode - An LeafNode object containing 4 graph nodes to be separated into this inner nodes child LeafNodes.
         *                 Last is done by calling the addNode method
         */
        private InnerNode(final Graph parent,
                          double rightmostLongitude, final double rightmostLatitude,
                          final double leftmostLongitude, final double leftmostLatitude,
                          final LeafNode leafNode) {
            this(parent, rightmostLongitude, rightmostLatitude, leftmostLongitude, leftmostLatitude);
            assert leafNode.nodeCount == 4 : "The nodeCount of the leafNode passed the constructor of a replacing new inner node is not 5!";

            // Optimization: prevent creation of new LeafNode object by "recycling" the old one to northEasternNode
            int[] nodeIds = leafNode.nodeIds;
            double[] longitudes = leafNode.longitudes;
            double[] latitudes = leafNode.latitudes;

            leafNode.parentNode = this;
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
         * Insted of saving the InnerNodes center longitude, this operation calculates & returns it
         * @return - The centers longitude
         */
        private double calculateCentersLongitude () {
            return (leftmostLongitude + rightmostLongitude) / 2;
        }

        /**
         * Returns the centers latitude to avoid some memory space
         * @return - The centers latitude
         */
        private double calculateCentersLatitude () {
            return  (leftmostLatitude + rightmostLatitude) / 2;
        }

        /**
         * Adds the specified node "recursively" to one of the directional sub-nodes by calculating the relative positioning
         * to the nodes center coordinates and then decides which sub node fits.
         * Initializes with the node as a LeafNode containing the graph node, if the determined child node is null at the moment of calling.
         * Also splits the sub node, if it's a LeafNode and the capacity is exhausted.
         * Performances > code quality :/
         * @param nodeId - graph nodes Id
         * @param longitude - graph nodes longitude
         * @param latitude - graph nodes latitude
         */
        @Override
        public void addNode(final int nodeId, final double longitude, final double latitude) {
            // TODO This log call should be "switch-cased"
            // if (enableLogging) logger.info(String.format("Adding node\t%d (%f, %f)\tto InnerNode\t%s",nodeId, longitude, latitude, this));

            /*
             * Determines which field to which the graph node is passed to by comparing relative longitude and latitude with 0.
             * If the graph nodes coords lay on one of this nodes center coords, the bottom nodes are selected
             * Optimization: Redundant boolean operations; refactoring
             * Anti-optimization: Everything else
             */
            int destinationFieldNumber = this.getFieldNumberFor(longitude, latitude);
            if (this.getFieldNumbersNode(destinationFieldNumber) == null) {
                switch (destinationFieldNumber) {
                    case 0 -> this.northeasternNode = new LeafNode(this, nodeId, longitude, latitude);
                    case 1 -> this.southeasternNode = new LeafNode(this, nodeId, longitude, latitude);
                    case 2 -> this.southwesternNode = new LeafNode(this, nodeId, longitude, latitude);
                    case 3 -> this.northwesternNode = new LeafNode(this, nodeId, longitude, latitude);
                }
            } else {
                switch (destinationFieldNumber) {
                    case 0 -> {
                        this.substituteLeafNodeIfNecessary(this.northeasternNode);
                        northeasternNode.addNode(nodeId, longitude, latitude);
                    }
                    case 1 -> {
                        this.substituteLeafNodeIfNecessary(this.southeasternNode);
                        southeasternNode.addNode(nodeId, longitude, latitude);
                    }
                    case 2 -> {
                        this.substituteLeafNodeIfNecessary(this.southwesternNode);
                        southwesternNode.addNode(nodeId, longitude, latitude);
                    }
                    case 3 -> {
                    this.substituteLeafNodeIfNecessary(this.northwesternNode);
                    northwesternNode.addNode(nodeId, longitude, latitude);
                    }
                }
            }
        }

        /**
         * Determines which field to which the graph node is passed to by comparing relative longitude and latitude with
         * the objects center point. Returns the field - again for performance's sake - as an int n, 0 <= n < 4
         * @param longitude - The x-value of the coordinate which should the relative positioning should be determined
         * @param latitude - The y-value "
         * @return - The field of the InnerNode the coordinate fits in
         */
        private int getFieldNumberFor(final double longitude, final double latitude) {
            double differenceLongitude = this.calculateCentersLongitude() - longitude;
            double differenceLatitude = this.calculateCentersLatitude() - latitude;

            if (differenceLongitude > 0) {
                if (differenceLatitude > 0) {    // case: northeastern tile
                    return 0;
                } else {     // case: southwestern tile || somewhere on this centers latitude coordinate ---x---
                    return 1;
                }
            } else {
                if (differenceLongitude <= 0) {     // case: southwestern tile || somewhere on this centers longitude coordinate
                    return 2;
                } else {    // case northwest tile
                    return 3;
                }
            }
        }

        private Node getFieldNumbersNode(final int nodeNumber) {
            assert 0 <= nodeNumber && nodeNumber < 4;

            return switch (nodeNumber) {
                case 0 -> this.northeasternNode;
                case 1 -> this.southeasternNode;
                case 2 -> this.southwesternNode;
                case 3 -> this.northwesternNode;
                default -> throw new RuntimeException("You so dumb IntelliJ throws DumbUserException");
            };
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
                    this.northeasternNode = new InnerNode(this, this.rightmostLongitude, this.rightmostLatitude,
                            this.calculateCentersLongitude(), this.calculateCentersLatitude(), leafNode);

                } else if (maybeALeafNode == this.southeasternNode) {
                    double rightCornersX = this.rightmostLongitude;
                    double centersY = this.calculateCentersLatitude();
                    double centersX = this.calculateCentersLongitude();
                    double leftCornersY = this.leftmostLatitude;
                    northeasternNode = new InnerNode(this, rightCornersX, centersY, centersX, leftCornersY, leafNode);

                } else if (maybeALeafNode == this.southwesternNode) {
                    this.southwesternNode= new InnerNode(this, this.calculateCentersLongitude(), this.calculateCentersLatitude(),
                            this.leftmostLongitude, this.leftmostLatitude, leafNode);

                } else {
                    double centersX = this.calculateCentersLongitude();
                    double rightCornersY = this.rightmostLatitude;
                    double leftCornersX = this.leftmostLongitude;
                    double centersY = this.calculateCentersLatitude();
                    northeasternNode = new InnerNode(this, centersX, rightCornersY, leftCornersX, centersY, leafNode);
                }
            }
        }

        @Override
        public int getNodeNextTo(final int longitude, final int latitude) {
            int destinationField = this.getFieldNumberFor(longitude, latitude);
            Node destinationNode = this.getFieldNumbersNode(destinationField);
            if (destinationNode instanceof LeafNode leafNode) {     // leafNode must not be null
                double relativeDistanceOfDest = leafNode.getSmallestRelativeDistanceTo(longitude, latitude);
            }
            // TODO!!!
            return 0;
        }
    }

    private class LeafNode implements Node {
        // A reference to the nodes parent, which is not final due to the recycling of LeafNodes which are about to be replaced by an InnerNode
        private InnerNode parentNode;
        private int nodeCount;

        // Values are not final due to recycling optimization when capacity of this node is full and the node gets passed
        // to an empty InnerNode instance
        int[] nodeIds;
        double[] longitudes;  // TODO longitude & latitude values may be unnecessary due to lookup in struct?
        double[] latitudes;

        /**
         * Creates an empty LeafNode only containing arrays in the length of 4.
         * It's called ONLY on the creation of the QuadTree objects initial root node untill the QuadTrees total node
         * count is higher than 4 and therefore sets the temporarys root node to null!
         */
        private LeafNode() {
            assert totalNodeCount == 4;

            // The initial root node is null because a reference to QuadTree makes no sense here
            this.parentNode = null;

            this.initializeArrays();
        }

        /**
         * Created a new LeafNode with a parameter graph node by calling the standard constructor & adding the node
         * @param nodeId - Graph nodes ID
         * @param longitude - Graph nodes longitude
         * @param latitude - Graph nodes latitude
         */
        LeafNode(final InnerNode parent,
                 final int nodeId, final double longitude, final double latitude) {
            assert parent != null : "On creation of a new LeafNode object the passed by parent graph is null";
            this.parentNode = parent;

            this.initializeArrays();
            this.addNode(nodeId, longitude, latitude);
        }

        private void initializeArrays() {
            this.nodeIds = new int[4];
            this.longitudes = new double[4];
            this.latitudes = new double[4];
        }

        @Override
        public void addNode(final int nodeId, final double longitude, final double latitude) {
            assert 4 <= this.nodeCount;
            assert this.parentNode != null || (this.parentNode == null && 4 <= totalNodeCount) :
                    "Parent Node of this InnerNode is set to null and the total number of nodes is > 4"; // For the sake of readability

            if (enableLogging && parentNode != null) logger.info(String.format("Adding node\t%d (%f, %f)\tto LeafNode\t%s which parent nodes center is on (%f, %f)",
                    nodeId, longitude, latitude, this, parentNode.calculateCentersLongitude(), parentNode.calculateCentersLatitude()));

            this.nodeIds[nodeCount] = nodeId;
            this.longitudes[nodeCount] = longitude;
            this.latitudes[nodeCount] = latitude;
            this.nodeCount++;
        }

        /**
         * Retruns the smallest distance from the specified coordinate to the nearest node contained by this LeafNode.
         * @param longitude - Longitude coordinate of the coordinates the relative shortest distance should be found out
         * @param latitude - Latitude "
         * @return The smallest possible distance to a node contained by this Object
         */
        private double getSmallestRelativeDistanceTo(final double longitude, final double latitude) {
            double nearestNodesRelativeDistance = Double.MAX_VALUE;

            for (int i = 0; i < 4; i++) {
                if (longitudes[i] == 0d) break;     // When the array is not dully filled with real nodes, the loop terminates

                double relativeDistanceToI = Math.abs(longitude - this.longitudes[i]) + Math.abs(latitude - this.latitudes[i]);
                if (nearestNodesRelativeDistance > relativeDistanceToI) {
                    nearestNodesRelativeDistance = relativeDistanceToI;
                }
            }

            return nearestNodesRelativeDistance;
        }

        @Override
        public int getNodeNextTo(int longitude, int latitude) {
            return 0;
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
        if (QuadTree.enableLogging) QuadTree.logger.info("\n\nAdding new node " + nodeId + "to graph. Setting node count to " + this.totalNodeCount + 1);

        if (this.totalNodeCount == 4) {
            double[] outerCoordinates = adjacencyGraph.getOutestCoordinates();
            this.root = new InnerNode(this, outerCoordinates[0], outerCoordinates[1], outerCoordinates[2], outerCoordinates[3]);
        }
        this.root.addNode(nodeId, longitude, latitude);
        totalNodeCount++;
    }

    @Override
    public int getNodeNextTo(int longitude, int latitude) {

        return 0;
    }
}
