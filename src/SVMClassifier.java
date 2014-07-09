import it.acubelab.batframework.metrics.MetricsResultSet;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import libsvm.svm_model;
import libsvm.svm_problem;


public class SVMClassifier {
	String categoryName;

	Vector<double[]> posVectors;
	Vector<double[]> negVectors;	
	String[] presenceCategoryPerQuery;	
	BinaryExampleGatherer trainGatherer = new BinaryExampleGatherer();;
	BinaryExampleGatherer testGatherer = new BinaryExampleGatherer();

	public enum setType{
		TRAINING,DEVELOPMENT
	}
	
	
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
			System.out.printf("MICRO:  %.5f%%\t%.5f%%\t%.5f%%%n",
					metrics.getMicroPrecision() * 100, metrics.getMicroRecall() * 100,
					metrics.getMicroF1() * 100);
			
		//	svm.svm_save_model("models/" + categoryName + ".model", model);
		}
			
		
	}
	
	public SVMClassifier(String categoryName){
		this.categoryName=categoryName;
	}
	
	
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
