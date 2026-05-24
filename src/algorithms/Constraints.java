package algorithms;

import model.Assignment;
import model.Event;
import GraphUtil.Graph;
import model.Schedule;
import model.Waiter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// אחראי על כל האילוצים הקשיחים של השיבוץ
public class Constraints {
    private static final int MAX_LOCATION_PATH_WEIGHT = 60;
    private final Graph Graph = new Graph();

    // בודק האם מותר לשבץ מלצר מסוים לאירוע מסוים
    public boolean canAssign(Schedule schedule, Waiter waiter, Event event) {
        if (waiter.getId() == 0) return true;

        return isWaiterCloseEnough(waiter, event)
                && isWaiterUnderWorkLimit(schedule, waiter)
                && isWaiterFreeOnEventDate(schedule, waiter, event)
                && !isWaiterAlreadyAssignedToEvent(schedule, waiter, event);
    }

    // בודק האם כל לוח השיבוץ חוקי
    public boolean isScheduleValid(Schedule schedule, List<Event> events) {
        Set<String> waiterDayKeys = new HashSet<>();
        Set<String> eventWaiterKeys = new HashSet<>();
        Map<Integer, Integer> waiterWorkCounts = new HashMap<>();

        for (Assignment assignment : schedule.getAssignments()) {
            if (!isScheduleAssignmentValid(assignment, waiterDayKeys,
                    eventWaiterKeys, waiterWorkCounts)) {
                return false;
            }
        }

        return allEventsHaveEnoughWaiters(schedule, events);
    }

    private boolean isScheduleAssignmentValid(Assignment assignment, Set<String> waiterDayKeys,
                                              Set<String> eventWaiterKeys,
                                              Map<Integer, Integer> waiterWorkCounts) {
        return isAssignmentValid(assignment)
                && isUniqueWaiterDay(assignment, waiterDayKeys)
                && isUniqueEventWaiter(assignment, eventWaiterKeys)
                && isWaiterUnderWorkLimit(assignment, waiterWorkCounts);
    }

    // בודק האם לאירוע יש מספיק מלצרים
    public boolean eventHasEnoughWaiters(Schedule schedule, Event event) {
        return schedule.getAssignmentsForEvent(event).size() >= event.getRequiredWaiters();
    }

    // בודק האם המלצר זמין ביום של האירוע
    private boolean isWaiterAvailable(Waiter waiter, Event event) {
        return waiter.isAvailable(event.getDayOfMonth());
    }

    private boolean isWaiterCloseEnough(Waiter waiter, Event event) {
        int pathWeight = Graph.Dijkstra(waiter.getHomeVertex(), event.getVenue().getVertex());
        return pathWeight <= MAX_LOCATION_PATH_WEIGHT;
    }

    private boolean isWaiterUnderWorkLimit(Schedule schedule, Waiter waiter) {
        return schedule.countAssignmentsForWaiter(waiter) < waiter.getMaxWorkDays();
    }

    // בודק האם המלצר פנוי ביום של האירוע
    private boolean isWaiterFreeOnEventDate(Schedule schedule, Waiter waiter, Event event) {
        return !schedule.isWaiterAssignedOnDay(waiter, event.getDayOfMonth());
    }

    // בודק האם המלצר כבר שובץ לאותו אירוע
    private boolean isWaiterAlreadyAssignedToEvent(Schedule schedule, Waiter waiter, Event event) {
        return schedule.isWaiterAssignedToEvent(waiter, event);
    }

    // בודק האם שיבוץ יחיד הוא חוקי מבחינת זמינות
    private boolean isAssignmentValid(Assignment assignment) {
        Waiter waiter = assignment.getWaiter();
        if (waiter.getId() == 0) {
            return true;
        }

        return isWaiterCloseEnough(waiter, assignment.getEvent());
    }

    private boolean isWaiterUnderWorkLimit(Assignment assignment, Map<Integer, Integer> waiterWorkCounts) {
        Waiter waiter = assignment.getWaiter();
        if (waiter.getId() == 0) {
            return true;
        }

        int newCount = waiterWorkCounts.getOrDefault(waiter.getId(), 0) + 1;
        waiterWorkCounts.put(waiter.getId(), newCount);
        return newCount <= waiter.getMaxWorkDays();
    }

    // בודק שאין למלצר שני שיבוצים באותו יום
    private boolean isUniqueWaiterDay(Assignment assignment, Set<String> waiterDayKeys) {
        if (assignment.getWaiter().getId() == 0) {
            return true;
        }

        String waiterDayKey = assignment.getWaiter().getId()
                + "-" + assignment.getEvent().getDayOfMonth();
        return waiterDayKeys.add(waiterDayKey);
    }

    // בודק שאין כפילות של אותו מלצר באותו אירוע
    private boolean isUniqueEventWaiter(Assignment assignment, Set<String> eventWaiterKeys) {
        if (assignment.getWaiter().getId() == 0) {
            return true;
        }

        String eventWaiterKey = assignment.getEvent().getId()
                + "-" + assignment.getWaiter().getId();
        return eventWaiterKeys.add(eventWaiterKey);
    }

    // בודק שכל האירועים קיבלו מספיק מלצרים
    private boolean allEventsHaveEnoughWaiters(Schedule schedule, List<Event> events) {
        for (Event event : events) {
            if (!eventHasEnoughWaiters(schedule, event)) {
                return false;
            }
        }
        return true;
    }
    Graph getGraph(){
        return this.Graph;
    }
}
