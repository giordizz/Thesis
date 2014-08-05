package it.giordizz.Thesis;

public class Result implements Comparable<Result> {
	
	Integer categoryID;
	float avgF1;
	
	public Result(Integer category, float F1) {
		categoryID = category;
		avgF1 = F1;
		
	}

	@Override
	public int compareTo(Result o) {		
		return -Float.compare(avgF1, o.avgF1);
	}		
	
	
	
	
}
