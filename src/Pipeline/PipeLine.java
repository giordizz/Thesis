package it.giordizz.Thesis;

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

//	private static final int THREADS_NUM = ;

	public static void main(String[] args) throws Exception {
		System.err.println("thread "+Integer.parseInt(args[0]));
		
		int maxIter = 1000;
		System.err.println("# iter : " + maxIter );
//		BufferedReader br5 = new BufferedReader(new FileReader("../data/CatIntermadiateID"));
		HashSet<Integer> intermediate_categories= new HashSet<Integer> ();
		BufferedReader br5 = new BufferedReader(new FileReader("data/CatIntermadiateID"));
		String line;
		while( (line=br5.readLine())!=null) 
		     intermediate_categories.add(Integer.parseInt(line));		     		
		br5.close();
		
		Vector<Integer> allCategories = new Vector<Integer>();
//		BufferedReader br7 = new BufferedReader(new FileReader("../data/AllCategoriesSorted.txt"));
		BufferedReader br7 = new BufferedReader(new FileReader("data/AllCategoriesSorted.txt"));
		while( (line=br7.readLine())!=null)  {
			Integer catID = Integer.parseInt(line);
		
			if (!intermediate_categories.contains(catID))
				allCategories.add(Integer.parseInt(line));		
		}
		br7.close();
		
		intermediate_categories.clear();
		
		
		
		

		
		
		int topK = 21; //TODO
		Container container = new Container(topK);		
		container.getStatus();

		
		Gatherer g = new Gatherer();
		g.gatherData();
		
		
		
		
		
		ExecutorService execServ = Executors
				.newFixedThreadPool(Integer.parseInt(args[0]));
		List<Future<Result>> futures = new Vector<>();
		
		int horizont = 8;
	
		int iterationIdx = 0;
		for (Integer categoryID : allCategories){
//			System.err.println("adding task");
			futures.add(execServ.submit(new Worker(horizont, container, categoryID, new SVMClassifier(g.getCopyOfProblems()))));
			
			if (++iterationIdx == maxIter) //TODO provvis
//			if (++iterationIdx == 1000)
				break;
		}
			

		PrintWriter writer = new PrintWriter("stats.txt", "UTF-8");
		for (Future<Result> future : futures) {
			Result r = future.get();
			writer.println(   r.avgF1 + "\t" + r.categoryID  );
			writer.flush();
		}
		

		writer.close();
		
			
		execServ.shutdown();
  
	}
	
}
	


