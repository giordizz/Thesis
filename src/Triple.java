
public class Triple<T1,T2,T3> {
	public T1 left;
	public T2 middle;
	public T3 right;
	
	public Triple(T1 l,T2 m,T3 r) {
		left=l;
		middle=m;
		right=r;
	}
	
	public T1 getLeft() {
		return left;
	}
	public T2 getMiddle() {
		return middle;
	}
	public T3 getRight() {
		return right;
	}
	
}
