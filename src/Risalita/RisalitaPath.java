
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;



public class RisalitaPath {
	
	Map<String,Vector<String>> articles = new HashMap<String, Vector<String>>();
	Map<String,Vector<String>> categories = new HashMap<String, Vector<String>>();
	Map<String,String> redirects = new HashMap<String, String>();
	
	
	int maxHeight = 10;
	Vector<String> main_topic;
	JSONArray json;
	Vector<String> special_categories= new Vector<String> ();
	
	
	RisalitaPath(){
		String[] aux= new String[]{"694871","4892515","694861","3103170","693800","751381","693555","1004110","1784082","8017451","691928","690747","692348","696603","691008","695027","42958664","691182","693708","696648"};
		main_topic = new Vector<String>(Arrays.asList(aux));
	}
	
	public void getStatus(String setName) throws IOException{
		GZIPInputStream gzip2=null;
		GZIPInputStream	gzip1=null;
		
		gzip1 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));

		BufferedReader br1 = new BufferedReader(new InputStreamReader(gzip1));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(gzip2));


		String riga="";
		
		while((riga=br1.readLine())!=null) {
			final String[] s=riga.split("\t");
			Vector<String> cur = articles.get(s[0]);
			if (cur==null) 
				articles.put(s[0],new Vector<String>(){{add(s[1]);}});
			 else {
				cur.add(s[1]);
				articles.put(s[0],cur);

			}
		}

		while((riga=br2.readLine())!=null) {
			final String[] s=riga.split("\t");
			Vector<String> cur = categories.get(s[0]);
			if (cur==null) 
				categories.put(s[0],new Vector<String>(){{add(s[1]);}});
			else {
				cur.add(s[1]);
				categories.put(s[0],cur);
			}
		}		
		
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"_set_tagged.JSON")));
		
		/** ADD ONLY LAST RECORD REDIRECT */
		BufferedReader reader2 = new BufferedReader(new FileReader("../Download/My_redirect_ID_2"));
		String line;
		while( (line=reader2.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(s[0],s[1]);
		     //TODO
		}
		
		reader2.close();
		
		/** ADD ALL REDIRECTS */
//		while( (line=reader2.readLine())!=null) {
//			final String[] s=line.split("\t");
//			Vector<String> cur =redirects.get(s[0]);
//			if (cur==null) 
//				redirects.put(s[0],new Vector<String>(){{add(s[1]);}});
//			else {
//				cur.add(s[1]);
//				redirects.put(s[0],cur);
//			}
//		     //TODO
//		}
		
		
		

		
		reader2.close();
		
		
		
		BufferedReader reader = new BufferedReader(new FileReader("../data/CatIntermadiateID"));
		String line1;
		while( (line1=reader.readLine())!=null) {
		     special_categories.add(line1);
		    
		}
		
		reader.close();
		
		
		System.err.println("************** Status loaded! **************");
	}
	
	
	
	
	public Vector<String> checkVisited(String cat, Vector<String> visited){
		Vector<String> aux=new  Vector<String>();
		try{
		
			for (String c : this.categories.get(cat)) {
				if (!visited.contains(c))
					aux.add(c);
			}
	} catch(Exception e) {

		
	}
	
			return aux;
		
		
	}
	
	
	
	
	public Vector<Pair<String, Integer>> climb(Vector<String> categories, Vector<Pair<String, Integer>> result,Vector<String> visited,int height){
		if (categories.isEmpty()|| height > maxHeight){
			
				visited.clear();
				return result;
		}
		Vector<String> toExplore= new Vector<String>();;

		for(String cat: categories) {

			if (!main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,visited));

			
			if (special_categories.contains(cat)) {

				Pair<String,Integer> p = new Pair<String,Integer>(cat,height);
				if (!result.contains(p))
					result.add(p);

			}
				
		}
	
		visited.addAll(categories);
		
		return climb(toExplore,result,visited,height+1);
		
		
		
	}	
	

	


	public Vector<String> getCategoriesByTag(Integer tag){
		Vector<String> cats= new Vector<String>();
	
		try {
			cats.addAll(articles.get(tag.toString()));
		} catch (Exception e) {
			try {
				cats.addAll(articles.get(redirects.get(tag.toString())));
			} catch (Exception e1) {

			}
			
		}
	
	return cats;
	
	
}
	
	
	
	
	public Vector<Pair<String, Integer>> tag(String query,Integer tags){
		
		Vector<String> aux= getCategoriesByTag(tags);

		return climb(aux,new Vector<Pair<String,Integer>>(),new Vector<String>(),1);
		
	}	
	
	public void tagAll(){
		System.err.println("************** Start tagging! **************");
		Iterator i =json.iterator();
		int count=1;
		
		HashMap<String,Integer> result = new HashMap<String,Integer>();

		
		while( i.hasNext()){
			JSONObject q = (JSONObject) i.next();
			JSONArray aux = (JSONArray) q.get("tags");
			int len = aux.size();

			result.clear();
			Vector<Pair<String, Integer>> part_ris;
			for (int i1=0;i1<len;i1++){ 
				Integer id = (Integer) ( (Long)aux.get(i1)).intValue();
				part_ris = tag((String) q.get("query"), id);
				 for (Pair<String,Integer> p: part_ris){
						Integer curr_path_length =result.get(p.first);
						if (curr_path_length == null)
							result.put(p.first,p.second);
						else {
							if (curr_path_length> p.second)
								result.put(p.first, p.second);
						}						
				 }
				 part_ris.clear();
			}
			  

			printResult(result);

			
			
			
			System.err.println("Tagged #" + count++);
		}
		
		
		
	}
	
	private void printResult(HashMap<String, Integer> result) {
		String aux="";
		boolean first=true;
		for (String speccat: special_categories){
			Integer ii =result.get(speccat);
			if (!first)
					aux+=",";
			else
				first=false;
			if (ii==null)
					aux+="0";
			else
				aux+=ii.toString();
		}
		
		System.out.println(aux);
		
	}

	public static void main (String args[]) throws IOException{
	
			if (args.length!=1) {
				System.err.println("Error: specify the set type!");
				return;
			}
			String set=args[0];
		
			RisalitaPath r = new RisalitaPath();
			
			System.err.println("----------------------> "+ set);
			r.getStatus(set);

			r.tagAll();

	
	}
}


