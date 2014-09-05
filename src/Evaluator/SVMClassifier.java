package it.giordizz.Thesis;

import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;
import it.giordizz.Thesis.SVMClassifier.setType;

import java.beans.FeatureDescriptor;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
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

	String suffixByMode=".txt";

	svm_problem trainProblem;
	svm_problem develProblem;
	svm_problem testProblem;
	
	
	String dir="";
	/**
	 * presenceCategoryPerQuery: array di n stringhe nell'alfabeto {"0","1"},
	 * dove n e' il numero di query che compongono il set considerato (o training
	 * o dev). L'i-esimo elemento dell'array e' "1" se la i-esima query del set e'
	 * taggata con la categoria in esame, "0" altrimenti.
	 */
	boolean[] presenceCategoryPerQuery = null;
	int totNumOfFtrs=-1;


	/**
	 * setType: specifica se i dati da raccogliere fanno parte del training o
	 * del development set
	 */
	public enum setType {
		TRAINING, DEVELOPMENT, TEST
	}

	/**
	 * Raccoglie gli esempi dai file, inserendoli nel rispettivo gatherer a
	 * seconda del parametro specificato.
	 * <p/>
	 * Nei file di tipo "results*" sono presenti tante righe quante sono le
	 * query del rispettivo set; ogni riga del file identifica l'array di
	 * features di una specifica query nel formato "f1,f2,...fn" dove n e'
	 * attualmente 123.
	 * <p/>
	 * Nei file di tipo "presence_cat-target*" sono presenti 2 righe per ogni
	 * categoria target (quindi in tutto ci sono 134 righe); La prima di queste
	 * due righe identifica il nome della categoria, la seconda e', invece, nel
	 * formato "B1	B2		BN" dove N e' la dimensione del rispettivo set; il
	 * generico valore Bi e' 1 se la i-esima query del set e' taggata con la su
	 * detta categoria, 0 altrimenti.
	 * 
	 * @param T
	 *            permette di identificare se si desidera prelevare gli esempi
	 *            dal training set o dal development set.
	 * @throws Exception 
	 */

	public void gatherUnlabeledData(setType T) throws Exception {

		String featuresFile = (T == setType.TRAINING) ? "data/"+ dir +"results_training" + suffixByMode
				: (T == setType.DEVELOPMENT) ? "data/"+ dir +"results_validation" + suffixByMode :
					"data/"+ dir +"results_test" + suffixByMode ;



		
		BufferedReader reader = new BufferedReader(new FileReader(featuresFile));

		
		
		
		
		Vector<svm_node[]> auxVector =  new Vector<svm_node[]>();
		String lineOfFeatures;
		while  ( (lineOfFeatures = reader.readLine()) != null) {
			String[] features = lineOfFeatures.split(",");


			Vector<svm_node> auxNodeVect = new Vector<svm_node>();
			int ftrIdx = 0;
			for (; ftrIdx < features.length; ftrIdx++) {
				Double d = Double.parseDouble(features[ftrIdx]);
		
				if (d!=0.) {
					
					svm_node auxNode = new svm_node();
					auxNode.index = ftrIdx+1;
					auxNode.value = d;
					auxNodeVect.add(auxNode);
					
					}
					
			}

			if (totNumOfFtrs == -1)
				totNumOfFtrs = features.length;
			
			svm_node[] nodeVect = new svm_node[auxNodeVect.size()];
			for (int i=0; i< nodeVect.length; i++)
				nodeVect[i] = auxNodeVect.elementAt(i);

			auxVector.add(nodeVect);
	
		}

		
		svm_problem problem = new svm_problem();
		problem.l = auxVector.size();
		problem.x = new svm_node[problem.l][];
		problem.y = new double[problem.l];
		
		
		for  ( int i=0; i < problem.l ; i++ ) 
			problem.x[i]=auxVector.elementAt(i);
		
		
		
		if (T == setType.TRAINING)			
			trainProblem=problem;
		else if (T == setType.DEVELOPMENT)	
			develProblem=problem;
		else
			testProblem=problem;
		
		reader.close();

	}
	

	public int setLabels(String categoryName,setType T) throws IOException {

			String presenceFile = (T == setType.TRAINING) ? "data/presence_cat-target_training.txt"
					:  (T == setType.DEVELOPMENT) ? "data/presence_cat-target_validation.txt"
							:  "data/presence_cat-target_test.txt";

			svm_problem problem = (T == setType.TRAINING) ? trainProblem 
					:  (T == setType.DEVELOPMENT) ? develProblem 
							: testProblem;

			
			
			BufferedReader reader = new BufferedReader(new FileReader(presenceFile));

			int countExmplsPos = 0;
			String line = null;
			boolean catOk = false;
			while ( !catOk && (line = reader.readLine()) != null )
				if (line.equals(categoryName)) {
					String[] aux = reader.readLine().split("\t");

					for (int presenceIdx = 0; presenceIdx < aux.length ; presenceIdx++ ){
						if (aux[presenceIdx].equals("1"))
							countExmplsPos++;
						problem.y[presenceIdx] = (aux[presenceIdx].equals("1")) ? 1.0 : -1.0 ;	
					}
										
					catOk = true;
				}
			
			reader.close();
			
			if (!catOk)
				throw new CategoryNotPresent(categoryName);
			
			return countExmplsPos;
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
	public Pair<Pair<Pair<Float, Float>, Float>, HashSet<Integer>> test(setType typeOfTestSet, Pair<Float,Float>  weight) throws Exception {


		svm_problem problemToTest = (typeOfTestSet == setType.DEVELOPMENT) ? develProblem : testProblem;
		
		Pair<Pair<Float, Float>, Float> res = new Pair<Pair<Float, Float>, Float>(null,0.f);

		svm_model model = SupportSVM.trainModel(svm_parameter.C_SVC, weight.first, weight.second, trainProblem,(double) 3.0 / (double) totNumOfFtrs, 5/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
		Pair<MetricsResultSet, HashSet<Integer>> results = ParameterTester.computeMetrics(model, problemToTest);
		MetricsResultSet metrics = results.first;
	

		float currF1 = metrics.getMacroF1() * 100;
		

		res.first = weight;
		res.second = currF1;
		

		

		return new Pair<Pair<Pair<Float, Float>, Float>, HashSet<Integer>>(res, results.second);

	}
	
	
	public  Pair<Pair<Pair<Float, Float>, Float>, HashSet<Integer>> test(setType typeOfTestSet) throws Exception {

		svm_problem problemToTest = (typeOfTestSet == setType.DEVELOPMENT) ? develProblem : testProblem;

		Pair<Pair<Float, Float>, Float> res = new Pair<Pair<Float, Float>, Float>(new Pair<Float, Float>(1f,1f),0.f);
		HashSet<Integer> categoryIndexes= new HashSet<Integer>();

		for (float ww = 1f; ww < 50f ; ww *= 51.1f) {
			for (float w = 1f; w < 50f ; w *= 1.1f) {
	
				svm_model model = SupportSVM.trainModel(svm_parameter.C_SVC, w, ww, trainProblem,(double) 3.0 / (double) totNumOfFtrs , 5/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
				Pair<MetricsResultSet, HashSet<Integer>> results = ParameterTester.computeMetrics(model, problemToTest);
				MetricsResultSet metrics = results.first;
			
	
				float currF1 = metrics.getMacroF1() * 100;
				
				if (currF1 > res.second ) { 
		
					res.first.first = w;
					res.first.second = ww;
					res.second = currF1;
					categoryIndexes= results.second;
				}
	
			}
		}

		return new Pair<Pair<Pair<Float, Float>, Float>, HashSet<Integer>>(res, categoryIndexes);

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

	public class CategoryNotPresent extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public CategoryNotPresent(String categoryName) {
			super("Category <" +categoryName+"> not found..");
		}
		
	}
	
//
//	public SVMClassifier(int mode) {
//
//		
//		switch (mode) {
//			case 0: break;
//			case 1: this.suffixByMode = "_path.txt";
//					break;
//			case 2: this.suffixByMode = "_path_range.txt";
//					break;
//			case 3: this.suffixByMode = "_range.txt";
//		}
//		
//	}
//
//
//	public SVMClassifier(int mode, int topK) { //TODO
//		switch (mode) {
//		case 0: break;
//		case 1: this.suffixByMode = "_path.txt";
//				break;
//		case 2: this.suffixByMode = "_path_range.txt";
//				break;
//		case 3: this.suffixByMode = "_range.txt";
//		
//	
//		}
//		
////		dir="NEWNEW8top" + topK + "/";
//		dir="initialBestAll/";
//	}


	public void scaleProblems(){

		Pair<double[], double[]> minsAndMaxs = LibSvmUtils.findRanges(trainProblem,totNumOfFtrs);
	
		LibSvmUtils.scaleProblem(trainProblem, minsAndMaxs.first, minsAndMaxs.second);
		LibSvmUtils.scaleProblem(develProblem,  minsAndMaxs.first, minsAndMaxs.second);
		LibSvmUtils.scaleProblem(testProblem,  minsAndMaxs.first, minsAndMaxs.second);
	}
	
	public void scaleProblems(setType setToTest){
		System.err.println("numero di features " + totNumOfFtrs);
		Pair<double[], double[]> minsAndMaxs = LibSvmUtils.findRanges(trainProblem,totNumOfFtrs);
	
		LibSvmUtils.scaleProblem(trainProblem, minsAndMaxs.first, minsAndMaxs.second);
		
		if (setToTest == setType.DEVELOPMENT)
			LibSvmUtils.scaleProblem(develProblem,  minsAndMaxs.first, minsAndMaxs.second);
		else
			LibSvmUtils.scaleProblem(testProblem,  minsAndMaxs.first, minsAndMaxs.second);
	}

	public void createSVMProblemsByFiles(String prefixDir) throws Exception {
		this.dir=prefixDir + "_Features/";
		
		System.err.println("start gathering training examples..");
		gatherUnlabeledData(setType.TRAINING);
		System.err.println("-> training examples gathered! <-");
		
		System.err.println("start gathering development examples..");
		gatherUnlabeledData(setType.DEVELOPMENT);		
		System.err.println("->  development examples gathered! <-");
		
		System.err.println("start gathering test examples..");
		gatherUnlabeledData(setType.TEST);		
		System.err.println("->  development examples gathered! <-");
		
		System.err.println("start scaling problems..");
		scaleProblems();
		System.err.println("-> problems scaled! <-");
	}


	public Triple<Integer, Integer, Integer> setLabels(String categoryName) throws IOException {
		
//		Triple<Integer, Integer, Integer> exemplesPosCountForCat = 
		System.err.println("start labeling examples..");
		int t = setLabels(categoryName,setType.TRAINING);
		int d = setLabels(categoryName,setType.DEVELOPMENT);
		int tt = setLabels(categoryName,setType.TEST);
		System.err.println("end labeling examples..");
		
		return new Triple<Integer,Integer,Integer>(t, d , tt);
		
	}


	public double[] getRegressorWeightsByCategory() {
		return null;
		//TODO
	}


	public double[] getRegressorWeightsByCategory(setType typeOfDataSet, svm_model model) {
		svm_problem problem = (typeOfDataSet == setType.TRAINING) ? trainProblem 
				: (typeOfDataSet == setType.DEVELOPMENT) ? develProblem : testProblem;
		
		return generateFeatures(model, problem);

		
		

	}


	private double[] generateFeatures(svm_model model, svm_problem develProblem) {
		double[] weights= new double[develProblem.l];
		
		for (int j = 0; j < develProblem.l; j++) {
			svm_node[] svmNode = develProblem.x[j];
			weights[j] = svm.svm_predict(model, svmNode);
			
		}
		return weights;
	
		
	}

//	public svm_problem updateTrainProblem(setType double[][] secondStageFeatures) {
//		
//		int numFtrsSndStage = secondStageFeatures[0].length;
//		
//		trainProblem.x = new svm_node[trainProblem.l][numFtrsSndStage];
//
//		
//		for (int ftrIdx=0; ftrIdx < numFtrsSndStage; ftrIdx++)
//			for(int catIdx=0; catIdx < secondStageFeatures.length ; catIdx++){
//				trainProblem.x[catIdx][ftrIdx].value = secondStageFeatures[catIdx][ftrIdx];
//				trainProblem.x[catIdx][ftrIdx].index = ftrIdx+1;
//			}
//				
//		return null; 
//
//	}
	public void updateProblem(setType typeOfDataSet, double[][] secondStageFeatures) throws FileNotFoundException, UnsupportedEncodingException {
		
		int numFtrsSndStage = secondStageFeatures.length;
		svm_problem problem = (typeOfDataSet == setType.TRAINING) ? trainProblem 
				: (typeOfDataSet == setType.DEVELOPMENT) ? develProblem : testProblem;
		
		problem.x = new svm_node[problem.l][numFtrsSndStage];

		PrintWriter writer = new PrintWriter( typeOfDataSet.name() + "_features.txt", "UTF-8");
		for (int exmplIdx=0; exmplIdx < problem.l; exmplIdx++){
			for(int catIdx=0; catIdx < secondStageFeatures.length ; catIdx++){
				problem.x[exmplIdx][catIdx] = new svm_node();
				
				problem.x[exmplIdx][catIdx].value = secondStageFeatures[catIdx][exmplIdx];
				writer.print(secondStageFeatures[catIdx][exmplIdx] + " ");
				problem.x[exmplIdx][catIdx].index = catIdx+1;
			}
			writer.println();
		}
				
		writer.close();
	}


	public void updateProblems(setType setToTest, double[][] secondStageFeaturesTrain,
			double[][] secondStageFeaturesToTest) throws FileNotFoundException, UnsupportedEncodingException {
		
		totNumOfFtrs = secondStageFeaturesTrain.length;
		
		updateProblem(setType.TRAINING, secondStageFeaturesTrain);
		
		if (setToTest == setType.DEVELOPMENT)
			updateProblem(setType.DEVELOPMENT, secondStageFeaturesToTest);
		else
			updateProblem(setType.TEST, secondStageFeaturesToTest);
		
		
		System.err.println("start scaling problems..");
		scaleProblems(setToTest);
		System.err.println("-> problems scaled! <-");
	}


	public void writeLabelsToFile(setType setToTest,
			ArrayList<Pair<String, HashSet<Integer>>> labelsPerCategory) throws IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("data/finalOrder/" + setToTest.name().toLowerCase() + "FinalOrder.txt"));
		PrintWriter out = new PrintWriter(setToTest.name().toLowerCase() + "Labels");
		String query;
		for(int count=0; (query = reader.readLine()) != null; count++) {
			out.println(query);
			for (Pair<String,HashSet<Integer>> labelsCurrCategory: labelsPerCategory){
				if (labelsCurrCategory.second.contains(count))
					out.print("\t" + labelsCurrCategory.first);
			}
			out.println();
		}
		reader.close();
	}


	public Pair<Pair<Float, Float>, Float> tuneWeights() throws IOException {
		

	
			Pair<Pair<Float, Float>, Float> res = new Pair<Pair<Float, Float>, Float>(new Pair<Float, Float>(1f,1f),0.f);
			HashSet<Integer> categoryIndexes= new HashSet<Integer>();

			for (float ww = 1f; ww < 50f ; ww *= 51.1f) {
				for (float w = 1f; w < 50f ; w *= 1.1f) {
		
					svm_model model = SupportSVM.trainModel(svm_parameter.C_SVC, w, ww, trainProblem,(double) 3.0 / (double) totNumOfFtrs , 1.2/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
					float currF1 = ParameterTester.computeMetricsSingleCategory(model, develProblem);

					if (currF1 > res.second ) { 
						res.first.first = w;
						res.first.second = ww;
						res.second = currF1;
					}
		
				}
			}
			return res;

		
	}

}
