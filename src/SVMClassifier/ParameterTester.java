package it.giordizz.Thesis;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;

import it.acubelab.batframework.metrics.Metrics;
import it.acubelab.batframework.metrics.MetricsResultSet;

public class ParameterTester {
	private Vector<Integer> features;
	Vector<ModelConfigurationResult> scoreboard;

	public enum OptimizaionProfiles {
		MAXIMIZE_TN, MAXIMIZE_MICRO_F1, MAXIMIZE_MACRO_F1
	}

	public ParameterTester(double wPos, double wNeg,
			double editDistanceThreshold, Vector<Integer> features,
			BinaryExampleGatherer trainEQFGatherer,
			BinaryExampleGatherer testEQFGatherer,
			OptimizaionProfiles optProfile, double optProfileThreshold,
			double gamma, double C, Vector<ModelConfigurationResult> scoreboard) {
		this.features = features;
		Collections.sort(this.features);
		this.scoreboard = scoreboard;
	}

	public static MetricsResultSet computeMetrics(svm_model model,
			svm_problem testProblem) throws IOException {

		List<HashSet<Integer>> outputOrig = new Vector<>();
		List<HashSet<Integer>> goldStandardOrig = new Vector<>();
		
		HashSet<Integer> goldPairs = new HashSet<>();
		HashSet<Integer> resPairs = new HashSet<>();
		for (int j = 0; j < testProblem.l; j++) {
			svm_node[] svmNode = testProblem.x[j];
			double gold = testProblem.y[j];
			double pred = svm.svm_predict(model, svmNode);
			if (gold > 0.0)
				goldPairs.add(j);
			if (pred > 0.0)
				resPairs.add(j);
		}
		goldStandardOrig.add(goldPairs);
		outputOrig.add(resPairs);

		Metrics<Integer> metrics = new Metrics<>();
		MetricsResultSet results = metrics.getResult(outputOrig,
				goldStandardOrig, new IndexMatch());

		return results;
	}

}