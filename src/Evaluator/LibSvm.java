package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;
import it.giordizz.Thesis.SVMClassifier.setType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class LibSvm {

	
	static void evaluate(setType setToTest, Vector<Pair<Float, Float>> weights, String featuresPrefixDir) throws Exception{
		
		
		PrintWriter writer;
		
		if (setToTest== setType.DEVELOPMENT)
			 writer = new PrintWriter(featuresPrefixDir + "_conf.txt", "UTF-8");
		else
			 writer = new PrintWriter(featuresPrefixDir + "_test.txt", "UTF-8");
		
		SVMClassifier S = new SVMClassifier();

		S.createSVMProblemsByFiles(featuresPrefixDir);
		

		BufferedReader reader = new BufferedReader(new FileReader("data/CatTarget.txt"));

		
		ArrayList<Pair<String,HashSet<Integer>>> labelsPerCategory = new ArrayList<Pair<String,HashSet<Integer>>>(67);
		
		float avgF1 = 0.f;
		float numOfTargetCategory = 0.f; 
		String categoryName;
		while ((categoryName = reader.readLine()) != null) {			
			try {

				S.setLabels(categoryName);
				
				System.err.println("TESTING..");
				Pair<Pair<Float, Float>, Float> res = null;
				if (setToTest== setType.DEVELOPMENT){
					Pair<Pair<Pair<Float, Float>, Float>, HashSet<Integer>> results = S.test(setToTest);

						
					res = results.first;
//					labelsPerCategory.add(new Pair<String,HashSet<Integer>>(categoryName, results.second));
				} else {
					Pair<Pair<Pair<Float, Float>, Float>, HashSet<Integer>> results = S.test(setToTest, weights.get((int) numOfTargetCategory));
					res = results.first;
//					labelsPerCategory.add(new Pair<String,HashSet<Integer>>(categoryName, results.second));
				
				}
				
					
				numOfTargetCategory+=1.f;
				avgF1+=res.second;
				writer.printf("%s:\n\tW: %f\t%f\n\tF1: %f\n", categoryName, res.first.first, res.first.second, res.second);
				System.err.println("**** Category number " + numOfTargetCategory + " computed ****");
			} catch (Exception e) {
				e.printStackTrace();
				writer.println(e.getMessage());
			}
		}			
		reader.close();
		writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1: " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");

//		S.writeLabelsToFile(setToTest, labelsPerCategory);
		
		writer.close();
	}
	
	static void twoStageEvaluation(setType setToTest,Vector<Pair<Float, Float>> weights, String featuresPrefixDir) throws Exception{
		
		PrintWriter writer = new PrintWriter(featuresPrefixDir+ "_x", "UTF-8");
//		for (float w = 1f; w < 50f ; w *= 1.1f) {
			
		
			SVMClassifier S = new SVMClassifier();
	
			S.createSVMProblemsByFiles(featuresPrefixDir);
		
			
			int numOfTargetCategories = 67;
			ArrayList<String> targetCategories  = new ArrayList<String>(numOfTargetCategories);
			BufferedReader reader = new BufferedReader(new FileReader("data/CatTarget.txt"));
			String categoryName;
			while ((categoryName = reader.readLine()) != null )
				targetCategories.add(categoryName);
			reader.close();
			
			double[][] secondStageFeaturesTrain = new double[numOfTargetCategories][];
			double[][] secondStageFeaturesToTest = new double[numOfTargetCategories][];

	
			for (int catIdx = 0; catIdx  < numOfTargetCategories ; catIdx ++) {			
				try {
					S.setLabels(targetCategories.get(catIdx));
					svm_model model;
//					if (weights!=null)
						model=SupportSVM.trainModel(svm_parameter.EPSILON_SVR, 1, 1, S.trainProblem, (double) 11.0 / (double) S.totNumOfFtrs , 1.2/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
//					else
//						model=SupportSVM.trainModel(svm_parameter.EPSILON_SVR, w, 1, S.trainProblem, (double) 11.0 / (double) S.totNumOfFtrs , 1.2/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);
				
					secondStageFeaturesTrain[catIdx] = S.getRegressorWeightsByCategory(setType.TRAINING, model);
					secondStageFeaturesToTest[catIdx] = S.getRegressorWeightsByCategory(setToTest, model);
					
					

				} catch (Exception e) {
					e.printStackTrace();

				}
			}			
			
			S.updateProblems(setToTest, secondStageFeaturesTrain, secondStageFeaturesToTest);


			
			
			

			float avgF1 = 0.f;
			float numOfTargetCategory = 0.f; 

			for (; numOfTargetCategory < targetCategories.size() ;) {			
				try {
					S.setLabels(targetCategories.get((int) numOfTargetCategory));
					
					Pair<Pair<Float, Float>, Float> res;
					
//					res = S.test(setToTest).first;
					res = S.test(setToTest, weights.get((int) numOfTargetCategory)).first;
					
					writer.printf("%s:\n\tW+: %f\t%f\n\tF1: %f\n", targetCategories.get((int) numOfTargetCategory), res.first.first, res.first.second, res.second);
					numOfTargetCategory+=1.f;
					avgF1+=res.second;
					
					System.err.println("**** Category number " + (int)numOfTargetCategory + " computed ****");
				} catch (Exception e) {
					e.printStackTrace();
					writer.println(e.getMessage());
				}
			}			

//			if (weights!=null) {
				writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1: " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");
//				break;
//			} else
//				writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1 with W+= " + w + " : " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");
			
//		}
		
		writer.close();
	}
	
	
	public static void main(String[] args) throws Exception {

		if (args.length!=1) {
			System.err.println("specify argument");
			System.exit(1);
		}
			
		
		
		

		
		Vector<Pair<Float, Float>> weights = new Vector<Pair<Float, Float>>();
//		BufferedReader r = new BufferedReader(new FileReader("data/bestWeights.txt"));
//		BufferedReader r = new BufferedReader(new FileReader("data/weights_entity+bolds.txt"));
//		BufferedReader r = new BufferedReader(new FileReader("entity+entitySnippets+snippets+bolds_conf.txt"));
////		String weight;
//		int c= 67;
//		while ((r .readLine()) != null) {
//		
//			String[] Ws = r .readLine().split(":")[1].split("\t");
//			if (Ws.length==1)
//				weights.add(new Pair<Float, Float>(Float.parseFloat(Ws[0]),1.f));
//			else
//				weights.add(new Pair<Float, Float>(Float.parseFloat(Ws[0]),Float.parseFloat(Ws[1])));
//			r .readLine();
//			
//			if(--c==0)
//				break;
//		}
//			
//		r.close();
		
		evaluate(setType.DEVELOPMENT, null, args[0]);
//		evaluate(setType.TEST, weights, args[0]);
//		System.err.println(" ----------> " + weights.size());
//		twoStageEvaluation(setType.TEST, weights, args[0]);
//		twoStageEvaluation(setType.DEVELOPMENT, null, args[0]);
		
	}
}
	


