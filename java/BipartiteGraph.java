
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Regular bipartite graph that supports dummy edges
 */
public class BipartiteGraph {

	int n;
	int d;
	List<List<Edge>> dfsEdges;
	List<AdjacentEdges> adjEdges;
	int [] rightSides;
	
	// Must fill in the edges immediately after
	public BipartiteGraph(int n, int d) {
		this.n = n;
		this.d = d;

		dfsEdges = new ArrayList<List<Edge>>();
		adjEdges = new ArrayList<AdjacentEdges>();

		for (int i = 0; i < n*2; i++) {
			dfsEdges.add(new ArrayList<Edge>());
			adjEdges.add(new AdjacentEdges((i<n)));
		}
	}

	public BipartiteGraph(int n, int d, int [] rightSides, boolean addDummies) {
		dfsEdges = new ArrayList<List<Edge>>();
		adjEdges = new ArrayList<AdjacentEdges>();
		this.n = n;
		this.d = d;
		this.rightSides = rightSides;
		
		for (int i = 0; i < n*2; i++) {
			dfsEdges.add(new ArrayList<Edge>());
			adjEdges.add(new AdjacentEdges((i<n)));
		}
		
		addRightSides(rightSides);

		if (addDummies) {
			addDummyEdges();
		}
		
	}

	/*
	public BipartiteGraph(int n, int d, List<AdjacentEdges> adjLists, List<List<Edge>> dfsEdges) {

		this.n = n;
		this.d = d;
		this.adjEdges = adjLists;
		this.dfsEdges = dfsEdges;
	}*/


	// Assumes that the first edges in the adj lists of the left vertices are dummy edges
	public static BipartiteGraph getGraphFromString(String graphString) {

		Scanner in = new Scanner(graphString);

		int n = Integer.parseInt(in.nextLine());
		int d = Integer.parseInt(in.nextLine());

		System.err.println(n+","+d);
		int [] rightSides = new int[n*d];
		int index = 0;

		while (in.hasNextLine()) {

			String nextLine = in.nextLine();

			String [] split = nextLine.split(":");
			int vertex = Integer.parseInt(split[0]);
			
			String [] edges  = split[1].split(";");
			for (int i=1; i <=d; i++) { // skip dummy?

				// Get the right vertex in each edge
				int right = Integer.parseInt(edges[i].split(",")[1]);
				rightSides[index] = right;
				index++;
			}					

		}
		in.close();
		
		return new BipartiteGraph(n, d, rightSides, true);

	}
	
	/**
	 * These only appear on the left adjacency lists
	 */
	public void addDummyEdges() {
		for (int i = 0; i < n; i++) {
			dfsEdges.get(i).add(new Edge(i, i+n, true));
		}
		this.d++;
	}

	// assumes that dummies are there
	// remove dummies from the left adjacency lists
	public void removeDummyEdges() {
		for (int i = 0; i < n; i++) {
			Edge dummy = null;
			for (Edge e : dfsEdges.get(i)) {
				if (e.isDummy) {
					dummy = e;
				}
			}
			if (dummy != null) {
				dfsEdges.get(i).remove(dummy);

			}
		}
		this.d--;
	}

	// We need to make every edge in dfs edges unvisited
	public void resetEdges() {
		for (int i = 0; i < n; i++) {

			EdgeNode noDir = adjEdges.get(i).noDir;

			for (Edge e : dfsEdges.get(i)) {
				e.direction = 0;
				e.twin.direction = 0;
				e.visited = false;
				e.twin.visited = false;
				e.directed = Edge.NOT_DIRECTED;
				e.twin.directed = Edge.NOT_DIRECTED;
				e.searchNum = 0;
				e.twin.searchNum = 0;

				// we also need to splice this edge and put it in nodir
				// same with twin
				AdjacentEdges.spliceEdge(e.node);
				AdjacentEdges.spliceEdge(e.node.twin);

				AdjacentEdges.insertEdge(noDir, e.node);
				AdjacentEdges.insertEdge(adjEdges.get(e.right).noDir, e.node.twin);

			}

			// look in adjedges
			/*moveToNoDir(adjEdges.get(i).dirR);
			moveToNoDir(adjEdges.get(i).dirL);
			moveToNoDir(adjEdges.get(i).takenR);
			moveToNoDir(adjEdges.get(i).takenL);*/

		}

	}

	// Takes the head of a given adjlist and moves all edges to nodir 
	/*private void moveToNoDir(EdgeNode head) {
		EdgeNode noDir1;
		EdgeNode noDir2;

		while (head.next != null) {
			edgenode = head.next;

			// splice the edges out of this list
			AdjacentEdges.spliceEdge(edgenode);
			AdjacentEdges.spliceEdge(edgenode.twin);

			noDir1 = adjEdges.get(edgenode.edge.left).noDir;
			noDir2 = adjEdges.get(edgenode.edge.right).noDir;

			// insert edges
			AdjacentEdges.insertEdge(noDir1, edgenode);
			AdjacentEdges.insertEdge(noDir2, edgenode.twin);
		}
	} */


	public static int [] getRightSides(int n, int d) {
		
		int [] rightSides = new int[n*d];
		
		// Every vertex appears d times
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < d; j++) {
				rightSides[i*d+j] = i + n;
			}
		}
		
		// shuffle
		for (int i = n*d-1; i> 1; i--) {
			int temp = rightSides[i];
			int r = (int) (Math.random()*i);
			rightSides[i] = rightSides[r];
			rightSides[r] = temp;
		}
				
		return rightSides;
		
	}
	
	// Assumes that if it has one dummy, it has all
	public boolean hasDummies() {

		for (Edge e : dfsEdges.get(0)) {
			if (e.isDummy) {
				return true;
			}
		}

		return false;
	}

	// These edges are not dummies
	public void addRightSides(int [] rightSides) {
		
		
		for (int i = 0; i < n ;i++) {
			for (int j = 0; j < d; j++) {
				int opposite =  rightSides[i*d+j];

				Edge leftCopy = new Edge(i, opposite, false);
				Edge rightCopy = new Edge(i, opposite, false);
				
				leftCopy.twin = rightCopy;
				rightCopy.twin = leftCopy;
				
				dfsEdges.get(i).add(leftCopy);
				dfsEdges.get(opposite).add(rightCopy);	

				// Insert into buckets
				EdgeNode node1 = AdjacentEdges.insertEdge(adjEdges.get(i).noDir, leftCopy); // left side
				EdgeNode node2 = AdjacentEdges.insertEdge(adjEdges.get(opposite).noDir, rightCopy);
				node1.twin = node2;
				node2.twin = node1;

				leftCopy.node = node1;
				rightCopy.node = node2;

			}
		}
		
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(n + "\n");
		sb.append(d + "\n");
		
		for (int i = 0; i < dfsEdges.size(); i++) {
			sb.append(i + ":");
			
			String prefix = "";
			for (Edge edge : dfsEdges.get(i)) {
				sb.append(prefix);
				prefix = ";";
				sb.append(edge);
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}

	
	/*
	 * Create some example graphs
	 */
	public static void main(String [] args) {

		int [] rightSides = getRightSides(4, 3);
		BipartiteGraph graph1 = new BipartiteGraph(4, 3, rightSides, true);
		System.out.println(graph1);
		
		BipartiteGraph graph2 = new BipartiteGraph(4, 3, rightSides, false);
		System.out.println(graph2);
		
	}
}
