

public class Edge {
		
	public static final int DIRECTED_R = -1;
	public static final int NOT_DIRECTED = 0;
	public static final int DIRECTED_L = 1;

	int left;
	int right;
	boolean isDummy;
	int direction; // +-1
	Edge twin;
	EdgeNode node;

	boolean visited;
	int timesExamined;
	int timesVisited;
	int searchNum; // last search the edge was visited in?
	int directed; // using constants above
	
	public Edge(int left, int right, boolean isDummy) {
		this.left = left;
		this.right = right;
		this.isDummy = isDummy;
	}
	
	public boolean sameEdge(Edge otherEdge) {
		return (this.left == otherEdge.left && this.right == otherEdge.right);
	}
	// Basic toString function.
	public String toString() {
		String ret = left + "," + right + " dir " + direction + " directed " + directed;
		if (isDummy) {
			ret += " dummy";
		}
		return ret;
	}
	
}
