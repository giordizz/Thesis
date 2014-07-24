package it.giordizz.Thesis;

import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
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
	String categoryName;
	String suffixByMode=".txt";
	
//	Vector<double[]> posVectors;
//	Vector<double[]> negVectors;
	svm_problem trainProblem;
	svm_problem develProblem;
	

	/**
	 * presenceCategoryPerQuery: array di n stringhe nell'alfabeto {"0","1"},
	 * dove n e' il numero di query che compongono il set considerato (o training
	 * o dev). L'i-esimo elemento dell'array e' "1" se la i-esima query del set e'
	 * taggata con la categoria in esame, "0" altrimenti.
	 */
	boolean[] presenceCategoryPerQuery = null;
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
//
//	public void gatherData(setType T) throws Exception {
//
//		String featuresFile = (T == setType.TRAINING) ? "data/results_training" + suffixByMode
//				: "data/results_validation" + suffixByMode;
//		String presenceFile = (T == setType.TRAINING) ? "data/presence_cat-target_training.txt"
//				: "data/presence_cat-target_validation.txt";
//
//		
//		BufferedReader reader1 = new BufferedReader(new FileReader(featuresFile));
//		BufferedReader reader2 = new BufferedReader(new FileReader(presenceFile));
//
//		String line;
//		while ((line = reader2.readLine()) != null)
//			if (line.equals(categoryName)) {
//				String[] aux = reader2.readLine().split("\t");
//				presenceCategoryPerQuery = new boolean[aux.length];
//				for (int presenceIdx = 0; presenceIdx < aux.length ; presenceIdx++ )
//					presenceCategoryPerQuery[presenceIdx] = aux[presenceIdx].equals("1") ;					
//				break;
//			}
//		reader2.close();
//		
//		if (presenceCategoryPerQuery==null)
//			throw new CategoryNotPresent(categoryName);
//		
//		
//		int setSize = presenceCategoryPerQuery.length;
//
//		svm_problem problem = new svm_problem();
//		problem.l = setSize;
//		
//		
//		problem.x = new svm_node[problem.l][];
//		problem.y = new double[problem.l];
//
//		for (int setIdx = 0; setIdx < setSize; setIdx++) {
////			System.out.println("query numero " + (setIdx) );
//			String[] features = reader1.readLine().split(",");
////			double[] aux = new double[features.length];
//
////			for (int ftrIdx = 0; ftrIdx < features.length; ftrIdx++)
////				aux[ftrIdx] = Double.parseDouble(features[ftrIdx]);
//			int size=0;
//			Vector<svm_node> vv = new Vector<svm_node>();
//			for (int ftrIdx = 0; ftrIdx < features.length; ftrIdx++) {
//				//SvmNode aux= new SvmNode();
//				Double d = Double.parseDouble(features[ftrIdx]);
//				if (d!=0) {
//					size++;
//					svm_node aux= new svm_node();
//					aux.index = ftrIdx+1;
//					aux.value = d;
//					vv.add(aux);
//				}
//					
//			}
//			problem.x[setIdx]  = new svm_node[size];
//			
//			for (int i=0; i< size; i++)
//				problem.x[setIdx][i] = vv.elementAt(i);
//			
////			
////			for (int ftrIdx = 0; ftrIdx < features.length; ftrIdx++) {
////				//SvmNode aux= new SvmNode();
////				double d = Double.parseDouble(features[ftrIdx]);
////				if (d!=0f)
////				svm_node aux= new svm_node();
////				aux.index = ftrIdx+1;
////				aux.value = Double.parseDouble(features[ftrIdx]);
////				problem.x[setIdx][ftrIdx] =aux;
////						
//////				
//////				problem.x[setIdx][ftrIdx].index = ftrIdx+1;
//////				problem.x[setIdx][ftrIdx].value = Double.parseDouble(features[ftrIdx]);
////			}
//			
//			problem.y[setIdx]= (presenceCategoryPerQuery[setIdx]) ? 1f : -1f;
//			
//			
//				
//				
////			if (T == setType.TRAINING)
////				if (presenceCategoryPerQuery[setIdx])
////					trainGatherer.addPositiveExample(aux);
////				else
////					trainGatherer.addNegativeExample(aux);
////			else
////				if (presenceCategoryPerQuery[setIdx])
////					testGatherer.addPositiveExample(aux);
////				else
////					testGatherer.addNegativeExample(aux);
//			
//		}
//		
//		
//		if (T == setType.TRAINING)			
//			trainProblem=problem;
//		else
//			develProblem=problem;
//		
//		
//		reader1.close();
//
//	}
//
//	
	public void gatherUnlabeledData(setType T) throws Exception {

		String featuresFile = (T == setType.TRAINING) ? "data/results_training" + suffixByMode
				: "data/results_validation" + suffixByMode;



		
		BufferedReader reader = new BufferedReader(new FileReader(featuresFile));

		
		
		
		
		Vector<svm_node[]> auxVector =  new Vector<svm_node[]>();
		String lineOfFeatures;
		while  ( (lineOfFeatures = reader.readLine()) != null) {
			String[] features = lineOfFeatures.split(",");


			Vector<svm_node> auxNodeVect = new Vector<svm_node>();
			int ftrIdx = 0;
			for (; ftrIdx < features.length; ftrIdx++) {
				Double d = Double.parseDouble(features[ftrIdx]);
//	TODO			
				if (d!=0.) {
					
					svm_node auxNode = new svm_node();
					auxNode.index = ftrIdx+1;
					auxNode.value = d;
					auxNodeVect.add(auxNode);
//						
					}
					
			}

			if (totNumOfFtrs == -1)
				totNumOfFtrs = ftrIdx;
			
			svm_node[] nodeVect = new svm_node[auxNodeVect.size()];
			for (int i=0; i< nodeVect.length; i++)
				nodeVect[i] = auxNodeVect.elementAt(i);
			
//			auxNodeVect.clear();
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
		else
			develProblem=problem;
		
		reader.close();

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
	 * @throws Exception 
	 */
	public Pair<Float, Float> test() throws Exception {



		Pair<Float, Float> res = new Pair<Float, Float>(0.f,-1.f);

//		System.err.println("start test weight");
//		int iter=0;
		for (float w = 1f; w < 50f ; w *= 1.1f) {
			
//			System.err.println("starting training..");
			svm_model model = SupportSVM.trainModel(w, 1, trainProblem, 1.0 / (double) totNumOfFtrs
					/* gamma */, 1 /* C */);
//			System.err.println("ending training..");

			MetricsResultSet metrics = ParameterTester.computeMetrics(model, develProblem);
		
//			dumpCategorization(model,develProblems);
//			
//			System.out.printf("MACRO:  %.5f%%\t%.5f%%\t%.5f%%%n",
//					metrics.getMacroPrecision() * 100,
//					metrics.getMacroRecall() * 100, metrics.getMacroF1() * 100);

			float currF1 = metrics.getMacroF1() * 100;
			
			if (currF1 > res.second ) {
				res.first = w;
				res.second = currF1;
			}
//			iter++;
			// svm.svm_save_model("models/" + categoryName + ".model", model);
		}
//		System.err.println("----> num of iter " + iter);
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
	
	/**
	 * @param categoryName
	 *            nom della categoria target di cui si vuole generare il
	 *            modello.
	 * @param mode 
	 * 			  specifica quali features usare, 
	 */
//	public SVMClassifier(String categoryName, int mode) {
//		this.categoryName = categoryName;
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
	
	public SVMClassifier(int mode) {

		
		switch (mode) {
			case 0: break;
			case 1: this.suffixByMode = "_path.txt";
					break;
			case 2: this.suffixByMode = "_path_range.txt";
					break;
			case 3: this.suffixByMode = "_range.txt";
		}
		
	}

	/**
	 * @param args
	 *            come input deve essere fornita la stringa che identifica la
	 *            categoria target di cui si vuole generare il modello.
	 */
//	public static void main(String[] args) {
//
//		if (args.length != 1) {
//			System.err.println("Error: specify the category name!");
//			return;
//		}
//		
//		
//		SVMClassifier s = new SVMClassifier(args[0],1);
//		try {
//			Pair<Float, Float> res = s.test();
//			System.err.printf("%s:\n\tW+: %f\n\tF1: %f\n", args[0], res.first, res.second);
//		} catch (Exception e) {
//			
//			System.err.println(e.getMessage());
//		}
//
//	}
//	
	public void scaleProblems(){
		

//		Pair<double[], double[]> minsAndMaxs = LibSvmUtils.findRanges(trainProblem);
		Pair<double[], double[]> minsAndMaxs1 = LibSvmUtils.findRanges1(trainProblem,totNumOfFtrs);
		
//		if (!Arrays.equals(minsAndMaxs.first, minsAndMaxs1.first))
//				System.err.println("ERRORE");
//		
//		if (!Arrays.equals(minsAndMaxs.second, minsAndMaxs1.second))
//			System.err.println("ERRORE");
		
		System.err.println();
		for (double d: minsAndMaxs1.first)
			System.err.print(" " + d);
//		System.err.println();
//		System.err.println(minsAndMaxs1.first);
		System.err.println();
//		System.err.println(minsAndMaxs.second.toString());
		for (double d: minsAndMaxs1.second)
			System.err.print(" " + d);
//		System.err.println();
//		System.err.println(minsAndMaxs1.second);	
//		
		
//		System.err.print(minsAndMaxs.first.length + " "+ minsAndMaxs.second.length+ " "+ minsAndMaxs1.first.length+ " "+ minsAndMaxs1.second.length );
//		int ii=0;
//		for (double d :minsAndMaxs1.first) {
//			if (d != minsAndMaxs.first[ii])
//				System.err.println("******************** ERRORE ***************** ");
//			ii++;
//		}
//		 ii=0;
//		 
//		for (double d :minsAndMaxs1.second){
//			if (d != minsAndMaxs.second[ii++])
//				System.err.println("******************** ERRORE ***************** ");
//			ii++;
//		}
				
				
		LibSvmUtils.scaleProblem(trainProblem, minsAndMaxs1.first, minsAndMaxs1.second);
		LibSvmUtils.scaleProblem(develProblem,  minsAndMaxs1.first, minsAndMaxs1.second);
	}


	public void gatherData() throws Exception {
		System.err.println("start gathering training examples..");
		gatherUnlabeledData(setType.TRAINING);
		System.err.println("-> training examples gathered! <-");
		
		System.err.println("start gathering development examples..");
		gatherUnlabeledData(setType.DEVELOPMENT);		
		System.err.println("->  development examples gathered! <-");
		
		System.err.println("start scaling problems..");
		scaleProblems();
		System.err.println("-> problems scaled! <-");
	}


	public void setLabels(String categoryName) throws IOException {
		System.err.println("start labeling examples..");
		setLabels(categoryName,setType.TRAINING);
		setLabels(categoryName,setType.DEVELOPMENT);
		System.err.println("end labeling examples..");
		
	}


}
