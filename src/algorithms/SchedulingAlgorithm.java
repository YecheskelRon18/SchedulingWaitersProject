package algorithms;

import model.Event;
import model.Schedule;
import model.Waiter;

import java.util.List;

// ממשק משותף לכל אלגוריתם שיבוץ במערכת
public interface SchedulingAlgorithm {
    // מקבל מלצרים ואירועים ומחזיר לוח שיבוץ מלא
    Schedule createSchedule(List<Waiter> waiters, List<Event> events);

}
