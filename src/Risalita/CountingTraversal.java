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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * Effettua la risalita del grafo delle categorie di Wikipedia per ogni entity associata a ciascuna query del dataset specificato in input.
 *  Genera un file contenente tante righe quante sono le query del dataset; ogni riga contiene 122 feature separate da virgole.
 * @author Giordano Galanti
 *
 */
public class CountingTraversal {
	
	
	public class SPair {
		Integer id;
		Float value;
		
		public SPair(Integer id, Float value) {
			this.id=id;
			this.value=value;
		}

		public SPair() {
			id=0;
			value=0f;
		}
		
		
	}
	
	
	
	
	
	/** Map degli article to categories di Wikipedia */
	Map<Integer,Vector<Integer>> articles = new HashMap<Integer, Vector<Integer>>();
	/** Map delle categories to super-categories di Wikipedia */
	Map<Integer,Vector<Integer>> categories = new HashMap<Integer, Vector<Integer>>();
	/** Per ogni articolo di Wikipedia una lista di coppie (ID pagina Wiki, valore Jaccard similarity), ordina nell'ordine non crescente di Jaccard sim. */
	Map<Integer,Vector<SPair>> jaccardIndexes = new HashMap<Integer, Vector<SPair>>();
	/** Map dei redirects*/
	Map<Integer,Integer> redirects = new HashMap<Integer, Integer>();
	
	/** Orizzonte della visita del grafo*/
	int maxHeight;
	/** ID categorie in main topic classifications*/
	HashSet<Integer> main_topic;
	/** JSON contenente per ogni query l'id delle entity associate alla query */
	JSONArray json;
	/** ID delle bridge categories */
	TreeSet<Integer> bridge_categories= new TreeSet<Integer> ();
	/** Query senza entity */
	HashSet<String> queryNotTagged = new HashSet<String>();
	/** Il tipo di dataset per cui si vogliono generare le feature. */
	String setName;
	PrintWriter output;

	
	CountingTraversal(int height,String setName) throws FileNotFoundException, UnsupportedEncodingException{
		maxHeight = height;
		this.setName = setName;
		Integer[] aux= new Integer[]{694871,4892515,694861,3103170,693800,751381,693555,1004110,1784082,8017451,691928,690747,692348,696603,691008,695027,42958664,691182,693708,696648};
		main_topic = new HashSet<Integer>(Arrays.asList(aux));
	}
	
