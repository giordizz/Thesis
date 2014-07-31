package it.giordizz.Thesis;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import libsvm.svm_node;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



public class RisalitaCounting {
	
	public class SPair {
		Integer id=0;
		Float value=0f;
		
		public SPair(Integer id, Float value) {
			this.id=id;
			this.value=value;
		}

		public SPair() {
			// TODO Auto-generated constructor stub
		}
		
		
	}
	
	Map<Integer,Vector<Integer>> articles = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Vector<Integer>> categories = new HashMap<Integer, Vector<Integer>>();
//	Map<Integer,Vector<Integer>> jaccardIndexes = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Vector<SPair>> jaccardIndexes = new HashMap<Integer, Vector<SPair>>();	
	Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
	
	int maxHeight;
	HashSet<Integer> main_topic;
	JSONArray jsonTraining;
	JSONArray jsonValidation;
	Vector<Integer> intermediate_categories= new Vector<Integer> ();
	TreeSet<Integer> S ;
	HashSet<String> queryNotTagged = new HashSet<String>();
	String setName;

	int topK;
	
	RisalitaCounting(int height, int topK) throws FileNotFoundException, UnsupportedEncodingException{
		maxHeight = height;
		this.topK = topK;
//		output = new PrintWriter("../Thesis/data/new8top"+topK+"/results_"+ setName  +".txt"  , "UTF-8");
		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
		main_topic = new HashSet<Integer>(Arrays.asList(aux));
	}
	
