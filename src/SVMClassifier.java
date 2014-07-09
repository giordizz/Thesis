import it.acubelab.batframework.metrics.MetricsResultSet;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import libsvm.svm_model;
import libsvm.svm_problem;



/**
* <h1>SVMClassifier</h1>
* La classe SVMClassifier si occupa della raccolta degli esempi,
* del training del classificatore e della successiva valutazione 
* 
* @author Giordano
* @version 1.0
* 
*/
public class SVMClassifier {
	String categoryName;

	Vector<double[]> posVectors;  
	Vector<double[]> negVectors;	
	
	/** presenceCategoryPerQuery: array di n stringhe nell'alfabeto {"0","1"},
	 *  dove n è il numero di query che compongono il set considerato (o training o dev).
	 *  L'i-esimo elemento dell'array è "1" se la i-esima query del set è taggata con la categoria in esame, "0" altrimenti.
	*/ 
	String[] presenceCategoryPerQuery;	
	
	BinaryExampleGatherer trainGatherer = new BinaryExampleGatherer();
	BinaryExampleGatherer testGatherer = new BinaryExampleGatherer();

	
	/**
	 *  setType: specifica se i dati da raccogliere fanno parte del training o del development set
	 */
	public enum setType{
		TRAINING,DEVELOPMENT
	}
	
	
	
	/**
	 * Raccoglie gli esempi dai file, inserendoli nel rispettivo gatherer a seconda del parametro specificato.
	 * <p/>
	 * Nei file di tipo "results*" sono presenti tante righe quante sono le query del rispettivo set; 
	 * ogni riga del file identifica l'array di features di una specifica query nel formato "f1,f2,...fn" dove n è attualmente 123.
	 * <p/>
	 * Nei file di tipo "presence_cat-target*" sono presenti 2 righe per ogni categoria target (quindi in tutto ci sono 134 righe);
	 * La prima di queste due righe identifica il nome della categoria, la seconda è, invece, nel formato "B1	B2		BN" dove N è la dimensione del rispettivo set;
	 * il generico valore Bi è 1 se la i-esima query del set è taggata con la su detta categoria, 0 altrimenti.
	 * 
	 * @param T permette di identificare se si desidera prelevare gli esempi dal training set o dal development set.
	 */
	public void gatherData(setType T) throws IOException{
		
		
		String featuresFile = (T == setType.TRAINING) ?"data/results_training.txt" : "data/results_validation.txt";
		String presenceFile = (T == setType.TRAINING) ? "data/presence_cat-target_training.txt": "data/presence_cat-target_validation.txt";

//		String featuresFile =  (T == setType.TRAINING) ? "C:/Users/giordano/Desktop/Python/results_taining.txt" : "C:/Users/giordano/Desktop/Python/results_validation.txt";
//		String presenceFile =  (T == setType.TRAINING) ? "C:/Users/giordano/Desktop/Python/presence_cat-target_training.txt" : "C:/Users/giordano/Desktop/Python/presence_cat-target_validation.txt";
		
		int setSize = (T == setType.TRAINING) ? 500 : 200;
		int numFeatures = 123;
			

		BufferedReader reader1 = new BufferedReader(new FileReader(featuresFile));
		BufferedReader reader2 = new BufferedReader(new FileReader(presenceFile));

		
		String line;
		while( (line=reader2.readLine())!=null) 
		     if (line.equals(categoryName)) {
		    	 presenceCategoryPerQuery=reader2.readLine().split("\t");
		    	 break;
		     }
		reader2.close();
			
			
		for (int index=0; index < setSize; index++ ) {
			String[] features = reader1.readLine().split(",");
			posVectors = new Vector<double[]>();
			negVectors = new Vector<double[]>();					
			double[] aux = new double[numFeatures]; 
	
			for (int count=0; count< features.length; count++)
				aux[count]=Double.parseDouble(features[count]);
			

			if (presenceCategoryPerQuery[index].equals("1"))
				posVectors.add(aux);
			else 
				negVectors.add(aux);
				
			if (T == setType.TRAINING)
				trainGatherer.addExample(posVectors, negVectors);
			else
				testGatherer.addExample(posVectors, negVectors);
		}
		reader1.close();
		
	}

	
	
	/**
	 * Questo metodo, ancora incompleto, permette di effettura la fase di learning e la successiva fase di valutazione che consente,
	 * di valutare quale modello produce migliori risultati in termini di precision, recall e F1 per una specifica categoria.
	 */
	public void test() throws IOException{

		Triple<svm_problem, double[], double[]> ftrsMinsMaxs = SupportSVM.getScaledTrainProblem( trainGatherer);
		svm_problem trainProblem = ftrsMinsMaxs.getLeft();
		Vector<Integer> features = SupportSVM.getAllFtrVect(trainGatherer.getFtrCount());
		LibSvmUtils.dumpRanges(ftrsMinsMaxs.getMiddle(),ftrsMinsMaxs.getRight(), "model.range");
		
		float w=0.1f, fact=3;
		
		System.out.println("Starting training...");
		while (w<200){		
			System.out.println(" ----------> weightPos = " + w);	
			svm_model model = SupportSVM.trainModel(w, 1,features, trainProblem, 1.0 /*gamma*/, 1 /*C*/);
			w*=fact;
			Vector<svm_problem> ssvm= SupportSVM.getScaledTestProblems(features,testGatherer,ftrsMinsMaxs.getMiddle(), ftrsMinsMaxs.getRight());
			MetricsResultSet metrics = ParameterTester.computeMetrics(model,ssvm );
			
			
			System.out.printf("MACRO:  %.5f%%\t%.5f%%\t%.5f%%%n",
					metrics.getMacroPrecision() * 100, metrics.getMacroRecall() * 100,
					metrics.getMacroF1() * 100);
//			System.out.printf("MICRO:  %.5f%%\t%.5f%%\t%.5f%%%n",
//					metrics.getMicroPrecision() * 100, metrics.getMicroRecall() * 100,
//					metrics.getMicroF1() * 100);
			
		//	svm.svm_save_model("models/" + categoryName + ".model", model);
		}
			
		
	}
	
	
	/**
	 * @param categoryName nom della categoria target di cui si vuole generare il modello.
	 */
	public SVMClassifier(String categoryName){
		this.categoryName=categoryName;
	}
	
	
	/**
	 * @param args come input deve essere fornita la stringa che identifica la categoria target di cui si vuole generare il modello.
	 */
	public static void main(String[] args){
		
		if (args.length!=1) {
			System.err.println("Error: specify the category name!");
			return;
		}
		
			
		SVMClassifier s = new SVMClassifier(args[0]);
		
		
		
		try {
			s.gatherData(setType.TRAINING);
			s.gatherData(setType.DEVELOPMENT);

			s.test();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
