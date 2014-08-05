package it.giordizz.Thesis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import libsvm.svm_node;
import libsvm.svm_problem;
import it.giordizz.Thesis.SVMClassifier.setType;

public class Gatherer {

	svm_problem trainProblem;
	svm_problem develProblem;
	
	int totNumOfFtrs=-1;


	public class Problems {
		svm_problem trainProblem;
		svm_problem develProblem;
		int totNumOfFtrs;
		
		Problems(svm_problem trainProblem, svm_problem develProblem, int totNumOfFtrs) {
			
			this.totNumOfFtrs = totNumOfFtrs;
			
			this.trainProblem = new svm_problem();
			this.trainProblem.l = trainProblem.l;
			this.trainProblem.y = new double[trainProblem.l];
			
			this.trainProblem.x = new svm_node[trainProblem.l][];
			
			for (int i=0; i < trainProblem.x.length ; i++){
				svm_node[] aux= trainProblem.x[i];
				this.trainProblem.x[i] = new svm_node[aux.length];
				for (int ii=0; ii < aux.length ; ii++){
					this.trainProblem.x[i][ii]= new svm_node();				
					this.trainProblem.x[i][ii].index =aux[ii].index;
					this.trainProblem.x[i][ii].value =aux[ii].value;
				}
			}
				
					
//					trainProblem.x.clone();
			
			
			
			
			this.develProblem = new svm_problem();
			this.develProblem.l = develProblem.l;
			this.develProblem.y = new double[develProblem.l];
			
			
			this.develProblem.x = new svm_node[develProblem.l][];
			
			for (int i=0; i < develProblem.x.length ; i++){
				svm_node[] aux= develProblem.x[i];
				this.develProblem.x[i] = new svm_node[aux.length];
				for (int ii=0; ii < aux.length ; ii++){
					this.develProblem.x[i][ii]= new svm_node();				
					this.develProblem.x[i][ii].index =aux[ii].index;
					this.develProblem.x[i][ii].value =aux[ii].value;
				}
			}
			
		}
		
		
		
	}
	
	
	/**
	 * setType: specifica se i dati da raccogliere fanno parte del training o
	 * del development set
	 */
	public enum setType {
		TRAINING, DEVELOPMENT
	}

	
	
	private void gatherUnlabeledData(setType T) throws Exception {

		String featuresFile = (T == setType.TRAINING) ? "data/results_training.txt" 
				: "data/results_validation.txt";



		
		BufferedReader reader = new BufferedReader(new FileReader(featuresFile));

		
		
		
		
		Vector<svm_node[]> auxVector =  new Vector<svm_node[]>();
		String lineOfFeatures;
		while  ( (lineOfFeatures = reader.readLine()) != null) {
			String[] features = lineOfFeatures.split(",");


			Vector<svm_node> auxNodeVect = new Vector<svm_node>();
			int ftrIdx = 0;
			for (; ftrIdx < features.length; ftrIdx++) {
				Double d = Double.parseDouble(features[ftrIdx]);
//	TODO			
				if (d!=0.) {
					
					svm_node auxNode = new svm_node();
					auxNode.index = ftrIdx+1;
					auxNode.value = d;
					auxNodeVect.add(auxNode);
//						
					}
					
			}

			if (totNumOfFtrs == -1)
				totNumOfFtrs = ftrIdx+2;
			
			svm_node[] nodeVect = new svm_node[auxNodeVect.size()+2];
			int i=0;
			for (; i< auxNodeVect.size(); i++)
				nodeVect[i] = auxNodeVect.elementAt(i);
			
			svm_node plus1Node = new svm_node();
			plus1Node.index = 247;
			plus1Node.value = 0;
			nodeVect[i++]= plus1Node;
			
			svm_node plus2Node = new svm_node();
			plus2Node.index = 248;
			plus2Node.value = 0;
			nodeVect[i]= plus2Node;
			
//			auxNodeVect.clear();
			auxVector.add(nodeVect);
	
		}

		
		svm_problem problem = new svm_problem();
		problem.l = auxVector.size();
		problem.x = new svm_node[problem.l][];
//		problem.y = new double[problem.l];
		
		
		for  ( int i=0; i < problem.l ; i++ ) 
			problem.x[i]=auxVector.elementAt(i);
		
		
		
		if (T == setType.TRAINING)			
			trainProblem=problem;
		else
			develProblem=problem;
		
		reader.close();

	}

	public void gatherData() throws Exception {
		System.err.println("start gathering training examples..");
		gatherUnlabeledData(setType.TRAINING);
		System.err.println("-> training examples gathered! <-");
		
		System.err.println("start gathering development examples..");
		gatherUnlabeledData(setType.DEVELOPMENT);		
		System.err.println("->  development examples gathered! <-");

	}
	
	public Problems getCopyOfProblems(){
		
		return new Problems(trainProblem, develProblem, totNumOfFtrs);
	}

}
