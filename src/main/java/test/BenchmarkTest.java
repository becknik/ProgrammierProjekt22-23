package test;

import execution.Benchmark;
import org.junit.jupiter.api.Test;

import javax.naming.OperationNotSupportedException;

public class BenchmarkTest {
	@Test
	public void parameterFormattingTest() throws OperationNotSupportedException
	{
		String[] params = new String[]{
				null,
				System.getProperty("user.dir") + System.getProperty("file.separator") + "stgtregbz.fmi", null,
				"0.0", null,
				"0.0", null,
				System.getProperty("user.dir") + System.getProperty("file.separator") + "Benchs"  + System.getProperty("file.separator") + "stgtregbz.que", null,
				"42"};
		Benchmark.main(params);
	}
}
