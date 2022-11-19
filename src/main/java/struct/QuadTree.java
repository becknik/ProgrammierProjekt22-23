package struct;

public class QuadTree implements Graph {
    private interface Node extends Graph {
        int getNodeCount();
    }

    private class InnerNode implements Node {
        final int nodeCount = -1;
       private final double centersLongitude;
        private final double centersLatitude;
        private final double upperRightLongitude;
        private final double upperRightLatitude;
        private final double bottomLeftLongitude;
        private final double bottomLeftLatitude;
        private Node northEasternNode;  // 0 <- imaginary filed
        private Node eastSouthernNode;  // 1
        private Node southWesternNode;  // 2
        private Node westNorthernNode;  // 3

        private InnerNode(double upperRightLongitude, final double upperRightLatitude,
                          final double bottomLeftLongitude, final double bottomLeftLatitude){
            this.upperRightLongitude = upperRightLongitude;
            this.upperRightLatitude = upperRightLatitude;
            this.bottomLeftLongitude = bottomLeftLongitude;
            this.bottomLeftLatitude = bottomLeftLatitude;

            this.centersLongitude = (bottomLeftLongitude + upperRightLongitude) / 2;
            this.centersLatitude = (bottomLeftLatitude + upperRightLatitude) / 2;
        }

        private InnerNode(double upperRightLongitude, final double upperRightLatitude,
                          final double bottomLeftLongitude, final double bottomLeftLatitude,
                          final int[] nodeIds, final double[] longitudes, final double[] latitudes) {
            this(upperRightLongitude, upperRightLatitude, bottomLeftLongitude, bottomLeftLatitude);

            this.addNode(nodeIds[0], longitudes[0], latitudes[0]);  // TODO remove this
            this.addNode(nodeIds[1], longitudes[1], latitudes[1]);
            this.addNode(nodeIds[2], longitudes[2], latitudes[2]);
            this.addNode(nodeIds[3], longitudes[3], latitudes[3]);
        }

        /**
         * Adds the specified node "recursively" to one of the directional sub-nodes by calculating the relative positioning
         * to the inner nodes center coordinates. Initializes with the node as parameter if fitting node is null.
         * @param nodeId
         * @param longitude
         * @param latitude
         */
        @Override
        public void addNode(int nodeId, double longitude, double latitude) {
            double differenceLongitude = this.centersLongitude - longitude;
            double differenceLatitude = this.centersLatitude - latitude;

            if (differenceLatitude > 0) {    // latitude = x, longitude = y TODO check values
               if (differenceLongitude > 0) {   // case upper right
                    if (northEasternNode != null) {
                        this.substituteLeafNodeIfNecessary(0);
                        northEasternNode.addNode(nodeId, longitude, latitude);
                    } else {
                        northEasternNode = new LeafNode(nodeId, longitude, latitude);
                    }
               } else { // case bottom right
                   if (eastSouthernNode != null) {
                       this.substituteLeafNodeIfNecessary(1);
                       eastSouthernNode.addNode(nodeId, longitude, latitude);
                   } else {
                       eastSouthernNode = new LeafNode(nodeId, longitude, latitude);
                   }
               }
            } else {
                if (differenceLongitude <= 0) {     // case bottom left
                    if (southWesternNode != null) {
                        this.substituteLeafNodeIfNecessary(2);
                        southWesternNode.addNode(nodeId, longitude, latitude);
                    } else {
                        southWesternNode = new LeafNode(nodeId, longitude, latitude);
                    }
                } else {    // case upper left
                    if (westNorthernNode != null) {
                        this.substituteLeafNodeIfNecessary(3);
                        westNorthernNode.addNode(nodeId, longitude, latitude);
                    } else {
                        westNorthernNode = new LeafNode(nodeId, longitude, latitude);
                    }
                }
            }
        }