	public void getStatus() throws IOException{
		
		GZIPInputStream	gzip1=null, gzip2=null, gzip3=null;
		
		gzip1 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));
		gzip3 = new GZIPInputStream(new FileInputStream("../dbpedia/jaccardFileWithVals.txt.gz"));
		
		
		
		BufferedReader br1 = new BufferedReader(new InputStreamReader(gzip1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(gzip2));
		BufferedReader br3 = new BufferedReader(new InputStreamReader(gzip3));


		String line="";
		while((line=br1.readLine())!=null) {
			final String[] s=line.split("\t");
			Vector<Integer> cur = articles.get(Integer.parseInt(s[0]));
			if (cur==null) 
				articles.put(Integer.parseInt(s[0]),new Vector<Integer>(){{add(Integer.parseInt(s[1]));}});
			 else {
				cur.add(Integer.parseInt(s[1]));
				articles.put(Integer.parseInt(s[0]),cur);

			}
		}
		br1.close();

		while((line=br2.readLine())!=null) {
			final String[] s=line.split("\t");
			Vector<Integer> cur = categories.get(Integer.parseInt(s[0]));
			if (cur==null) 
				categories.put(Integer.parseInt(s[0]),new Vector<Integer>(){{add(Integer.parseInt(s[1]));}});
			else {
				cur.add(Integer.parseInt(s[1]));
				categories.put(Integer.parseInt(s[0]),cur);
			}
		}	
		br2.close();
		

//		if (topK>0) 
//			while((line=br3.readLine())!=null) {
//				Integer id = Integer.parseInt(line);
//				final String[] s=br3.readLine().split("\t");
//	
//					
//				Vector<Integer> aux = new Vector<Integer>();
//				
//				for (int topI=0; topI < Math.min(s.length,topK) ; topI++ )
//					aux.add(Integer.parseInt(s[topI]));
//				jaccardIndexes.put(id, aux);
//	
//			}			
//		br3.close();
		
		if (topK>0) 
			while((line=br3.readLine())!=null) {
				Integer id = Integer.parseInt(line);
				final String[] s=br3.readLine().split("\t");
	
					
				Vector<SPair> aux = new Vector<SPair>();
				
				for (int topI=0; topI < Math.min(s.length,topK) ; topI++ ){
					
				
					final String[] ss= s[topI].split(",");
					aux.add(new SPair(Integer.parseInt(ss[0]),Float.parseFloat(ss[1])));
					
				}
				jaccardIndexes.put(id, aux);
	
			}			
		br3.close();
		
		
		jsonTraining= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/training_set_tagged.JSON")));
		jsonValidation= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/validation_set_tagged.JSON")));
		
		
		BufferedReader br4 = new BufferedReader(new FileReader("../Download/My_redirect_ID_2"));
		while( (line=br4.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
		   
		}
		
		br4.close();
		
		
		//TODO
//		BufferedReader br5 = new BufferedReader(new FileReader("../data/AllCategoriesSorted.txt"));
		BufferedReader br5 = new BufferedReader(new FileReader("../data/CatIntermadiateID"));
		while( (line=br5.readLine())!=null) 
		     intermediate_categories.add(Integer.parseInt(line));
		     			
		br5.close();
		
		S = new TreeSet<Integer>();
		
		
		BufferedReader br6 = new BufferedReader(new FileReader("query_not_tagged.txt"));
		while( (line=br6.readLine())!=null) 
		     queryNotTagged.add(line);
		     			
		br6.close();
		
		System.err.println("************** Status loaded! **************");
	}
	
	
	
//	
//	public Vector<Integer> checkVisited(Integer cat, Vector<Integer> visited){
//		Vector<Integer> aux=new  Vector<Integer>();
//		try{
//		
//			for (Integer c : this.categories.get(cat)) {
////				if (!visited.contains(c))
//					aux.add(c);
//			}
//	} catch(Exception e) {
//		
//	}
//	
//			
//			
//			return aux;
//		
//		
//	}
//	
//	
//	
//	public HashSet<Integer> climb(Vector<Integer> categories, HashSet<Integer> result,Vector<Integer> visited,int height){
//		if (categories.isEmpty()|| height > maxHeight )
//				return result;
//
//		Vector<Integer> toExplore= new Vector<Integer>();;
//
//		
//	
//		for(Integer cat: categories) {
//
////			if (!main_topic.contains(cat))
//				toExplore.addAll(checkVisited(cat,visited));
//			
//			if (S.contains(cat)) {
////				TODO
////				if (!result.contains(cat))
//					result.add(cat);
//
//			}
//				
//		}
//	
//		
//		
//		return climb(toExplore,result,categories,height+1);
//		
//		
//		
//	}	
//	
//	public Vector<Integer> getCategoriesByTags(Vector<Integer> tags){
//		Vector<Integer> cats= new Vector<Integer>();
//		
//		for (Integer tag: tags) {
//
//			try {
//				cats.addAll(articles.get(tag.toString()));
//			} catch (Exception e) {
//				try {
//					cats.addAll(articles.get(redirects.get(tag.toString())));
//				} catch (Exception e1) {
//
//				}
//				
//			}
//		}
//		
//		return cats;
//				
//	}
//	
//	
//
//
//	public Vector<Integer> getCategoriesByTag(Integer tag){
//	
//		Vector<Integer> cats= new Vector<Integer>();
//
//		try {
//			cats.addAll(articles.get(tag));
//		} catch (Exception e) {
//			try {
//				cats.addAll(articles.get(redirects.get(tag)));
//			} catch (Exception e1) {
//
//			}
//			
//		}
//	
//	return cats;
//	
//	
//}
//	
//	
//	
//	
//	public HashSet<Integer> tag(String query,Integer tags){
//		
//		Vector<Integer> aux= getCategoriesByTag(tags);
//
//		return climb(aux,new HashSet<Integer>(),new Vector<Integer>(),1);
//		
//	}	
	
	public Vector<Integer> checkVisited(Integer cat, HashSet<Integer> visited){
		Vector<Integer> aux=new  Vector<Integer>();
		
		
		try{
//			aux.addAll(this.categories.get(cat));	
			for (Integer c : this.categories.get(cat)) {
				if (!visited.contains(c))
					aux.add(c);
			}
	} catch(Exception e) {
		
	}
	
			
			
			return aux;
		
		
	}
	
	public HashSet<Integer> climb(HashSet<Integer> categories, HashSet<Integer> result,HashSet<Integer> visited,int height){
		if (categories.isEmpty()|| height > maxHeight )
				return result;

		HashSet<Integer> toExplore= new HashSet<Integer>();;

		
	
		for(Integer cat: categories) {

			if (!main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,visited));
			
			if (S.contains(cat)) {
//				TODO
//				if (!result.contains(cat))
					result.add(cat);

			}
				
		}
		
//		visited.addAll(categories);
		visited.addAll(toExplore);
//		System.out.println("da esploreare: \n" + toExplore);
//		System.out.println("curr results: \n" + result);
		
		return climb(toExplore,result,visited,height+1);
		
		
		
	}	
	
//	public HashSet<Integer> climb(Vector<Integer> categories, HashSet<Integer> result,HashSet<Integer> visited,int height){
//		if (categories.isEmpty()|| height > maxHeight )
//				return result;
//
//		Vector<Integer> toExplore= new Vector<Integer>();;
//
//		
//	
//		for(Integer cat: categories) {
//
//			if (!main_topic.contains(cat))
//				toExplore.addAll(checkVisited(cat,visited));
//			
//			if (S.contains(cat)) {
////				TODO
////				if (!result.contains(cat))
//					result.add(cat);
//
//			}
//				
//		}
//		
//		visited.addAll(categories);
//		
//		
//		return climb(toExplore,result,visited,height+1);
//		
//		
//		
//	}	
	
	public Vector<Integer> getCategoriesByTags(Vector<Integer> tags){
		Vector<Integer> cats= new Vector<Integer>();
		
		for (Integer tag: tags) {

			try {
				cats.addAll(articles.get(tag.toString()));
			} catch (Exception e) {
				try {
					cats.addAll(articles.get(redirects.get(tag.toString())));
				} catch (Exception e1) {

				}
				
			}
		}
		
		return cats;
				
	}
	
	


