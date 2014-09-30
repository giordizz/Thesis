package it.giordizz.Thesis;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;
import it.acubelab.batframework.metrics.Metrics;
import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;

public class ParameterTester {


	/**
	 * Calcola F1 per il classificatore associato alla categoria target corrente.
	 * @param model
	 * @param testProblem
	 * @return
	 * @throws IOException
	 */
	public static float computeMetricsSingleCategory(svm_model model, svm_problem testProblem) throws IOException{
		Pair<HashSet<Integer>, HashSet<Integer>> goldAndRes = getOutputForCategory(model, testProblem);
		List<HashSet<Integer>> outputOrig = new Vector<>();
		List<HashSet<Integer>> goldStandardOrig = new Vector<>();

		outputOrig.add(goldAndRes.first);
		goldStandardOrig.add(goldAndRes.second);
		
//		ACCURACY
//		MetricsResultSet res = new Metrics<Integer>().getResult(outputOrig, goldStandardOrig, new IndexMatch());		
//		float TN = testProblem.l * 67 - (res.getTPs(0) + res.getFPs(0) + res.getFNs(0));
//		return (float) (res.getTPs(0) + TN) / (res.getTPs(0) + res.getFPs(0) + res.getFNs(0) + TN);
		
		return new Metrics<Integer>().getResult(outputOrig, goldStandardOrig, new IndexMatch()).getF1s(0);

	}


		public static Pair<HashSet<Integer>, HashSet<Integer>> getOutputForCategory(svm_model model,
				svm_problem testProblem) throws IOException {

			
			HashSet<Integer> resPairs = new HashSet<>();
			HashSet<Integer> goldPairs = new HashSet<>();
			

			
			for (int j = 0; j < testProblem.l; j++) {
				svm_node[] svmNode = testProblem.x[j];
				double gold = testProblem.y[j];
				double pred = svm.svm_predict(model, svmNode);
				if (gold > 0.0)
					goldPairs.add(j);
				if (pred > 0.0) 
					resPairs.add(j);
			}

			return new Pair<HashSet<Integer>, HashSet<Integer>>(resPairs, goldPairs);
		}


	public static Pair<MetricsResultSet, HashSet<Integer>> computeMetrics(svm_model model,
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

		return new Pair<MetricsResultSet, HashSet<Integer>>(results, resPairs);
	}

}