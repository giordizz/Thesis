package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;


import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class SupportSVM {
	public static svm_parameter getParameters(double wPos,
			double wNeg, double gamma, double C) {
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 2;
		param.gamma = gamma;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = C;
		param.eps = 0.001;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 2;
		param.weight_label = new int[] { 1, -1 };
		param.weight = new double[] { wPos, wNeg };
		return param;
	}

	public static svm_model trainModel(double wPos, double wNeg, svm_problem trainProblem,double gamma, double C) {
		svm_parameter param = getParameters(wPos, wNeg, gamma, C);

		String error_msg = svm.svm_check_parameter(trainProblem, param);

		if (error_msg != null) {
			System.err.print("ERROR: " + error_msg + "\n");
			System.exit(1);
		}

		return svm.svm_train(trainProblem, param);
	}
	
	

	public static Triple<svm_problem, double[], double[]> getScaledProblem(svm_problem problem) {



		// find ranges for all features of training set
//		Pair<double[], double[]> minsAndMaxs = LibSvmUtils.findRanges(problem);
//
//		double[] mins = minsAndMaxs.first;
//		double[] maxs = minsAndMaxs.second;
//
//
//		LibSvmUtils.scaleProblem(problem, mins, maxs);

		return new Triple<svm_problem, double[], double[]>(
				problem, null, null);
	}
	
	public static svm_problem getScaledTestProblem(
			Vector<Integer> pickedFtrsI, BinaryExampleGatherer testGatherer,
			double[] mins, double[] maxs) {
		
		svm_problem testProblem = testGatherer.generateLibSvmProblem();
		LibSvmUtils.scaleProblem(testProblem, mins, maxs);
		return testProblem;
	}

	


	public static Vector<Integer> getAllFtrVect(int ftrCount) {
		Vector<Integer> res = new Vector<>();
		for (int i = 1; i < ftrCount + 1; i++)
			res.add(i);
		return res;
	}
	
}
