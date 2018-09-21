

import java.util.List;
import java.io.PrintWriter;

public class EdgeColor {

	// We assume that graphs coming in are reset - edge have no directions...

	public static void color(BipartiteGraph graph, Result result, SearchComparator originalleft,
		SearchComparator originalright) {

		System.err.println("Coloring ");
		System.err.println(graph);

		OriginalSearch original;
		GetStuckSearch getstuck;

		// Graphs that we split into 
		BipartiteGraph new1;
		BipartiteGraph new2;

		if (graph.d == 1) { // base case
			// We are done
			// Color these edges all one color - we don't really have to do this
			return;
		}

		// Recurse
		if (graph.d%2 == 0 && graph.hasDummies()) { // should be the same problem...
			if (graph.d == 4) { // we want to do getstuck 
				// remove the dummies and add new dummies
				graph.removeDummyEdges();
				getstuck = new GetStuckSearch(graph, result);
				getstuck.makeCycles(); 

			} else { // do original
				original = new OriginalSearch(graph, result, originalleft, originalright);
				original.search();
			}

		} else if (graph.d%2 != 0 && graph.hasDummies()) { // trace cycles normally 
			
			graph.removeDummyEdges();

		} else if (graph.d%2 != 0) { // no dummies odd degree, add dummies and recurse
			
			if (graph.d == 3) { // we do get stuck 
				getstuck = new GetStuckSearch(graph, result);
				getstuck.makeCycles();
			} else {
				graph.addDummyEdges(); // adds them straight across
				original = new OriginalSearch(graph, result, originalleft, originalright);
				original.search();
			}
	
		}

		traceRemainingCycles(graph);

		System.err.println(graph);
		// Now, the graph has all of its edges directed correctly, and result has accumulated more stats
		new1 = new BipartiteGraph(graph.n, graph.d/2);
		new2 = new BipartiteGraph(graph.n, graph.d/2);
		splitGraph(graph, new1, new2); 
		color(new1, result, originalleft, originalright);
		color(new2, result, originalleft, originalright);

	}

	// Split the original graph into two new ones with reset edges
	// all (+) edges go to new1, all (-) edges go to new2
	public static void splitGraph(BipartiteGraph original, BipartiteGraph new1, BipartiteGraph new2) {


		for (int i = 0; i < original.n; i++) {
			List<Edge> edges = original.dfsEdges.get(i);
			for (Edge e : edges) {

				if (e.direction == 1) {
					// put in new1
					new1.dfsEdges.get(e.left).add(e);
					new1.dfsEdges.get(e.right).add(e.twin);

					// put into adjEdges of new graph, not sure we even have to splice...
					AdjacentEdges.spliceEdge(e.node);
					AdjacentEdges.spliceEdge(e.node.twin);

					AdjacentEdges.insertEdge(new1.adjEdges.get(e.left).noDir, e.node);
					AdjacentEdges.insertEdge(new1.adjEdges.get(e.right).noDir, e.node.twin);

				} else { // direction = -1
					// put in new2
					new2.dfsEdges.get(e.left).add(e);
					new2.dfsEdges.get(e.right).add(e.twin);

					// put into adjEdges of new graph, not sure we even have to splice...
					AdjacentEdges.spliceEdge(e.node);
					AdjacentEdges.spliceEdge(e.node.twin);

					AdjacentEdges.insertEdge(new2.adjEdges.get(e.left).noDir, e.node);
					AdjacentEdges.insertEdge(new2.adjEdges.get(e.right).noDir, e.node.twin);

				}
			}

		}

		new1.resetEdges();
		new2.resetEdges();
	}

	// All edges with direction 0 can be traced
	public static void traceRemainingCycles(BipartiteGraph graph) {
		// Go down entire left side and walk on any edges that have not been taken
		for (int i = 0; i < graph.n; i++) {
			walk(graph, i);
		}
	}

	// We start walking from left until we are back at left and can no longer leave
	public static void walk(BipartiteGraph graph, int left) {

		int right = 0;

		while (true) {

			// go from left to right, can get stuck
			boolean found = false;
			for (Edge e : graph.dfsEdges.get(left)) {
				if (e.direction == 0) {
					e.direction++;
					e.twin.direction++;
					right = e.right;
					found = true;
					break;
				}
			}

			if (!found) {
				return;
			}

			// right to left, cant get stuck
			for (Edge e : graph.dfsEdges.get(right)) {
				if (e.direction == 0) {
					e.direction--;
					e.twin.direction--;
					left = e.left;
					break;
				}
			}
		}

	}

	// Let's test this out
	public static void main(String args []) {

		if (args.length < 3) {
			System.err.println("Wrong number of parameters.");
			return;
		}

		int startN = Integer.parseInt(args[0]);
		int endN = Integer.parseInt(args[1]);
		int degree = Integer.parseInt(args[2]);
		int iterations = Integer.parseInt(args[3]);
		int increment = Integer.parseInt(args[4]);
		String docName = args[5];

		// Open the output file
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(docName+startN+ "-" + endN+".txt", "UTF-8");

		} catch (Exception e) {
			System.err.println("File not found...");
		}

		int numSearch = 0;
		int total = 0;
		if (writer != null) {
			for (int N = startN; N <= endN; N+=increment) { 
				System.err.println("N = " + N);
				for (int i = 0; i < iterations; i++) {
					System.err.println(i);
					int [] rightSides = BipartiteGraph.getRightSides(N, degree);
					BipartiteGraph graph = new BipartiteGraph(N, degree, rightSides, false);

					//System.err.println("New graph: " + graph2.toString());
						
					Result result = new Result(graph);
					SearchComparator originalleft = new DummyZero();
					SearchComparator originalright = new HomeAvailableZero();

					color(graph, result, originalleft, originalright);

					writer.println((graph.n*(graph.d)) + "," + result);

				}
			}
			writer.close();
		}
	}
}