public class EdgeNode {

		Edge edge;
		EdgeNode next;
		EdgeNode prev;
		EdgeNode twin;

		public EdgeNode(Edge edge) {
			this.edge = edge;
		}

		// Header
		public EdgeNode() {
		}

		public String toString() {
			if (edge != null) {
				return "[" + edge.toString() + "]";
			}

			return "HEAD";
		}
	}