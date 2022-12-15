package test;

import execution.Benchmark;
import org.junit.jupiter.api.Test;

public class BenchmarkTest {
	@Test
	public void parameterFormattingTest() {
		String[] params = new String[]{
				null,
				System.getProperty("user.dir") + System.getProperty("file.separator") + "germany.fmi", null,
				"0.0", null,
				"0.0", null,
				System.getProperty("user.dir") + System.getProperty("file.separator") + "Benchs"  + System.getProperty("file.separator") + "germany.que", null,
				"42"};
		Benchmark.main(params);
	}
}
