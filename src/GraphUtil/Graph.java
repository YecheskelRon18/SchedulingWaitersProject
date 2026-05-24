package GraphUtil;

import model.LocationV;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {
    private final List<Vertex> vertices;
    private final List<Edge> edges;
    private final Map<LocationV, Vertex> vertexByLocation;
    private final Map<String, Integer> distanceMap;

    public Graph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        vertexByLocation = new HashMap<>();
        distanceMap = new HashMap<>();
        buildDefaultLocationGraph();
    }

    public void addVertex(Vertex vertex) {
        if (!vertices.contains(vertex)) {
            vertices.add(vertex);
        }
    }

    public void addEdge(Vertex source, Vertex destination, int weight) {
        edges.add(new Edge(source, destination, weight));
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    // Dijkstra and subMethods

    public int Dijkstra(LocationV start, LocationV target) {
        String key = distanceKey(start, target);
        if (distanceMap.containsKey(key)) {
            return distanceMap.get(key);
        }

        int distance = Dijkstra(vertexByLocation.get(start), vertexByLocation.get(target));
        distanceMap.put(key, distance);
        distanceMap.put(distanceKey(target, start), distance);
        return distance;
    }

    private String distanceKey(LocationV start, LocationV target) {
        return start.name() + "-" + target.name();
    }

    public int Dijkstra(Vertex start, Vertex target) {
        if (start == null || target == null) return -1;

        Map<Vertex, Integer> distances = DijFindFirstDistance(start);
        Set<Vertex> visited = new HashSet<>();

        Vertex current = DijFindClosestUnvisitedVertex(distances, visited);
        while (current != null) {
            if (current.equals(target)) {
                return distances.get(target);
            }
            visited.add(current);
            DijUpdateNeighbors(current, distances, visited);
            current = DijFindClosestUnvisitedVertex(distances, visited);
        }
        return -1;
    }

    private Map<Vertex, Integer> DijFindFirstDistance(Vertex start) {
        Map<Vertex, Integer> distances = new HashMap<>();
        for (Vertex vertex : vertices) {
            distances.put(vertex, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        return distances;
    }

    private Vertex DijFindClosestUnvisitedVertex(Map<Vertex, Integer> distances, Set<Vertex> visited) {
        Vertex closest = null;
        int smallestDistance = Integer.MAX_VALUE;
        for (Vertex vertex : vertices) {
            int distance = distances.get(vertex);
            if (!visited.contains(vertex) && distance < smallestDistance) {
                smallestDistance = distance;
                closest = vertex;
            }
        }
        return closest;
    }

    private void DijUpdateNeighbors(Vertex current, Map<Vertex, Integer> distances, Set<Vertex> visited) {
        for (Edge edge : edges) {
            Vertex neighbor = findNeighbor(edge, current);
            if (neighbor != null && !visited.contains(neighbor)) {
                updateDistance(current, neighbor, edge, distances);
            }
        }
    }

    private Vertex findNeighbor(Edge edge, Vertex current) {
        if (edge.getSource().equals(current)) {
            return edge.getDestination();
        }
        if (edge.getDestination().equals(current)) {
            return edge.getSource();
        }
        return null;
    }

    private void updateDistance(Vertex current, Vertex neighbor, Edge edge, Map<Vertex, Integer> distances) {
        int newDistance = distances.get(current) + edge.getWeight();
        if (newDistance < distances.get(neighbor)) {
            distances.put(neighbor, newDistance);
        }
    }

    // end of Dijkstra and subMethods

    public void printGraph() {
        System.out.println("Vertices:");
        for (Vertex vertex : vertices) {
            System.out.println(vertex);
        }

        System.out.println("\nEdges:");
        for (Edge edge : edges) {
            System.out.println(edge);
        }
    }

    private void buildDefaultLocationGraph() {
        for (LocationV location : LocationV.values()) {
            Vertex vertex = new Vertex(location.getDisplayName());
            addVertex(vertex);
            vertexByLocation.put(location, vertex);
        }
        addNorthernEdges();
        addCenterEdges();
        addSouthernEdges();
    }

    private void addNorthernEdges() {
        addLocationEdge(LocationV.GOLAN_HEIGHTS, LocationV.KINNERET, 42);
        addLocationEdge(LocationV.GOLAN_HEIGHTS, LocationV.SAFED, 38);
        addLocationEdge(LocationV.SAFED, LocationV.AKKO, 30);
        addLocationEdge(LocationV.SAFED, LocationV.KINNERET, 25);
        addLocationEdge(LocationV.SAFED, LocationV.JEZREEL, 35);
        addLocationEdge(LocationV.KINNERET, LocationV.JEZREEL, 28);
        addLocationEdge(LocationV.AKKO, LocationV.HAIFA, 20);
        addLocationEdge(LocationV.AKKO, LocationV.JEZREEL, 33);
        addLocationEdge(LocationV.HAIFA, LocationV.HADERA, 27);
        addLocationEdge(LocationV.HAIFA, LocationV.JEZREEL, 22);
        addLocationEdge(LocationV.HADERA, LocationV.SHARON, 24);
    }

    private void addCenterEdges() {
        addLocationEdge(LocationV.SHARON, LocationV.TEL_AVIV_JAFFA, 18);
        addLocationEdge(LocationV.SHARON, LocationV.PETAH_TIKVA, 20);
        addLocationEdge(LocationV.PETAH_TIKVA, LocationV.RAMLA, 17);
        addLocationEdge(LocationV.PETAH_TIKVA, LocationV.TEL_AVIV_JAFFA, 12);
        addLocationEdge(LocationV.RAMLA, LocationV.REHOVOT, 14);
        addLocationEdge(LocationV.RAMLA, LocationV.TEL_AVIV_JAFFA, 16);
        addLocationEdge(LocationV.REHOVOT, LocationV.JERUSALEM, 40);
        addLocationEdge(LocationV.REHOVOT, LocationV.ASHKELON, 30);
    }

    private void addSouthernEdges() {
        addLocationEdge(LocationV.ASHKELON, LocationV.BEERSHEBA, 48);
        addLocationEdge(LocationV.ASHKELON, LocationV.JERUSALEM, 52);
        addLocationEdge(LocationV.BEERSHEBA, LocationV.JERUSALEM, 65);
    }

    private void addLocationEdge(LocationV source, LocationV destination, int weight) {
        addEdge(vertexByLocation.get(source), vertexByLocation.get(destination), weight);
    }

}
