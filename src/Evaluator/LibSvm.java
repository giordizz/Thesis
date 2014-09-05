package it.giordizz.Thesis;

import it.acubelab.batframework.metrics.Metrics;
import it.acubelab.batframework.metrics.MetricsResultSet;
import it.acubelab.batframework.utils.Pair;
import it.giordizz.Thesis.SVMClassifier.setType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;


public class LibSvm {

	
static void evaluate(String featuresPrefixDir) throws Exception{
		
		
		PrintWriter writer= new PrintWriter(featuresPrefixDir + "_test.txt", "UTF-8");
		
		SVMClassifier S = new SVMClassifier();

		S.createSVMProblemsByFiles(featuresPrefixDir);
		

		BufferedReader reader = new BufferedReader(new FileReader("data/CatTarget.txt"));

		
		ArrayList<Pair<String,HashSet<Integer>>> labelsPerCategory = new ArrayList<Pair<String,HashSet<Integer>>>(67);
		


		String categoryName;
		List<Pair<Float, Float>> bestWeights = new Vector<>();
		ArrayList<String> categories = new ArrayList<String>();
		int numOfTargetCategory=0;
		ArrayList<Triple<Integer,Integer,Integer>> exemplesPosCountForSet = new ArrayList<Triple<Integer,Integer,Integer>>(67);
		while ((categoryName = reader.readLine()) != null) {			
			try {
				categories.add(categoryName);
				exemplesPosCountForSet.add(S.setLabels(categoryName));
				
				System.err.println("TUNING..");
				Pair<Pair<Float, Float>, Float> res = null;
	
				res = S.tuneWeights();
//				labelsPerCategory.add(new Pair<String,HashSet<Integer>>(categoryName, results.second));
				
				
				bestWeights.add(res.first);
				System.err.println("**** Category number " + ++numOfTargetCategory + " tuned ****");
			} catch (Exception e) {
				e.printStackTrace();
				writer.println(e.getMessage());
			}
		}
		reader.close();

		List<HashSet<Integer>> output = new Vector<>();
		List<HashSet<Integer>> goldStandard = new Vector<>();
		
		System.err.println("TESTING..");
//		for (String cat: categories){
		for (int catIdx = 0; catIdx < categories.size() ; catIdx++){
	
			S.setLabels(categories.get(catIdx));
			Pair<Float, Float> bestWeightsForCat = bestWeights.get(catIdx);
			svm_model model = SupportSVM.trainModel(svm_parameter.C_SVC, bestWeightsForCat.first, bestWeightsForCat.second, S.trainProblem,(double) 3.0 / (double) S.totNumOfFtrs, 1.2/* 2.0 / (double) totNumOfFtrs*//* gamma *//*, 1  C */);

			Pair<HashSet<Integer>, HashSet<Integer>> outputandGoldForCat = ParameterTester.getOutputForCategory(model, S.testProblem);
			output.add(outputandGoldForCat.first);
			goldStandard.add(outputandGoldForCat.second);
			System.err.println("**** Prediction for category " + (catIdx+1) + " completed ****");
		}
		MetricsResultSet results = new Metrics<Integer>().getResult(output, goldStandard, new IndexMatch());
//		results.getMacroF1(); //macro f1
//		results.getMicroF1(); // micro f1
//		results.getF1s(i) //F1 per la categoria i
//		results.getPrecisions(i) //Precision per la categoria i
//		results.getRecall(i) //Recallper la categoria i
		
		for (int catIdx = 0; catIdx < categories.size() ; catIdx++){
			writer.printf("%s:\n\tW: %f\t%f\n\tF1: %f\n\tP: %f\n\tR: %f\n\ttp: %d\n\tfp: %d\n\tfn: %d\n\texTraining: %d\n\texDevelopment: %d\n\texTest: %d\n",
					categories.get(catIdx),
					bestWeights.get(catIdx).first,
					bestWeights.get(catIdx).second,
					results.getF1s(catIdx),
					results.getPrecisions(catIdx),
					results.getRecalls(catIdx),
					results.getTPs(catIdx),
					results.getFPs(catIdx),
					results.getFNs(catIdx),
					exemplesPosCountForSet.get(catIdx).left,
					exemplesPosCountForSet.get(catIdx).middle,
					exemplesPosCountForSet.get(catIdx).right
			);
		}
		
		
		writer.println(">>>>>>>>>>>>>>>>>>>>>> Macro F1: " + results.getMacroF1() + "  <<<<<<<<<<<<<<<<<<<<<<<");
		writer.println(">>>>>>>>>>>>>>>>>>>>>> Micro F1: " + results.getMicroF1() + "  <<<<<<<<<<<<<<<<<<<<<<<");
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
			
		
		
		

		
		
//		evaluate(setType.DEVELOPMENT, null, args[0]);
		evaluate(args[0]);
//		System.err.println(" ----------> " + weights.size());
//		twoStageEvaluation(setType.TEST, weights, args[0]);
//		twoStageEvaluation(setType.DEVELOPMENT, null, args[0]);
		
	}
}
	


