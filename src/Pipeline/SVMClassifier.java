package it.giordizz.Thesis;

import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;
import it.giordizz.Thesis.Gatherer.Problems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

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
//	String categoryName;
//	String suffixByMode=".txt";
	
//	Vector<double[]> posVectors;
//	Vector<double[]> negVectors;
	svm_problem trainProblem;
	svm_problem develProblem;
	
//	String dir="";
	/**
	 * presenceCategoryPerQuery: array di n stringhe nell'alfabeto {"0","1"},
	 * dove n e' il numero di query che compongono il set considerato (o training
	 * o dev). L'i-esimo elemento dell'array e' "1" se la i-esima query del set e'
	 * taggata con la categoria in esame, "0" altrimenti.
	 */
//	boolean[] presenceCategoryPerQuery = null;
	int totNumOfFtrs=-1;

//	BinaryExampleGatherer trainGatherer = new BinaryExampleGatherer();
//	BinaryExampleGatherer testGatherer = new BinaryExampleGatherer();

	/**
	 * setType: specifica se i dati da raccogliere fanno parte del training o
	 * del development set
	 */
	public enum setType {
		TRAINING, DEVELOPMENT
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

//	public static void gatherUnlabeledData(setType T) throws Exception {
//
//		String featuresFile = (T == setType.TRAINING) ? "data/results_training.txt" 
//				: "data/results_validation.txt";
//
//
//
//		
//		BufferedReader reader = new BufferedReader(new FileReader(featuresFile));
//
//		
//		
//		
//		
//		Vector<svm_node[]> auxVector =  new Vector<svm_node[]>();
//		String lineOfFeatures;
//		while  ( (lineOfFeatures = reader.readLine()) != null) {
//			String[] features = lineOfFeatures.split(",");
//
//
//			Vector<svm_node> auxNodeVect = new Vector<svm_node>();
//			int ftrIdx = 0;
//			for (; ftrIdx < features.length; ftrIdx++) {
//				Double d = Double.parseDouble(features[ftrIdx]);
////	TODO			
//				if (d!=0.) {
//					
//					svm_node auxNode = new svm_node();
//					auxNode.index = ftrIdx+1;
//					auxNode.value = d;
//					auxNodeVect.add(auxNode);
////						
//					}
//					
//			}
//
//			if (totNumOfFtrs == -1)
//				totNumOfFtrs = ftrIdx+2;
//			
//			svm_node[] nodeVect = new svm_node[auxNodeVect.size()+2];
//			int i=0;
//			for (; i< auxNodeVect.size(); i++)
//				nodeVect[i] = auxNodeVect.elementAt(i);
//			
//			svm_node plus1Node = new svm_node();
//			plus1Node.index = 247;
//			plus1Node.value = 0;
//			nodeVect[i++]= plus1Node;
//			
//			svm_node plus2Node = new svm_node();
//			plus2Node.index = 248;
//			plus2Node.value = 0;
//			nodeVect[i]= plus2Node;
//			
////			auxNodeVect.clear();
//			auxVector.add(nodeVect);
//	
//		}
//
//		
//		svm_problem problem = new svm_problem();
//		problem.l = auxVector.size();
//		problem.x = new svm_node[problem.l][];
//		problem.y = new double[problem.l];
//		
//		
//		for  ( int i=0; i < problem.l ; i++ ) 
//			problem.x[i]=auxVector.elementAt(i);
//		
//		
//		
//		if (T == setType.TRAINING)			
//			trainProblem=problem;
//		else
//			develProblem=problem;
//		
//		reader.close();
//
//	}
	

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

	public class CategoryNotPresent extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public CategoryNotPresent(String categoryName) {
			super("Category <" +categoryName+"> not found..");
		}
		
	}
	


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
//		dir="newnew8top" + topK + "/";
//	}



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

//
//	public static void gatherData() throws Exception {
//		System.err.println("start gathering training examples..");
//		gatherUnlabeledData(setType.TRAINING);
//		System.err.println("-> training examples gathered! <-");
//		
//		System.err.println("start gathering development examples..");
//		gatherUnlabeledData(setType.DEVELOPMENT);		
//		System.err.println("->  development examples gathered! <-");
//		
////		System.err.println("start scaling problems..");
////		scaleProblems();
////		System.err.println("-> problems scaled! <-");
//	}


	public void setLabels(String categoryName) throws IOException {
//		System.err.println("start labeling examples..");
		setLabels(categoryName,setType.TRAINING);
		setLabels(categoryName,setType.DEVELOPMENT);
//		System.err.println("end labeling examples..");
		
	}


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
			develProblem.x[queryIdx][develProblem.x[queryIdx].length-2].value = resultsDevelopment[queryIdx][0];
			develProblem.x[queryIdx][develProblem.x[queryIdx].length-1].value = resultsDevelopment[queryIdx][1];
		}
	}


}
