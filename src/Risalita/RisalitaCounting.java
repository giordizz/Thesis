

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class RisalitaCounting {
	
	Map<Integer,Vector<Integer>> articles = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Vector<Integer>> categories = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Vector<Integer>> jaccardIndexes = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
	
	int maxHeight;
	HashSet<Integer> main_topic;
	JSONArray json;
	HashSet<Integer> intermediate_categories= new HashSet<Integer> ();
	HashSet<String> queryNotTagged = new HashSet<String>();
	String setName;
	PrintWriter output;
	int topK;
	
	RisalitaCounting(int height,String setName, int topK) throws FileNotFoundException, UnsupportedEncodingException{
		maxHeight = height;
		this.setName = setName;
		this.topK = topK;
		output = new PrintWriter("../Thesis/data/results_"+ setName  +".txt"  , "UTF-8");
		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
		main_topic = new HashSet<Integer>(Arrays.asList(aux));
	}
	
	public void getStatus(int topK) throws IOException{
		
		GZIPInputStream	gzip1=null, gzip2=null, gzip3=null;
		
		gzip1 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));
		gzip3 = new GZIPInputStream(new FileInputStream("../dbpedia/jaccardFile.txt.gz"));
		
		
		
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
//				articles.put(Integer.parseInt(s[0]),cur);

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
//				categories.put(Integer.parseInt(s[0]),cur);
			}
		}	
		br2.close();
		

		if (topK>0) 
			while((line=br3.readLine())!=null) {
				Integer id = Integer.parseInt(line);
				final String[] s=br3.readLine().split("\t");
	
					
				Vector<Integer> aux = new Vector<Integer>();
				
				for (int topI=0; topI < Math.min(s.length,topK) ; topI++ )
					aux.add(Integer.parseInt(s[topI]));
				jaccardIndexes.put(id, aux);
	
			}			
		br3.close();
		
		
		
		
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"_set_tagged.JSON")));
		
		
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
		
		BufferedReader br6 = new BufferedReader(new FileReader("query_not_tagged.txt"));
		while( (line=br6.readLine())!=null) 
		     queryNotTagged.add(line);
		     			
		br6.close();
		
		System.err.println("************** Status loaded! **************");
	}
	
	
	
	
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
	
	
	
	public HashSet<Integer> climb(Vector<Integer> categories, HashSet<Integer> result,HashSet<Integer> visited,int height){
		if (categories.isEmpty()|| height > maxHeight )
				return result;

		Vector<Integer> toExplore= new Vector<Integer>();;

		
	
		for(Integer cat: categories) {

			if (!main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,visited));
			
			if (intermediate_categories.contains(cat)) {
//				TODO
//				if (!result.contains(cat))
					result.add(cat);

			}
				
		}
		
		visited.addAll(categories);
		
		
		return climb(toExplore,result,visited,height+1);
		
		
		
	}	
	
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
	
	


	public Vector<Integer> getCategoriesByTag(Integer tag){
	
		Vector<Integer> cats= new Vector<Integer>();

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
	
	
	
	
	public HashSet<Integer> tag(String query,Integer tags){
		
//		Vector<Integer> aux= getCategoriesByTag(tags);

		return climb(getCategoriesByTag(tags),new HashSet<Integer>(),new HashSet<Integer>(),1);
		
	}	
	
	
//	private void printResult(HashMap<String, Integer> result, float numOfEntity) {
//		String aux="";
//		boolean first=true;
//		for (String speccat: intermediate_categories){
//			Integer count =result.get(speccat);
//			if (!first)
//					aux+=",";
//			else
//				first=false;
//			if (count==null)
//					aux+="0";
//			else {
//				float norm = (float) count / 10;
//				aux+=norm;
//			}
//				
//		}
//		
//		System.out.println(aux);
//		
//	}
	
	private void printResult(HashMap<Integer, Integer> result) {
		String aux="";
		boolean first=true;
		for (Integer speccat: intermediate_categories){
			Integer ii = result.get(speccat);
			if (!first)
					aux+=",";
			else
				first=false;
			if (ii==null)
					aux+="0";
			else
				aux+=ii;
		}
		
		System.out.println(aux);
		
	}

	
	private void writeResultOnFile(HashMap<Integer, Integer> result) {
	
		boolean first=true;
		for (Integer speccat: intermediate_categories){
			Integer ii = result.get(speccat);
			if (!first)
					output.append(",");
			else
				first=false;
			if (ii==null)
				output.append("0");
			else
				output.print(ii);
		}
		output.println();
		//System.out.println(aux);
		
	}
	private void writeResultOnFile(HashMap<Integer, Integer> result1, HashMap<Integer, Integer> result2) {
		
		boolean first=true;
		for (Integer speccat: intermediate_categories){
			Integer ii = result1.get(speccat);
			if (!first)
					output.append(",");
			else
				first=false;
			if (ii==null)
				output.append("0");
			else
				output.print(ii);
		}
		for (Integer speccat: intermediate_categories){
			Integer ii = result2.get(speccat);
			if (!first)
					output.append(",");
			else
				first=false;
			if (ii==null)
				output.append("0");
			else
				output.print(ii);
		}
		output.println();
		//System.out.println(aux);
		
	}
	public void tagAll(){
		System.err.println("************** Start tagging! **************");
		Iterator<JSONObject> i =json.iterator();
		int count=1;
		
		HashMap<Integer,Integer> result1 = new HashMap<Integer,Integer>(); 
		HashMap<Integer,Integer> result2 = new HashMap<Integer,Integer>(); 
		
		while( i.hasNext()){
			JSONObject q = i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
			
			
			ArrayList<Integer> entities = new ArrayList<Integer>();
				
			for (Long id : (ArrayList<Long>) q.get("tags"))
				entities.add(id.intValue());
			
//			System.err.println("prima " + entities.size());
			
			ArrayList<Integer> topKs = getTopKSimilarEntity(entities);
			
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
			writeResultOnFile(result1, result2);
			
			
			
			System.err.println("Tagged #" + count++);
		}
		
		
		output.close();

		System.err.println("results written on  -> ../Thesis/data/results_"+ setName  +".txt");
	}
	


	private ArrayList<Integer> getTopKSimilarEntity(ArrayList<Integer> entities) {
//		int numOfEntity = entities.size();
//		for (int indexEntity = 0; indexEntity < numOfEntity ; indexEntity++ )

		ArrayList<Integer> aux= new ArrayList<Integer>(); 
		for (Integer id : entities)
			try {
//				System.err.println("ok");
//				for (Integer topI :)
				aux.addAll(jaccardIndexes.get(id));
//				entities.addAll( jaccardIndexes.get( ((Long) entities.get(indexEntity)).intValue() ));
				
			} catch (Exception e) {
//				e.printStackTrace();
//				System.err.println("errore "+ id );
			}
			

		
		return aux;
	}

	public static void main (String args[]) throws IOException{

		
			if (args.length==2) {
				
				String set=args[0];

				System.err.println("----------------------> "+ set);
				for  (int topK=17;topK<18; topK+=2){
					
					
					RisalitaCounting r = new RisalitaCounting(Integer.parseInt(args[1]),set,topK);
					
					r.getStatus(topK);

					r.tagAll();
					System.err.println(" ----------------------- topk = " + topK + "  ----------------------- ");
				}
				
				System.exit(0);
			}
			
			if (args.length!=3) {
				System.err.println("Error: set type, height or topK not specified");
				System.exit(1);;
			}
			String set=args[0];

			System.err.println("----------------------> "+ set);
			
			RisalitaCounting r = new RisalitaCounting(Integer.parseInt(args[1]),set,Integer.parseInt(args[2]));
			
			r.getStatus(Integer.parseInt(args[2]));

			r.tagAll();
//			
//			JSONArray json = (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("C:\\Users\\giordano\\Desktop\\Python\\JSONs\\validation_set_tagged.JSON")));
//			JSONObject ar = (JSONObject) json.get(2);
//			ArrayList<Integer> v = (ArrayList<Integer>) ar.get("tags");
//			System.out.println(v);
	
	}
}
