import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.lang.Exception;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;

public class OriginalSearch extends Search {

	BipartiteGraph graph;
	SearchComparator leftComp;
	SearchComparator rightComp;
	
	Predecessor [] pred;
	//boolean [] onCycle; // whether each vertex has been on the cycle yet
	Result result;
	int searchNum = 1;
	//int end;
	
 	public OriginalSearch(BipartiteGraph graph, Result result, SearchComparator left, SearchComparator right) {
		this.graph = graph;
		available = new ArrayList<Integer>();
		leftComp = left;
		rightComp = right;
		pred = new Predecessor[graph.n*2];
		this.result = result;
		result.graph = graph;
		
		leftComp.search = this;
		rightComp.search = this;
		
		for (int i = 0; i < pred.length; i++) {
			pred[i] = new Predecessor();
		}
		for (int i = 0; i < graph.n; i++) {
			available.add(i);
		}
		
	}

	public void search() {
		//StringBuilder output = new StringBuilder();
		//output.append("New search!\n");
		
		while (available.size() > 0) {
			
			
			end = available.remove(0);
			
			for (int i = 0; i < graph.n; i++) { // left
				Collections.sort(graph.dfsEdges.get(i), leftComp);
			}
			
			for (int i = graph.n; i < graph.n*2; i++) {
				Collections.sort(graph.dfsEdges.get(i), rightComp);
			}
			
			//output.append("Available: " + end + "\n");
			Edge dummy = graph.dfsEdges.get(end).get(0);
			
			pred[dummy.right].searchNum = searchNum;
			pred[dummy.right].edge = dummy;
			
			dfs(dummy.right, end);
			
			if (pred[end].searchNum < searchNum) { // we did not find a cycle
	            System.err.println("No cycle found...");
	            // Dump information 
	            //System.err.println(graph);
	            //System.err.println(output.toString());
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
				//onCycle[right-graph.n] = true;
				//output.append("Tracing cycle: edge = " + edge + "\n");

				edge = pred[right].edge;
				edge.direction++;
				if (!edge.isDummy) {
					edge.twin.direction++;
				}
				left = edge.left;
				//output.append("Tracing cycle: edge = " + edge + "\n");
				
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
	            result.edgesExamined +=2;
	            
	            if (left == end)
	                break;
			}
			
			searchNum++;
		}		
	}
	
	public void dfs(int start, int end) {
		
		//output.append("New dfs\n");
		Stack<VertexEdge> stack = new Stack<VertexEdge>();
		stack.push(new VertexEdge(start, 0));
		
		while (true) {
			if (stack.isEmpty()) {
				return;
			}
			VertexEdge popped = stack.pop();
			int vertex = popped.vertex;
			int edgeIndex = popped.edgeIndex;
			
			List<Edge> adjList = graph.dfsEdges.get(vertex);
			Edge edge = adjList.get(popped.edgeIndex);
			//output.append("Popped: " + vertex + " Edge index: " + popped.edgeIndex + " Edge: " + edge + "\n");

			result.edgesExamined++;
			edge.timesExamined++;
			if (edge.twin != null) {
				edge.twin.timesExamined++;
			}

			// We try not to push anything to the stack that is illegal
			// - we've reached the end of the list
			// - we're on the right and have reached negative values
			// - we're on the left and have reached positive values
			int nextIndex = edgeIndex+1;
			if (nextIndex < adjList.size()) { // if we're not at the end of a list 
				if ((vertex < graph.n && adjList.get(nextIndex).direction <= 0) ||
						(vertex >= graph.n && adjList.get(nextIndex).direction >= 0)) {  // on the left and we've reached positive values
					stack.push(new VertexEdge(vertex, nextIndex));
					//output.append("Pushed: vertex = " + vertex + " index = " + nextIndex+"\n");
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
				pred[opposite].searchNum = searchNum;
				pred[opposite].edge = edge;
				
				if (opposite == end) {
					return;
				} else {
					// Note that this push might have a dummy that is taken
					// On the right, this will be an option
					stack.push(new VertexEdge(opposite, 0));
					//output.append("Pushed: vertex = " + opposite + " index = 0\n");
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
		int iterations = Integer.parseInt(args[2]);
		int increment = Integer.parseInt(args[3]);

		String docName = args[4];
		
		// Open the output file
		PrintWriter writer = null;
		try{
			writer = new PrintWriter(docName+startN+ "-" + endN+".txt", "UTF-8");

		} catch (Exception e) {
			System.err.println("File not found...");
		}

		if (writer != null) {
			for (int N = startN; N <= endN; N+=increment) { 
				System.err.println("N = " + N);
				for (int i = 0; i < iterations; i++) {
					System.err.println(i);
					int [] rightSides = BipartiteGraph.getRightSides(N, 7);

					// Try 4 combinations on this graph
					//BipartiteGraph graph1 = new BipartiteGraph(N, 3, rightSides, false);
					BipartiteGraph graph2 = new BipartiteGraph(N, 7, rightSides, true);
					//System.err.println(graph2);
					//Result result1 = (new Search(graph1, new DummyZero(), new AvailableZero())).search();
					Result result2 = new Result(graph2);
					OriginalSearch search = new OriginalSearch(graph2, result2, new DummyZero(), new HomeAvailableZero());
					search.search();
					
					//writer.println("1;"+result1);
					writer.println(result2);
					
				}
			}
			writer.close();

		}

/* CODE FOR STARTING WITH GRAPH */

/*
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
