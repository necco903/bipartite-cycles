

public class Result {

	BipartiteGraph graph; // get edges from this

	int edgesExamined; // edges looked at at all during search
	int edgesTraversed; // edges put on cycles
	
	int [] dummiesPerCycle; 
	int [] cycleLength;

	int numSearches;

	
	public Result(BipartiteGraph graph) {
		this.graph = graph;
		
		dummiesPerCycle = new int [graph.n]; // at most n cycles
		cycleLength = new int[graph.n]; 
	}

	public Result() {
		
	}
	
	/*private double largestCycleDummyRatio() {
		
		double largestRatio = 0;
		for (int i = 0; i < graph.n; i++) {
			int length = cycleLength[i];
			int dummies = dummiesPerCycle[i];
			if (length != 0 && dummies != 0) { // if we have a cycle here
				if (((double) length)/dummies > largestRatio) {
					largestRatio = ((double) length)/dummies;
				}
			}
		}
		
		return largestRatio;
	}*/
 	
 	/*
	public double edgesExaminedRatio() {
		int edges = graph.n*(graph.d+1);
		return ((double)edgesExamined)/edges;
	}*/

	// This will be a single row in the outfile file
	public String toString() {
		int edges = graph.n*(graph.d); // include dummies
		return 
		//((double)edgesTraversed)/edges + "," + 
		((double)edgesExamined)/edges +"";
	}

/*
	public int maxTimesEdgeExamined() {

		int max = 0;

		for (int i = 0; i < graph.n; i++) {
			for (Edge e : graph.adjLists.get(i)) {
				if (e.timesExamined > max) {
					max = e.timesExamined;
				}
			}
		}
		return max;
	}*/

}
