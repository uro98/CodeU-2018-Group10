import java.util.*;


/**
 * This class represents a directed graph data structure.
 * A graph can be described as a set of vertices. In case graph
 * is directed each vertex has its indegree and outdegree vertices.
 */
public class Graph<T extends Comparable<T>> {
    private Map<T, Vertex> vertexByValue;

    public Graph() {
        vertexByValue = new HashMap<>();
    }

    /**
     * This method adds a new edge between two vertices in a graph.
     * If a vertex with a value passed as method parameter doesn't
     * exist in a computed graph than it will be added automatically
     * @param parent is a parent vertex value
     * @param child is a child vertex value
     */
    public void addEdge(T parent, T child) {
        if(parent.compareTo(child) == 0) {
            return;
        }
        Vertex parentVertex;
        Vertex childVertex;
        if (vertexByValue.containsKey(parent)) {
            parentVertex = vertexByValue.get(parent);
        } else {
            parentVertex = new Vertex(parent);
            vertexByValue.put(parent, parentVertex);
        }
        if (vertexByValue.containsKey(child)) {
            childVertex = vertexByValue.get(child);
        } else {
            childVertex = new Vertex(child);
            vertexByValue.put(child, childVertex);
        }
        parentVertex.addOutdegreeVertex(childVertex);
        childVertex.addIndegreeVertex(parentVertex);
    }

    /**
     * This method adds a new vertex in a directed graph.
     *
     * <p>Graph vertices are unique - only one vertex can be created for each T value
     * @param value is a value of a new vertex
     */
    public void addVertex(T value) {
        vertexByValue.put(value, new Vertex(value));
    }

    /**
     * This method is to find one of possible topological
     * orders (if a graph has more than one) of a graph vertices
     * @return topological ordered list of vertices of a graph
     */
    public List<T> getTopologicalOrder() {
        List<T> alphabet = new ArrayList<>();
        dfs(alphabet);
        Collections.reverse(alphabet);
        vertexByValue.values().forEach(e -> e.setState(VertexState.UNVISITED));
        return alphabet;
    }

    /**
     * This method is to find all possible orders of topological
     * of vertices of the graph
     * @return all topological orders of vertices of a graph
     */
    public List<List<T>> getAllTopologicalOrders() {
        List<List<T>> alphabets = new ArrayList<>();
        List<T> alphabet = new ArrayList<>();
        dfsForAllTopologicalOrders(alphabet, alphabets);
        vertexByValue.values().forEach(e -> e.setState(VertexState.UNVISITED));
        return alphabets;
    }

    private void dfs(List<T> alphabet) {
        for (T value : vertexByValue.keySet()) {
            Vertex vertex = vertexByValue.get(value);
            if (vertex.getState() == VertexState.UNVISITED) {
                vertex.setState(VertexState.VISITING);
                dfsRecursive(vertexByValue.get(value), alphabet);
                alphabet.add(value);
                vertex.setState(VertexState.VISITED);
            }
        }
    }

    private void dfsRecursive(Vertex currentVertex, List<T> alphabet) {
        for (Vertex childVertex : vertexByValue.get(currentVertex.getValue()).getOutdegrees()) {
            if (childVertex.getState() == VertexState.VISITING) {
                throw new IllegalArgumentException("Dictionary is inconsistent");
            } else if (childVertex.getState() == VertexState.UNVISITED) {
                childVertex.setState(VertexState.VISITING);
                dfsRecursive(childVertex, alphabet);
                alphabet.add(childVertex.getValue());
                childVertex.setState(VertexState.VISITED);
            }
        }
    }

    private void dfsForAllTopologicalOrders(List<T> alphabet, List<List<T>> alphabets) {
        if (alphabet.size() == vertexByValue.size()) {
            alphabets.add(new ArrayList<>(alphabet));
        }
        for (Vertex vertex : vertexByValue.values()) {
            if (vertex.getState() == VertexState.UNVISITED && vertex.getIndegrees().size() == 0) {
                vertex.getOutdegrees().forEach(child -> child.getIndegrees().remove(vertex));
                alphabet.add(vertex.getValue());
                vertex.setState(VertexState.VISITED);
                dfsForAllTopologicalOrders(alphabet, alphabets);
                vertex.setState(VertexState.UNVISITED);
                alphabet.remove(alphabet.size() - 1);
                vertex.getOutdegrees().forEach(child -> child.getIndegrees().add(vertex));
            }
        }
    }

    public Set<Constraint<T>> findCircuits() {
        Set<Constraint<T>> constraints = new HashSet<>();
        for (Vertex vertex : vertexByValue.values()) {
            if (vertex.state == VertexState.UNVISITED) {
                vertex.state = VertexState.VISITING;
                dfsWithCircuitCheckRecursive(vertex, null, constraints);
                vertex.state = VertexState.VISITED;
            }
        }
        vertexByValue.values().forEach(e -> e.setState(VertexState.UNVISITED));
        return constraints;
    }

    private void dfsWithCircuitCheckRecursive(Vertex current, Vertex parent, Set<Constraint<T>> constraints) {
        for (Vertex childVertex : vertexByValue.get(current.getValue()).getOutdegrees()) {
            if (childVertex.state == VertexState.VISITING) {
                 constraints.add(new Constraint<>(parent.value, current.value));
                 childVertex.state = VertexState.VISITED;
            } else if (childVertex.state == VertexState.UNVISITED) {
                childVertex.state = VertexState.VISITING;
                dfsWithCircuitCheckRecursive(childVertex, current, constraints);
                childVertex.state = VertexState.VISITED;
            }
        }
    }

    /**
     * This class represents a vertex of a graph.
     * Each vertex can be described by its character-value,
     * indegree vertices (set of vertices that can direct you to the current vertex)
     * and outdegree vertices (set of vertices that can be achieved from the current one).
     * Vertex state is a special state to track the state of vertex during depth first search
     * in a graph.
     */
    public class Vertex {
        private T value;
        private Set<Vertex> indegrees;
        private Set<Vertex> outdegrees;
        private VertexState state;

        public Vertex(T value) {
            this.value = value;
            this.indegrees = new HashSet<>();
            this.outdegrees = new HashSet<>();
            this.state = VertexState.UNVISITED;
        }

        public void addIndegreeVertex(Vertex vertex) {
            indegrees.add(vertex);
        }

        public void addOutdegreeVertex(Vertex vertex) {
            outdegrees.add(vertex);
        }

        public void setState(VertexState state) {
            this.state = state;
        }

        public T getValue() {
            return value;
        }

        public Set<Vertex> getIndegrees() {
            return indegrees;
        }

        public Set<Vertex> getOutdegrees() {
            return outdegrees;
        }

        public VertexState getState() {
            return state;
        }
    }

}
