package algorithms;

import GraphUtil.Graph;
import model.Assignment;
import model.Event;
import model.Schedule;
import model.Waiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// מחשב את איכות הפתרון לצורך האלגוריתם הגנטי
public class Fitness {
    private static final int MAX_LOCATION_PATH_WEIGHT = 60;
    private final Graph graph = new Graph();

    // מחזיר ציון כולל בין אפס לאחד
    public double calculateScore(Schedule schedule, List<Waiter> waiters, List<Event> events) {
        Constraints constraints = new Constraints();
        if (!constraints.isScheduleValid(schedule, events)) {
            return 0.0;
        }

        double manPowerScore = manPowerScore(schedule, events);
        double fairnessScore = fairnessScore(schedule, waiters);
        double experienceScore = experienceScore(schedule, waiters);
        double balanceScore = experienceBalanceScore(schedule, waiters, events);
        double distanceScore = distanceScore(schedule);
        double qualityScore = 0.35 * distanceScore + 0.20 * fairnessScore + 0.30 * experienceScore + 0.15 * balanceScore;
        double score = qualityScore * manPowerScore;

        return numRangeFix(score);
    }

    private double manPowerScore(Schedule schedule, List<Event> events) {
        double realWaiterScore = realWaiterScore(schedule, events);
        return realWaiterScore * realWaiterScore * realWaiterScore;
    }

    // מחשב את רמת הכיסוי של האירועים על ידי מלצרים אמיתיים
    private double realWaiterScore(Schedule schedule, List<Event> events) {
        int totalRequiredWaiters = 0;
        int totalRealAssignments = 0;

        for (Event event : events) {
            totalRequiredWaiters += event.getRequiredWaiters();
        }

        for (Assignment assignment : schedule.getAssignments()) {
            if (assignment.getWaiter().getId() != 0) {
                totalRealAssignments++;
            }
        }

        if (totalRequiredWaiters == 0) {
            return 1.0;
        }

        return numRangeFix((double) totalRealAssignments / totalRequiredWaiters);
    }

    // מחשב עד כמה רמת הניסיון מאוזנת בין האירועים
    private double experienceBalanceScore(Schedule schedule, List<Waiter> waiters, List<Event> events) {
        if (events.isEmpty()) {
            return 1.0;
        }

        ExperienceRange range = buildExperienceRange(schedule, events);
        double maxExperienceLevel = maxExperienceLevel(waiters);

        if (!range.found || maxExperienceLevel == 0) {
            return 1.0;
        }

        return numRangeFix(1.0 - ((range.max - range.min) / maxExperienceLevel));
    }

    // מחשב עד כמה העבודה מחולקת בצורה הוגנת בין המלצרים
    private double fairnessScore(Schedule schedule, List<Waiter> waiters) {
        Map<Integer, Integer> assignmentCounts = createAssignmentCounts(schedule);
        double totalAssignments = totalAssignments(assignmentCounts);

        if (totalAssignments == 0) {
            return 1.0;
        }

        double idealAssignments = totalAssignments / assignmentCounts.size();
        double averageDeviation = averageDeviation(assignmentCounts, idealAssignments);
        return numRangeFix(1.0 - (averageDeviation / Math.max(1.0, idealAssignments)));
    }

    // מחשב את רמת הניסיון הממוצעת של המלצרים האמיתיים בשיבוץ
    private double experienceScore(Schedule schedule, List<Waiter> waiters) {
        double totalExperience = 0;
        int realAssignments = 0;

        for (Assignment assignment : schedule.getAssignments()) {
            if (assignment.getWaiter().getId() != 0) {
                totalExperience += assignment.getWaiter().getExperienceLevel();
                realAssignments++;
            }
        }

        double maxExperienceLevel = maxExperienceLevel(waiters);
        if (realAssignments == 0 || maxExperienceLevel == 0) {
            return 0.0;
        }

        return numRangeFix((totalExperience / realAssignments) / maxExperienceLevel);
    }

    private double distanceScore(Schedule schedule) {
        int realAssignments = 0;
        double totalDistanceScore = 0;

        for (Assignment assignment : schedule.getAssignments()) {
            if (assignment.getWaiter().getId() != 0) {
                totalDistanceScore += assignmentDistanceScore(assignment);
                realAssignments++;
            }
        }

        if (realAssignments == 0) {
            return 0.0;
        }

        return numRangeFix(totalDistanceScore / realAssignments);
    }

