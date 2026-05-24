package algorithms;

import genetic.Chromosome;
import genetic.Population;
import model.Assignment;
import model.Event;
import model.Schedule;
import model.Waiter;
import util.AppLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.List;

public class GeneticAlgorithm implements SchedulingAlgorithm {
    private int maxRuns;
    private int populationSize;
    private double mutationRate;
    Random rand = new Random();
    Constraints Constraint = new Constraints();
    Fitness fitness = new Fitness();
    GreedyAlgorithm Gready = new GreedyAlgorithm();

    public GeneticAlgorithm(int maxRuns, int populationSize, double mutationRate) {
        // GeneticAlgorithm:
        // Store the run count, population size, and mutation rate chosen by the user.
        this.maxRuns = maxRuns;
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;

    }
    // בונה Schedule ללא seedSchedule
    // קורא לפונקציה עם אותו השם אך מכניס באופציה של SeedSchedule  את הערך NULL
    @Override
    public Schedule createSchedule(List<Waiter> waiters, List<Event> events) {
        return (createSchedule(waiters,events,null));
    }

    // הפונקציה מקבלת רשימת מלצרים רשימת אירועים וschedule שאם הוא לא NULL עליו נבנה הפופולציה החדשה וממנו נבחר הכרומוזום הטוב ביותר
    // הפונקציה בודקת האם הלוח שיבוצים שקיבלנו הוא null אם כן היא קוראת לפונקציה createSchedule בקלאס greadyAlgorithm
    //הפונקציה לאחר מכן משתמש בSchedule שיש לנו כדי לבנות אוכלוסיה
    // לאחר מכן הפונקציה משפרת את האוכלוסיה ולבסוף מחזירה את הכרומוזום הטוב ביותר ממנה
    public Schedule createSchedule(List<Waiter> waiters, List<Event> events, Schedule seedSchedule) {

        if(seedSchedule == null)
        {
        AppLogger.log("GeneticAlgorithm", "no seed schedule received, creating greedy seed");
        seedSchedule = Gready.createSchedule(waiters,events);
        }
        double greedyFitness = fitness.calculateScore(seedSchedule, waiters, events);
        AppLogger.log("GeneticAlgorithm", "greedy fitness before genetic algorithm: " + greedyFitness);
        AppLogger.log("GeneticAlgorithm", "create initial population");
        Population population;
        population = createInitialPopulation(waiters,events,seedSchedule);
        AppLogger.log("GeneticAlgorithm", "start evolving population");
        population = evolvePopulation(population,waiters,events);
        logFinalComparison(greedyFitness, population);
        AppLogger.log("GeneticAlgorithm", "return best schedule from final population");
        return population.getBestChromosome().getSchedule();
    }


    // הפונקציה מקבל Schedule רשימת מלצרים ורשימת אירועים
    //הפונקציה יוצרת אוכלסיה חדשה ריקה ומוסיפה אליה את הלוח שיבוץ שקיבלה
    // לאחר מכן חצי מהאוכלוסיה נוצרת באמצעות אקראיות מוחלטת
    // והחצי השני שומר את השיבוצים החוקיים ומנסה לשבץ שיבוצים אקראיים לשאר המקומות הפנויים
    // כל הלוחות שיבוצים/ כרומוזומים נשמרים לאוכלוסיה והפונקציה מחזירה את האוכלוסיה הזאת
    private Population createInitialPopulation(List<Waiter> waiters, List<Event> events, Schedule seedSchedule) {

        Population FirstPopulation = new Population();
        addScheduleToPopulation(FirstPopulation, seedSchedule,waiters,events);
        AppLogger.log("GeneticAlgorithm", "seed chromosome added to population");
        Schedule tempSchedule = new Schedule();
        for(int i = 1; i < this.populationSize; i++)
        {
           if (i <  populationSize / 5) {
               tempSchedule = createRandomScheduleFromSeed(seedSchedule, waiters, events);
           }
           else {
               tempSchedule = createRandomSchedule(waiters,events);
           }
           addScheduleToPopulation(FirstPopulation, tempSchedule,waiters,events);
        }
        AppLogger.log("GeneticAlgorithm", "initial population finished with "
                + FirstPopulation.size() + " chromosomes");
        return FirstPopulation;
    }


