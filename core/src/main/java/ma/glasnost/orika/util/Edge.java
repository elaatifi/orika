package ma.glasnost.orika.util;

class Edge<T> {
	public final Node<T> from;
	public final Node<T> to;

	public Edge(Node<T> from, Node<T> to) {
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean equals(Object obj) {
		Edge<?> e = (Edge<?>) obj;
		return e.from == from && e.to == to;
	}

	@Override
	public int hashCode() {
		return to.hashCode() ^ from.hashCode();
	}

	@Override
	public String toString() {
		return from.toString() + " -> " + to.toString();
	}
}