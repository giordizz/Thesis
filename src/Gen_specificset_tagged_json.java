

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

public class Gen_specificset_tagged_json {



	
	Vector<String> set= new Vector<String> ();;
	JSONArray json;
	String setname;

	public Gen_specificset_tagged_json(String s) {
		setname=s;
		
		
	}
	public void getStatus() throws IOException{
	
		
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("tagged911.JSON")));
		
		
		BufferedReader reader2 = new BufferedReader(new FileReader("dataset/"+ setname +"_set"));
		String line;
		while( (line=reader2.readLine())!=null) {
			
			set.add(line);
			
		     //TODO
		}

		reader2.close();
		
		
		
		
		go();
		System.err.println("************** Status loaded! **************");
	}

	


	
	
	
	public void go(){
		System.err.println("************** Start tagging! **************");
		Iterator i =json.iterator();
		int count=0;
		String f = "[";
		while( i.hasNext()){
			JSONObject q = (JSONObject) i.next();
			if ( set.contains(q.get("query").toString())) {
					f+=q.toJSONString() + ",";
				//	System.out.println(q.get("query").toString());
					count++;
			}
			
			//System.out.println(aux.toJSONString());
//			for (int i1=0;i1<len;i1++){ 
//				Integer[] id = (Integer[]) ( aux.get(i1));
//							
//				
//			}

		}
		
		  
	System.out.println(f.subSequence(0, f.length()-1) + "]");


		
		
		
		System.err.println("computed #" + count++);	
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
			
		
			Gen_specificset_tagged_json r = new Gen_specificset_tagged_json(args[0]);
			r.getStatus();
//			
//			BufferedReader reader = new BufferedReader(new FileReader("WikiCat-ID.txt"));
//			
//			while( (reader.readLine())!=null) {
//			     r.special_categories.add(reader.readLine());
//			}
//			
//			reader.close();
			
			
//			System.out.println("articoli falliti " + r.failed);
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


