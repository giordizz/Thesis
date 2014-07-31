package it.giordizz.ShortestPath;

//import it.giordizz.Thesis.RisalitaCounting.SPair;

//import RisalitaCounting;

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
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.json.simple.JSONArray;
//import org.json.simple.JSONArray;
//import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class ShortestPath {
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

	Map<Integer,Vector<SPair>> jaccardIndexes = new HashMap<Integer, Vector<SPair>>();
//	Map<Integer,Vector<Integer>> jaccardIndexes = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
	
	UnmodifiableDirectedGraph<Integer, DefaultEdge> directedGraph ;

	int maxHeight;
	HashSet<Integer> main_topic;
	JSONArray json;
	TreeSet<Integer> intermediate_categories= new TreeSet<Integer> ();
	HashSet<String> queryNotTagged = new HashSet<String>();
	String setName;
	PrintWriter output;
	
	
	ShortestPath(int height,String setName) throws FileNotFoundException, UnsupportedEncodingException{
		maxHeight = height;
		this.setName = setName;
//		this.topK = topK;
		
		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
		main_topic = new HashSet<Integer>(Arrays.asList(aux));
	}
	
	public void getStatus(int topK) throws IOException{
		
		GZIPInputStream	gzip1=null, gzip2=null, gzip3=null;
		
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


		 DirectedGraph<Integer, DefaultEdge> auxdirectedGraph =
        new DefaultDirectedGraph<Integer, DefaultEdge>
        (DefaultEdge.class);
		while((line=br2.readLine())!=null) {
			final String[] s=line.split("\t");
			auxdirectedGraph.addVertex(Integer.parseInt(s[0]));
			auxdirectedGraph.addVertex(Integer.parseInt(s[1]));
			auxdirectedGraph.addEdge(Integer.parseInt(s[0]),Integer.parseInt( s[1]));
		}	
		br2.close();
		directedGraph = new UnmodifiableDirectedGraph<Integer, DefaultEdge>(auxdirectedGraph);
		

		
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
	private ArrayList<Integer> getTopKSimilarEntity2(ArrayList<Integer> entities, int topK) {
		
		ArrayList<List<SPair>> aux = new ArrayList<List<SPair>>();
		
		
		int i=0;
		int  err = 0;
		for (Integer id : entities) {
			try {

				Vector<SPair> l = (jaccardIndexes.get(id));
				int toIdx = Math.min(l.size(), topK);
				aux.add(l.subList(0, toIdx)); //TODO .subList(0, topK)

			} catch (Exception e) {

				err++;

			}

			i++;

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
	
	

	
	public void tagAll(int topK) throws FileNotFoundException, UnsupportedEncodingException{
		output = new PrintWriter("../Thesis/data/NEWNEW8top" + topK +"/results_"+ setName  +".txt"  , "UTF-8");
		
		System.err.println("************** Start tagging! **************");
		Iterator<JSONObject> i =json.iterator();
		int count=1;
		

		while( i.hasNext()){
			JSONObject q = i.next();
			String query = (String) q.get("query");
			
			if (queryNotTagged.contains(query))
				continue;
			
			
			ArrayList<Integer> entities = new ArrayList<Integer>();
				
			for (Long id : (ArrayList<Long>) q.get("tags"))
				entities.add(id.intValue());
			

			
			ArrayList<Integer> topKs = getTopKSimilarEntity2(entities, topK);

			writeResultOnFile(entities, topKs);
			
			
			
			System.err.println("Tagged #" + count++);
		}
		
		
		output.close();

		System.err.println("../Thesis/data/NEWNEW8top" + topK +"/results_"+ setName  +".txt");
	}
	


	private void writeResultOnFile(ArrayList<Integer> entities,
		ArrayList<Integer> topKs) {
		int[] ris = new int[246];
		
		
		for (Integer ent: entities) {
			
			Vector<Integer> cats = getCategoriesByTag(ent);

			int catIdx = 0;
			for (Integer c : intermediate_categories){ 
				for (Integer cat: cats)
					try {
//						DijkstraShortestPath<Integer, DefaultEdge> d = new DijkstraShortestPath<Integer, DefaultEdge>(directedGraph, cat, c,maxHeight-1);
//						d.getPathLength();
						BellmanFordShortestPath<Integer, DefaultEdge> d = new BellmanFordShortestPath<Integer, DefaultEdge>(directedGraph, cat,maxHeight-1);
						if (d.getCost( c)<=maxHeight-1) {
							ris[catIdx]++;
							break;
						}
					} catch(Exception e){
						
					}
				catIdx++;
			}
			
		}
			
//		for (Integer ent: topKs) {
//			Vector<Integer> cats = getCategoriesByTag(ent);
//			
//			
//			int catIdx = 123;
//			for (Integer c : intermediate_categories){
//				for (Integer cat: cats)
//					try {
//						if (DijkstraShortestPath.findPathBetween(directedGraph, cat, c).size()<=maxHeight) {
//							ris[catIdx]++;
//							break;
//						}
//					} catch(Exception e){
//						
//					}
//				catIdx++;
//			}
//				 
//		}
	
		boolean first=true;
		for (Integer r: ris){
			if (!first)
					output.append(",");
			else
				first=false;

			output.print(r);
		}
		output.println();
		
	
}

	public static void main (String[ ] args) throws IOException{

		
		if (args.length==2) {
			
			String set=args[0];

			System.err.println("----------------------> "+ set);
			ShortestPath sp = new ShortestPath(Integer.parseInt(args[1]),set);
			sp.getStatus(0);
			
			
				sp.tagAll(1);
				System.err.println(" ----------------------- topk = " + 0 + "  ----------------------- ");

				
			System.exit(0);
		}
		

}

	
}
