import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Queue;
import java.lang.Exception;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;

// Note: searches should not make any changes to the graph except reshuffle the adjacency lists
public class AdaptiveSearch extends Search {

	/*BipartiteGraph graph;
	SearchComparator leftComp;
	SearchComparator rightComp;
	
	List<Integer> available;
	Predecessor [] pred;
	boolean [] onCycle; // whether each vertex has been on the cycle yet
	Result result;*/
	int searchNum = 1;
	//int end;
	
	/*public class Predecessor {
		int searchNum;
		Edge edge;
		
		public Predecessor(Edge edge) {
			searchNum = 0;
			this.edge = edge;
		}
		public void update(Edge edge) {
			this.edge = edge;
			searchNum++;
		}
	}
	
	public class VertexEdge {
		int vertex;
		int edgeIndex;
		
		public VertexEdge(int vertex, int edgeIndex) {
			this.vertex = vertex;
			this.edgeIndex = edgeIndex;
		}
	}*/

 	public AdaptiveSearch(BipartiteGraph graph, SearchComparator left, SearchComparator right) {
		/*this.graph = graph;
		available = new ArrayList<Integer>();
		leftComp = left;
		rightComp = right;
		pred = new Predecessor[graph.n*2];
		onCycle = new boolean[graph.n];
		this.result = new Result(graph);
		
		leftComp.search = this;
		rightComp.search = this;
		
		for (int i = 0; i < pred.length; i++) {
			pred[i] = new Predecessor(null);
		}
		for (int i = 0; i < graph.n; i++) {
			available.add(i);
		}*/
		super(graph, left, right);
		
	}
	
	public Result search() {
		StringBuilder output = new StringBuilder();
		output.append("New search!\n");
		
		while (available.size() > 0) {
			
			
			end = available.remove(0);
			
			for (int i = 0; i < graph.n; i++) { // left
				Collections.sort(graph.adjLists.get(i), leftComp);
			}
			
			for (int i = graph.n; i < graph.n*2; i++) {
				Collections.sort(graph.adjLists.get(i), rightComp);
			}
			
			output.append("Available: " + end + "\n");
			Edge dummy = graph.adjLists.get(end).getDummy();
			
			// Only use these for dfs
			//pred[dummy.right].searchNum = searchNum;
			//pred[dummy.right].edge = dummy;
			
			bfs(dummy.right, end, output);
			
			if (pred[end].searchNum < searchNum) { // we did not find a cycle
	            System.err.println("No cycle found...");
	            // Dump information 
	            System.err.println(graph);
	            System.err.println(output.toString());
	            return null;
	        }
			
			// Trace out the cycles that we found
			// If an edge is a dummy, we remove its vertex from available
			int left = end;
			int right;
			Edge edge;
			while (true) {
				edge = pred[left].edge;
				edge.direction--; // taken from right to left
				edge.twin.direction--;
				right = edge.right;
				onCycle[right-graph.n] = true;
				output.append("Tracing cycle: edge = " + edge + "\n");

				edge = pred[right].edge;
				edge.direction++;
				if (!edge.isDummy) {
					edge.twin.direction++;
				}
				left = edge.left;
				output.append("Tracing cycle: edge = " + edge + "\n");
				
				// if the edge is a dummy
				if (edge.isDummy) {
					// Remove this node from available
					Integer toRemove = null;
					for (Integer vertex : available) {
						if (vertex == left) {
							toRemove = vertex;
						}
					}
					available.remove(toRemove);
					
					result.dummiesPerCycle[searchNum-1]++;
					
				}
				result.cycleLength[searchNum-1] += 2;
	            result.edgesTraversed += 2;
	            
	            if (left == end)
	                break;
			}
			
			searchNum++;
		}		
		//System.err.println(output.toString());
	    //System.err.println(graph);

		return result;
	}
	
	public void dfs(int start, int end, StringBuilder output) {
		
		output.append("New dfs\n");
		Stack<VertexEdge> stack = new Stack<VertexEdge>();
		stack.push(new VertexEdge(start, 0));
		
		while (true) {
			if (stack.isEmpty()) {
				return;
			}
			VertexEdge popped = stack.pop();
			int vertex = popped.vertex;
			int edgeIndex = popped.edgeIndex;
			
			BipartiteGraph.AdjacencyList adjList = graph.adjLists.get(vertex);
			Edge edge = adjList.get(popped.edgeIndex);
			output.append("Popped: " + vertex + " Edge index: " + popped.edgeIndex + " Edge: " + edge + "\n");

			result.edgesExamined++;

			// We try not to push anything to the stack that is illegal
			// - we've reached the end of the list
			// - we're on the right and have reached negative values
			// - we're on the left and have reached positive values
			int nextIndex = edgeIndex+1;
			if (nextIndex < adjList.size()) { // if we're not at the end of a list 
				if ((vertex < graph.n && adjList.get(nextIndex).direction <= 0) ||
						(vertex >= graph.n && adjList.get(nextIndex).direction >= 0)) {  // on the left and we've reached positive values
					stack.push(new VertexEdge(vertex, nextIndex));
					output.append("Pushed: vertex = " + vertex + " index = " + nextIndex+"\n");
				}			
			}
	
			// Skip the dummy if we've already taken it
			if (edge.isDummy && edge.direction != 0) continue;
			
			
			int opposite;
			if (vertex < graph.n) {
				opposite = edge.right;
			} else {
				opposite = edge.left;
			}
			
			if (pred[opposite].searchNum < searchNum) { // we haven't seen it in this search yet
				result.edgesExaminedUnvisited++;
				pred[opposite].searchNum = searchNum;
				pred[opposite].edge = edge;
				
				if (opposite == end) {
					return;
				} else {
					// Note that this push might have a dummy that is taken
					// On the right, this will be an option
					stack.push(new VertexEdge(opposite, 0));
					output.append("Pushed: vertex = " + opposite + " index = 0\n");
				}
				
			}
		}
	}

