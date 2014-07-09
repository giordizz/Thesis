import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;

import it.acubelab.batframework.metrics.Metrics;
import it.acubelab.batframework.metrics.MetricsResultSet;

public class ParameterTester implements
			Callable<ModelConfigurationResult> {
		private double wPos, wNeg, editDistanceThreshold, gamma, C;
		private BinaryExampleGatherer trainEQFGatherer;
		private BinaryExampleGatherer testGatherer;
		private Vector<Integer> features;
		Vector<ModelConfigurationResult> scoreboard;

		public enum OptimizaionProfiles {
			MAXIMIZE_TN, MAXIMIZE_MICRO_F1, MAXIMIZE_MACRO_F1
		}
		
		
		public ParameterTester(double wPos, double wNeg,
				double editDistanceThreshold, Vector<Integer> features,
				BinaryExampleGatherer trainEQFGatherer,
				BinaryExampleGatherer testEQFGatherer,
				OptimizaionProfiles optProfile, double optProfileThreshold, double gamma, double C,
				Vector<ModelConfigurationResult> scoreboard) {
			this.wPos = wPos;
			this.wNeg = wNeg;
			this.editDistanceThreshold = editDistanceThreshold;
			this.features = features;
			this.trainEQFGatherer = trainEQFGatherer;
			this.testGatherer = testEQFGatherer;
			Collections.sort(this.features);
			this.scoreboard = scoreboard;
			this.gamma = gamma;
			this.C = C;
		}

		public static MetricsResultSet computeMetrics(svm_model model,
				Vector<svm_problem> testProblems) throws IOException {
			// Compute metrics
			/*
			 * { int tp = 0, fp = 0, fn = 0, tn = 0; for (int i = 0; i <
			 * testProblem.l; i++) { svm_node[] svmNode = testProblem.x[i];
			 * double gold = testProblem.y[i]; double pred =
			 * svm.svm_predict(model, svmNode); if (gold > 0 && pred > 0) tp++;
			 * if (gold < 0 && pred > 0) fp++; if (gold > 0 && pred < 0) fn++;
			 * if (gold < 0 && pred < 0) tn++; } float f1 =
			 * Metrics.F1(Metrics.recall(tp, fp, fn), Metrics.precision(tp,
			 * fp)); float fnRate = (float) fn / (float) (fn + tp); }
			 */

			List<HashSet<Integer>> outputOrig = new Vector<>();
			List<HashSet<Integer>> goldStandardOrig = new Vector<>();
			for (svm_problem testProblem : testProblems) {
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
			}

			Metrics<Integer> metrics = new Metrics<>();
			MetricsResultSet results = metrics.getResult(outputOrig,
					goldStandardOrig, new IndexMatch());
			

			return results;
		}

		@Override
		public ModelConfigurationResult call() throws Exception {

			Triple<svm_problem, double[], double[]> ftrsMinsMaxs = SupportSVM.getScaledTrainProblem(trainEQFGatherer);
			double[] mins = ftrsMinsMaxs.getMiddle();
			double[] maxs = ftrsMinsMaxs.getRight();
			svm_problem trainProblem = ftrsMinsMaxs.getLeft();

			svm_model model = SupportSVM.trainModel(wPos, wNeg, this.features,
					trainProblem, gamma, C);

			// Generate test problem and scale it.
			Vector<svm_problem> testProblems = SupportSVM.getScaledTestProblems(this.features,testGatherer, mins, maxs);

			MetricsResultSet metrics = computeMetrics(model, testProblems);

			int tp = metrics.getGlobalTp();
			int fp = metrics.getGlobalFp();
			int fn = metrics.getGlobalFn();
			float microF1 = metrics.getMicroF1();
			float macroF1 = metrics.getMacroF1();
			float macroRec = metrics.getMacroRecall();
			float macroPrec = metrics.getMacroPrecision();

			ModelConfigurationResult mcr = new ModelConfigurationResult(
					features, wPos, wNeg, editDistanceThreshold, tp, fp, fn,
					testGatherer.getFtrVectorCount() - tp - fp - fn, microF1,
					macroF1, macroRec, macroPrec);

			synchronized (scoreboard) {
				scoreboard.add(mcr);
			}
			return mcr;

		}

	

}