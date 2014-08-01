package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;

import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.Callable;



public class Worker implements Callable<Result> {
	int maxHeight;
	Container container;
	int[][] resultsTraining;
	int[][] resultsDevelopment;
	Integer category;
	SVMClassifier svmClassifier;
	
	

	
	
	
	public Worker(int horizont, Container container, Integer categoryForDescending, SVMClassifier svmClassifier) {
		maxHeight = horizont;
		this.container=container;
		category = categoryForDescending;
		
		resultsTraining = new int[container.training.length][2];
		resultsDevelopment = new int[container.development.length][2];
		
		this.svmClassifier = svmClassifier;
		
	}
	
	
	

	@Override
	public Result call() throws Exception {
//		System.err.println("task called");
		
//		descend(new HashSet<Integer>(category), new  HashSet<Integer>(), 1 );
		
		svmClassifier.updateFeatures(resultsTraining, resultsDevelopment);
		svmClassifier.scaleProblems();
		
		
		float avgF1 = 0.f;
		float numOfTargetCategory = 0.f; 
		for (String categoryName : container.targetCategories) {			
			try {
				//Pair<Float, Float> res = new SVMClassifier(line,mode).test();
				svmClassifier.setLabels(categoryName);
				for (double f:svmClassifier.trainProblem.y)
					System.err.print(f);
				for (double f:svmClassifier.develProblem.y)
					System.err.print(f);
				Pair<Float, Float> res = svmClassifier.test(container.weights[(int) numOfTargetCategory]);
				
				numOfTargetCategory+=1.f;
				avgF1+=res.second;
//				writer.printf("%s:\n\tW+: %f\n\tF1: %f\n", categoryName, res.first, res.second);
//				System.err.println("**** Category number " + numOfTargetCategory + " computed ****");
			} catch (Exception e) {
				e.printStackTrace();
//				writer.println(e.getMessage());
			}
		}			
		return new Result(category, avgF1 / numOfTargetCategory );
	}


	private void descend(HashSet<Integer> categories,HashSet<Integer> visited,int height) {
		if (categories.isEmpty()|| height > maxHeight )
			return ;
		
		HashSet<Integer> toExplore= new HashSet<Integer>();;
		
		for(Integer cat: categories) {

			
			toExplore.addAll(checkVisited(cat,visited));
	
			updateResults(cat);
		}
		visited.addAll(toExplore);
		
		descend(toExplore,visited,height+1);
	}
	

		
	private void updateResults(Integer cat) {
		
		for (int Idx=0; Idx < container.training.length ; Idx ++) {
			Vector<HashSet<Integer>>[] pair = container.training[Idx];
			for (HashSet<Integer> entitiesGroup: pair[0])
				if (entitiesGroup.contains(cat))
					resultsTraining[Idx][0]++;
			
			for (HashSet<Integer> topKentitiesGroup: pair[1])
				if (topKentitiesGroup.contains(cat))
					resultsTraining[Idx][1]++;
		}
		
		for (int Idx=0; Idx < container.development.length ; Idx ++) {
			Vector<HashSet<Integer>>[] pair = container.development[Idx];
			for (HashSet<Integer> entitiesGroup: pair[0])
				if (entitiesGroup.contains(cat))
					resultsDevelopment[Idx][0]++;
			
			for (HashSet<Integer> topKentitiesGroup:  pair[1])
				if (topKentitiesGroup.contains(cat))
					resultsDevelopment[Idx][1]++;
		}
		
	}

	public Vector<Integer> checkVisited(Integer cat, HashSet<Integer> visited){
		Vector<Integer> aux=new  Vector<Integer>();
		
		
		try{
//			aux.addAll(this.categories.get(cat));	
			for (Integer c : container.reverseCategories.get(cat)) {
				if (!visited.contains(c))
					aux.add(c);
			}
	} catch(Exception e) {
		
	}
	
			
			
			return aux;
		
		
	}






}
