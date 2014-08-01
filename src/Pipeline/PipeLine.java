package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PipeLine {

	private static final int THREADS_NUM = 1;

	public static void main(String[] args) throws Exception {

		
		BufferedReader br5 = new BufferedReader(new FileReader("../data/CatIntermadiateID"));
		HashSet<Integer> intermediate_categories= new HashSet<Integer> ();
//		BufferedReader br5 = new BufferedReader(new FileReader("data/CatIntermadiateID"));
		String line;
		while( (line=br5.readLine())!=null) 
		     intermediate_categories.add(Integer.parseInt(line));		     		
		br5.close();
		
		Vector<Integer> allCategories = new Vector<Integer>();
		BufferedReader br7 = new BufferedReader(new FileReader("../data/AllCategoriesSorted.txt"));
//		BufferedReader br7 = new BufferedReader(new FileReader("data/AllCategoriesSorted.txt"));
		while( (line=br7.readLine())!=null)  {
			Integer catID = Integer.parseInt(line);
		
			if (!intermediate_categories.contains(catID))
				allCategories.add(Integer.parseInt(line));		
		}
		br7.close();
		
		intermediate_categories.clear();
		
		
		
		

		
		
		int topK = 0; //TODO
		Container container = new Container(topK);		
		container.getStatus();
		
		
		
//		SVMClassifier s = new SVMClassifier();
//		s.gatherData();
		
		Gatherer g = new Gatherer();
		g.gatherData();
		
		
		
		
		
		ExecutorService execServ = Executors
				.newFixedThreadPool(THREADS_NUM);
		List<Future<Result>> futures = new Vector<>();
		
		int horizont = 8;
		
		int iterationIdx = 0;
		for (Integer categoryID : allCategories){
//			System.err.println("adding task");
			futures.add(execServ.submit(new Worker(horizont, container, categoryID, new SVMClassifier(g.getCopyOfProblems()))));
			
			if (++iterationIdx == 1) //TODO provvisorio
				break;
		}
			
//		System.err.println("stop");
		PrintWriter writer = new PrintWriter("stats.txt", "UTF-8");
		for (Future<Result> future : futures) {
			Result r = future.get();
			writer.println(">>>>>>>>> ID: " + r.categoryID + " - avgF1:" + r.avgF1 + " <<<<<<<<<");
		}
		writer.close();
			
		execServ.shutdown();
//	
//		for (int negI = 0; (wNeg = computeWeight(wNegMax, wNegMin,kappaNeg, negI, steps)) <= wNegMax; negI++)
//				futures.add(execServ.submit(new ParameterTester(wPos, wNeg,
//						editDistanceThreshold, features, trainEQFGatherer,
//						testEQFGatherer, optProfile, optProfileThreshold,
//						gamma, C, scoreboard)));

		ModelConfigurationResult best = null;
//		for (Future<ModelConfigurationResult> future : futures)
		
//		
//		
//		Integer currCategory;
//		int count=0;
//		while ( (currCategory = r.updateProblems(s))!= null){
//			
//			s.scaleProblems();
//			
//			
//			float avgF1 = 0.f;
//			float numOfTargetCategory = 0.f; 
//			for (String categoryName : cats) {			
//				try {
//					//Pair<Float, Float> res = new SVMClassifier(line,mode).test();
//					s.setLabels(categoryName);
//					
//					Pair<Float, Float> res = s.test(weights.elementAt((int) numOfTargetCategory));
//					
//					numOfTargetCategory+=1.f;
//					avgF1+=res.second;
////					writer.printf("%s:\n\tW+: %f\n\tF1: %f\n", categoryName, res.first, res.second);
////					System.err.println("**** Category number " + numOfTargetCategory + " computed ****");
//				} catch (Exception e) {
//					e.printStackTrace();
////					writer.println(e.getMessage());
//				}
//			}			
//			reader.close();
//			System.err.println(">>>>>>>>>>>>>>>>>>>>>> "+ count++ +" Avarage F1 adding " + currCategory  +" : " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");
////			writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1 adding " + currCategory  +" : " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");
//			
//		}
//		for (int I=0 ; I <123 ; I++){ 
//				
//				System.err.println("**** excluding " + I + "  ****");
//				r.excludeI(I);
//				
//				s.init();
//				r.tagAll(s,0);
//				r.tagAll(s,1);
//				
//				
//				
//				float avgF1 = 0.f;
//				float numOfTargetCategory = 0.f; 
//				for (String categoryName : cats) {			
//					try {
//						//Pair<Float, Float> res = new SVMClassifier(line,mode).test();
//						s.setLabels(categoryName);
//						
//						Pair<Float, Float> res = s.test(weights.elementAt((int) numOfTargetCategory));
//						
//						numOfTargetCategory+=1.f;
//						avgF1+=res.second;
////						writer.printf("%s:\n\tW+: %f\n\tF1: %f\n", categoryName, res.first, res.second);
//						System.err.println("**** Category number " + numOfTargetCategory + " computed ****");
//					} catch (Exception e) {
//						e.printStackTrace();
//						writer.println(e.getMessage());
//					}
//				}			
//				reader.close();
//				writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1 excluding " + I  +" : " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");
//		}
//		
//		writer.close();
//		
	}
	
}
	


