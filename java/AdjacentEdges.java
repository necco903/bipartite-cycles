// Each vertex has one
public class AdjacentEdges {

	boolean left;
	// For left
	EdgeNode dirR;
	EdgeNode noDir;
	EdgeNode dirL;
	EdgeNode takenR;
	EdgeNode takenL;

	// for right
	EdgeNode dummy;

	public AdjacentEdges(boolean left) {
			this.left = left;

			dirR = new EdgeNode();
			noDir = new EdgeNode();
			dirL = new EdgeNode();
			takenR = new EdgeNode();
			takenL = new EdgeNode();
			//dummy = new EdgeNode();

			if (left) {
				dirR.next = noDir;
				noDir.prev = dirR;

			} else {
				dirL.next = noDir;
				noDir.prev = dirL;
			}
	}

	// It doesn't matter where in the list we add it
	public static EdgeNode insertEdge(EdgeNode head, Edge edge) {
		EdgeNode newNode = new EdgeNode(edge);
		insertEdge(head, newNode);
		return newNode;
	}

	public static void insertEdge(EdgeNode head, EdgeNode edgeNode) {

		EdgeNode next = head.next;
		head.next = edgeNode;
		edgeNode.prev = head;
		edgeNode.next = next;
		if (next != null) {
			next.prev = edgeNode;
		}
	}


	// Do we have the edgenode reference?
	public static void spliceEdge(EdgeNode edgeNode) {
		
		edgeNode.prev.next = edgeNode.next;
		if (edgeNode.next != null) {
			edgeNode.next.prev = edgeNode.prev;
		}

	}

	public EdgeNode getFirst() {
		EdgeNode edge = null;

		if (left) {
			edge = dirR.next;
			if (edge == noDir) {
				edge = noDir.next;
			}
		} else {
			edge = dirL.next;
			if (edge == noDir) {
				edge = noDir.next;
			}
		}

		return edge;
	}

}