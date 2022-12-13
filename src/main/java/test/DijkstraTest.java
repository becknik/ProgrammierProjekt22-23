package test;

import loader.GraphReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import struct.AdjacencyGraph;

import java.io.File;
import java.util.Optional;
import java.util.Queue;

public class DijkstraTest {

	static final int TEST_REPETITIONS = 3;  // TODO This is ugly

	static GraphFileType graphFileType;
	AdjacencyGraph adjGraph;

	@BeforeAll
	public static void setUp () {
		DijkstraTest.graphFileType = GraphFileType.GERMANY;  // TODO set this one into the run config

		//GraphReader.enableLogging = true;
		//AdjacencyGraph.enableLogging = true;
	}

	@BeforeEach
	public void prepareAdjGraph () {
		File file = new File(DijkstraTest.graphFileType.fileName);
		this.adjGraph = GraphReader.createAdjacencyGraphOf(file);
	}

	@Tag("Benchmark")
	@ParameterizedTest(name = "oneToAllBenchmark{0}")
	@ValueSource(ints = {3, 42})
	public void oneToOneBenchmark (int targetNode) {
		// TODO How to do test parameterization with multiple parameters?

		long oneToOneDijkstraStart = System.currentTimeMillis();
		Optional<Queue<Integer>> thisIsTheWay = this.adjGraph.dijkstra(0, targetNode);
		long oneToOneDijkstraEnd = System.currentTimeMillis();

		long oneToOneDijkstraElapsedTime = oneToOneDijkstraEnd - oneToOneDijkstraStart;

		Logging.logTestBenchmark(DijkstraTest.graphFileType, "oneToOne", oneToOneDijkstraElapsedTime);
		System.out.println("The path from node 0 to " + targetNode + ":\n" + thisIsTheWay.get());
	}

	@Tag("Benchmark")
	@RepeatedTest(TEST_REPETITIONS)
	public void oneToAllBenchmark () {
		long oneToAllDijkstraStart = System.currentTimeMillis();
		this.adjGraph.dijkstra(12323);
		long oneToAllDijkstraEnd = System.currentTimeMillis();

		long oneToAllDijkstraElapsedTime = oneToAllDijkstraEnd - oneToAllDijkstraStart;

		Logging.logTestBenchmark(DijkstraTest.graphFileType, "oneToAll", oneToAllDijkstraElapsedTime);
	}
}
