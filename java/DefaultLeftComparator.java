public class DefaultLeftComparator extends SearchComparator {

	public int compare(Edge edge1, Edge edge2) {

		if (edge1.visited && !edge2.visited) {
			return 1;
		} else if (!edge1.visited && edge2.visited) {
			return -1;
		} else if (edge1.visited && edge2.visited) {

			// we cannot take edges from this search
			if (edge1.searchNum == search.searchNum && edge2.searchNum < search.searchNum) {
				return 1;
			} else if (edge2.searchNum == search.searchNum && edge1.searchNum < search.searchNum) {
				return -1;
			}

			// Either both are in this search - cannot take
			// Or both are not visited in this search
			
			// if edge1 is further from zero than edge2 in positive direction
			if (edge1.direction < edge2.direction) {
				return -1;
			} else if (edge2.direction < edge1.direction) {
				return 1;
			}
		} 

		// both are unvisited... if one is directed and one isn't, we need to take the directed one if it is the right direction
		// or the undirected otherwise

		if (edge1.directed < edge2.directed) {
			return -1;
		} else if (edge2.directed < edge1.directed) {
			return 1;
		}

		return edge1.right - edge2.right; // lowest rumber 
	}
}
