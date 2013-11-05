/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ma.glasnost.orika.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TopologicalSorter<V> {

	public static <T> List<T> sort(Collection<Node<T>> nodes) {
		// Set of all nodes with no incoming edges
		Set<Node<T>> openSet = new LinkedHashSet<Node<T>>();
		Set<Edge<T>> edgesDone = new HashSet<Edge<T>>();

		for (Node<T> to : nodes) {
			if (to.inEdges.isEmpty()) {
				openSet.add(to);
			}
		}

		// Empty list that will contain the sorted elements
		ArrayList<T> result = new ArrayList<T>();

		// while openSet is non-empty do
		while (!openSet.isEmpty()) {
			// remove a node n from openSet
			Node<T> n = openSet.iterator().next();
			openSet.remove(n);

			// insert n into result
			result.add(n.getValue());

			// for each node m with an edge e from n to m do
			for (Iterator<Edge<T>> it = n.outEdges.iterator(); it.hasNext();) {
				Edge<T> e = it.next();
				if (!edgesDone.contains(e)) {
					// remove edge e from the graph
					Node<T> m = e.to;
					edgesDone.add(e);// Remove edge from m (mark it done)

					// if m has no other incoming edges then insert m into S
					if (edgesDone.containsAll(m.inEdges)) {
						openSet.add(m);
					}
				}
			}
		}

		if (nodes.size() > result.size()) {
			throw new IllegalStateException("Ordering contains cycles");
		}
		return result;
	}
}