package it.giordizz.Thesis;

import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;
import it.giordizz.Thesis.Gatherer.Problems;

import java.io.IOException;
import java.util.ArrayList;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;

/**
 * <h1>SVMClassifier</h1> La classe SVMClassifier si occupa della raccolta degli
 * esempi, del training del classificatore e della successiva valutazione
 * 
 * @author Giordano
 * @version 1.0
 * 
 */
public class SVMClassifier {

	svm_problem trainProblem;
	svm_problem develProblem;

	int totNumOfFtrs=-1;

	/**
	 * setType: specifica se i dati da raccogliere fanno parte del training o
	 * del development set
	 */
	public enum setType {
		TRAINING, DEVELOPMENT
	}

	

	public void setLabels(String categoryName,ArrayList<Boolean> presences, setType T) throws IOException {

			svm_problem problem = (T == setType.TRAINING) ? trainProblem : develProblem;


			for (int presenceIdx = 0; presenceIdx < presences.size() ; presenceIdx++ )
				problem.y[presenceIdx] = (presences.get(presenceIdx)) ? 1.0 : -1.0 ;			

	}

	
	/**
	 * Questo metodo, ancora incompleto, permette di effettura la fase di
	 * learning e la successiva fase di valutazione che consente, di valutare
	 * quale modello produce migliori risultati in termini di precision, recall
	 * e F1 per una specifica categoria.
	 * @param pow 
	 * @param weight 
	 * @throws Exception 
	 */
	public Pair<Float, Float> test( float weight) throws Exception {



		Pair<Float, Float> res = new Pair<Float, Float>(0.f,0.f);

		svm_model model = SupportSVM.trainModel(weight, 1, trainProblem,(double) 3.0 / (double) totNumOfFtrs , 1/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
			MetricsResultSet metrics = ParameterTester.computeMetrics(model, develProblem);

			float currF1 = metrics.getMacroF1() * 100;
			
			if (true || currF1 > res.second ) { //TODO
				res.first = weight;
				res.second = currF1;
			}


		return res;

	}
	
	
	
	
	

	public void dumpCategorization(svm_model model, svm_problem testProblem)
			throws IOException {
		for (int j = 0; j < testProblem.l; j++) {
			svm_node[] svmNode = testProblem.x[j];
			double gold = testProblem.y[j];
			double pred = svm.svm_predict(model, svmNode);
			if (gold > 0.0 && pred > 0.0)
				System.out.printf("Query id=%d: TP%n", j);
			if (gold > 0.0 && pred < 0.0)
				System.out.printf("Query id=%d: FN%n", j);
			if (gold < 0.0 && pred > 0.0)
				System.out.printf("Query id=%d: FP%n", j);
			if (gold < 0.0 && pred < 0.0)
				System.out.printf("Query id=%d: TN%n", j);
		}

	}

	


	public SVMClassifier(Problems copyOfProblems) {
		trainProblem = copyOfProblems.trainProblem;
		develProblem = copyOfProblems.develProblem;
		totNumOfFtrs = copyOfProblems.totNumOfFtrs;
	}


	public void scaleProblems(){

		Pair<double[], double[]> minsAndMaxs = LibSvmUtils.findRanges(trainProblem,totNumOfFtrs);
		

				
		LibSvmUtils.scaleProblem(trainProblem, minsAndMaxs.first, minsAndMaxs.second);
		LibSvmUtils.scaleProblem(develProblem,  minsAndMaxs.first, minsAndMaxs.second);
	}



	public void setLabels(String categoryName, Container container) throws IOException {

		setLabels(categoryName, container.trainingLabels.get(categoryName), setType.TRAINING);
		setLabels(categoryName, container.developmentLabels.get(categoryName), setType.DEVELOPMENT);

		
	}


//	public void updateFeatures(HashSet<Integer>[][] resultsTraining,
//			HashSet<Integer>[][] resultsDevelopment) {
//		
//		for (int queryIdx=0; queryIdx <resultsTraining.length; queryIdx++  ) {
////			System.err.println(resultsTraining[queryIdx][0] + " - " +resultsTraining[queryIdx][1]);
//			
//			if (resultsTraining[queryIdx][0]!=null)
//				trainProblem.x[queryIdx][trainProblem.x[queryIdx].length-2].value = 
//						resultsTraining[queryIdx][0].size();
//			
//			if (resultsTraining[queryIdx][1]!=null)
//				trainProblem.x[queryIdx][trainProblem.x[queryIdx].length-1].value =
//						resultsTraining[queryIdx][1].size();
//		}
//		for (int queryIdx=0; queryIdx <resultsDevelopment.length; queryIdx++  ) {
////			System.err.println(resultsDevelopment[queryIdx][0] + " - " +resultsDevelopment[queryIdx][1]);
//			if (resultsDevelopment[queryIdx][0]!=null)
//				develProblem.x[queryIdx][develProblem.x[queryIdx].length-2].value =
//					resultsDevelopment[queryIdx][0].size();
//			
//			if (resultsDevelopment[queryIdx][1]!=null)
//				develProblem.x[queryIdx][develProblem.x[queryIdx].length-1].value = 
//					resultsDevelopment[queryIdx][1].size();
//		}
//	}
	public void updateFeatures(int[][] resultsTraining,
			int[][] resultsDevelopment) {
		
		for (int queryIdx=0; queryIdx <resultsTraining.length; queryIdx++  ) {
//			System.err.println(resultsTraining[queryIdx][0] + " - " +resultsTraining[queryIdx][1]);

				trainProblem.x[queryIdx][trainProblem.x[queryIdx].length-2].value = 
						resultsTraining[queryIdx][0];

				trainProblem.x[queryIdx][trainProblem.x[queryIdx].length-1].value =
						resultsTraining[queryIdx][1];
		}
		for (int queryIdx=0; queryIdx <resultsDevelopment.length; queryIdx++  ) {
//			System.err.println(resultsDevelopment[queryIdx][0] + " - " +resultsDevelopment[queryIdx][1]);

				develProblem.x[queryIdx][develProblem.x[queryIdx].length-2].value =
					resultsDevelopment[queryIdx][0];
			
				develProblem.x[queryIdx][develProblem.x[queryIdx].length-1].value = 
					resultsDevelopment[queryIdx][1];
		}
	}

}
