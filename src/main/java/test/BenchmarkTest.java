package test;

import execution.Benchmark;
import org.junit.jupiter.api.Test;

public class BenchmarkTest {
	@Test
	public void parameterFormattingTest() {
		String[] params = new String[]{
				null,
				"/home/jnnk/Uni/current-courses/Programmier Projekt/ProPro/stgtregbz.fmi", null,
				"0.0", null,
				"0.0", null,
				"/home/jnnk/Uni/current-courses/Programmier Projekt/Benchs/stgtregbz.que", null,
				"42"};
		Benchmark.main(params);
	}
}
