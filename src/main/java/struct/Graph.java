package struct;

public interface Graph {
    void addNode(int nodeId, double longitude, double latitude);
    int getNodeNextTo(int longitude, int latitude);
}