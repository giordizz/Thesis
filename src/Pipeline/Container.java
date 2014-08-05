package it.giordizz.Thesis;


import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

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
	

	Int2ObjectMap<ArrayList<Integer>> reverseCategories = new Int2ObjectOpenHashMap<ArrayList<Integer>>();
	private Int2IntMap redirects = new  Int2IntOpenHashMap();
	private Int2ObjectMap<ArrayList<SPair>> jaccardIndexes = new Int2ObjectOpenHashMap< ArrayList<SPair>>();	
	private Int2ObjectMap<ArrayList<Integer>> categories = new Int2ObjectOpenHashMap<ArrayList<Integer>>();
	private Int2ObjectMap<ArrayList<Integer>> articles = new Int2ObjectOpenHashMap<ArrayList<Integer>>();
	
	Object2ObjectOpenHashMap<String,ArrayList<Boolean>> trainingLabels = new Object2ObjectOpenHashMap<String, ArrayList<Boolean>>();
	Object2ObjectOpenHashMap<String,ArrayList<Boolean>> developmentLabels = new Object2ObjectOpenHashMap<String, ArrayList<Boolean>>();
	
	IntOpenHashSet main_topic;
	
	
//	Map<Integer,ArrayList<Integer>> reverseCategories = new HashMap<Integer, ArrayList<Integer>>();
//	private Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
//	private Map<Integer,ArrayList<SPair>> jaccardIndexes = new HashMap<Integer, ArrayList<SPair>>();	
//	private Map<Integer,ArrayList<Integer>> categories = new HashMap<Integer, ArrayList<Integer>>();
//	private Map<Integer,ArrayList<Integer>> articles = new HashMap<Integer, ArrayList<Integer>>();
//	
//	Map<String,ArrayList<Boolean>> trainingLabels = new HashMap<String, ArrayList<Boolean>>();
//	Map<String,ArrayList<Boolean>> developmentLabels = new HashMap<String, ArrayList<Boolean>>();
//	
//	HashSet<Integer> main_topic;


//	@SuppressWarnings("unchecked")
//	ArrayList<HashSet<Integer>>[][] training = new ArrayList[428][2],  development = new ArrayList[174][2] ;
//	ArrayList<ArrayList<Integer>>[][] training = new ArrayList[428][2],  development = new ArrayList[174][2] ;
	int[][][][] training = new int[428][2][][],  development = new int[174][2][][];
	
	Float[] weights = new Float[67];
	String[] targetCategories = new String[67];

	private JSONArray jsonTraining, jsonValidation;

	

	private int topK;
	
	Container(int topK) throws FileNotFoundException, UnsupportedEncodingException{
		this.topK = topK;
		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
		main_topic = new IntOpenHashSet(Arrays.asList(aux));

	}
	
	@SuppressWarnings("unchecked")
	public void getStatus() throws IOException{
		
		GZIPInputStream	gzip1=null, gzip2=null, gzip3=null;
		
		gzip1 = new GZIPInputStream(new FileInputStream("data/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("data/wiki-categories-only-links-sorted.gz"));
		gzip3 = new GZIPInputStream(new FileInputStream("data/jaccardFileWithVals.txt.gz"));
//		gzip1 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-article-categories-links-sorted.gz"));
//		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));
//		gzip3 = new GZIPInputStream(new FileInputStream("../dbpedia/jaccardFileWithVals.txt.gz")); 
		
		
		BufferedReader br1 = new BufferedReader(new InputStreamReader(gzip1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(gzip2));
		BufferedReader br3 = new BufferedReader(new InputStreamReader(gzip3));
		

		String line="";
		while((line=br1.readLine())!=null) {
			final String[] s=line.split("\t");
			ArrayList<Integer> cur = articles.get(Integer.parseInt(s[0]));
			if (cur==null) 
				articles.put(Integer.parseInt(s[0]),new ArrayList<Integer>(){/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

				{add(Integer.parseInt(s[1]));}});
			 else {
				cur.add(Integer.parseInt(s[1]));

			}
		}
		br1.close();


		while((line=br2.readLine())!=null) {
			final String[] s=line.split("\t");
			ArrayList<Integer> cur1 = categories.get(Integer.parseInt(s[0]));
			if (cur1==null) 
				categories.put(Integer.parseInt(s[0]),new ArrayList<Integer>(){/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

				{add(Integer.parseInt(s[1]));}});
			else {
				cur1.add(Integer.parseInt(s[1]));
			}
			
			ArrayList<Integer> cur2 = reverseCategories.get(Integer.parseInt(s[1]));
			if (cur2==null) 
				reverseCategories.put(Integer.parseInt(s[1]),new ArrayList<Integer>(){/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

				{add(Integer.parseInt(s[0]));}});
			else {
				cur2.add(Integer.parseInt(s[0]));
			}
		}	
		br2.close();


//		jsonTraining= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/training_set_tagged.JSON")));
//		jsonValidation= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/validation_set_tagged.JSON")));
		jsonTraining= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("data/training_set_tagged.JSON")));
		jsonValidation= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("data/validation_set_tagged.JSON")));
//		

		
		if (topK>0) 
			while((line=br3.readLine())!=null) {
				Integer id = Integer.parseInt(line);
				final String[] s=br3.readLine().split("\t");
	
					
				ArrayList<SPair> aux = new ArrayList<SPair>();
				
				for (int topI=0; topI < Math.min(s.length,topK) ; topI++ ){
					
				
					final String[] ss= s[topI].split(",");
					aux.add(new SPair(Integer.parseInt(ss[0]),Float.parseFloat(ss[1])));
					
				}
				jaccardIndexes.put(id, aux);
	
			}			
		br3.close();
		
		
