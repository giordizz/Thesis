

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class RisalitaCounting {
	
	Map<Integer,Vector<Integer>> articles = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Vector<Integer>> categories = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
	
	int maxHeight;
	Vector<Integer> main_topic;
	JSONArray json;
	Vector<Integer> intermediate_categories= new Vector<Integer> ();
	Vector<String> queryNotTagged = new Vector<String>();
	String setName;
	PrintWriter output;
	
	RisalitaCounting(int height,String setName) throws FileNotFoundException, UnsupportedEncodingException{
		maxHeight = height;
		this.setName = setName;
		output = new PrintWriter("../Thesis/data/results_"+ setName  +".txt"  , "UTF-8");
		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
		main_topic = new Vector<Integer>(Arrays.asList(aux));
	}
	
	public void getStatus() throws IOException{
		GZIPInputStream gzip2=null;
		GZIPInputStream	gzip1=null;
		
		gzip1 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));

		BufferedReader br1 = new BufferedReader(new InputStreamReader(gzip1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(gzip2));


		String riga="";
		
		while((riga=br1.readLine())!=null) {
			final String[] s=riga.split("\t");
			Vector<Integer> cur = articles.get(Integer.parseInt(s[0]));
			if (cur==null) 
				articles.put(Integer.parseInt(s[0]),new Vector<Integer>(){{add(Integer.parseInt(s[1]));}});
			 else {
				cur.add(Integer.parseInt(s[1]));
				articles.put(Integer.parseInt(s[0]),cur);

			}
		}

		while((riga=br2.readLine())!=null) {
			final String[] s=riga.split("\t");
			Vector<Integer> cur = categories.get(Integer.parseInt(s[0]));
			if (cur==null) 
				categories.put(Integer.parseInt(s[0]),new Vector<Integer>(){{add(Integer.parseInt(s[1]));}});
			else {
				cur.add(Integer.parseInt(s[1]));
				categories.put(Integer.parseInt(s[0]),cur);
			}
		}		
		
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"_set_tagged.JSON")));
		
		
		BufferedReader reader2 = new BufferedReader(new FileReader("../Download/My_redirect_ID_2"));
		String line;
		while( (line=reader2.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
		   
		}
		
		reader2.close();
		
		
		//TODO
		BufferedReader br3 = new BufferedReader(new FileReader("../data/AllCategoriesSorted.txt"));
//		BufferedReader br3 = new BufferedReader(new FileReader("../data/CatIntermadiateID"));
		while( (riga=br3.readLine())!=null) 
		     intermediate_categories.add(Integer.parseInt(riga));
		     			
		br3.close();
		
		BufferedReader br4 = new BufferedReader(new FileReader("query_not_tagged.txt"));
		while( (riga=br4.readLine())!=null) 
		     queryNotTagged.add(riga);
		     			
		br4.close();
		
		System.err.println("************** Status loaded! **************");
	}
	
	
	
	
	public Vector<Integer> checkVisited(Integer cat, Vector<Integer> visited){
		Vector<Integer> aux=new  Vector<Integer>();
		try{
		
			for (Integer c : this.categories.get(cat)) {
				if (!visited.contains(c))
					aux.add(c);
			}
	} catch(Exception e) {
		
	}
	
			
			
			return aux;
		
		
	}
	
	
	
	public Vector<Integer> climb(Vector<Integer> categories, Vector<Integer> result,Vector<Integer> visited,int height){
		if (categories.isEmpty()|| height > maxHeight )
				return result;

		Vector<Integer> toExplore= new Vector<Integer>();;

		
	
		for(Integer cat: categories) {

			if (!main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,visited));
			
//			if (intermediate_categories.contains(cat)) {
//				TODO
				if (!result.contains(cat))
					result.add(cat);

//			}
				
		}
	
		
		
		return climb(toExplore,result,categories,height+1);
		
		
		
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
	
	
	
	
	public Vector<Integer> tag(String query,Integer tags){
		
		Vector<Integer> aux= getCategoriesByTag(tags);

		return climb(aux,new Vector<Integer>(),new Vector<Integer>(),1);
		
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
	
	public void tagAll(){
		System.err.println("************** Start tagging! **************");
		Iterator i =json.iterator();
		int count=1;
		
		HashMap<Integer,Integer> result = new HashMap<Integer,Integer>(); ;

		
		while( i.hasNext()){
			JSONObject q = (JSONObject) i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
//			
			
			JSONArray aux = (JSONArray) q.get("tags");
			int numOfEntity = aux.size();

			result.clear();
			for (int i1=0;i1<numOfEntity;i1++){ 
				Integer id = (Integer) ( (Long)aux.get(i1)).intValue();
				
				/*
				 * itero sui resultati
				 */
				 for (Integer cat: tag(query, id)){
						Integer curr_count =result.get(cat);
						if (curr_count == null)
							result.put(cat,1);
						else
							result.put(cat, curr_count+1);
							
				 }
			}
			//System.err.println("stampo i risultati");

			writeResultOnFile(result);

			
			
			
			System.err.println("Tagged #" + count++);
		}
		
		
		output.close();
		System.err.println("results written on  -> ../Thesis/data/results_"+ setName  +".txt");
	}
	


	public static void main (String args[]) throws IOException{

		
			
			
			if (args.length!=2) {
				System.err.println("Error: set type or height not specified");
				return;
			}
			String set=args[0];

			System.err.println("----------------------> "+ set);
			
			RisalitaCounting r = new RisalitaCounting(Integer.parseInt(args[1]),set);
			
			r.getStatus();

			r.tagAll();
			

	
	}
}