//	public Vector<Integer> getCategoriesByTag(Integer tag){
//	
//		Vector<Integer> cats= new Vector<Integer>();
//
//		try {
//			cats.addAll(articles.get(tag));
//		} catch (Exception e) {
//			try {
//				cats.addAll(articles.get(redirects.get(tag)));
//			} catch (Exception e1) {
//
//			}
//			
//		}
//	
//	return cats;
//	
//	
//}
//	
	public HashSet<Integer> getCategoriesByTag(Integer tag){
		
		HashSet<Integer> cats= new HashSet<Integer>();

		try {
			cats.addAll(articles.get(tag));
		} catch (Exception e) {
			try {
				cats.addAll(articles.get(redirects.get(tag)));
			} catch (Exception e1) {

			}
			
		}
	
	return cats;
	
	
}
	
	public HashSet<Integer> tag(String query,Integer tag){
		
		HashSet<Integer> aux= getCategoriesByTag(tag);

		return climb(aux,new HashSet<Integer>(),new HashSet<Integer>(aux),1);
		
	}	
	
	
//	public HashSet<Integer> tag(String query,Integer tags){
//		
////		Vector<Integer> aux= getCategoriesByTag(tags);
//
//		return climb(getCategoriesByTag(tags),new HashSet<Integer>(),new HashSet<Integer>(),1);
//		
//	}	

	private void writeResultOnFile(HashMap<Integer, Integer> result1, HashMap<Integer, Integer> result2, int whichDataSet, SVMClassifier s) {
		
		
		

		Vector<svm_node> auxNodeVect = new Vector<svm_node>();
		
		int c=0;
		for (Iterator<Integer> it = S.iterator(); it.hasNext(); c++ ) {
			Integer ii = result1.get(it.next());
			if (ii!=null){
				
				svm_node auxNode = new svm_node();
				auxNode.index = c+1;
				auxNode.value = ii;
				auxNodeVect.add(auxNode);
//					
			}
		}
		
		for (Iterator<Integer> it = S.iterator(); it.hasNext(); c++ ) {
			Integer ii = result2.get(it.next());
			if (ii!=null){
				
				svm_node auxNode = new svm_node();
				auxNode.index = c+1;
//				if (auxNode.index==123){
//					System.err.println("er node");
//					System.exit(1);
//				}
					
				auxNode.value = ii;
				auxNodeVect.add(auxNode);
//					
			}
		}
		
		
		
		svm_node[] nodeVect = new svm_node[auxNodeVect.size()];
		for (int i=0; i< nodeVect.length; i++)
			nodeVect[i] = auxNodeVect.elementAt(i);
		
		
//		if (nodeVect.length!=1)
		s.addExample(nodeVect, whichDataSet);
		
	}
	public void tagAll(SVMClassifier s, int whichDataSet){
		System.err.println("************** Start tagging! **************");
		
		
		Iterator i = (whichDataSet==0) ? jsonTraining.iterator() : jsonValidation.iterator();
		int count=1;
		
		HashMap<Integer,Integer> result1 = new HashMap<Integer,Integer>(); 
		HashMap<Integer,Integer> result2 = new HashMap<Integer,Integer>(); 
		
		while( i.hasNext()){
			JSONObject q = (JSONObject) i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
			
			
			ArrayList<Integer> entities = new ArrayList<Integer>();
				
			for (Long id : (ArrayList<Long>) q.get("tags"))
				entities.add(id.intValue());
			
//			System.err.println("prima " + entities.size());
			
			ArrayList<Integer> topKs = getTopKSimilarEntity2(entities);
			
//			entities.addAll(topKs);  --> metodo con fusione topk #feature=123
//			System.err.println("dopo " + entities.size());
			
//			int numOfEntity = entities.size();

			result1.clear();
			result2.clear();
//			for (int i1=0;i1<numOfEntity;i1++){ 
				
			for (Integer id : entities){

				/*
				 * itero sui resultati
				 */
				 for (Integer cat: tag(query, id)){
						Integer curr_count =result1.get(cat);
						if (curr_count == null)
							result1.put(cat,1);
						else
							result1.put(cat, curr_count+1);
							
				 }
			}
			
			
			for (Integer id : topKs){

				/*
				 * itero sui resultati
				 */
				 for (Integer cat: tag(query, id)){
						Integer curr_count =result2.get(cat);
						if (curr_count == null)
							result2.put(cat,1);
						else
							result2.put(cat, curr_count+1);
							
				 }
			}
			
			
			//System.err.println("stampo i risultati");

//			writeResultOnFile(result);
			writeResultOnFile(result1, result2, whichDataSet, s);
			
			
			
			System.err.println("Tagged #" + count++);
		}
		
		
		
		System.err.println("stop tagging");
//		System.err.println("results written on  -> ../Thesis/data/results_"+ setName  +".txt");
//		System.err.println("results written on  -> ../Thesis/data/new8top"+topK+"/results_"+ setName  +".txt");
	}
	


