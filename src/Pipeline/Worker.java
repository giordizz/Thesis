package it.giordizz.Thesis;

import it.acubelab.batframework.utils.Pair;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import java.util.ArrayList;
import java.util.concurrent.Callable;



public class Worker implements Callable<Result> {
	int maxHeight;
	Container container;
//	HashSet<Integer>[][] resultsTraining;
//	HashSet<Integer>[][] resultsDevelopment;
	int[][] resultsTraining;
	int[][] resultsDevelopment;
	Integer category;
	SVMClassifier svmClassifier;
	
	IntOpenHashSet visited;

	
	
	
//	@SuppressWarnings("unchecked")
	public Worker(int horizont, Container container, Integer categoryForDescending, SVMClassifier svmClassifier) {
		maxHeight = horizont;
		this.container=container;
		category = categoryForDescending;
		
//		resultsTraining = new HashSet[container.training.length][2];		
//		resultsDevelopment = new HashSet[container.development.length][2];
		
		resultsTraining = new int[container.training.length][2];
		resultsDevelopment = new int[container.development.length][2];
		
		this.svmClassifier = svmClassifier;
		

		
	}
	
	
	

	@Override
	public Result call() throws Exception {

		IntOpenHashSet startingSet =  new IntOpenHashSet() {/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		{ add(category); }};
		
		
	
//		descend(startingSet, new  HashSet<Integer>(category), 1 );

		visited = descend2(startingSet, new  IntOpenHashSet(startingSet), 1);
		updateResults2();	
		
//		for (Integer v : visited)
//			updateResults(v);
		

		
		svmClassifier.updateFeatures(resultsTraining, resultsDevelopment);
		svmClassifier.scaleProblems();
	

		float avgF1 = 0.f;
		float numOfTargetCategory = 0.f; 
		for (String categoryName : container.targetCategories) {			
			try {

				svmClassifier.setLabels(categoryName, container);

				Pair<Float, Float> res = svmClassifier.test(container.weights[(int) numOfTargetCategory]);
				
				numOfTargetCategory+=1.f;
				avgF1+=res.second;

			} catch (Exception e) {
//				e.printStackTrace();

			}
		}			
		return new Result(category, avgF1 / numOfTargetCategory );
	}


//	private void descend(HashSet<Integer> categories,HashSet<Integer> visited,int height) {
//		if (categories.isEmpty()|| height > maxHeight )
//			return ;
//
//		HashSet<Integer> toExplore= new HashSet<Integer>();;
//		
//		for(Integer cat: categories) {
//
//			if (height == 1 || !container.main_topic.contains(cat))
//				toExplore.addAll(checkVisited(cat,visited));
//	
//			updateResults(cat);
//		}
//		visited.addAll(toExplore);
//		
//		descend(toExplore,visited,height+1);
//	}
//	
//	private void updateResults(Integer cat) {
//
//		/**
//		 *  Training  
//		 */
//		for (int queryIdx=0; queryIdx < container.training.length ; queryIdx ++) {
//			ArrayList<HashSet<Integer>>[] pair = container.training[queryIdx];
//			
//			for (int entityIdx=0; entityIdx < pair[0].size() ; entityIdx ++) 
//				if (/*entitiesGroup*/ pair[0].get(entityIdx).contains(cat)) {
//					if (resultsTraining[queryIdx][0] == null) 
//						resultsTraining[queryIdx][0] = new HashSet<Integer>();
//					resultsTraining[queryIdx][0].add(entityIdx);
//				}
//			
//			for (int entityIdx=0; entityIdx < pair[1].size() ; entityIdx ++) 
//				if (/*topKentitiesGroup*/ pair[1].get(entityIdx).contains(cat)) {
//					if (resultsTraining[queryIdx][1] == null) 
//						resultsTraining[queryIdx][1] = new HashSet<Integer>();					
//					resultsTraining[queryIdx][1].add(entityIdx);
//				}
//		}
//		
//		/**
//		 *  Development  
//		 */
//		for (int queryIdx=0; queryIdx < container.development.length ; queryIdx ++) {
//			ArrayList<HashSet<Integer>>[] pair = container.development[queryIdx];
//			
//			for (int entityIdx=0; entityIdx < pair[0].size() ; entityIdx ++)
//				if (/*entitiesGroup*/ pair[0].get(entityIdx).contains(cat)) {
//					if (resultsDevelopment[queryIdx][0] == null) 
//						resultsDevelopment[queryIdx][0] = new HashSet<Integer>();	
//					resultsDevelopment[queryIdx][0].add(entityIdx);
//				}
//					
//				
//			for (int entityIdx=0; entityIdx < pair[1].size() ; entityIdx ++) 
//				if (/*topKentitiesGroup*/ pair[1].get(entityIdx).contains(cat)) {
//					if (resultsDevelopment[queryIdx][1] == null) 
//						resultsDevelopment[queryIdx][1] = new HashSet<Integer>();	
//					resultsDevelopment[queryIdx][1].add(entityIdx);
//				}
//					
//		
//		}
//	}
	
	
	private void updateResults2() {

		/**
		 *  Training  
		 */
		for (int queryIdx=0; queryIdx < container.training.length ; queryIdx ++) {
			int[][][] pair = container.training[queryIdx];
			
			for (int entityIdx=0; entityIdx < pair[0].length ; entityIdx ++) 
				for (Integer cateogoryOfEntity : pair[0][entityIdx]) 
					if (visited.contains(cateogoryOfEntity)) {
//						if (resultsTraining[queryIdx][0] == null) 
//							resultsTraining[queryIdx][0] = new HashSet<Integer>();
//						resultsTraining[queryIdx][0].add(entityIdx);
						resultsTraining[queryIdx][0]++;
						break;
					}
						
					
					
				
			
			for (int entityIdx=0; entityIdx < pair[1].length ; entityIdx ++) 
				for (Integer cateogoryOfEntity : pair[1][entityIdx]) 
					if (visited.contains(cateogoryOfEntity)) {
//						if (resultsTraining[queryIdx][1] == null) 
//							resultsTraining[queryIdx][1] = new HashSet<Integer>();
//						resultsTraining[queryIdx][1].add(entityIdx);
						resultsTraining[queryIdx][1]++;
						break;
					}
		}
		
		/**
		 *  Development  
		 */
		for (int queryIdx=0; queryIdx < container.development.length ; queryIdx ++) {
			int[][][] pair = container.development[queryIdx];
			
					
			for (int entityIdx=0; entityIdx < pair[0].length ; entityIdx ++) 
				for (Integer cateogoryOfEntity : pair[0][entityIdx]) 
					if (visited.contains(cateogoryOfEntity)) {
//						if (resultsDevelopment[queryIdx][0] == null) 
//							resultsDevelopment[queryIdx][0] = new HashSet<Integer>();
//						resultsDevelopment[queryIdx][0].add(entityIdx);
						resultsDevelopment[queryIdx][0]++;
						break;
					}
						
					
					
				
			
			for (int entityIdx=0; entityIdx < pair[1].length ; entityIdx ++) 
				for (Integer cateogoryOfEntity : pair[1][entityIdx]) 
					if (visited.contains(cateogoryOfEntity)) {
//						if (resultsDevelopment[queryIdx][1] == null) 
//							resultsDevelopment[queryIdx][1] = new HashSet<Integer>();
//						resultsDevelopment[queryIdx][1].add(entityIdx);
						resultsDevelopment[queryIdx][1]++;
						break;
					}
		}
	}
	
	
	private IntOpenHashSet descend2(IntOpenHashSet startingSet, IntOpenHashSet intOpenHashSet, int height) {
		if (startingSet.isEmpty()|| height > maxHeight )
			return intOpenHashSet;

		IntOpenHashSet toExplore= new IntOpenHashSet();;
		
		for(Integer cat: startingSet) 
			if (height == 1 || !container.main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,intOpenHashSet));

		
		if (height+1 <= maxHeight)
			intOpenHashSet.addAll(toExplore);
		
		
		return descend2(toExplore,intOpenHashSet,height+1);
	}

		
