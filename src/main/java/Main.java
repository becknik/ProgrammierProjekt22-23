import loader.GraphReader;
import struct.AdjacencyGraph;
import struct.QuadTree;

import java.io.*;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) {
        // Logging file parsing & adjacency graph creation to System.out:
        //GraphReader.enableLogging = true;
        //AdjacencyGraph.enableLogging = true;

        long createAdjacencyGraphStart = System.currentTimeMillis();
        File file = new File("stgtregbz.fmi");    // "germany.fmi"
        AdjacencyGraph testAdjacencyGraph = GraphReader.read(file);
        long createAdjacencyGraphEnd = System.currentTimeMillis();

        //Main.writeAdjacencyGraph(testAdjacencyGraph, file);

        QuadTree.enableLogging = true;
        try {
            FileHandler fileHandler = new FileHandler(file + "-quad-tree-creation.log");
            fileHandler.setFormatter(new SimpleFormatter());
            QuadTree.logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        QuadTree testQuadTree = new QuadTree(testAdjacencyGraph);



       // Benchmarking:
        long adjacencyGraphCreationTime = createAdjacencyGraphEnd - createAdjacencyGraphStart;
        Main.logBenchmark(adjacencyGraphCreationTime, file);
    }

    private static void writeAdjacencyGraph(AdjacencyGraph adjacencyGraph, File graphFile) {

        try {
            adjacencyGraph.printOutStructs(new PrintStream(new PrintStream(graphFile.getName() + "-adjacency-graph.txt")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void logBenchmark(final long adjacencyGraphCreation, final File graphFile) {

        String formattedOutput = String.format("%s@%s - %tc%n\tCreation of adjArray:\t%f secs%n%n",
                System.getProperty("user.name"), System.getProperty("os.name"), new Date(), adjacencyGraphCreation * 10E-4);

        try (
                FileWriter writer = new FileWriter(graphFile.getName() + "-benchmark.log", true)
        ) {
            writer.write(formattedOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}