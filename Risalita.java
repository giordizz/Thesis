

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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

	Vector<String> main_topic;
	JSONArray json;
	Vector<String> special_categories= new Vector<String> ();
	
	int failed=0,faileddd=0;
	Vector<Integer > artfailed= new Vector<Integer >();
	Risalita(){
		String[] aux= new String[]{"694871","4892515","694861","3103170","693800","751381","693555","1004110","1784082","8017451","691928","690747","692348","696603","691008","695027","42958664","691182","693708","696648"};
		main_topic = new Vector<String>(Arrays.asList(aux));
	}
	
	public void getStatus() throws IOException{
		GZIPInputStream gzip1=null,gzip2=null;
		
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
		
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("tagged.JSON")));
		
		BufferedReader reader = new BufferedReader(new FileReader("WikiCat-ID.txt"));
	
		while( (reader.readLine())!=null) {
		     special_categories.add(reader.readLine());
		}
		
		reader.close();
		
		
		System.out.println("status loaded!");
	}
	
	public HashMap<String, Integer> climb(Vector<String> categories, HashMap<String, Integer> result){
		if (categories.isEmpty())
				return result;
		
		Vector<String> toExplore= new Vector<String>();
		
		try {
		for(String cat: categories) {
			if (main_topic.contains(cat))
				toExplore.addAll(this.categories.get(cat));
			
			if (special_categories.contains(cat)) {
				Integer curr_count =result.get(cat);
				if (curr_count == null)
					result.put(cat, 1);
				else
					result.put(cat, curr_count+1);
			}
				
		}
		} catch(Exception e) {
			
			faileddd++;
			
		}
		
		return climb(toExplore,result);
		
		
		
	}	
	
	public Vector<String> getCategoriesByTags(Vector<Integer> tags){
		Vector<String> cats= new Vector<String>();
		
		for (Integer tag: tags) {
			//System.out.println("ID -> " + tag.toString());
			try {
				cats.addAll(articles.get(tag.toString()));
			} catch (Exception e) {
				artfailed.add(tag);
				failed++;
			}
		}
		return cats;
		
		
	}
	
	
	public void tag(String query,Vector<Integer> tags){
		
		HashMap<String, Integer> result= climb(getCategoriesByTags(tags),new HashMap<String, Integer>());
		System.out.println(result.toString());
		
	}	
	
	public void tagAll(){
		Iterator i =json.iterator();
	//	for (JSONObject q : ){
		while( i.hasNext()){
			JSONObject q = (JSONObject) i.next();
			//q.get("query");
			int len = ((JSONArray) q.get("tags")).size();
			Vector<Integer> vv=new Vector<Integer>();
			JSONArray aux = (JSONArray) q.get("tags");
			for (int i1=0;i1<len;i1++){ 
				Integer ing = (Integer) ( (Long)aux.get(i1)).intValue();
				 vv.add( ing  );
			}
			  
		//	System.out.println( (String) q.get("query")  + " " + vv.toString());
			tag((String) q.get("query"), vv);
		}
		
		
		
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
			Vector<Integer> prova = new Vector<Integer>();
			prova.add(35159);
			prova.add(1712068);
			prova.add(9044625);
			//r.tag("prova",prova );
			r.tagAll();
			
			System.out.println("articoli falliti " + r.failed);
			 Collections.sort(r.artfailed);
			System.out.println(r.artfailed.toString());
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
