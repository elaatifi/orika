package ma.glasnost.orika.test.benchmarks.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.caliper.Result;
import com.google.caliper.ResultsReader;
import com.google.caliper.Runner;

public class BenchmarkDriver {
	
	protected Result runBenchmark(Class<? extends com.google.caliper.Benchmark> benchmark) throws IOException {
			
		File targetFolder = new File(getClass().getClassLoader().getResource("").getFile()).getParentFile();
		File resultFile = new File(targetFolder, "caliper-results/results.json");
			
		String[] args = new String[]{
				benchmark.getCanonicalName(), 
				"--saveResults", resultFile.getAbsolutePath()//,
				};
		new Runner().run(args);
		
		// TODO: why can't we just directly capture the Result object?
		Result result = new ResultsReader().getResult(new FileInputStream(resultFile));
		
		return result;
	}
}