    private double assignmentDistanceScore(Assignment assignment) {
        int distance = graph.Dijkstra(
                assignment.getWaiter().getHomeVertex(),
                assignment.getEvent().getVenue().getVertex()
        );
        return distanceToScore(distance);
    }

    private double distanceToScore(int distance) {
        if (distance < 0) {
            return 0.0;
        }
        return numRangeFix(1.0 - ((double) distance / (MAX_LOCATION_PATH_WEIGHT * 2)));
    }

    // מחזיר את רמת הניסיון הגבוהה ביותר ברשימת המלצרים
    private double maxExperienceLevel(List<Waiter> waiters) {
        double maxExperienceLevel = 0;

        for (Waiter waiter : waiters) {
            if (waiter.getId() != 0) {
                maxExperienceLevel = Math.max(maxExperienceLevel, waiter.getExperienceLevel());
            }
        }

        return maxExperienceLevel;
    }

    // מגביל את הציון לטווח שבין אפס לאחד
    private double numRangeFix(double score) {
        return Math.max(0.0, Math.min(1.0, score));
    }

    // בונה את טווח הניסיון בין האירועים בשיבוץ
    private ExperienceRange buildExperienceRange(Schedule schedule, List<Event> events) {
        ExperienceRange range = new ExperienceRange();

        for (Event event : events) {
            double avgExp = averageExperienceForEvent(schedule, event);
            if (avgExp >= 0) {
                range.include(avgExp);
            }
        }

        return range;
    }

    // מחשב את רמת הניסיון הממוצעת באירוע אחד
    private double averageExperienceForEvent(Schedule schedule, Event event) {
        double totalExp = 0;
        int realWaitersCount = 0;

        for (Assignment assignment : schedule.getAssignmentsForEvent(event)) {
            if (assignment.getWaiter().getId() != 0) {
                totalExp += assignment.getWaiter().getExperienceLevel();
                realWaitersCount++;
            }
        }

        return realWaitersCount == 0 ? -1 : totalExp / realWaitersCount;
    }

    // יוצר מפת ספירה ראשונית לכל המלצרים
    private Map<Integer, Integer> createAssignmentCounts(Schedule schedule) {
        Map<Integer, Integer> assignmentCounts = new HashMap<>();

        for (Assignment assignment : schedule.getAssignments()) {
            int waiterId = assignment.getWaiter().getId();
            if (waiterId != 0 && !assignmentCounts.containsKey(waiterId)) {
                assignmentCounts.put(waiterId, 0);
            }
        }

        fillAssignmentCounts(schedule, assignmentCounts);
        return assignmentCounts;
    }

    // ממלא את מפת הספירה לפי השיבוצים בפועל
    private void fillAssignmentCounts(Schedule schedule, Map<Integer, Integer> assignmentCounts) {
        for (Assignment assignment : schedule.getAssignments()) {
            int waiterId = assignment.getWaiter().getId();
            if (waiterId != 0 && assignmentCounts.containsKey(waiterId)) {
                assignmentCounts.put(waiterId, assignmentCounts.get(waiterId) + 1);
            }
        }
    }

    // מחשב את מספר השיבוצים הכולל
    private double totalAssignments(Map<Integer, Integer> assignmentCounts) {
        double totalAssignments = 0;

        for (int count : assignmentCounts.values()) {
            totalAssignments += count;
        }

        return totalAssignments;
    }

    // מחשב את סטיית התקן הפשוטה מול הממוצע הרצוי
    private double averageDeviation(Map<Integer, Integer> assignmentCounts, double idealAssignments) {
        double totalDeviation = 0;

        for (int count : assignmentCounts.values()) {
            totalDeviation += Math.abs(count - idealAssignments);
        }

        return totalDeviation / assignmentCounts.size();
    }

    // מחלקת עזר לשמירת ערך מינימום ומקסימום של ניסיון
    private static class ExperienceRange {
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private boolean found = false;

        // מעדכן את הטווח לפי ערך חדש
        private void include(double value) {
            found = true;
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
    }
}
