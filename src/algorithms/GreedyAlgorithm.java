package algorithms;

import model.*;
import GraphUtil.*;

import java.util.ArrayList;
import java.util.List;
import util.AppLogger;

public class GreedyAlgorithm implements SchedulingAlgorithm {
    Constraints Constraint = new Constraints();



    // פונקציה מקבלת רשימה של מלצרים ורשימה של אירועים
    // הפונקציה ממיינת את רשימת האירועים
    // לכל אירוע ממיינת את רשימת המלצרים לפי מרחק זמינות ורמת ניסיון
    // המערכת משבצת מלצרים לאירוע לפי רשימת המלצרים הממוינים לאירוע זה ומחזירה את הפתרון הראשון
    @Override
    public Schedule createSchedule(List<Waiter> waiters, List<Event> events) {
        AppLogger.log("GreedyAlgorithm", "build first solution");
        Schedule FirstSolution = new Schedule();
        events = sortEventsByDay(events);
        AppLogger.log("GreedyAlgorithm", "events sorted by day");
        for (Event event : events) {
            List<Waiter> sortedWaiters = sortedCandidates(waiters,FirstSolution,event);
            assignWaitersToEvent(FirstSolution, Constraint, sortedWaiters, event);
        }
        AppLogger.log("GreedyAlgorithm", "first solution finished with "
                + FirstSolution.getAssignments().size() + " assignments");
        return FirstSolution;
    }

    // מקבלת רשימת אירועים וממיינת אותם לפי יום
    private List<Event> sortEventsByDay(List<Event> events) {
        // sortEventsByDay:
        // Return a copy of the event list ordered by day of month from low to high.
        List<Event> sortedEvents = new ArrayList<>(events);
        for (int i = 0; i < sortedEvents.size() - 1; i++) {
            for (int j = 0; j < sortedEvents.size() - 1 - i; j++) {
                Event current = sortedEvents.get(j);
                Event next = sortedEvents.get(j + 1);
                if (current.getDayOfMonth() > next.getDayOfMonth()) {
                    sortedEvents.set(j, next);
                    sortedEvents.set(j + 1, current);
                }
            }
        }
        return sortedEvents;
    }

    // מקבלת את הschedule וconstraints רשימת מלצרים ואירוע
    // הפונקציה עוברת על הרשימה ולכל מלצר בודקת האם השיבוץ אפשרי, אם כן משבצת אותו אם לא, מדלגת עליו
    // הפונקציה תמשיך לשבץ מלצרים עד שהאירוע מלא או שאין מלצרים מתאימים לאירוע
    // הפונקציה לא מחזירה כלום אך מוסיפה שיבוצים לschedule

    private void assignWaitersToEvent(Schedule schedule, Constraints Constraint, List<Waiter> waiters, Event event)
    {
        int j = 0;
        while (!eventIsFull(schedule, event) && j < waiters.size())
        {
            while (j < waiters.size() && !Constraint.canAssign(schedule, waiters.get(j),event))
            {
                j++;
            }
            if (j < waiters.size())
            {
                Assignment assign = new Assignment(event, waiters.get(j));
                schedule.addAssignment(assign);
                j++;
            }
        }
    }

    //ממיינת את רשימת המלצרים באמצעות אלגוריתם merge sort
    private List<Waiter> sortedCandidates(List<Waiter> waiters, Schedule schedule, Event event) {
        List<Waiter> sorted = new ArrayList<>(waiters);
        return mergeSort(sorted, schedule, event);
    }

    private List<Waiter> mergeSort(List<Waiter> waiters, Schedule schedule, Event event) {
        if (waiters.size() <= 1) {
            return waiters;
        }
        int middle = waiters.size() / 2;
        List<Waiter> left = mergeSort(new ArrayList<>(waiters.subList(0, middle)), schedule, event);
        List<Waiter> right = mergeSort(new ArrayList<>(waiters.subList(middle, waiters.size())), schedule, event);
        return merge(left, right, schedule, event);
    }

    private List<Waiter> merge(List<Waiter> left, List<Waiter> right, Schedule schedule, Event event) {
        List<Waiter> merged = new ArrayList<>();
        int leftIndex = 0;
        int rightIndex = 0;

        while (leftIndex < left.size() && rightIndex < right.size()) {
            if (shouldSwap(left.get(leftIndex), right.get(rightIndex), schedule, event)) {
                merged.add(right.get(rightIndex++));
            } else {
                merged.add(left.get(leftIndex++));
            }
        }

        addRemaining(merged, left, leftIndex);
        addRemaining(merged, right, rightIndex);
        return merged;
    }

    private void addRemaining(List<Waiter> merged, List<Waiter> source, int startIndex) {
        for (int i = startIndex; i < source.size(); i++) {
            merged.add(source.get(i));
        }
    }

    private boolean shouldSwap(Waiter current, Waiter next, Schedule schedule, Event event) {
        int currentWorkload = schedule.countAssignmentsForWaiter(current);
        int nextWorkload = schedule.countAssignmentsForWaiter(next);
        if (currentWorkload != nextWorkload)
        {
            return currentWorkload > nextWorkload;
        }
        int currentDistance = Constraint.getGraph().Dijkstra(current.getHomeVertex(), event.getVenue().getVertex());
        int nextDistance = Constraint.getGraph().Dijkstra(next.getHomeVertex(), event.getVenue().getVertex());
        if (currentDistance != nextDistance) {
            return currentDistance > nextDistance;
        }

        return current.getExperienceLevel() < next.getExperienceLevel();
    }

    // סיום הmerge sort ופונקציות העזר שלו


    // בודק האם אירוע הוא מלא
    private boolean eventIsFull(Schedule schedule, Event event) {

        if (event.getRequiredWaiters() <= schedule.getAssignmentsForEvent(event).size()) {
            return true;
        }
        return false;
    }
}
