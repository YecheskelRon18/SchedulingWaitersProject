package algorithms;

import model.Event;
import model.Schedule;
import model.Waiter;
import util.AppLogger;

import java.util.List;

public class AlgorithmSync implements SchedulingAlgorithm {
    private GeneticAlgorithm geneticAlgorithm;
    private Constraints constraints;

    public AlgorithmSync(int maxRuns, int populationSize, double mutationRate) {
        // AlgorithmSync:
        // Create the helper objects needed to run greedy first and genetic second.
        this.geneticAlgorithm = new GeneticAlgorithm(maxRuns, populationSize, mutationRate);
        this.constraints = new Constraints();
    }

    @Override
    public Schedule createSchedule(List<Waiter> waiters, List<Event> events) {
        AppLogger.log("AlgorithmSync", "start algorithm sync");
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm();
        AppLogger.log("AlgorithmSync", "send waiters and events to greedy algorithm");
        Schedule firstSchedule = greedyAlgorithm.createSchedule(waiters,events);
        AppLogger.log("AlgorithmSync", "greedy first solution created");
        AppLogger.log("AlgorithmSync", "send greedy solution as seed to genetic algorithm");
        return geneticAlgorithm.createSchedule(waiters,events,firstSchedule);
    }
}
