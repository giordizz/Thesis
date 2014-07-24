package it.giordizz.Thesis;

import java.util.Vector;

import libsvm.svm_node;
import libsvm.svm_problem;

public class BinaryExampleGatherer {
	private Vector<double[]> positiveFeatureVectors = new Vector<>();
	private Vector<double[]> negativeFeatureVectors = new Vector<>();
	private int ftrCount = -1;

	private static Vector<double[]> getPlain(Vector<Vector<double[]>> vectVect) {
		Vector<double[]> res = new Vector<>();
		for (Vector<double[]> vect : vectVect)
			res.addAll(vect);
		return res;
	}


	public svm_problem generateLibSvmProblem() {
		Vector<Integer> allFtrs = new Vector<>();
		for (int i = 1; i < this.getFtrCount() + 1; i++)
			allFtrs.add(i);
		return generateLibSvmProblem(allFtrs);
	}

	public int getFtrCount() {
		return ftrCount;
	}

	public void addPositiveExample(double[] posVectors) {
		
			
		if (ftrCount == -1)
			ftrCount = posVectors.length;
		else if (ftrCount != posVectors.length)
			throw new RuntimeException(
					"Adding feature of a wrong size. ftrCount="
							+ ftrCount);
	
		
		positiveFeatureVectors.add(posVectors);

	}
	public void addNegativeExample(double[] negVectors) {
		
		
		if (ftrCount == -1)
			ftrCount = negVectors.length;
		else if (ftrCount != negVectors.length)
			throw new RuntimeException(
					"Adding feature of a wrong size. ftrCount="
							+ ftrCount);
	

		negativeFeatureVectors.add(negVectors);
	}

	public svm_problem generateLibSvmProblem(Vector<Integer> pickedFtrsI) {
		Vector<Double> targets = new Vector<Double>();
		Vector<svm_node[]> ftrVectors = new Vector<svm_node[]>();
		for (double[] posVect : getPlain(positiveFeatureVectors)) {
			ftrVectors.add(LibSvmUtils
					.featuresArrayToNode(posVect, pickedFtrsI));
			targets.add(1.0);
		}
		for (double[] negVect : getPlain(negativeFeatureVectors)) {
			ftrVectors.add(LibSvmUtils
					.featuresArrayToNode(negVect, pickedFtrsI));
			targets.add(-1.0);
		}

		svm_problem problem = new svm_problem();
		problem.l = targets.size();
		problem.x = new svm_node[problem.l][];
		for (int i = 0; i < problem.l; i++)
			problem.x[i] = ftrVectors.elementAt(i);
		problem.y = new double[problem.l];
		for (int i = 0; i < problem.l; i++)
			problem.y[i] = targets.elementAt(i);

		return problem;

	}


}