    // מקבלת אוכלוסיה, לוח שיבוץ, רשימת מלצרים ורשימת אירועים
    // הפונקציה מחשבת את דירוג הפתרון (fitness) הופכת את הSchedule לכרומוזום ומוסיפה את הכרומוזום לאוכלוסיה
    private void addScheduleToPopulation(Population population, Schedule schedule, List<Waiter> waiters, List<Event> events) {
        // addScheduleToPopulation:
        // Calculate the fitness score of a schedule and store it as a chromosome.
        Chromosome chromosome = new Chromosome(schedule,fitness.calculateScore(schedule,waiters,events));
        population.addChromosome(chromosome);
    }
    // הפונקציה מקבלת רשימת מלצרים ואירועים ויוצרת Schedule חדש אקראי וחוקי
    // הפונקציה מחזירה את הSchedule החדש
    private Schedule createRandomSchedule(List<Waiter> waiters, List<Event> events) {
        Schedule schedule = new Schedule();
        for(Event event : events)
        {
            assignRandomWaiters(schedule, event,waiters);
        }
        return schedule;
    }

    // הפונקציה מקבל Schedule רשמת מלצרים ואירועים
    // הפונקציה יוצרת Schedule חדש מעתיקה אליו את כל השיבוצים החוקיים ומשבצת באקראיות מלצרים באירועים בהם חסרים מלצרים
    private Schedule createRandomScheduleFromSeed(Schedule seedSchedule, List<Waiter> waiters, List<Event> events) {

        Schedule schedule = new Schedule();
        for(Event event: events)
        {
            List<Assignment> assignList = seedSchedule.getAssignmentsForEvent(event);
            for(Assignment assign: assignList)
            {
                if(Constraint.canAssign(schedule,assign.getWaiter(),event)){
                    schedule.addAssignment(new Assignment(event, assign.getWaiter()));
                }
            }
            if(!eventIsFull(schedule,event)){
                assignRandomWaiters(schedule,event,waiters);
            }
        }
        return schedule;
    }


    // מקבל אירוע רשימת מלצרים וSchedule
    // הפונקציה תקרא לפונקציה שתערבב את רשימת המלצרים כדי לקבל באירועים שונים מלצרים אחרים שישובצו קודם
    // יוצר שיבוצים אקראים וחוקיים לאירוע
    // הפונקציה תמשיך לשבץ כל עוד האירוע לא מלא ומספר ההרצות לא עלה על מספר המלצרים ברשימה
    private void assignRandomWaiters(Schedule schedule, Event event, List<Waiter> waiters)
    {
        int i =0;
        List<Waiter> ShuffledWaiters = ShuffleList(waiters);
            while(!eventIsFull(schedule, event) && i < ShuffledWaiters.size())
            {
                Waiter waiter = ShuffledWaiters.get(i);
                if(Constraint.canAssign(schedule,waiter, event)){
                    Assignment assign = new Assignment(event, waiter);
                    schedule.addAssignment(assign);
                }
                i++;
            }

    }
    // מערבב את הרשימה של המלצרים
    private List<Waiter> ShuffleList(List<Waiter> waiters) {
        List<Waiter> shuffledWaiters = new ArrayList<>(waiters);
        Collections.shuffle(shuffledWaiters, rand);
        return shuffledWaiters;
    }








    // הפונקציה מקבל אוכלוסיה
    // הפונקציה מתוך האוכלוסיה תבחר שני הורים (כרומוזומים) רנדומלים ומתוכם תבחר בטוב ביותר ותחזיר אותו

    private Chromosome selectParent(Population population) {
        List<Chromosome> ChromoList = population.getChromosomes();
        int num1 = rand.nextInt(0, ChromoList.size());
        int num2 = rand.nextInt(0, ChromoList.size());
        if(ChromoList.get(num1).getFitnessScore() > ChromoList.get(num2).getFitnessScore() )
        {
            return ChromoList.get(num1);
        }
        return ChromoList.get(num2);
    }


