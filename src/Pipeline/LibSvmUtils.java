package it.giordizz.Thesis;
import it.acubelab.batframework.utils.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import libsvm.svm_node;
import libsvm.svm_problem;

public class LibSvmUtils {
	//[-1,1]
	public static double scale(double value, double rangeMin, double rangeMax) {
		return rangeMax == rangeMin? 0.0 : (value - rangeMin) / (rangeMax - rangeMin) * 2f - 1;
	}
	
	//[0,1]
	public static double scale2(double value, double rangeMin, double rangeMax) {
		return rangeMax == rangeMin? 0.0 : (value - rangeMin) / (rangeMax - rangeMin) ;
	}



	public static Pair<double[], double[]> findRanges(svm_problem problem, int maxnftrs) {

		double[] rangeMins = new double[maxnftrs];
		double[] rangeMaxs = new double[maxnftrs];

		
		for ( svm_node[] v : problem.x)
			for ( svm_node n : v) {
				if (rangeMins[n.index-1] > n.value)
					rangeMins[n.index-1] = n.value;

				if (rangeMaxs[n.index-1] < n.value)
					rangeMaxs[n.index-1] =n.value;
			}
		
		

		return new Pair<>(rangeMins,rangeMaxs);
	}
	
	public static void scaleProblem(svm_problem problem, double[] rangeMins,
			double[] rangeMaxs) {
		for (int i = 0; i < problem.l; i++)
			scaleNode(problem.x[i], rangeMins, rangeMaxs);
	}

	public static void dumpRanges(double[] mins, double[] maxs, String filename)
			throws IOException {
		BufferedWriter br = new BufferedWriter(new FileWriter(filename));
		br.write("x\n-1 1\n");
		for (int i = 0; i < mins.length; i++)
			br.write(String.format("%d %f %f%n", i + 1, mins[i], maxs[i]));
		br.close();
	}

	public static void scaleNode(svm_node[] ftrVect, double[] rangeMins,
			double[] rangeMaxs) {
		
		for (int i = 0; i < ftrVect.length; i++)
			ftrVect[i].value = scale2(ftrVect[i].value, rangeMins[ftrVect[i].index-1],
					rangeMaxs[ftrVect[i].index-1]);

	}
}