//	private ArrayList<Integer> getTopKSimilarEntity(ArrayList<Integer> entities) {
////		int numOfEntity = entities.size();
////		for (int indexEntity = 0; indexEntity < numOfEntity ; indexEntity++ )
//
//		ArrayList<Integer> aux= new ArrayList<Integer>(); 
//		for (Integer id : entities)
//			try {
////				System.err.println("ok");
////				for (Integer topI :)
//				aux.addAll(jaccardIndexes.get(id));
////				entities.addAll( jaccardIndexes.get( ((Long) entities.get(indexEntity)).intValue() ));
//				
//			} catch (Exception e) {
////				e.printStackTrace();
////				System.err.println("errore "+ id );
//			}
//			
////		if (dim!=entities.size())
////		System.err.println("wow");
////		entities.addAll(aux);
//		
//		return aux;
//	}
	private ArrayList<Integer> getTopKSimilarEntity2(ArrayList<Integer> entities) {
//		int numOfEntity = entities.size();
//		for (int indexEntity = 0; indexEntity < numOfEntity ; indexEntity++ )

//		(Vector<Integer>)[] aux1 = new (Vector<Integer>)[entities.size())];
		
		ArrayList<List<SPair>> aux = new ArrayList<List<SPair>>();
		
		
		int i=0;
		int  err = 0;
		for (Integer id : entities) {
			try {
//				System.err.println("ok");
//				for (Integer topI :)
				aux.add((jaccardIndexes.get(id))); //TODO .subList(0, topK)
//				System.out.println(aux.get(i).size());
//				entities.addAll( jaccardIndexes.get( ((Long) entities.get(indexEntity)).intValue() ));
				
			} catch (Exception e) {
				e.printStackTrace();
				err++;
//				e.printStackTrace();
//				System.err.println("errore "+ id );
			}
//			
//			if (aux[i]!=null)
//				System.out.println(aux[i].size());
			i++;

		}
		if (aux.size()==0)
			return  new ArrayList<Integer>();
			
//		if (err== entities.size())
//			System.err.println("SONO CAZZI");
		int[] indexes = new int[aux.size()];
		
//		System.out.println(err);
		ArrayList<SPair> ris= new ArrayList<SPair>(topK); 	
		for (int ii=0 ; ii< topK; ii++	) {
			ris.add(new SPair());
		}
		
		
			for (int j=0;j< topK; j++){
				int maxIndex=0;
	//			ris.set(j, aux[0].elementAt(indexes[0]++));
				for (int jj=0; jj< aux.size(); jj++)
						if (aux.get(jj)==null || indexes[jj] >= aux.get(jj).size())
							continue;
						else if (ris.get(j).value < aux.get(jj).get(indexes[jj]).value) {
								ris.set(j,aux.get(jj).get(indexes[jj]));
								maxIndex=jj;
							}
				indexes[maxIndex]++;
			}
		
	
		ArrayList<Integer> risID = new ArrayList<Integer>(topK); 
		for (int k=0; k< ris.size();k++)
			risID.add(ris.get(k).id);
//		System.out.println(risID.get(0));
//		System.out.println(risID.toString());
		return risID;
	}
	public void excludeI(int I) {
		// TODO Auto-generated method stub
		
		S.clear();
		
		for (int i = 0, j=0; i< intermediate_categories.size()-1; i++){
			if (i==I)
				j++;
			
//			S.set(i, intermediate_categories.elementAt(j++));
			S.add( intermediate_categories.elementAt(j++));
		}
		
//		if ( intermediate_categories.size() == S.size()) {
//			System.err.println("errore dim S");
//			System.exit(1);
//		}
			
			
			
			
				
		
	}



}
