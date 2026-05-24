package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// מייצג פתרון שיבוץ מלא
public class Schedule {
    // רשימת כל השיבוצים ומבני עזר לחיפושים מהירים
    private List<Assignment> assignments;
    private Map<Integer, List<Assignment>> eventAssignmentsMap;
    private Map<Integer, Integer> waiterAssignmentsCount;
    private Map<String, Boolean> waiterBusyCheck;
    private Map<String, Boolean> eventWaiterCheck;

    // בונה לוח שיבוץ ריק
    public Schedule() {
        this.assignments = new LinkedList<>();
        this.eventAssignmentsMap = new HashMap<>();
        this.waiterAssignmentsCount = new HashMap<>();
        this.waiterBusyCheck = new HashMap<>();
        this.eventWaiterCheck = new HashMap<>();
    }

    // מוסיף שיבוץ חדש ומעדכן את כל מבני הנתונים הפנימיים
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        int eventId = assignment.getEvent().getId();
        if (!eventAssignmentsMap.containsKey(eventId)) {
            eventAssignmentsMap.put(eventId, new LinkedList<>());
        }
        eventAssignmentsMap.get(eventId).add(assignment);

        int waiterId = assignment.getWaiter().getId();
        waiterAssignmentsCount.put(waiterId, countAssignmentsForWaiter(assignment.getWaiter()) + 1);

        String busyKey = assignment.getWaiter().getId() + "-" + assignment.getEvent().getDayOfMonth();
        waiterBusyCheck.put(busyKey, true);

        String eventWaiterKey = eventId + "-" + assignment.getWaiter().getId();
        eventWaiterCheck.put(eventWaiterKey, true);
    }

    public boolean removeAssignment(Assignment assignment) {
        if (!assignments.remove(assignment)) {
            return false;
        }

        removeFromEventMap(assignment);
        decreaseWaiterAssignmentCount(assignment.getWaiter());
        cleanBusyCheck(assignment);
        cleanEventWaiterCheck(assignment);
        return true;
    }

    // מחזיר את כל השיבוצים בלוח
    public List<Assignment> getAssignments() {
        return assignments;
    }

    // מחזיר את כל השיבוצים של אירוע מסוים
    public List<Assignment> getAssignmentsForEvent(Event event) {
        List<Assignment> result = eventAssignmentsMap.get(event.getId());
        if (result == null) {
            return new LinkedList<>();
        }
        return result;
    }

    // סופר כמה שיבוצים יש למלצר מסוים
    public int countAssignmentsForWaiter(Waiter waiter) {
        return waiterAssignmentsCount.getOrDefault(waiter.getId(), 0);
    }

    // בודק אם מלצר כבר שובץ ביום מסוים
    public boolean isWaiterAssignedOnDay(Waiter waiter, int dayOfMonth) {
        String busyKey = waiter.getId() + "-" + dayOfMonth;
        return waiterBusyCheck.containsKey(busyKey);
    }

    // בודק אם מלצר כבר שובץ לאירוע מסוים
    public boolean isWaiterAssignedToEvent(Waiter waiter, Event event) {
        String key = event.getId() + "-" + waiter.getId();
        return eventWaiterCheck.containsKey(key);
    }

    private void removeFromEventMap(Assignment assignment) {
        int eventId = assignment.getEvent().getId();
        List<Assignment> eventAssignments = eventAssignmentsMap.get(eventId);
        if (eventAssignments != null) {
            eventAssignments.remove(assignment);
            if (eventAssignments.isEmpty()) {
                eventAssignmentsMap.remove(eventId);
            }
        }
    }

    private void decreaseWaiterAssignmentCount(Waiter waiter) {
        int currentCount = waiterAssignmentsCount.getOrDefault(waiter.getId(), 0);
        if (currentCount <= 1) {
            waiterAssignmentsCount.remove(waiter.getId());
        } else {
            waiterAssignmentsCount.put(waiter.getId(), currentCount - 1);
        }
    }

    private void cleanBusyCheck(Assignment removedAssignment) {
        String busyKey = removedAssignment.getWaiter().getId()
                + "-" + removedAssignment.getEvent().getDayOfMonth();
        if (!hasAssignmentForWaiterOnDay(removedAssignment.getWaiter(),
                removedAssignment.getEvent().getDayOfMonth())) {
            waiterBusyCheck.remove(busyKey);
        }
    }

    private void cleanEventWaiterCheck(Assignment removedAssignment) {
        String eventWaiterKey = removedAssignment.getEvent().getId()
                + "-" + removedAssignment.getWaiter().getId();
        if (!hasAssignmentForWaiterInEvent(removedAssignment.getWaiter(),
                removedAssignment.getEvent())) {
            eventWaiterCheck.remove(eventWaiterKey);
        }
    }

    private boolean hasAssignmentForWaiterOnDay(Waiter waiter, int dayOfMonth) {
        for (Assignment assignment : assignments) {
            if (assignment.getWaiter().getId() == waiter.getId()
                    && assignment.getEvent().getDayOfMonth() == dayOfMonth) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAssignmentForWaiterInEvent(Waiter waiter, Event event) {
        for (Assignment assignment : getAssignmentsForEvent(event)) {
            if (assignment.getWaiter().getId() == waiter.getId()) {
                return true;
            }
        }
        return false;
    }
}
