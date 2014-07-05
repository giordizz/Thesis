

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Risalita {
	
	Map<String,Vector<String>> articles = new HashMap<String, Vector<String>>();
	Map<String,Vector<String>> categories = new HashMap<String, Vector<String>>();
	Map<String,String> redirects = new HashMap<String, String>();
	
	
	Vector<String> main_topic;
	JSONArray json;
	Vector<String> special_categories= new Vector<String> ();
	
	//int failed=0,faileddd=0;
	Vector<Integer > artfailed= new Vector<Integer >();
	HashSet<String> catfailed= new HashSet<String>();
	
	Risalita(){
		String[] aux= new String[]{"694871","4892515","694861","3103170","693800","751381","693555","1004110","1784082","8017451","691928","690747","692348","696603","691008","695027","42958664","691182","693708","696648"};
		main_topic = new Vector<String>(Arrays.asList(aux));
	}
	
	public void getStatus() throws IOException{
		GZIPInputStream gzip2=null;
		GZIPInputStream	gzip1=null;
		
		gzip1 = new GZIPInputStream(new FileInputStream("dbpedia/wiki-article-categories-links-sorted.gz"));
		gzip2 = new GZIPInputStream(new FileInputStream("dbpedia/wiki-categories-only-links-sorted.gz"));

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
		
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("test_set_tagged.JSON")));
		
		
		BufferedReader reader2 = new BufferedReader(new FileReader("Download/My_redirect_ID_2"));
		String line;
		while( (line=reader2.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(s[0],s[1]);
		     //TODO
		}
		
		reader2.close();
		
		
		
		BufferedReader reader = new BufferedReader(new FileReader("CatIntermadiateID"));
		String line1;
		while( (line1=reader.readLine())!=null) {
		     special_categories.add(line1);
		     //TODO
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
	//	System.err.println("Errore!");
		//catfailed.add(cat);
		//System.err.println(cat);
		//faileddd++;
		
	}
	
			
			
			return aux;
		
		
	}
	
	public Vector<String> climb(Vector<String> categories, Vector<String> result,Vector<String> visited,int height){
		if (categories.isEmpty()|| height>9)
				return result;

		Vector<String> toExplore= new Vector<String>();;

		
	
		for(String cat: categories) {
	//		System.out.println("Curr cat " + cat);
			if (!main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,visited));
			
			if (special_categories.contains(cat)) {
//				Integer curr_count =result.get(cat);
//				if (curr_count == null)
				if (!result.contains(cat))
					result.add(cat);
//				else
//					result.put(cat, curr_count+1);
			}
				
		}
	
		
		
		return climb(toExplore,result,categories,++height);
		
		
		
	}	
	
	public Vector<String> getCategoriesByTags(Vector<Integer> tags){
		Vector<String> cats= new Vector<String>();
		
		for (Integer tag: tags) {
			//System.out.println("ID -> " + tag.toString());
			try {
				cats.addAll(articles.get(tag.toString()));
			} catch (Exception e) {
				try {
					cats.addAll(articles.get(redirects.get(tag.toString())));
				} catch (Exception e1) {
				
					//artfailed.add(tag);
					//failed++;
				}
				
			}
		}
		return cats;
		
		
	}
	
	