//	private void updateResults(Integer cat) {
////		System.err.println("jkghgkgh");
//		for (int Idx=0; Idx < container.training.length ; Idx ++) {
//			ArrayList<HashSet<Integer>>[] pair = container.training[Idx];
//			for (HashSet<Integer> entitiesGroup: pair[0])
//				if (entitiesGroup.contains(cat))
//					resultsTraining[Idx][0]++;
//			
//			for (HashSet<Integer> topKentitiesGroup: pair[1])
//				if (topKentitiesGroup.contains(cat))
//					resultsTraining[Idx][1]++;
//		}
//		
//		for (int Idx=0; Idx < container.development.length ; Idx ++) {
//			ArrayList<HashSet<Integer>>[] pair = container.development[Idx];
//			for (HashSet<Integer> entitiesGroup: pair[0])
//				if (entitiesGroup.contains(cat))
//					resultsDevelopment[Idx][0]++;
//			
//			for (HashSet<Integer> topKentitiesGroup:  pair[1])
//				if (topKentitiesGroup.contains(cat))
//					resultsDevelopment[Idx][1]++;
//		}
//		
//	}

	public ArrayList<Integer> checkVisited(Integer cat, IntOpenHashSet visited){
		ArrayList<Integer> aux=new  ArrayList<Integer>();
		
		
		try{
			for (Integer c : container.reverseCategories.get(cat)) {
				if (!visited.contains(c))
					aux.add(c);
			}
		} catch(Exception e) {
			
		}
			
		return aux;
				
	}






}
