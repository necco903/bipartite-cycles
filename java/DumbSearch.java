
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

// Find cycles in the bipartite graph with dummy edges
// Rules: 
// 1) Always start at an available vertex and take the dummy
// 3) If you are on the right and can go home, go home
// 2) If you can't go home but you can go to an available vertex, go there
public class DumbSearch extends Search {
	
	List<Integer> available;
	BipartiteGraph graph;
	BipartiteGraph graph2;
	
	// Each vertex on the left must keep track of the highest degree of any of its neighbors
	int [] highestDegreesLeft;

	public DumbSearch(BipartiteGraph graph) {
		this.graph = graph;
		available = new ArrayList<Integer>();
		highestDegreesLeft = new int[graph.n];
		for (int i = 0; i < graph.n; i++) {
			highestDegreesLeft[i] = graph.d;
		}
		
		for (int i = 0; i < graph.n; i++) {
			available.add(i);
		}
	}
	
	// returns true if the search was successful
	// false if we got stuck
	public boolean search(boolean comments, boolean goHome) {
				
		while (available.size() > 0) {
			int end = available.remove(0);
			int start = graph.adjLists.get(end).getDummy().right; // we actually just know this
			// remove this dummy edge from the adjacency list 
			graph.adjLists.get(end).removeDummy();
			if (comments) System.out.println("Start: " + start + " End: " + end);
			if (!findCycle(start, end, comments, goHome) && !findCycle(start, end, comments, goHome)) return false;
		}
		
		return true;
	}
	
	// return false if we are forced to take a dummy edge backwards
	// remove edges from adjacency list
	public boolean findCycle(int start, int end, boolean comments, boolean goHome) {
				
		int current = start;
		while (current != end) {
			if (comments) System.out.println("Current = " + current);
			BipartiteGraph.AdjacencyList adjList = graph.adjLists.get(current);
			
			if (adjList.isEmpty()) { // we are stuck
				if (comments) System.out.println("Stuck at vertex " + current);
				return false;
			}
			
			if (current < graph.n) {
				// if we are on the left and can take a dummy, take it
				Edge dummy = adjList.getDummy();
				if (dummy != null) {
					adjList.remove(dummy);
					// remove this vertex from available
					Integer remove = null;
					for (Integer i : available) {
						if (i == current) {
							remove = i;
						}
					}
					available.remove(remove);
					
					current = dummy.right;
					if (comments) System.out.println("Took dummy " + dummy);
				} else { 
					// Go to the highest degree neighbor, remove degree from list
					Edge edge = null;
					int max = -1;
					
					for (Edge e : adjList) {
						int opposite = e.right;
						if (graph.adjLists.get(opposite).size() > max) {
							max = graph.adjLists.get(opposite).size();
							edge = e;
						}
					}
					adjList.remove(edge);
					graph.adjLists.get(edge.right).remove(edge.twin);
					
					updateHighestDegrees(graph);
					
					current = edge.right;
					if (comments) System.out.println("Took " + edge);
				}
			} else { // on the right
				
				if (goHome) {
					if (adjList.getEdge(current, end) != null) {
					// if we are on the right and can go home, go home
					Edge toHome = adjList.getEdge(current, end);
					adjList.remove(toHome);
					graph.adjLists.get(end).remove(toHome.twin);
					current = end;
					if (comments) System.out.println("Went home via " + toHome);
					} else {
						
						// else if we can go to an available, go to available
						boolean avail = false;
						for (int vertex : available) {
							if (adjList.getEdge(current, vertex) != null) {
								Edge toAvail = adjList.getEdge(current, vertex);
								adjList.remove(toAvail);
								graph.adjLists.get(vertex).remove(toAvail.twin);
								current = toAvail.left;
								avail = true;
								if (comments) System.out.println("To available " + toAvail);
								break;
							}
						}
						if (!avail) {
							// pick any left vertex
							Edge edge = adjList.remove(adjList.size() -1);
							graph.adjLists.get(edge.left).remove(edge.twin);
							current = edge.left;
							if (comments) System.out.println("Took " + edge);
						}
					}
				} else {
					boolean avail = false;
					for (int vertex : available) { // order by highest degree neighbor
						if (adjList.getEdge(current, vertex) != null) {
							Edge toAvail = adjList.getEdge(current, vertex);
							adjList.remove(toAvail);
							graph.adjLists.get(vertex).remove(toAvail.twin);
							current = toAvail.left;
							avail = true;
							if (comments) System.out.println("To available " + toAvail);
							break;
						}
					}
					/*if (!avail && adjList.getEdge(current, end) != null) {
						// if we are on the right and can go home, go home
						Edge toHome = adjList.getEdge(current, end);
						adjList.remove(toHome);
						graph.adjLists.get(end).remove(toHome.twin);
						current = end;
						if (comments) System.out.println("Went home via " + toHome);
					} else */
					if (!avail) {
						
						// pick left vertex with the highest degree neighbor on the right
						int best = -1;
						Edge edge = null;
						for (Edge e : adjList) {
							if (highestDegreesLeft[e.left] > best) {
								best = highestDegreesLeft[e.left];
								edge = e;
							}
						}
						
						adjList.remove(edge);
						graph.adjLists.get(edge.left).remove(edge.twin);
						current = edge.left;
						
						updateHighestDegrees(graph);
						
						if (comments) System.out.println("Took " + edge);
						
					}
				}
					
			}
					
		}
		
		return true; // we reached the end
	}

	// Update highest degrees left
	// For every vertex make sure that it's highest degree neighbor is saved
	private void updateHighestDegrees(BipartiteGraph graph) {
		
		for (int vertex = 0; vertex < graph.n; vertex++) {
			
			int highest = -1;
			for (Edge e : graph.adjLists.get(vertex)) {
				int opposite = e.right;
				if (highest == -1 || graph.adjLists.get(opposite).size() > highest) {
					highest = graph.adjLists.get(opposite).size();
				}
			}
			highestDegreesLeft[vertex] = highest;

		}
	}
	public static void main(String [] args) {
		
	
	}

}
