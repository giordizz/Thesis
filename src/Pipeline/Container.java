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

import javax.swing.text.StyledEditorKit.AlignmentAction;

import libsvm.svm_node;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



public class Container {
	
	public class SPair {
		Integer id=0;
		Float value=0f;
		
		public SPair(Integer id, Float value) {
			this.id=id;
			this.value=value;
		}

		public SPair() {
		}
		
		
	}
	
	
	Map<Integer,Vector<Integer>> reverseCategories = new HashMap<Integer, Vector<Integer>>();
	private Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
	private Map<Integer,Vector<SPair>> jaccardIndexes = new HashMap<Integer, Vector<SPair>>();	
	private Map<Integer,Vector<Integer>> categories = new HashMap<Integer, Vector<Integer>>();
	private Map<Integer,Vector<Integer>> articles = new HashMap<Integer, Vector<Integer>>();
	
//	int[][] resultsTraining;
//	int[][] resultsDevelopment;
	

	Vector<HashSet<Integer>>[][] training = new Vector[428][2];
	Vector<HashSet<Integer>>[][] development = new Vector[174][2];
	
	
	Float[] weights = new Float[67];
	String[] targetCategories = new String[67];
//	int currentIterationIdx = 0;
	
	
	
//	private HashSet<Integer> main_topic;
	private JSONArray jsonTraining;
	private JSONArray jsonValidation;

	

	private int topK;
	
	Container(int topK) throws FileNotFoundException, UnsupportedEncodingException{
		this.topK = topK;

//		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
//		main_topic = new HashSet<Integer>(Arrays.asList(aux));
	}
	
	public void getStatus() throws IOException{
		
		GZIPInputStream	gzip1=null, gzip2=null, gzip3=null;
		
//		gzip1 = new GZIPInputStream(new FileInputStream("wiki-article-categories-links-sorted.gz"));
//		gzip2 = new GZIPInputStream(new FileInputStream("wiki-categories-only-links-sorted.gz"));
//		gzip3 = new GZIPInputStream(new FileInputStream("jaccardFileWithVals.txt.gz"));
		gzip1 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));
		gzip3 = new GZIPInputStream(new FileInputStream("../dbpedia/jaccardFileWithVals.txt.gz")); //TODO
		
		
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

			}
		}
		br1.close();


		while((line=br2.readLine())!=null) {
			final String[] s=line.split("\t");
			Vector<Integer> cur1 = categories.get(Integer.parseInt(s[0]));
			if (cur1==null) 
				categories.put(Integer.parseInt(s[0]),new Vector<Integer>(){{add(Integer.parseInt(s[1]));}});
			else {
				cur1.add(Integer.parseInt(s[1]));
//				categories.put(Integer.parseInt(s[0]),cur);
			}
			
			Vector<Integer> cur2 = reverseCategories.get(Integer.parseInt(s[1]));
			if (cur2==null) 
				reverseCategories.put(Integer.parseInt(s[1]),new Vector<Integer>(){{add(Integer.parseInt(s[0]));}});
			else {
				cur2.add(Integer.parseInt(s[0]));
			}
		}	
		br2.close();


		jsonTraining= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/training_set_tagged.JSON")));
		jsonValidation= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/validation_set_tagged.JSON")));
		

		
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
		
		
		BufferedReader br4 = new BufferedReader(new FileReader("../Download/My_redirect_ID_2"));
//		BufferedReader br4 = new BufferedReader(new FileReader("My_redirect_ID_2"));
		while( (line=br4.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
		   
		}
		
		br4.close();
		

		
		HashSet<String> queryNotTagged = new HashSet<String>();
		BufferedReader br6 = new BufferedReader(new FileReader("query_not_tagged.txt"));
		while( (line=br6.readLine())!=null) 
		     queryNotTagged.add(line);
		     			
		br6.close();
		
		
		
		
		BufferedReader br7 = new BufferedReader(new FileReader("data/weights_newnew8top21_2.txt"));	
		for  (int idx=0; (line = br7 .readLine()) != null ; idx++) 
			weights[idx]=Float.parseFloat(line);		
		br7.close();
		
		



		BufferedReader br8 = new BufferedReader(new FileReader("data/CatTarget.txt"));
		for  (int idx=0; (line = br8 .readLine()) != null ; idx++) 
			targetCategories[idx]=line;		
		br8.close();

		
		Iterator<JSONObject> i = jsonTraining.iterator();
		
		int countExamples = 0;
		while( i.hasNext()){
			JSONObject q = i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
			
			
			training[countExamples][0] = new Vector<HashSet<Integer>>();
			Vector<Integer> entities = new Vector<Integer>();
			for (Long id : (ArrayList<Long>) q.get("tags")) {
				entities.add(id.intValue());
				training[countExamples][0].add(getCategoriesByTag(id.intValue()));
			}
			
			
			training[countExamples][1] = new Vector<HashSet<Integer>>();
			for (Integer id : getTopKSimilarEntity2(entities)){
				training[countExamples][1].add(getCategoriesByTag(id));
			}
	
			countExamples++;
		}
		
		countExamples = 0;
		i = jsonValidation.iterator();
		while( i.hasNext()){
			JSONObject q = i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
			
//			
			
			development[countExamples][0] = new Vector<HashSet<Integer>>();
			
			Vector<Integer> entities = new Vector<Integer>();
			for (Long id : (ArrayList<Long>) q.get("tags")) {
				entities.add(id.intValue());
//				aux.get(0).add(getCategoriesByTag(id.intValue()));
				development[countExamples][0].add(getCategoriesByTag(id.intValue()));
			}
			
			
			development[countExamples][1] = new Vector<HashSet<Integer>>();
			for (Integer id : getTopKSimilarEntity2(entities)){
				development[countExamples][1].add(getCategoriesByTag(id));
			}
	
			countExamples++;
		}

		
		
		jsonTraining.clear();
		jsonValidation.clear();
		categories.clear();
		articles.clear();
		redirects.clear();
		jaccardIndexes.clear();
		queryNotTagged.clear();
		
		System.err.println("************** Status loaded! **************");
	}
	
	


	
	

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
	

	private Vector<Integer> getTopKSimilarEntity2(Vector<Integer> entities) {
		Vector<Vector<SPair>> aux = new Vector<Vector<SPair>>();
		
		
		int i=0;
		int  err = 0;
		for (Integer id : entities) {
			try {
				aux.add((jaccardIndexes.get(id))); //TODO .subList(0, topK)
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			i++;

		}
		if (aux.size()==0)
			return  new Vector<Integer>();
			
		int[] indexes = new int[aux.size()];
		
		Vector<SPair> ris= new Vector<SPair>(topK); 	
		for (int ii=0 ; ii< topK; ii++	) {
			ris.add(new SPair());
		}
		
		
			for (int j=0;j< topK; j++){
				int maxIndex=0;
				for (int jj=0; jj< aux.size(); jj++)
						if (aux.get(jj)==null || indexes[jj] >= aux.get(jj).size())
							continue;
						else if (ris.get(j).value < aux.get(jj).get(indexes[jj]).value) {
								ris.set(j,aux.get(jj).get(indexes[jj]));
								maxIndex=jj;
							}
				indexes[maxIndex]++;
			}
		
	
			Vector<Integer> risID = new Vector<Integer>(topK); 
		for (int k=0; k< ris.size();k++)
			risID.add(ris.get(k).id);
		
		
		
		return risID;
	}




}