    // פונקציה זו מקבלת שתי כרומוזומים שיהיו הורים, רשימת מלצרים רשימת אירועים
    // הפונקציה באקראיות כל פעם תבחר הורה אחר שממנו היא תעתיק ללוח שיבוצים חדש את השיבוצים  של אותו הורה לאירוע מסויים
    // הפונקציה תעבור על כל אירוע ברשימת האירועים
    // הפונקציה תחזיר את הSchedule של שני ההורים לאחר ביצוע של הפונקציה Repair עליו
    private Schedule crossover(Chromosome firstParent, Chromosome secondParent, List<Waiter> waiters, List<Event> events) {
        Schedule Child = new Schedule();
        for(Event event: events){
            if(rand.nextBoolean()){
                copyEventAssignments(firstParent.getSchedule(),Child,event);
            }
            else{
                copyEventAssignments(secondParent.getSchedule(),Child,event);
            }
        }
        return repair(Child,waiters,events);
    }


    // הפונקציה מקבלת schedule רשימת מלצרים ואירועים
    // הפונקציה יוצר Schedule חדש
    // משם היא עוברת על כל אירוע ומגרילה מספר בין 0 ל 1.0
    // אם המספר בטווח של Mutation Rate אז לאותו אירוע ישובצו מלצרים רנדומלים
    // אחרת השיבוצים לאירוע הנל יעתקו לSchedule החדש
    // הפונקציה מחזירה Schedule
    private Schedule mutate(Schedule schedule, List<Waiter> waiters, List<Event> events) {
        Schedule mutateSchedule = copySchedule(schedule, events);
        if (!events.isEmpty() && rand.nextDouble() < mutationRate) {
            Event event = events.get(rand.nextInt(events.size()));
            replaceOneWaiter(mutateSchedule, event, waiters);
        }
        return mutateSchedule;
    }

    private Schedule copySchedule(Schedule sourceSchedule, List<Event> events) {
        Schedule copiedSchedule = new Schedule();
        for (Event event : events) {
            copyEventAssignments(sourceSchedule, copiedSchedule, event);
        }
        return copiedSchedule;
    }

    private void replaceOneWaiter(Schedule schedule, Event event, List<Waiter> waiters) {
        removeRandomAssignment(schedule, event);
        tryAddReplacementWaiter(schedule, event, waiters);
    }

    private void removeRandomAssignment(Schedule schedule, Event event) {
        List<Assignment> eventAssignments = new ArrayList<>(schedule.getAssignmentsForEvent(event));
        if (!eventAssignments.isEmpty()) {
            Assignment assignment = eventAssignments.get(rand.nextInt(eventAssignments.size()));
            schedule.removeAssignment(assignment);
        }
    }

    private void tryAddReplacementWaiter(Schedule schedule, Event event, List<Waiter> waiters) {
        for (Waiter waiter : ShuffleList(waiters)) {
            if (Constraint.canAssign(schedule, waiter, event)) {
                schedule.addAssignment(new Assignment(event, waiter));
                return;
            }
        }
    }


    // הפונקציה מקבלת Schedule רשימת מלצרים ורשימת אירועים
    // הפונקציה יוצרת Schedule חדש
    // לכל אירוע ברשימת האירועים הפונקציה תעתיק אל לוח השיבוצים החדש את כל השיבוצים החוקיים שיש בלוח המקורי לאותו אירוע
    // לאחר מכן אם האירוע לא מלא הוא מנסה למלא אותו באמצעות backtracking
    // אם האירוע עדיין לא מלא הוא מוסיף מלצרים מחברת כוח אדם
    private Schedule repair(Schedule schedule, List<Waiter> waiters, List<Event> events) {
        Schedule RepairSchedule = new Schedule();

        for(Event event: events){
            copyRepairAssignments(schedule,RepairSchedule,event);
            if (!eventIsFull(RepairSchedule, event)) {
                backtrackFillEvent(RepairSchedule, event,
                        backtrackingCandidates(RepairSchedule, event, waiters), 0);
            }
            if (!eventIsFull(RepairSchedule, event)) {
                fillWithExtraManPower(RepairSchedule, event);
            }
        }

        return RepairSchedule;
    }


