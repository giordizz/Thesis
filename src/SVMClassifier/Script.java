package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;
import it.giordizz.Thesis.SVMClassifier.setType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Script {
	
	
	public static void main(String[] args) throws Exception {
		
		PrintWriter writer = new PrintWriter("stats.txt", "UTF-8");
		int mode=0,numOfMode=4;
		
		if (args.length==1){
				int aux=Integer.parseInt(args[0]);
				if (aux<0 || aux>=numOfMode) {
					
					System.out.println("Not existing mode");
					System.exit(1);
					
				}
					
				mode=aux;
				numOfMode=aux+1;
				System.out.println("Specific mode chosen");
		}
			
			
		for (; mode < numOfMode ; mode++) {
			writer.println("************************* MODE " + mode + " *************************");
		
			BufferedReader reader = new BufferedReader(new FileReader("data/CatTarget.txt"));
			
			float avgF1 = 0.f;
			float numOfTargetCategory = 0.f; 
			
			
			
			SVMClassifier c = new SVMClassifier(mode);
			
			c.gatherData();
			
			
			String categoryName;
			int count=1;
			while ((categoryName = reader.readLine()) != null) {			
				try {
					//Pair<Float, Float> res = new SVMClassifier(line,mode).test();
					c.setLabels(categoryName);
					
					Pair<Float, Float> res = c.test();
					
					numOfTargetCategory+=1.f;
					avgF1+=res.second;
					writer.printf("%s:\n\tW+: %f\n\tF1: %f\n", categoryName, res.first, res.second);
					System.err.println("**** Category number " + count++ + " computed ****");
				} catch (Exception e) {
					e.printStackTrace();
					writer.println(e.getMessage());
				}
			}			
			reader.close();
			
		

			
			writer.println(">>>>>>>>>>>>>>>>>>>>>> Avarage F1: " + avgF1 / numOfTargetCategory + "  <<<<<<<<<<<<<<<<<<<<<<<\n");
		}
		
		writer.close();
	}
}
