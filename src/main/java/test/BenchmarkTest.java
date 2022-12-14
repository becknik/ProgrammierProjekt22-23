package test;

import execution.Benchmark;
import org.junit.jupiter.api.Test;

public class BenchmarkTest {
	@Test
	public void parameterFormattingTest() {
		String[] params = new String[]{
				null,
				"C:\\Users\\timex\\IdeaProjects\\ProPro\\germany.fmi", null,
				"0.0", null,
				"0.0", null,
				"C:\\Users\\timex\\IdeaProjects\\ProPro\\Benchs\\germany.que", null,
				"42"};
		Benchmark.main(params);
	}
}