    // הפונקציה מקבלת לוח שיבוצים קיים ולוח שיבוצים ריק
    // אל הלוח הריק הפוקציה תכניס את כל השיבוצים החוקיים שיש בלוח המקורי עד שהאירוע מלא או עד שנגמר השיבוצים
    // הפונקציה לא מחזירה כלום אלא משנה את הנתונים בtargetSchedule
    private void copyEventAssignments(Schedule sourceSchedule, Schedule targetSchedule, Event event) {
        List<Assignment> SourceEventAssignList = sourceSchedule.getAssignmentsForEvent(event);

           for(Assignment assignment: SourceEventAssignList){
               if(eventIsFull(targetSchedule,event)){
                   return;
               }
               if(Constraint.canAssign(targetSchedule,assignment.getWaiter(),event)){
                   Assignment NewAssign = new Assignment(event, assignment.getWaiter());
                   targetSchedule.addAssignment(NewAssign);
               }
           }

    }


    // פונקציה זו מקבלת אוכלוסיה רשימת מלצרים ורשימת אירועים
    // הפונקציה תרוץ maxRuns פעמים כשכל פעם היא משדרגת את האוכלוסיה ומחזירה לבסוף את האוכלוסיה המשודרגת
    private Population evolvePopulation(Population population, List<Waiter> waiters, List<Event> events) {
        for(int i = 0; i < maxRuns; i++){
            population = nextGeneration(population,waiters,events);
            logGenerationStats(i, population);
        }
        return population;
    }

    private void logGenerationStats(int runIndex, Population population) {
        Chromosome best = population.getBestChromosome();
        AppLogger.log("GeneticAlgorithm", "run " + (runIndex + 1)
                + " out of " + maxRuns
                + ", best fitness: " + best.getFitnessScore()
                + ", average fitness: " + averageFitness(population)
                + ", ManPower assignments: " + countManPowerAssignments(best.getSchedule()));
    }

    private double averageFitness(Population population) {
        double totalFitness = 0;
        for (Chromosome chromosome : population.getChromosomes()) {
            totalFitness += chromosome.getFitnessScore();
        }
        return population.size() == 0 ? 0 : totalFitness / population.size();
    }

    private int countManPowerAssignments(Schedule schedule) {
        int count = 0;
        for (Assignment assignment : schedule.getAssignments()) {
            if (assignment.getWaiter().getId() == 0) {
                count++;
            }
        }
        return count;
    }

    private void logFinalComparison(double greedyFitness, Population population) {
        double geneticFitness = population.getBestChromosome().getFitnessScore();
        AppLogger.log("GeneticAlgorithm", "final genetic fitness: " + geneticFitness
                + ", improvement over greedy: " + (geneticFitness - greedyFitness));
    }






    // הפונקציה מקבלת אוכלוסיה רשימת מלצרים ורשימת אירועים
    // הפונקציה ממיינת את הכרומוזומים באוכלוסיה לפי דירוג הFitness שלהם
    // הפונקציה יוצרת אוכלוסיה חדשה ומוסיפה אליה את הכרומוזום ההכי טוב של האוכלוסיה הנוכחית
    // הפונקציה תמלא את שאר הכרומוזומים באוכלוסיה החדשה באמצעות fillNextPopulation
    private Population nextGeneration(Population population, List<Waiter> waiters, List<Event> events) {

        population.sortByFitness();
        Population newPopulation = new Population();
        Chromosome bestChromosome = population.getBestChromosome();
        newPopulation.addChromosome(bestChromosome);
        fillNextPopulation(newPopulation,population,waiters,events);

        return newPopulation;
    }


    // הפונקציה מקבלת אוכלוסיה הבאה, אוכלוסיה נוכחית, רשימת מלצרים ורשימת אירועים
    // כל עוד גודל האוכלסיה הבאה קטנה מגודל האוכלוסיה הנוכחית זה יוסיף לאוכלוסיה החדשה כרומוזום ילד שנוצר על ידי הפונקציה
    // createChildChromosome