	public void bfs(int start, int end, StringBuilder output) {

		// Don't even use the start part

		output.append("New bfs\n");

		Queue<VertexEdge> queue = new LinkedList<VertexEdge>();
		queue.add(new VertexEdge(end, 0)); // push the dummy 
		result.edgesExamined++;

		while (true) {

			if (queue.isEmpty()) {
				return;
			}

			VertexEdge popped = queue.remove();
			int vertex = popped.vertex;
			int edgeIndex = popped.edgeIndex;
			
			BipartiteGraph.AdjacencyList adjList = graph.adjLists.get(vertex);
			Edge edge = adjList.get(popped.edgeIndex);
			output.append("Popped: " + vertex + " Edge index: " + popped.edgeIndex + " Edge: " + edge + "\n");
	
			// Skip the dummy if we've already taken it
			if (edge.isDummy && edge.direction != 0) {
				continue;
			}
			
			int opposite;
			if (vertex < graph.n) {
				opposite = edge.right;
			} else {
				opposite = edge.left;
			}

			if (pred[opposite].searchNum < searchNum) {
				pred[opposite].searchNum = searchNum;
				pred[opposite].edge = edge;

				if (opposite == end) {
					return;
				} 

				// Push all of the children of this new vertex
				adjList = graph.adjLists.get(opposite);
				// We try not to push anything to the stack that is illegal
				// - we've reached the end of the list
				// - we're on the right and have reached negative values
				// - we're on the left and have reached positive values
				int nextIndex = 0;
				while (nextIndex < adjList.size()) { // if we're not at the end of a list 

					if ((opposite < graph.n && adjList.get(nextIndex).direction <= 0) ||
							(opposite >= graph.n && adjList.get(nextIndex).direction >= 0)) {  // on the left and we've reached positive values
						queue.add(new VertexEdge(opposite, nextIndex));
						result.edgesExamined++;
						output.append("Pushed: vertex = " + opposite + " index = " + nextIndex+"\n");
					}	
					nextIndex++;

				}


			}
		}
	}
	
	public static void main(String [] args) {
		
		if (args.length < 3) {
			System.err.println("Wrong number of parameters.");
			return;
		}
		
		// Take in the range that we want
		int startN = Integer.parseInt(args[0]);
		int endN = Integer.parseInt(args[1]);

		String docName = args[2];
		
		// Open the output file
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(docName+startN+ "-" + endN+".txt", "UTF-8");

		} catch (Exception e) {
			System.err.println("File not found...");
		}

		if (writer != null) {
			for (int N = startN; N < endN; N+=5) { 
				System.err.println("N = " + N);
				for (int i = 0; i < 1000; i++) {
					int [] rightSides = BipartiteGraph.getRightSides(N, 3);

					BipartiteGraph graph1 = new BipartiteGraph(N, 3, rightSides, false);
					Result result1 = (new AdaptiveSearch(graph1, new DummyZero(), new HomeAvailableZero())).search();
					
					writer.println(result1);
					
				}
			}
			writer.close();

		}

		/*PrintWriter writer = null;
		try{
			writer = new PrintWriter("testresults.txt", "UTF-8");

		} catch (Exception e) {
			System.err.println("Could not create writer.");
		}

		if (writer != null) {
			
			try {
				File file = new File(args[0]);

				FileInputStream fis = new FileInputStream(file);
	    		byte[] data = new byte[(int)file.length()];
	    		fis.read(data);
	    		fis.close();
	        	String string = new String(data, "UTF-8");
	        	
				BipartiteGraph graph1 = BipartiteGraph.getGraphFromString(string);

				Result result1 = (new Search(graph1, new DummyZero(), new AvailableZero())).search();

				writer.println(result1);

				for (int i = 0; i < result1.dummiesPerCycle.length; i++) {
					if (result1.cycleLength[i] != 0) {
						writer.println("Cycle length-dummies:" + result1.cycleLength[i] + "-" + result1.dummiesPerCycle[i]);
					}
				}

			} catch (Exception e) {
				System.err.println("File IO error");

			}

    	
			writer.close();
		
		}*/

		
	
	}
}
