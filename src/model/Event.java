package model;

// מייצג אירוע יחיד בחודש
public class Event {
    // פרטי האירוע
    private int id;
    private int dayOfMonth;
    private Venue venue;
    private int guestAmount;

    // בונה אובייקט של אירוע חדש
    public Event(int id, int dayOfMonth, Venue venue, int guestAmount) {
        this.id = id;
        this.dayOfMonth = dayOfMonth;
        this.venue = venue;
        this.guestAmount = guestAmount;
    }

    // מחזיר את מזהה האירוע
    public int getId() {
        return id;
    }

    // מחזיר את היום בחודש שבו מתקיים האירוע
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    // מחזיר את האולם שבו מתקיים האירוע
    public Venue getVenue() {
        return venue;
    }

    // מחזיר את מספר האורחים באירוע
    public int getGuestAmount() {
        return guestAmount;
    }

    // מחשב כמה מלצרים דרושים לפי נוסחת הפרויקט
    public int getRequiredWaiters() {
        return (int) Math.ceil(guestAmount / 12.0 + 4);
    }
}