public Vector<String> getCategoriesByTag(Integer tag){
	Vector<String> cats= new Vector<String>();
	

		//System.out.println("ID -> " + tag.toString());
		try {
			cats.addAll(articles.get(tag.toString()));
		} catch (Exception e) {
			try {
				cats.addAll(articles.get(redirects.get(tag.toString())));
			} catch (Exception e1) {
			
				//artfailed.add(tag);
				
			//	failed++;
			}
			
		}
	
	return cats;
	
	
}
	
	
	
	
	public Vector<String> tag(String query,Integer tags){
		
		Vector<String> aux= getCategoriesByTag(tags);
		//System.out.println(aux.toString());
		
	//	HashMap<String, Integer> result= climb(aux,new HashMap<String, Integer>(),new Vector<String>(),0);
	//	System.out.println(query + "\n" + result.toString());
		return climb(aux,new Vector<String>(),new Vector<String>(),0);
		
	}	
	
	public void tagAll(){
		System.err.println("************** Start tagging! **************");
		Iterator i =json.iterator();
		int count=1;
		
		HashMap<String,Integer> result = new HashMap<String,Integer>(); ;

		
		while( i.hasNext()){
			JSONObject q = (JSONObject) i.next();
			//q.get("query");
			int len = ((JSONArray) q.get("tags")).size();
			//Vector<Integer> vv=new Vector<Integer>();
			JSONArray aux = (JSONArray) q.get("tags");
			
			//result = new HashMap<String,Integer>();
			result.clear();
			for (int i1=0;i1<len;i1++){ 
				Integer id = (Integer) ( (Long)aux.get(i1)).intValue();
				 for (String cat: tag((String) q.get("query"), id)){
						Integer curr_count =result.get(cat);
						if (curr_count == null)
							result.put(cat,1);
						else
							result.put(cat, curr_count+1);
							
				 }
			}
			  
			//System.out.println( (String) q.get("query")  + "\n" + result);
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
//		GZIPInputStream gzip1=null,gzip2=null;
//		//Vector<String> v =new  Vector<String>();
//

//		try {
//			gzip1 = new GZIPInputStream(new FileInputStream("dbpedia/wiki-article-categories-links-sorted.gz"));
//			gzip2 = new GZIPInputStream(new FileInputStream("dbpedia/wiki-categories-only-links-sorted.gz"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		BufferedReader br1 = new BufferedReader(new InputStreamReader(gzip1));
//		BufferedReader br2 = new BufferedReader(new InputStreamReader(gzip2));
//
//		try {
//			String riga="";
//			int i =0;
//			
//			while((riga=br1.readLine())!=null) {
//				final String[] s=riga.split("\t");
//				Vector<String> cur = articles.get(s[0]);
//				if (cur==null) {
////					if(s[0].equals("12"))
////						System.out.println(s[0] + " " + new Vector<String>(){{add(s[1]);}});
//					articles.put(s[0],new Vector<String>(){{add(s[1]);}});
//				} else {
//					cur.add(s[1]);
//					articles.put(s[0],cur);
////					if(s[0].equals("12"))
////						System.out.println(s[0] + " " + new Vector<String>(){{add(s[1]);}});
//				}
//			}
//	
//			while((riga=br2.readLine())!=null) {
//				final String[] s=riga.split("\t");
//				Vector<String> cur = categories.get(s[0]);
//				if (cur==null) {
////					if(s[0].equals("12"))
////						System.out.println(s[0] + " " + new Vector<String>(){{add(s[1]);}});
//					categories.put(s[0],new Vector<String>(){{add(s[1]);}});
//				} else {
//					cur.add(s[1]);
//					categories.put(s[0],cur);
////					if(s[0].equals("12"))
////						System.out.println(s[0] + " " + new Vector<String>(){{add(s[1]);}});
//				}
//			}			
//		JSONArray data= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("800Tagged.JSON")));
		//	JSONArray dropped = (JSONArray)data.get("dropped");
			
		
			Risalita r = new Risalita();
			r.getStatus();
//			
//			BufferedReader reader = new BufferedReader(new FileReader("WikiCat-ID.txt"));
//			
//			while( (reader.readLine())!=null) {
//			     r.special_categories.add(reader.readLine());
//			}
//			
//			reader.close();

			//r.tag("prova",prova );
			r.tagAll();
			
	//		System.err.println("articoli falliti " + r.failed);
			
//			System.err.println("categorie fallite " + r.catfailed.size());
//			System.err.println("elenco categorie fallite " + r.catfailed);
			
//			 Collections.sort(r.artfailed);
//			System.out.println(r.artfailed.toString());
			
			
			
		//	System.out.println("categorie fallite " + r.faileddd);
////			i =0;
////			
////			while((riga=br.readLine())!=null) {
////				i++;
////				
////			}System.out.println(i);
////			br.reset();
////			
////			i =0;
////			
////			while((riga=br.readLine())!=null) {
////				i++;
////				
////			}System.out.println(i);
////			br.reset();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	
	}
}
