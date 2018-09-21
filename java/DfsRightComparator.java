public class DfsRightComparator extends SearchComparator {

	public int compare(Edge edge1, Edge edge2) {

		// cannot take the dummy, can't both be dummies
		if (edge1.isDummy) {
			return 1;
		} else if (edge2.isDummy) {
			return -1;
		}

		if (edge1.visited && !edge2.visited) {
			return 1;
		} else if (!edge1.visited && edge2.visited) {
			return -1;
		} else if (edge1.visited && edge2.visited) { // we shouldn't get

			// we cannot take edges from this search
			/*if (edge1.searchNum == search.searchNum && edge2.searchNum < search.searchNum) {
				return 1;
			} else if (edge2.searchNum == search.searchNum && edge1.searchNum < search.searchNum) {
				return -1;
			}*/

			// Either both edges are in this search - cannot take
			// Both edges are not visited in this search
			
			// if edge1 is further from zero than edge2 in positive direction
			if (edge1.direction > edge2.direction) {
				return 1;
			} else if (edge2.direction > edge1.direction) {
				return -1;
			}
		}

		return edge1.left - edge2.left;
	}
}