	/**
	 * Carica tutti i dati in memoria, tra cui il grafo delle categorie, i redirects ecc...
	 * @param topK quante entity (con maggior valore di jaccard similarity) considerare per "l'arricchimento".
	 * @throws IOException
	 */
	public void getStatus(int topK) throws IOException{
		
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
				articles.put(Integer.parseInt(s[0]),new Vector<Integer>(){/**
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
			Vector<Integer> cur = categories.get(Integer.parseInt(s[0]));
			if (cur==null) 
				categories.put(Integer.parseInt(s[0]),new Vector<Integer>(){/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

				{add(Integer.parseInt(s[1]));}});
			else {
				cur.add(Integer.parseInt(s[1]));
			}
		}	
		br2.close();
		
	
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
		
//		json = (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("PipeLine/data/"+setName+"_entitiesSnippets.JSON")));
//		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"_set_tagged.JSON")));
//		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"v2_set_tagged.JSON")));
//		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"R_set_tagged.JSON")));
		json= (JSONArray)JSONValue.parse(new InputStreamReader(new FileInputStream("../data/"+setName+"P_set_tagged.JSON")));
		
		BufferedReader br4 = new BufferedReader(new FileReader("../Download/My_redirect_ID_2"));
		while( (line=br4.readLine())!=null) {
			final String[] s=line.split("\t");
		     redirects.put(Integer.parseInt(s[0]),Integer.parseInt(s[1]));
		   
		}		
		br4.close();
		
		
		BufferedReader br5 = new BufferedReader(new FileReader("../data/new_intermediateCatSorted"));
		while( (line=br5.readLine())!=null) 
		     bridge_categories.add(Integer.parseInt(line));		
		br5.close();
		
		BufferedReader br6 = new BufferedReader(new FileReader("query_not_tagged.txt"));
		while( (line=br6.readLine())!=null) 
		     queryNotTagged.add(line);
		     			
		br6.close();
		
		System.err.println("************** Status loaded! **************");
	}
	
	
	
	/** 
	 * Aggiunge la nuove categorie da visitare valutando se sono già state visitate.
	 * @param cat ID della categoria da espandere
	 * @param visited insieme degli ID delle categorie già visitate
	 * @return
	 */
	public Vector<Integer> checkVisited(Integer cat, HashSet<Integer> visited){
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
	
	
	/** 
	 * Visita del grafo per le categorie di appartenenza dell'entity corrente.
	 * @param categories categorie correntemente da visitare
	 * @param result accumulatore degli ID delle bridge category incontrate
	 * @param visited ID delle categorie visitate 
	 * @param height altezza corrente
	 * @return ID delle bridge category incontrate
	 */
	public HashSet<Integer> climb(HashSet<Integer> categories, HashSet<Integer> result,HashSet<Integer> visited,int height){
		
		if (categories.isEmpty()|| height > maxHeight )
				return result;

		HashSet<Integer> toExplore= new HashSet<Integer>();;

		
		for(Integer cat: categories) {

			if (!main_topic.contains(cat))
				toExplore.addAll(checkVisited(cat,visited));
			
			if (bridge_categories.contains(cat)) 
					result.add(cat);
							
		}
		

		visited.addAll(toExplore);

		return climb(toExplore,result,visited,height+1);
		
		
		
	}	
	/**
	 * Non più usato
	 * @param tags
	 * @return
	 */
	public Vector<Integer> getCategories(Vector<Integer> entities){
		Vector<Integer> cats= new Vector<Integer>();
		
		for (Integer tag: entities) {

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
	
	

	/**
	 * Resistuisce le categorie di appartenenza per l'entity specificata
	 * @param entity ID dell'entity di cui si vogliono recuperare le categorie di Wikipedia 
	 * @return ID delle categorie di Wikipedia a cui l'entity appartiene
	 */
	public HashSet<Integer> getCategories(Integer entity){
	
		HashSet<Integer> cats= new HashSet<Integer>();

		try {
			cats.addAll(articles.get(entity));
		} catch (Exception e) {
			try {
				cats.addAll(articles.get(redirects.get(entity)));
			} catch (Exception e1) {

			}
			
		}
	
		return cats;
		
	}
	
	
	
	/**
	 * Effettua la visita BFS del grafo delle categorie a cui l'entity specificata appartiene
	 * @param entity
	 * @return
	 */
	public HashSet<Integer> upWardsTraversal(Integer entity){
		
		HashSet<Integer> entityCategories= getCategories(entity);
		return climb(entityCategories,new HashSet<Integer>(),new HashSet<Integer>(entityCategories),1);
		
	}	
	
	

	/**
	 * Stampa i risultati per la query corrente, piuttosto di scriverli su file 
	 * @param result coppie (bridge category ID, valore della feature)
	 */
	public void printResult(HashMap<Integer, Integer> result) {
		String aux="";
		boolean first=true;
		for (Integer speccat: bridge_categories){
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


	/**
	 * Scrive i risultati su file per la query corrente. In particolare scrive 122+122 feature. 
	 * @param result1 coppie (bridge category ID, valore della feature) per le entity di partenza
	 * @param result2 coppie (bridge category ID, valore della feature) per le entity trovate con Jaccard.
	 */	
	private void writeResultOnFile(HashMap<Integer, Integer> result1, HashMap<Integer, Integer> result2) {
		
		boolean first=true;
		for (Integer speccat: bridge_categories){
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
		for (Integer speccat: bridge_categories){
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

		
	}
	
	/**
	 * Genera le feature con l'approccio "entity" ovvero quello che parte dalle entity recuperate interrogando SMAPH.
	 * @param topK K most similar entities
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public void generateFeatures(int topK) throws FileNotFoundException, UnsupportedEncodingException{
		
		String outFile = "../ThesisP/data/entityP_Features/results_"+ setName  +".txt";
		output = new PrintWriter(outFile, "UTF-8");
		
		System.err.println("************** Start tagging! **************");
		Iterator<JSONObject> i =json.iterator();
		int count=1;
		
		HashMap<Integer,Integer> result1 = new HashMap<Integer,Integer>(); 
		HashMap<Integer,Integer> result2 = new HashMap<Integer,Integer>(); 
		
		while( i.hasNext()){
			
			JSONObject q = i.next();
//			String query = (String) q.get("query");
			
				
//			if (queryNotTagged.contains(query))
//				continue;
			
			
			ArrayList<Integer> entities = new ArrayList<Integer>();
				
			for (Long id : (ArrayList<Long>) q.get("tags"))
				entities.add(id.intValue());
			

			ArrayList<Integer> topKs = getTopKMostSimilarEntities(entities, topK);
			

			result1.clear();
			result2.clear();

			for (Integer id : entities){

				/**
				 * itero sui resultati della visita del grafo delle categorie a partire dalle entity iniziali.
				 */
				 for (Integer cat: upWardsTraversal(id)){
						Integer curr_count =result1.get(cat);
						if (curr_count == null)
							result1.put(cat,1);
						else
							result1.put(cat, curr_count+1);
							
						
				 }
			}

			for (Integer id : topKs){

				/**
				 * itero sui resultati della visita del grafo delle categorie a partire dalle entity calcolate con Jaccard.
				 */
				 for (Integer cat: upWardsTraversal(id)){
						Integer curr_count =result2.get(cat);
						if (curr_count == null)
							result2.put(cat,1);
						else
							result2.put(cat, curr_count+1);
							
				 }
			}
			

			writeResultOnFile(result1, result2);

			System.err.println("Tagged #" + count++);
		}
	
		
		output.close();

		System.err.println("results written on  -> " + outFile);
	}
	



	/**
	 * Genera le feature con l'approccio "entitySnippets" ovvero quello che parte dalle entity estratte dagli snippets delle pagine risultate interrogando Bing con le query del dataset.
	 * @param topK K most similar entities
	 * @param firstN numero di entity da considerare (le entity recuperate dagli snippets sono molto maggiori di quelle recuperate con SMAPH; possibilià di pruning.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void generateFeatures(int topK, int firstN) throws FileNotFoundException, UnsupportedEncodingException{
		System.err.println("************** Start tagging! **************");
		
		String ss = "../Thesis/data/entitySnippets_Features/results_"+ setName  +".txt";
		output = new PrintWriter(ss, "UTF-8");

		HashMap<Integer,Integer> result1 = new HashMap<Integer,Integer>(); 
		HashMap<Integer,Integer> result2 = new HashMap<Integer,Integer>(); 

		
		int count=1;
		for (int idx=0; idx < json.size() ; idx++ ){
//			JSONObject q1 = (JSONObject) json.get(idx);
			JSONObject q2 = (JSONObject) json.get(idx);

			ArrayList<Integer> entities1 = new ArrayList<Integer>();
	
			for (Object query1:q2.entrySet())	{

				ArrayList<Long> ents = (ArrayList<Long>) ((Map.Entry) query1).getValue();
				System.err.println(((Map.Entry) query1).getKey());
				HashSet<Integer> set = new HashSet<Integer>();
				for (Long ent:ents)
					set.add(ent.intValue());
				
				if (firstN==-1)
					firstN=set.size();
				else
					firstN=Math.min(firstN, set.size());
				
				if (firstN==0)
					System.err.println("ERRORE!");
					
			
				Iterator<Integer> it = set.iterator();
				for (int firstI=0; firstI< firstN; firstI++)
					entities1.add(it.next());
				
				
			}


			ArrayList<Integer> topKs1 = getTopKMostSimilarEntities(entities1, topK);
			
			result1.clear();
			result2.clear();

			
			for (Integer id : entities1){

				/*
				 * itero sui resultati
				 */
				 for (Integer cat: upWardsTraversal(id)){
						Integer curr_count =result1.get(cat);
						if (curr_count == null)
							result1.put(cat,1);
						else
							result1.put(cat, curr_count+1);
							
				 }
			}
			
			
			for (Integer id : topKs1){

				/*
				 * itero sui resultati
				 */
				 for (Integer cat: upWardsTraversal(id)){
						Integer curr_count =result2.get(cat);
						if (curr_count == null)
							result2.put(cat,1);
						else
							result2.put(cat, curr_count+1);
							
				 }
			}
			
			

			writeResultOnFile(result1, result2);
			
				
			
			System.err.println("Tagged #" + count++);
		}
		
		output.close();	


	}
	
	/**
	 * Restiuisce le K entity con valore maggiore di Jaccard similarity rispetto alle entity di partenza.
	 * @param entities entity di partenza 
	 * @param topK K
	 * @return ID delle topK entity 
	 */
	private ArrayList<Integer> getTopKMostSimilarEntities(ArrayList<Integer> entities, int topK) {

		
		ArrayList<List<SPair>> aux = new ArrayList<List<SPair>>();
		
		

		for (Integer id : entities) 
			try {
				aux.add(jaccardIndexes.get(id));
			} catch (Exception e) {

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
	
	
	public static void main (String args[]) throws IOException{

		
			if (args.length==2) {
				
				String set=args[0];
				int K = 21;
				System.err.println("----------------------> "+ set);
				
				CountingTraversal r = new CountingTraversal(Integer.parseInt(args[1]),set);

				r.getStatus(K);
				
				r.generateFeatures(K);

				
			} else
				System.err.println("Error in specifying arguments!");

	}

}
