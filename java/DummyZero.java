
public class DummyZero extends SearchComparator {
	
	public int compare(Edge edge1, Edge edge2) {
		
		// TODO dummies should not go first if they are taken
		if (edge1.isDummy && !edge2.isDummy) {
			return -1;
		} else if (edge2.isDummy && !edge1.isDummy) {
			return 1;
		}
		
		// go for best direction - most negative
		if (edge1.direction < edge2.direction) {
			return -1;
		} else if (edge2.direction < edge1.direction) {
			return 1;
		}
		
		// lowest numbered edge - to make it deterministic
		return edge1.right - edge2.right;	
	}

}
