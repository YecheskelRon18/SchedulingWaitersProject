package GraphUtil;

public class Edge {
    private final Vertex source;
    private final Vertex destination;
    private final int weight;

    public Edge(Vertex source, Vertex destination, int weight) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public Vertex getSource() {
        return source;
    }

    public Vertex getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public boolean touches(Vertex vertex) {
        return source.equals(vertex) || destination.equals(vertex);
    }

    public Vertex otherSide(Vertex vertex) {
        if (source.equals(vertex)) {
            return destination;
        }
        if (destination.equals(vertex)) {
            return source;
        }
        return null;
    }

    @Override
    public String toString() {
        return source + " <--> " + destination + " (weight=" + weight + ")";
    }
}
