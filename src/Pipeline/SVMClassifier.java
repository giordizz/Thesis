package it.giordizz.Thesis;

import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
	String categoryName;
	String suffixByMode=".txt";
	
//	Vector<double[]> posVectors;
//	Vector<double[]> negVectors;
	svm_problem trainProblem; //TODO
	svm_problem develProblem;
	
	int countTrain=0, countDev=0;
	
	String dir="";
	/**
	 * presenceCategoryPerQuery: array di n stringhe nell'alfabeto {"0","1"},
	 * dove n e' il numero di query che compongono il set considerato (o training
	 * o dev). L'i-esimo elemento dell'array e' "1" se la i-esima query del set e'
	 * taggata con la categoria in esame, "0" altrimenti.
	 */
	boolean[] presenceCategoryPerQuery = null;
	int totNumOfFtrs=244; //TODO

//	BinaryExampleGatherer trainGatherer = new BinaryExampleGatherer();
//	BinaryExampleGatherer testGatherer = new BinaryExampleGatherer();

	/**
	 * setType: specifica se i dati da raccogliere fanno parte del training o
	 * del development set
	 */
	public enum setType {
		TRAINING, DEVELOPMENT
	}


	
	

	public void setLabels(String categoryName,setType T) throws IOException {

			String presenceFile = (T == setType.TRAINING) ? "data/presence_cat-target_training.txt"
					: "data/presence_cat-target_validation.txt";

			svm_problem problem = (T == setType.TRAINING) ? trainProblem : develProblem;

			BufferedReader reader = new BufferedReader(new FileReader(presenceFile));

			String line = null;
			boolean catOk = false;
			while ( !catOk && (line = reader.readLine()) != null )
				if (line.equals(categoryName)) {
					String[] aux = reader.readLine().split("\t");

					for (int presenceIdx = 0; presenceIdx < aux.length ; presenceIdx++ )
						problem.y[presenceIdx] = (aux[presenceIdx].equals("1")) ? 1.0 : -1.0 ;					
					catOk = true;
				}
			
			reader.close();
			
			if (!catOk)
				throw new CategoryNotPresent(categoryName);
	}

	
	/**
	 * Questo metodo, ancora incompleto, permette di effettura la fase di
	 * learning e la successiva fase di valutazione che consente, di valutare
	 * quale modello produce migliori risultati in termini di precision, recall
	 * e F1 per una specifica categoria.
	 * @param weight 
	 * @throws Exception 
	 */
	public Pair<Float, Float> test(float weight) throws Exception {



		Pair<Float, Float> res = new Pair<Float, Float>(0.f,0.f);

//		for (float w = 1f; w < 50f ; w *= 1.1f) {
		//double N = totNumOfFtrs < 1000 ? (double) 3.0 / (double) totNumOfFtrs : Math.pow(10, -3);
//			System.err.println(N);
//			svm_model model = SupportSVM.trainModel(weight, 1, trainProblem,Math.pow(10, -3), Math.pow(10, pow)/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
		svm_model model = SupportSVM.trainModel(weight, 1, trainProblem,(double) 3.0 / (double) totNumOfFtrs , 1/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
			MetricsResultSet metrics = ParameterTester.computeMetrics(model, develProblem);
		
//			dumpCategorization(model,develProblems);
//			
//			System.out.printf("MACRO:  %.5f%%\t%.5f%%\t%.5f%%%n",
//					metrics.getMacroPrecision() * 100,
//					metrics.getMacroRecall() * 100, metrics.getMacroF1() * 100);

			float currF1 = metrics.getMacroF1() * 100;
			
			if (true ||currF1 > res.second ) { //TODO
				res.first = weight;
				res.second = currF1;
			}

			// svm.svm_save_model("models/" + categoryName + ".model", model);
//		}

		return res;

	}
	
	


	public class CategoryNotPresent extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public CategoryNotPresent(String categoryName) {
			super("Category <" +categoryName+"> not found..");
		}
		
	}
	

	public SVMClassifier(int mode) {

		
		switch (mode) {
			case 0: break;
			case 1: this.suffixByMode = "_path.txt";
					break;
			case 2: this.suffixByMode = "_path_range.txt";
					break;
			case 3: this.suffixByMode = "_range.txt";
		}
		trainProblem = new svm_problem();
		trainProblem.l = 428; //TODO
		trainProblem.x = new svm_node[428][];
		trainProblem.y = new double[428];
		
		develProblem = new svm_problem();
		develProblem.l = 174; //TODO
		develProblem.x = new svm_node[174][];
		develProblem.y = new double[174];
		
		
	}





	public void scaleProblems(){

		Pair<double[], double[]> minsAndMaxs = LibSvmUtils.findRanges(trainProblem,totNumOfFtrs);
		

				
		LibSvmUtils.scaleProblem(trainProblem, minsAndMaxs.first, minsAndMaxs.second);
		LibSvmUtils.scaleProblem(develProblem,  minsAndMaxs.first, minsAndMaxs.second);
	}




	public void setLabels(String categoryName) throws IOException {
		System.err.println("start labeling examples..");
		setLabels(categoryName,setType.TRAINING);
		setLabels(categoryName,setType.DEVELOPMENT);
		System.err.println("end labeling examples..");
		
	}


	public void addExample(svm_node[] nodeVect, int whichDataSet) {
		
//		if (totNumOfFtrs==-1)
//			totNumOfFtrs=nodeVect.length;
		
		if (whichDataSet == 0) {
			
			trainProblem.x[countTrain++]=nodeVect;
		} else
			develProblem.x[countDev++]=nodeVect;
			
			
		
	}


	public void init() {
		countTrain=0;
		countDev=0;
		
	}


}
