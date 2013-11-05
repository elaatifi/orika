package ma.glasnost.orika.util;

import java.util.LinkedHashSet;

class Node<T> {
	public final T value;
	public final LinkedHashSet<Edge<T>> inEdges;
	public final LinkedHashSet<Edge<T>> outEdges;

	public Node(T value) {
		this.value = value;
		inEdges = new LinkedHashSet<Edge<T>>();
		outEdges = new LinkedHashSet<Edge<T>>();
	}

	public Node<T> addEdge(Node<T> node) {
		Edge<T> e = new Edge<T>(this, node);
		outEdges.add(e);
		node.inEdges.add(e);
		return this;
	}

	public T getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getValue().toString();
	}
}