package GraphUtil;

public class Vertex {
    private final String name;

    public Vertex(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Vertex vertex)) {
            return false;
        }
        return name.equals(vertex.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
