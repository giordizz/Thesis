package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Vector;


public class Script {

	public static void main(String[] args) throws Exception {

		
		PrintWriter writer = new PrintWriter("122features_stats.txt", "UTF-8");
		
		
		
		Vector<Float> weights = new Vector<Float>();
		BufferedReader r1 = new BufferedReader(new FileReader("data/weights_newnew8top21.txt"));
		String weight;
//		int count=0;
		while ((weight = r1 .readLine()) != null) {
			weights.add(Float.parseFloat(weight));
//			System.err.println(++count);
		}
		r1.close();
		
		String[] cats = new String[67];

//		if (args.length==1) {
		BufferedReader reader = new BufferedReader(new FileReader("data/CatTarget.txt"));
		String line;

		int c = 0;
		while ((line = reader.readLine()) != null) {	
			cats[c++]=line;
		}
		

		RisalitaCounting r = new RisalitaCounting(5, Integer.parseInt(args[0]));
		
		r.getStatus();
		SVMClassifier s = new SVMClassifier(0);
		
		
		for (int I=0 ; I <123 ; I++){ 
				
				System.err.println("**** excluding " + I + "  ****");
				r.excludeI(I);
				
				s.init();
				r.tagAll(s,0);
				r.tagAll(s,1);
				s.scaleProblems();
				
				
				float avgF1 = 0.f;
				float numOfTargetCategory = 0.f; 
				for (String categoryName : cats) {			
					try {
						//Pair<Float, Float> res = new SVMClassifier(line,mode).test();
						s.setLabels(categoryName);
						
						Pair<Float, Float> res = s.test(weights.elementAt((int) numOfTargetCategory));
						
						numOfTargetCategory+=1.f;
						avgF1+=res.second;
//						writer.printf("%s:\n\tW+: %f\n\tF1: %f\n", categoryName, res.first, res.second);
						System.err.println("**** Category number " + numOfTargetCategory + " computed ****");
					} catch (Exception e) {
						e.printStackTrace();
						writer.println(e.getMessage());
					}
				}			
				reader.close();
				writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1 excluding " + I  +" : " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<");
		}
		
		writer.close();
		
	}
	
}
	


