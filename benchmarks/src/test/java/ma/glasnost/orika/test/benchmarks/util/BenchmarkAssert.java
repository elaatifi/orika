package ma.glasnost.orika.test.benchmarks.util;

import java.util.Map;

import com.google.caliper.MeasurementSet;
import com.google.caliper.MeasurementType;
import com.google.caliper.Result;
import com.google.caliper.Scenario;
import com.google.caliper.ScenarioResult;

public abstract class BenchmarkAssert {

	
	public static double getMetricRatio(Result result, String benchmarkName, String parameterName, String parameterValue1, String parameterValue2, MetricType metricType) {
		
		// variables == {vm=java, trial=0, benchmark=SameObjectInstance, strategy=DOZER}
		
		Double value1 = null;
		Double value2 = null;
		
		for (Map.Entry<Scenario, ScenarioResult> entry: result.getRun().getMeasurements().entrySet()) {
			Scenario scenario = entry.getKey();
			Map<String, String> variables = scenario.getVariables();
			if (benchmarkName.equals(variables.get("benchmark"))) {
				if (parameterValue1.equals(variables.get(parameterName))) {
					MeasurementSet measurementSet = entry.getValue().getMeasurementSet(MeasurementType.TIME);
					value1 = metricType.getValue(measurementSet);
					if (value2 != null) {
						break;
					}
				} else if (parameterValue2.equals(variables.get(parameterName))) {
					MeasurementSet measurementSet = entry.getValue().getMeasurementSet(MeasurementType.TIME);
					value2 = metricType.getValue(measurementSet);
					if (value1 != null) {
						break;
					}
				}				
			}
		}
		if (value1 == null) {
			throw new IllegalArgumentException("result defined by '" + 
					parameterName + "=" + parameterValue1 + "' was not found");
		} 
		if (value2 == null) {
			throw new IllegalArgumentException("result defined by '" + 
					parameterName + "=" + parameterValue2 + "' was not found");
		}
		
		
		return value1.doubleValue() / value2.doubleValue();
	}
	
	
	public static enum MetricType {
		STANDARD_DEVIATION {
			double getValue(MeasurementSet measurement) {
				return measurement.standardDeviationRaw();
			}
		},
		MEAN {
			double getValue(MeasurementSet measurement) {
				return measurement.meanRaw();
			}
		},
		MEDIAN {
			double getValue(MeasurementSet measurement) {
				return measurement.medianRaw();
			}
		},
		MAX {
			double getValue(MeasurementSet measurement) {
				return measurement.maxRaw();
			}
		},
		MIN {
			double getValue(MeasurementSet measurement) {
				return measurement.minRaw();
			}
		};
	
		abstract double getValue(MeasurementSet measurement);
	}
	
	
}
