
// Right comparator
public class AvailableZero extends SearchComparator {

	public int compare(Edge edge1, Edge edge2) {
		// if one of these edges was negative then its left wouldn't be in available
		if (edge1.direction < 0 && edge2.direction >= 0) {
			return 1;
		} else if (edge2.direction < 0 && edge1.direction >=0) {
			return -1;
		}
		
		if (search.available.contains(edge1.left) && !search.available.contains(edge2.left)) {
			return -1;
		} else if (search.available.contains(edge2.left) && !search.available.contains(edge1.left)) {
			return 1;
		} 
		// either both available or unavailable - we want to take highest valued
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
