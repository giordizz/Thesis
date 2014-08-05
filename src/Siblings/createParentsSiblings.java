import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class createParentsSiblings {

	Map<Integer,Vector<Integer>> categories = new HashMap<Integer, Vector<Integer>>();
	Map<Integer,Vector<Integer>> reverse_categories = new HashMap<Integer, Vector<Integer>>();
	Vector<Integer> intermediate_categories= new Vector<Integer> ();

	
	public void getData() throws IOException{
		GZIPInputStream	gzip2=null;
		
		gzip2 = new GZIPInputStream(new FileInputStream("../dbpedia/wiki-categories-only-links-sorted.gz"));
		BufferedReader br2 = new BufferedReader(new InputStreamReader(gzip2));
		
		String riga;
		while((riga=br2.readLine())!=null) {
			final String[] s=riga.split("\t");
			Vector<Integer> cur1 = categories.get(s[0]);
			if (cur1==null) 
				categories.put(Integer.parseInt(s[0]),new Vector<Integer>(){{add(Integer.parseInt(s[1]));}});
			else {
				cur1.add(Integer.parseInt(s[1]));
//				categories.put(s[0],cur1);
			}
			Vector<Integer> cur2 = reverse_categories.get(s[1]);
			if (cur2==null) 
				reverse_categories.put(Integer.parseInt(s[1]),new Vector<Integer>(){{add(Integer.parseInt(s[0]));}});
			else {
				cur2.add(Integer.parseInt(s[0]));
//				reverse_categories.put(s[1],cur2);
			}			
			
		}		
		
		BufferedReader reader = new BufferedReader(new FileReader("../data/CatIntermadiateID"));
		String line1;
		while( (line1=reader.readLine())!=null) {
			intermediate_categories.add(Integer.parseInt(line1));
		     //TODO
		}
		
		reader.close();
		

		
		
	}
	
	
//	public void createFile(){
//		
//		
//		for ( String c: intermediate_categories){
//			System.out.println(c);
//			boolean first = true;
//			
//			Vector<String> sibs = new Vector<String> ();
//			
//			Vector<String> parents = categories.get(c);
//			if (parents == null) {
//				System.out.println("\n");
//				continue;
//			}
//					
//			for ( String p: parents){
//				Vector<String> a = reverse_categories.get(p);
//				if (a!=null)
//					sibs.addAll(a);
//				if (!first)
//					System.out.print("\t" + p);
//				else {
//					System.out.print(p);
//					first= false;
//				}
//			}
//			System.out.println();
//			first = true;
//			
//			Vector<String> siblings = reverse_categories.get(c);
//			
//			if (siblings==null) {
//				System.out.println();
//				continue;
//			}
//			for ( String s: reverse_categories.get(c)){
//				if (!s.equals(c))
//					if (!first)
//						System.out.print("\t" +s);
//					else {
//						System.out.print(s);
//						first= false;
//					}
//			}
//			System.out.println();
//		}
//			
//	}
	
	
	
	public void createSiblingsOfIntermediateCatsFile() throws FileNotFoundException, UnsupportedEncodingException{
		
		HashSet<Integer> siblings = new HashSet<Integer>();
		
		for ( Integer c: intermediate_categories){
//			System.out.println(c);
//			boolean first = true;

			
			Vector<Integer> parents = categories.get(c);
			if (parents == null) {
				continue;
			}
					
			for ( Integer p: parents){
				Vector<Integer> a = reverse_categories.get(p);
				if (a!=null)
					siblings.addAll(a);

			}

		
		}
		
		
		siblings.removeAll(intermediate_categories);
		
		
		PrintWriter writer = new PrintWriter("../data/intermediateCatsSiblings.txt", "UTF-8");
		for (Integer s : siblings)
			writer.println(s);
		writer.close();
		
			
	}
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException{
		
		createParentsSiblings C = new createParentsSiblings();
		try {
			C.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		C.createSiblingsOfIntermediateCatsFile();
		
	}
	
	
	
	
}
