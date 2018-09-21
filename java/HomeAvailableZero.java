

public class HomeAvailableZero extends SearchComparator {
	
	public int compare(Edge edge1, Edge edge2) {
		
		if (edge1.direction < 0 && edge2.direction >= 0) {
			return 1;
		} else if (edge2.direction < 0 && edge1.direction >=0) {
			return -1;
		}
		
		int home = search.end;
		// if one of the lefts is home then it can't be negative because if it was
		// then we took this edge to this vertex before and therefore took the dummy. 
		if (edge1.left == home && edge2.left != home) {
			return -1;
		} else if (edge2.left == home && edge1.left != home) {
			return 1;
		}
		
		// go to the one that is available
		if (edge1.left != home && edge2.left != home) { // this shouldn't matter
			if (search.available.contains(edge1.left) && !search.available.contains(edge2.left)) {
				return -1;
			} else if (search.available.contains(edge2.left) && !search.available.contains(edge1.left)) {
				return 1;
			} 
		}
		
		// go for best direction - most positive
		if (edge2.direction < edge1.direction) {
			return -1;
		} else if (edge1.direction < edge2.direction) {
			return 1;
		}
		
		// lowest numbered edge - to make it deterministic
		return edge1.left - edge2.left;	

	}
}