//		BufferedReader br4 = new BufferedReader(new FileReader("../Download/My_redirect_ID_2"));
		BufferedReader br4 = new BufferedReader(new FileReader("data/My_redirect_ID_2"));
		while( (line=br4.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
		   
		}
		
		br4.close();
		

		
		HashSet<String> queryNotTagged = new HashSet<String>();
//		BufferedReader br6 = new BufferedReader(new FileReader("query_not_tagged.txt"));
		BufferedReader br6 = new BufferedReader(new FileReader("data/query_not_tagged.txt"));
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

//		
//		String presenceFile = (T == setType.TRAINING) ? "data/presence_cat-target_training.txt"
				

		BufferedReader br9 = new BufferedReader(new FileReader("data/presence_cat-target_training.txt"));
		while ((line = br9.readLine()) != null ){

				String[] aux = br9.readLine().split("\t");

				ArrayList<Boolean> presences = new ArrayList<Boolean>();
				for (int presenceIdx = 0; presenceIdx < aux.length ; presenceIdx++ )
					presences.add(aux[presenceIdx].equals("1"));			
				
				trainingLabels.put(line, presences);
				

		}
		br9.close();

		
		BufferedReader br10 = new BufferedReader(new FileReader("data/presence_cat-target_validation.txt"));
		while ((line = br10.readLine()) != null ){

				String[] aux = br10.readLine().split("\t");

				ArrayList<Boolean> presences = new ArrayList<Boolean>();
				for (int presenceIdx = 0; presenceIdx < aux.length ; presenceIdx++ )
					presences.add(aux[presenceIdx].equals("1"));					

				developmentLabels.put(line, presences);
		}		
		br10.close();		
		
		
		
		
		
		Iterator<JSONObject> i = jsonTraining.iterator();
		
		
		for(int countExamples = 0; i.hasNext() ; ){
			JSONObject q = i.next();
			String query = (String) q.get("query");

			if (queryNotTagged.contains(query))
				continue;
			
			
//			training[countExamples][0] = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> entities = new ArrayList<Integer>();
			
			ArrayList<Long> tags = (ArrayList<Long>) q.get("tags");
			training[countExamples][0] = new int[tags.size()][];
			for (int ii = 0; ii < tags.size(); ii++) {
				entities.add(tags.get(ii).intValue());
//				training[countExamples][0].add(getCategoriesOfEntity(id.intValue()));
				training[countExamples][0][ii] = getCategoriesOfEntity(tags.get(ii).intValue());
			}
			
//			 = aux; //new int[aux.size()][];
			
			
			
//			training[countExamples][1] = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> topk = getTopKSimilarEntity2(entities);
			training[countExamples][1] = new int[topk.size()][];
			for (int ii = 0; ii < topk.size(); ii++) {

//				training[countExamples][1].add(getCategoriesOfEntity(id));
				training[countExamples][1][ii]= getCategoriesOfEntity(topk.get(ii));
			}
//			training[countExamples][1] = aux;
			countExamples++;
		}
		

		i = jsonValidation.iterator();
		for(int countExamples = 0; i.hasNext() ; ){
			JSONObject q = i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
			
//			
			
//			development[countExamples][0] = new ArrayList<ArrayList<Integer>>();
//			ArrayList<int[]> aux = new ArrayList<int[]>();
			ArrayList<Integer> entities = new ArrayList<Integer>();

			ArrayList<Long> tags = (ArrayList<Long>) q.get("tags");
			development[countExamples][0] = new int[tags.size()][];
			for (int ii = 0; ii < tags.size(); ii++) {
				entities.add(tags.get(ii).intValue());
//				aux.get(0).add(getCategoriesByTag(id.intValue()));
//				development[countExamples][0].add(getCategoriesOfEntity(id.intValue()));
				development[countExamples][0][ii] = getCategoriesOfEntity(tags.get(ii).intValue());
			}
//			 = aux;
			
			
			
//			training[countExamples][1] = new ArrayList<ArrayList<Integer>>();
			ArrayList<Integer> topk = getTopKSimilarEntity2(entities);
			development[countExamples][1]= new int[topk.size()][];
			for (int ii = 0; ii < topk.size(); ii++) {
//			development[countExamples][1] = new ArrayList<ArrayList<Integer>>();
//			for (Integer id : getTopKSimilarEntity2(entities)){
//				development[countExamples][1].add(getCategoriesOfEntity(id));
				development[countExamples][1][ii] = getCategoriesOfEntity(topk.get(ii));
			}
//			 = aux;
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
	
	
	public int[] getCategoriesOfEntity(Integer tag){
		
		ArrayList<Integer> cats= new ArrayList<Integer>();

		try {
			cats.addAll(articles.get(tag));
		} catch (Exception e) {
			try {
				cats.addAll(articles.get(redirects.get(tag)));
			} catch (Exception e1) {

			}
			
		}
		
		int[] aux = new int[cats.size()];
		for (int i=0; i< cats.size() ; i++)
			aux[i]=cats.get(i);
		
		return aux;
		
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
	

	private ArrayList<Integer> getTopKSimilarEntity2(ArrayList<Integer> entities) {
		ArrayList<ArrayList<SPair>> aux = new ArrayList<ArrayList<SPair>>();
		

		for (Integer id : entities) {
			try {
				aux.add((jaccardIndexes.get(id))); 
				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (aux.size()==0)
			return  new ArrayList<Integer>();
			
		int[] indexes = new int[aux.size()];
		
		ArrayList<SPair> ris= new ArrayList<SPair>(topK); 	
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
		
	
			ArrayList<Integer> risID = new ArrayList<Integer>(topK); 
		for (int k=0; k< ris.size();k++)
			risID.add(ris.get(k).id);
		
		
		
		return risID;
	}




}