    private void fillNextPopulation(Population nextPopulation, Population population, List<Waiter> waiters, List<Event> events) {


        while(nextPopulation.size() < populationSize){
            nextPopulation.addChromosome(createChildChromosome(population,waiters,events));
        }
    }
    // פונקציה מקבלת אוכלוסיה רשימת מלצרים ורשימת אירועים
    // הפונקציה בוחרת שני הורים מהאוכלוסיה ויוצרת Schdule חדש
    // את הלוח שיבוץ החדש יוצרת הפונקציה Crossover שמחזירה לוח שיבוצים הבנוי משני ההורים
    // לאחר מכן הלוח הילד נשלח לפונקציה Mutate שתשנה את חלק מהשיבוצים
    // ולבסוף הלוח שיבוצים יתוקן על ידי repair שתתקן את כל השיבוצים החסרים בלוח
    private Chromosome createChildChromosome(Population population, List<Waiter> waiters, List<Event> events) {

        Chromosome parent1 = selectParent(population);
        Chromosome parent2 = selectParent(population);
        Schedule childSchedule = crossover(parent1,parent2,waiters,events);
        childSchedule = mutate(childSchedule,waiters,events);
        childSchedule = repair(childSchedule,waiters,events);
        Chromosome ChildChromosome = new Chromosome(childSchedule,fitness.calculateScore(childSchedule,waiters,events));
        return ChildChromosome;
    }


    // פונקציה מקבלת לוח שיבוצים מקור ולוח שיבוצים שעליו נעבוד, בנוסף גם תקבל אירוע
    // הפונקציה עוברת על כל שיבוץ באירוע event וכל עוד לא נגמר השיבוצים והאירוע לא מלא
    // הפונקציה תבדוק האם שיבוץ הנל אפשרי אם כן היא תוסיף אותו
    // אם לא היא תעבור הלאה
    private void copyRepairAssignments(Schedule sourceSchedule, Schedule targetSchedule, Event event) {

        List<Assignment> assignmentsList = sourceSchedule.getAssignmentsForEvent(event);
        int i =0;
        while((!eventIsFull(targetSchedule,event)) && (i < assignmentsList.size())){
            if(Constraint.canAssign(targetSchedule,assignmentsList.get(i).getWaiter(),
                    assignmentsList.get(i).getEvent()))
            {
                targetSchedule.addAssignment(new Assignment(event, assignmentsList.get(i).getWaiter()));
            }
            i++;
        }
    }


    // פונקציה זו מקבלת Schedule ואירוע
    // כל עוד האירוע לא מלא היא תשבץ מלצרים מחברת כוח אדם
    private void fillWithExtraManPower(Schedule schedule, Event event) {
        while(!eventIsFull(schedule,event)){
            schedule.addAssignment(new Assignment(event, Waiter.createManPowerExtra()));
        }
    }

    private List<Waiter> backtrackingCandidates(Schedule schedule, Event event, List<Waiter> waiters) {
        List<Waiter> candidates = new ArrayList<>();
        for (Waiter waiter : ShuffleList(waiters)) {
            if (Constraint.canAssign(schedule, waiter, event)) {
                candidates.add(waiter);
            }
        }
        return candidates;
    }

    private boolean backtrackFillEvent(Schedule schedule, Event event, List<Waiter> candidates, int index) {
        if (eventIsFull(schedule, event)) {
            return true;
        }
        if (candidates.size() - index < event.getRequiredWaiters() - schedule.getAssignmentsForEvent(event).size()) {
            return false;
        }
        return tryBacktrackingCandidates(schedule, event, candidates, index);
    }

    private boolean tryBacktrackingCandidates(Schedule schedule, Event event, List<Waiter> candidates, int index) {
        for (int i = index; i < candidates.size(); i++) {
            Assignment assignment = new Assignment(event, candidates.get(i));
            schedule.addAssignment(assignment);
            if (backtrackFillEvent(schedule, event, candidates, i + 1)) {
                return true;
            }
            schedule.removeAssignment(assignment);
        }
        return false;
    }
    // פונקציה מקבל אירוע וschedule ומחזיקה TRUE אם באירוע יש מספיק מלצרים וFALSE אם האירוע לא מכיל מספיק מלצרים
    private boolean eventIsFull(Schedule schedule, Event event) {
        // eventIsFull:
        // Check whether the event already has the required number of assigned waiters.
        if (event.getRequiredWaiters() <= schedule.getAssignmentsForEvent(event).size()) {
            return true;
        }
        return false;
    }
}
