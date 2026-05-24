package model;

// מייצג אולם אירועים
public class Venue {
    // פרטי האולם
    private int id;
    private String name;
    private LocationV vertex;

    // בונה אובייקט של אולם חדש
    public Venue(int id, String name) {
        this(id, name, LocationV.TEL_AVIV_JAFFA);
    }

    public Venue(int id, String name, LocationV vertex) {
        this.id = id;
        this.name = name;
        this.vertex = vertex;
    }

    // מחזיר את מזהה האולם
    public int getId() {
        return id;
    }

    // מחזיר את שם האולם
    public String getName() {
        return name;
    }

    public LocationV getVertex() {
        return vertex;
    }
}