        private void substituteLeafNodeIfNecessary(final int fieldNumber) {
            Node currentField = switch (fieldNumber) {
                case 0 -> this.northEasternNode;
                case 1 -> this.eastSouthernNode;
                case 2 -> this.southWesternNode;
                case 3 -> this.westNorthernNode;
                default -> null;
            };
            if (currentField instanceof LeafNode leaf && leaf.getNodeCount() == 4) {    // TODO Maybe expensive & code smell, all in one line...
                this.substituteDeadLeaf(leaf, fieldNumber);
            }
        }

        private void substituteDeadLeaf(final LeafNode leaf, final int filedToSubstitute) {
            switch (filedToSubstitute) {
                case 0 -> northEasternNode = new InnerNode(this.upperRightLongitude, this.upperRightLatitude,
                        this.centersLongitude, this.centersLatitude, leaf.nodeIds, leaf.longitudes, leaf.latitudes);
                case 1 -> {
                    double centersRightLongitude = this.upperRightLongitude;
                    double centersRightLatitude = this.centersLatitude;
                    double centersBottomLongitude = this.upperRightLongitude;
                    double centersBottomLatitude = this.centersLatitude;

                    northEasternNode = new InnerNode(centersRightLongitude, centersRightLatitude, centersBottomLongitude,
                            centersBottomLatitude, leaf.nodeIds, leaf.longitudes, leaf.latitudes);
                }
                case 2 -> northEasternNode = new InnerNode(this.centersLongitude, this.centersLatitude,
                        this.bottomLeftLongitude, this.bottomLeftLatitude, leaf.nodeIds, leaf.longitudes, leaf.latitudes);
                case 3 -> {
                    double centersTopLongitude = this.centersLongitude;
                    double centersTopLatitude = this.upperRightLatitude;
                    double centersLeftLongitude = this.centersLongitude;
                    double centersLeftLatitude = this.bottomLeftLatitude;

                    northEasternNode = new InnerNode(centersTopLongitude, centersTopLatitude, centersLeftLongitude,
                            centersLeftLatitude, leaf.nodeIds, leaf.longitudes, leaf.latitudes);
                }
            }
        }

        @Override
        public int getNodeCount() {
            return this.nodeCount;
        }
    }

    private class LeafNode implements Node {
        private int nodeCount;

        final int[] nodeIds;
        final double[] longitudes;  // TODO longitude & latitude values may be unnecessary
        final double[] latitudes;

        private LeafNode(final int nodeId, final double longitude, final double latitude) {
            this();
            this.nodeIds[0] = nodeId;
            this.longitudes[0] = longitude;
            this.latitudes[0] = latitude;
            this.nodeCount++;
        }

        private LeafNode() {
            this.nodeIds = new int[4];
            this.longitudes = new double[4];
            this.latitudes = new double[4];
        }

        @Override
        public void addNode(int nodeId, double longitude, double latitude) {
            this.nodeIds[nodeCount + 1] = nodeId;
            this.longitudes[nodeCount + 1] = longitude;
            this.latitudes[nodeCount + 1] = latitude;
            this.nodeCount++;
            if (this.nodeCount > 4) throw new RuntimeException("Tried to add more than 4 nodes to an LeadNode object.");
        }

        @Override
        public int getNodeCount() {
            return this.nodeCount;
        }

        public int[] getNodeIds() {
            return nodeIds;
        }

        public double[] getLongitudes() {
            return longitudes;
        }

        public double[] getLatitudes() {
            return latitudes;
        }
    }

    private Node root;
    private int totalNodeCount;
    private final AdjacencyGraph adjacencyGraph;

    public QuadTree(final AdjacencyGraph adjacencyGraph) {
        this.adjacencyGraph = adjacencyGraph;
        this.root = new LeafNode();
    }

    public Node getRoot() {
        return root;
    }

    public void addNode(int nodeId, double longitude, double latitude) {
        if (this.totalNodeCount == 4) {
            double[] outerCoordinates = adjacencyGraph.getOutestCoordinates();
            this.root = new InnerNode(outerCoordinates[0], outerCoordinates[1], outerCoordinates[2], outerCoordinates[3]);
        }
        this.root.addNode(nodeId, longitude, latitude);
    }
}
