package model;

// מייצג שיבוץ של מלצר לאירוע
public class Assignment {
    // רכיבי השיבוץ
    private Event event;
    private Waiter waiter;

    // בונה שיבוץ חדש
    public Assignment(Event event, Waiter waiter) {
        this.event = event;
        this.waiter = waiter;
    }

    // מחזיר את האירוע של השיבוץ
    public Event getEvent() {
        return event;
    }

    // מחזיר את המלצר של השיבוץ
    public Waiter getWaiter() {
        return waiter;
    }
}
