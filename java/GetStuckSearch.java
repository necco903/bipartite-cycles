import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.lang.Exception;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Queue;
import java.util.LinkedList;
import java.lang.StringBuilder;
import java.util.Collections;
import java.util.Comparator;

public class GetStuckSearch extends Search {

	public static final int UNMATCHED = -1;

	BipartiteGraph graph;
	Result result;

	int [] pairs;
	int [] orientation;
	int [] directedRL; 

	// orientation at a vertex = directedLR - directedRL 
	// directedRL + orientation 

	// For dfs search 
	Predecessor [] pred;
	int searchNum = 1;


	public GetStuckSearch(BipartiteGraph graph, Result result) {
		this.graph = graph;

		pred = new Predecessor[graph.n*2];
		this.result = result;
		result.graph = graph;

		for (int i = 0; i < pred.length; i++) {
			pred[i] = new Predecessor();
		}

		available = new ArrayList<Integer>();
		for (int i = 0; i < graph.n; i++) {
			available.add(Integer.valueOf(i));
		}

		pairs = new int [2*graph.n];
		for (int i = 0; i < pairs.length; i++) {
			pairs[i] = UNMATCHED;
		}

		orientation = new int[graph.n*2];
		directedRL = new int[graph.n*2];
	}

	/** 
	* Find and mark all of the cycles used in the Euler decomposition
	*/
	public void makeCycles() {

		while (available.size() > 0) { 

			//System.err.println("Available size: " + available.size());

			// Take the first available vertex that we see
			boolean def = true;
			int matchedVertex = 0;

			// But if we can finish, do it. called quick match
			for (Integer avail : available) {
				// Look at all edges in dirR and noDir
				EdgeNode node = graph.adjEdges.get(avail).dirR.next;
				while (node != null) {
					Edge e = node.edge;
					if (e == null) {
						node = node.next;
						continue;
					}
					if (getStuck(e.twin)) { // if this edge will get us stuck, we want to take it unless it is already matched at the end
						// if this vertex is already matched, it's no good
						if (pairs[e.right] == UNMATCHED) {
							//System.err.println("Quick matched " + avail + " to " + e.right);
							takeEdge(node, true);

							match(avail, e.right);

							def = false;
							matchedVertex = avail;
							break;
						}
					}
					node = node.next;
				}

				if (!def) {
					break;
				}

			}

			if (def) {

				int left = available.remove(0);
				//System.err.println("Starting walk from " + left);
				Predecessor last = walk(left);

				// Didn't get home
				if (pairs[left] == UNMATCHED) { 
					//System.err.println("Couldn't match " + left);
					// Need to untrace all of the edges that we just took
					
					// Undo the edges we took
					Predecessor current = last;
					while (current != null) {
						EdgeNode e = current.edgeNode;
						int vertex = current.vertex;
						//System.err.println("Backtracking vertex: " + vertex + " edge: " + e);

						if (vertex < graph.n) {
							untakeEdge(e, true);
						} else {
							untakeEdge(e, false);
						}
						current = current.prev;
					}
				}
			
			} else {
				available.remove(Integer.valueOf(matchedVertex));
				directEdges(matchedVertex);
			}

			searchNum++;
		}

		// Now we check to see which vertices on the right do not have matches...
		for (int i = graph.n; i < graph.n*2; i++) {
			if (pairs[i] == UNMATCHED) {
				available.add(Integer.valueOf(i));
			}
		}


		if (available.size() > 0) {
			result.numSearches = available.size();
			
			//System.err.println("Need to search...");
		}

		SearchComparator leftComp = new DfsLeftComparator();
		SearchComparator rightComp = new DfsRightComparator();
		leftComp.search = this;
		rightComp.search = this;
		
		while (available.size() > 0) {
			for (int i = 0; i < graph.n; i++) {
				Collections.sort(graph.dfsEdges.get(i), leftComp);
				Collections.sort(graph.dfsEdges.get(i+graph.n), rightComp); 
			}

			// We are going to do a search from each of these vertices to any on the left with a duplicate match
			int right = available.remove(0);
			int res = dfs(right);
			
			// Now we need to trace out the cycle that we formed, BACKWARDS
			if (res == -1) {
				System.err.println("Could not find an unmatched...");
			}

			int current = res;
			while (current != right) {
				Edge edge = pred[current].edge;
				if (current < graph.n) {
					takeEdge(edge, true);
					current = edge.right;
				} else {
					takeEdge(edge, false);
					current = edge.left;	
				}
			}

			searchNum++;

		}

		// increment the graph degree by 1
		graph.d++;
		//System.err.println("Pairs:");
		//printPairs();
	}

	// start from a right vertex and search until we get to a left one
	private int dfs(int start) {
		
		Stack<VertexEdge> stack = new Stack<VertexEdge>();
		stack.push(new VertexEdge(start, 0));
		
		while (true) {
			if (stack.isEmpty()) {
				return -1;
			}
			VertexEdge popped = stack.pop();
			int vertex = popped.vertex;
			int edgeIndex = popped.edgeIndex;
			
			List<Edge> adjList = graph.dfsEdges.get(vertex);
			Edge edge = adjList.get(popped.edgeIndex);

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
				Edge potential = adjList.get(nextIndex);
				if (vertex < graph.n) {
					//if we are going to back to the start or the edge direction is bad, continue
					if (potential.right != start && potential.direction >= 0) {
						stack.push(new VertexEdge(vertex, nextIndex));
					}
				} else {
					// if we are taking the dummy or the edge direction is bad, continue
					if (potential.direction <= 0) {
						stack.push(new VertexEdge(vertex, nextIndex));
					}
				}
			}
		
			int opposite;
			if (vertex < graph.n) {
				opposite = edge.right;
			} else {
				opposite = edge.left;
			}
			
			if (pred[opposite].searchNum < searchNum) { // we haven't seen it in this search yet
				pred[opposite].searchNum = searchNum;
				pred[opposite].edge = edge;
				
				if (opposite < graph.n && pairs[opposite] == UNMATCHED) { // we match 
					match(opposite, start);
					return opposite;

				} else {
					// Note that this push might have a dummy that is taken
					// On the right, this will be an option
					stack.push(new VertexEdge(opposite, 0));
				}
				
			}

		}

	}

	private void match(int left, int right) {
		// Add dummy
		Edge dummy = new Edge(left, right, true);
		//graph.adjEdges.get(right).addDummy(dummy); // not sure if we need to do this...
		graph.dfsEdges.get(left).add(dummy);
		takeEdge(dummy, false);

		pairs[left] = right;
		pairs[right] = left;
	}

	private Predecessor walk(int home) {

		int current = home;
		Predecessor pred = null;

		while (true) { // we want to get stuck on the right

			//System.err.println("Current vertex: " + current);
			// Choose edge option, take the edge, change the direction
			AdjacentEdges list = graph.adjEdges.get(current);
			EdgeNode edgeNode = list.getFirst(); // this will be valid, or else we would have noticed that we got stuck on right, and can't get stuck on the left

			if (edgeNode == null) {
				//System.err.println("We are stuck at " + current);
				return pred;
			}
			//System.err.println("Taking edge: " + edgeNode.edge);

			int opposite;
			if (current < graph.n) { // take edge left to right
				opposite = edgeNode.edge.right;
				takeEdge(edgeNode, true);
				directEdges(current);

				// if we will get stuck here
				if (getStuck(edgeNode.edge.twin)) { 
					if (pairs[opposite] != UNMATCHED) { // it's already matched
						//System.err.println("Could not match " + home + " to " + opposite);
						return pred;

					} else {
						match(home, opposite);
						//System.err.println("Matched " + home + " to " + opposite);
						return null;
					}
				}

			} else { // take edge right to left
				opposite = edgeNode.edge.left;
				takeEdge(edgeNode, false);
				directEdges(current);
			}

			Predecessor next = new Predecessor();
			next.vertex = opposite;
			next.edgeNode = edgeNode;
			next.prev = pred;

			pred = next;
			current = opposite;

			//System.err.println(graph);
		}
	}

	/**
	* Also need to take the twin
	*/
	private void takeEdge(EdgeNode edgeNode, boolean right) {

		// Both of the endpoints need to have their lists updated

		//System.err.println("Taking edge: " + edgeNode + " " + right);
		Edge edge = edgeNode.edge;
		if (right && edge.directed == Edge.NOT_DIRECTED) {
			orientation[edge.right]++;
			orientation[edge.left]++;
		} else if (!right && edge.directed == Edge.NOT_DIRECTED) {
			orientation[edge.right]--;
			orientation[edge.left]--;
			// change to directed right to left
			directedRL[edge.right]++;
			directedRL[edge.left]++;
		}


		takeEdge(edge, right);

		AdjacentEdges.spliceEdge(edgeNode); // remove it from whatever list it was in and move the twin edge node too
		AdjacentEdges.spliceEdge(edgeNode.twin);

		EdgeNode head1;
		EdgeNode head2;

		if (right) {
			head1 = graph.adjEdges.get(edge.left).takenR;
			head2 = graph.adjEdges.get(edge.right).takenR;
		} else { // it was taken right to left
			head1 = graph.adjEdges.get(edge.left).takenL;
			head2 = graph.adjEdges.get(edge.right).takenL;
		}
		AdjacentEdges.insertEdge(head1, edgeNode);
		AdjacentEdges.insertEdge(head2, edgeNode.twin);	

	}

	/*private void printThings() {
		for (int i = 0; i < 2*graph.n; i++) {
			System.err.println("vertex " + i + " orient: " + orientation[i] + " dirRL: " + directedRL[i]);
		}
	}*/

	/**
	* Also need to take the twin
	*/
	private void takeEdge(Edge edge, boolean right) {
		edge.timesVisited++;
		result.edgesExamined++;

		edge.visited = true;
		edge.searchNum = searchNum;
		edge.direction = right ? edge.direction+1 : edge.direction -1;


		if (edge.twin != null) {
			edge.twin.visited = true;
			edge.twin.searchNum = searchNum;
			edge.twin.direction = edge.direction;
			edge.twin.timesVisited++;
		}

		
	}

	private void untakeEdge(EdgeNode edgeNode, boolean right) {
		result.edgesExamined++;

		Edge edge = edgeNode.edge;

		// if the edge was not directed, we need to undo some things
		if (right && edge.directed == Edge.NOT_DIRECTED) {
			orientation[edge.right]++;
			orientation[edge.left]++;
			directedRL[edge.right]--;
			directedRL[edge.left]--;
		} else if (!right && edge.directed == Edge.NOT_DIRECTED) {
			orientation[edge.right]--;
			orientation[edge.left]--;
		}

		edge.direction = right ? edge.direction+1 : edge.direction -1;
		edge.visited = false;

		if (edge.twin != null) {
			edge.twin.visited = false;
			edge.twin.direction = edge.direction;
		}

		AdjacentEdges.spliceEdge(edgeNode); // remove it from whatever list it was in and move the twin edge node too
		AdjacentEdges.spliceEdge(edgeNode.twin);

		EdgeNode head1;
		EdgeNode head2;

		// either goes into no dir or dirR or dirL
		if (edge.directed == Edge.NOT_DIRECTED) {
			head1 = graph.adjEdges.get(edge.left).noDir;
			head2 = graph.adjEdges.get(edge.right).noDir;
		} else if (edge.directed == Edge.DIRECTED_R) { // it was taken right to left
			head1 = graph.adjEdges.get(edge.left).dirR; 
			head2 = graph.adjEdges.get(edge.right).dirR;
		} else {
			head1 = graph.adjEdges.get(edge.left).dirL;
			head2 = graph.adjEdges.get(edge.right).dirL;
		}

		// DOES IT MATTER WHICH SIDE THE NODES ARE ON?
		// head1 is the left side
		// head2 is the right side

		AdjacentEdges.insertEdge(head1, edgeNode);
		AdjacentEdges.insertEdge(head2, edgeNode.twin);	

		//System.err.println("Untook edge: " + edgeNode.edge);
	}

	private void directEdge(EdgeNode edgeNode, boolean right) {
		if (edgeNode.edge.directed != Edge.NOT_DIRECTED || edgeNode.edge.visited) {
			return;
		}

		Edge edge = edgeNode.edge;

		// update orientation and directed RL
		if (right) {
			orientation[edge.right]++;
			orientation[edge.left]++;
		} else {
			orientation[edge.right]--;
			orientation[edge.left]--;
			directedRL[edge.right]++;
			directedRL[edge.left]++;
		}

		AdjacentEdges.spliceEdge(edgeNode); // remove it from whatever list it was in and move the twin edge node too
		AdjacentEdges.spliceEdge(edgeNode.twin);

		edgeNode.edge.directed = right ? Edge.DIRECTED_R : Edge.DIRECTED_L;
		edgeNode.twin.edge.directed = right ? Edge.DIRECTED_R : Edge.DIRECTED_L;

		// need to insert into the appropriate list
		EdgeNode node1 = null; 
		EdgeNode node2 = null;
		if (right) {
			node1 = graph.adjEdges.get(edge.left).dirR;
			node2 = graph.adjEdges.get(edge.right).dirR;
		} else {
			node1 = graph.adjEdges.get(edge.left).dirL;
			node2 = graph.adjEdges.get(edge.right).dirL;
		}
		AdjacentEdges.insertEdge(node1, edgeNode);
		AdjacentEdges.insertEdge(node2, edgeNode.twin);

		//System.err.println("Directing edge: " + edgeNode.edge + " " + right);

	}

	// If we will get stuck here if we come in on the only remaining edges
	private boolean getStuck(Edge inEdge) {

		int rightVertex = inEdge.right;

		EdgeNode node = graph.adjEdges.get(rightVertex).dirL.next;
		 
		// If we find an edge that we can take outwards then we are not stuck
		while (node != null) {
			if (node.edge != null && node.edge != inEdge) {
				return false;
			}
			node = node.next;
		}

		// Every edge is either taken or directed left to right
		return true;
	}

	
	// Given an edge recently directed, and the direction - either forwards or backwards
	// 1) make changes to relevant edges at target vertex
	// 2) return (if any) the edge that was altered as a result
	private void directEdges(int vertex) {

		//System.err.println("Directing edges from " + vertex);
		// count the number of "options" at this vertex
		AdjacentEdges adjList = graph.adjEdges.get(vertex);

		// if all edges are committed, we are done here
		if (adjList.noDir.next == null) {
			//System.err.println("None directed");
			return;
		}

		EdgeNode node;


		// If either d/2 is reached by R to L or d/2 + 1 by L to R

		if (directedRL[vertex] >= graph.d/2) { // will be 1 for d=3			
			//System.err.println("Case 1");

			// Get undirected edges and direct left to right, there are two undirected 
			while (adjList.noDir.next != null)  { // there are undirected edges
				node = adjList.noDir.next;

				directEdge(node, true);

				// We need to propagate these changes
				if (vertex < graph.n) {
					directEdges(node.edge.right);
				} else {
					directEdges(node.edge.left);
				}
			}
		
		} else if ((directedRL[vertex] + orientation[vertex]) >= graph.d/2 + 1) {
			//System.err.println("Case 2");


			// Get undirected edges and direct left to right, 
			while (adjList.noDir.next != null) {

				node = adjList.noDir.next;

				directEdge(node, false);

				// We need to propagate these changes
				if (vertex < graph.n) {
					directEdges(node.edge.right);
				} else {
					directEdges(node.edge.left);
				}
			}

		} else { // the rest of the edges are multiedges, so we know how we need to direct them

			int numUndirected = 0;
			node = adjList.noDir.next; 
			EdgeNode nextNode = node.next;

			if (node != null) {
				numUndirected = 1;
			}
			// check the remaining uncommitted edges
			while (node != null && nextNode != null) {

				if (!node.edge.sameEdge(nextNode.edge)) {
					return; // we are done
				}

				numUndirected++;
				node = nextNode;
				nextNode = nextNode.next;
			}

			// All are multiedges, so their orientation is already determined, it needs to be +1 at the end
			//System.err.println("Case 3");

			while (adjList.noDir.next != null) {

				node = adjList.noDir.next;

				if (orientation[vertex] <= 1) { // direct left to right
					directEdge(node, true);

				} else { // it is greater than 1, so we need to direct right to left
					directEdge(node, false);
				}

				// We need to propagate these changes
				if (vertex < graph.n) {
					directEdges(node.edge.right);
				} else {
					directEdges(node.edge.left);
				}
			}
		}

	}


	private void printPairs() {
		for (int i = 0; i < pairs.length/2; i++) {
			System.err.println(i + "," + pairs[i]);
		}
	}
	public static void main(String [] args) {

		if (args.length < 3) {
			System.err.println("Wrong number of parameters.");
			return;
		}

		int startN = Integer.parseInt(args[0]);
		int endN = Integer.parseInt(args[1]);
		int iterations = Integer.parseInt(args[2]);
		int increment = Integer.parseInt(args[3]);
		String docName = args[4];
		//String docName2 = args[5];

		// Open the output file
		PrintWriter writer = null;
		//PrintWriter writer2 = null;
		try{
			writer = new PrintWriter(docName+startN+ "-" + endN+".txt", "UTF-8");
			//writer2 = new PrintWriter(docName2+startN+"-" + endN + ".txt", "UTF-8");

		} catch (Exception e) {
			System.err.println("File not found...");
		}

		SearchComparator originalleft = new DummyZero();
		SearchComparator originalright = new HomeAvailableZero();

		if (writer != null) {
			for (int N = startN; N <= endN; N+=increment) { 
				System.err.println("N = " + N);
				for (int i = 0; i < iterations; i++) {
					System.err.println(i);
					int [] rightSides = BipartiteGraph.getRightSides(N, 5);
					BipartiteGraph originalgraph = new BipartiteGraph(N, 5, rightSides, true);
					BipartiteGraph getstuckgraph = new BipartiteGraph(N, 5, rightSides, false);

					//System.err.println("New graph: " + graph2.toString());
					Result originalresult = new Result(originalgraph);
					Result getstuckresult = new Result(getstuckgraph);

					OriginalSearch original = new OriginalSearch(originalgraph, originalresult,  originalleft, originalright);
					GetStuckSearch getstuck = new GetStuckSearch(getstuckgraph, getstuckresult);

					original.search();
					getstuck.makeCycles();

					writer.println((originalgraph.n*(originalgraph.d)) + "," + originalresult + "," + getstuckresult);
					writer.flush();

					/*
					rightSides = BipartiteGraph.getRightSides(N, 5);
					originalgraph = new BipartiteGraph(N, 5, rightSides, true);
					getstuckgraph = new BipartiteGraph(N, 5, rightSides, false);

					//System.err.println("New graph: " + graph2.toString());
					originalresult = new Result(originalgraph);
					getstuckresult = new Result(getstuckgraph);

					original = new OriginalSearch(originalgraph, originalresult, originalleft, originalright);
					getstuck = new GetStuckSearch(getstuckgraph, getstuckresult);

					original.search();
					getstuck.makeCycles();

					writer2.println((originalgraph.n*(originalgraph.d)) + "," + originalresult + "," + getstuckresult);
					writer2.flush();*/

				}
			}
			writer.close();
			//writer2.close();
		}

	}
}





