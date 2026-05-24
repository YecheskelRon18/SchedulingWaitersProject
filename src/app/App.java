package app;

import algorithms.Constraints;
import algorithms.Fitness;
import algorithms.AlgorithmSync;
import algorithms.SchedulingAlgorithm;
import database.SimulationDataFactory;
import model.Assignment;
import model.Event;
import GraphUtil.Graph;
import model.Schedule;
import model.Waiter;
import util.AppLogger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class App {
    private final SimulationDataFactory simulationDataFactory;
    private final Constraints constraints;
    private final Graph Graph;

    private List<Waiter> waiters;
    private List<Event> events;
    private Schedule schedule;
    private JFrame frame;

    private DefaultTableModel waitersTableModel;
    private DefaultTableModel eventsTableModel;
    private DefaultTableModel assignmentsTableModel;
    private JTextField maxRunsField;
    private JTextField populationSizeField;
    private JTextField mutationRateField;
    private JTextField northWaitersField;
    private JTextField centerWaitersField;
    private JTextField southWaitersField;
    private JTextField maxWorkDaysField;
    private JTextField northEventsField;
    private JTextField centerEventsField;
    private JTextField southEventsField;
    private JTextField minExperienceField;
    private JTextField maxExperienceField;
    private JTextField maxEventDayField;
    private JTextField minGuestsField;
    private JTextField maxGuestsField;
    private JTextArea statusArea;
    private JLabel scheduleScoreLabel;
    private JFrame logFrame;
    private DefaultTableModel logTableModel;
    private Timer logRefreshTimer;
    private int shownLogRows;

    public App() {
        this.simulationDataFactory = new SimulationDataFactory();
        this.constraints = new Constraints();
        this.Graph = new Graph();
        this.waiters = new ArrayList<>();
        this.events = new ArrayList<>();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().start());
    }

    public void start() {
        frame = new JFrame("Scheduling Waiters Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1000, 650));
        frame.setLayout(new BorderLayout());
        frame.add(createTopBar(), BorderLayout.NORTH);
        frame.add(createMainPanel(), BorderLayout.CENTER);
        frame.add(createStatusPanel(), BorderLayout.SOUTH);
        buildSimulationData();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 0, 6));
        panel.add(createLoadPanel());
        panel.add(createEventsPanel());
        panel.add(createSimulationRulesPanel());
        panel.add(createAlgorithmPanel());
        return panel;
    }

    private JPanel createLoadPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Waiter Scheduling Simulation"));
        northWaitersField = addNumberField(panel, "North:", "300");
        centerWaitersField = addNumberField(panel, "Center:", "500");
        southWaitersField = addNumberField(panel, "South:", "200");
        maxWorkDaysField = addNumberField(panel, "Max Work Days:", "23");
        panel.add(createButton("Build Simulation Data", this::buildSimulationData));
        return panel;
    }

    private JPanel createEventsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Events"));
        northEventsField = addNumberField(panel, "North:", "45");
        centerEventsField = addNumberField(panel, "Center:", "70");
        southEventsField = addNumberField(panel, "South:", "40");
        return panel;
    }

    private JPanel createSimulationRulesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Simulation Rules"));
        minExperienceField = addNumberField(panel, "Min Exp:", "1");
        maxExperienceField = addNumberField(panel, "Max Exp:", "9");
        maxEventDayField = addNumberField(panel, "Max Day:", "31");
        minGuestsField = addNumberField(panel, "Min Guests:", "180");
        maxGuestsField = addNumberField(panel, "Max Guests:", "560");
        return panel;
    }

    private JPanel createAlgorithmPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maxRunsField = addNumberField(panel, "Max Runs:", "100");
        populationSizeField = addNumberField(panel, "Population Size:", "100");
        mutationRateField = addNumberField(panel, "Mutation Rate:", "0.05");
        panel.add(createButton("Start Hybrid Schedule", this::buildSchedule));
        return panel;
    }

    private JButton createButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(event -> action.run());
        return button;
    }

    private JTextField addNumberField(JPanel panel, String label, String defaultValue) {
        JTextField field = new JTextField(defaultValue, 5);
        panel.add(new JLabel(label));
        panel.add(field);
        return field;
    }

    private JSplitPane createMainPanel() {
        createTableModels();
        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                createLeftSplit(),
                createAssignmentsPanel()
        );
        mainSplit.setResizeWeight(0.5);
        return mainSplit;
    }

    private void createTableModels() {
        waitersTableModel = new DefaultTableModel(waiterColumns(), 0);
        eventsTableModel = new DefaultTableModel(eventColumns(), 0);
        assignmentsTableModel = new DefaultTableModel(assignmentColumns(), 0);
    }

    private String[] waiterColumns() {
        return new String[]{"ID", "Name", "Phone", "Experience", "Home Vertex", "Max Work Days"};
    }

    private String[] eventColumns() {
        return new String[]{"ID", "Day", "Venue", "Location Vertex", "Guests", "Required"};
    }

    private String[] assignmentColumns() {
        return new String[]{"Day", "Venue", "Waiter", "Path Weight"};
    }

    private JSplitPane createLeftSplit() {
        JSplitPane leftSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                titledPanel("Waiters", new JScrollPane(new JTable(waitersTableModel))),
                titledPanel("Events", new JScrollPane(new JTable(eventsTableModel)))
        );
        leftSplit.setResizeWeight(0.5);
        return leftSplit;
    }

    private JPanel createAssignmentsPanel() {
        return createFinalSchedulePanel(new JScrollPane(new JTable(assignmentsTableModel)));
    }

    private JPanel titledPanel(String title, JScrollPane content) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFinalSchedulePanel(JScrollPane content) {
        JPanel panel = new JPanel(new BorderLayout());
        scheduleScoreLabel = new JLabel("Final Schedule (Fitness: 0.000)");
        panel.add(scheduleScoreLabel, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createStatusPanel() {
        statusArea = new JTextArea(4, 20);
        statusArea.setEditable(false);
        return new JScrollPane(statusArea);
    }

    private void buildSimulationData() {
        SimulationSettings settings = readSimulationSettings();
        waiters = simulationDataFactory.createWaiters(settings.northWaiters, settings.centerWaiters,
                settings.southWaiters, settings.maxWorkDays, settings.minExperience,
                settings.maxExperience);
        events = simulationDataFactory.createEvents(settings.northEvents, settings.centerEvents,
                settings.southEvents, settings.maxEventDay, settings.minGuests,
                settings.maxGuests);
        schedule = null;
        refreshLoadedData();
        setStatus(buildSimulationStatus(settings));
    }

    private SimulationSettings readSimulationSettings() {
        return new SimulationSettings(
                readWaiterAmount(northWaitersField, 300),
                readWaiterAmount(centerWaitersField, 500),
                readWaiterAmount(southWaitersField, 200),
                readMaxWorkDays(),
                readEventAmount(northEventsField, 45),
                readEventAmount(centerEventsField, 70),
                readEventAmount(southEventsField, 40),
                readMinExperience(),
                readMaxExperience(),
                readMaxEventDay(),
                readMinGuests(),
                readMaxGuests()
        );
    }

    private String buildSimulationStatus(SimulationSettings settings) {
        return "Simulation data created: " + waiters.size() + " waiters and " + events.size()
                + " events. North: " + settings.northWaiters
                + ", center: " + settings.centerWaiters
                + ", south: " + settings.southWaiters
                + ", max work days: " + settings.maxWorkDays
                + ". Events north: " + settings.northEvents
                + ", center: " + settings.centerEvents
                + ", south: " + settings.southEvents
                + ". Guests: " + settings.minGuests + "-" + settings.maxGuests
                + ", experience: " + settings.minExperience + "-" + settings.maxExperience
                + ", max event day: " + settings.maxEventDay + ".";
    }

    private void buildSchedule() {
        try {
            AppLogger.clear();
            AlgorithmSettings settings = readAlgorithmSettings();
            AppLogger.log("App", "start schedule run");
            AppLogger.log("App", "algorithm settings: " + settings.description());
            setStatus("Building schedule with greedy first, then genetic if greedy gets stuck...");
            showLogWindow();
            runScheduleInBackground(settings);
        } catch (RuntimeException exception) {
            AppLogger.log("App", "schedule run failed: " + exception.getMessage());
            setStatus("Could not build schedule: " + exception.getMessage());
            showLogWindow();
        }
    }

    private void runScheduleInBackground(AlgorithmSettings settings) {
        SwingWorker<Schedule, Void> worker = new SwingWorker<>() {
            @Override
            protected Schedule doInBackground() {
                return createSchedulingAlgorithm(settings).createSchedule(waiters, events);
            }

            @Override
            protected void done() {
                handleScheduleWorkerDone(this, settings);
            }
        };
        worker.execute();
    }

    private void handleScheduleWorkerDone(SwingWorker<Schedule, Void> worker,
                                          AlgorithmSettings settings) {
        try {
            schedule = worker.get();
            AppLogger.log("App", "schedule run finished");
            showBuiltSchedule(settings);
            refreshLogWindow();
        } catch (Exception exception) {
            AppLogger.log("App", "schedule run failed: " + exception.getMessage());
            setStatus("Could not build schedule: " + exception.getMessage());
            refreshLogWindow();
        }
    }

    private AlgorithmSettings readAlgorithmSettings() {
        return new AlgorithmSettings(readMaxRuns(), readPopulationSize(), readMutationRate());
    }

    private SchedulingAlgorithm createSchedulingAlgorithm(AlgorithmSettings settings) {
        return new AlgorithmSync(settings.maxRuns, settings.populationSize, settings.mutationRate);
    }

    private void showBuiltSchedule(AlgorithmSettings settings) {
        refreshAssignmentsTable();
        updateScheduleScore();
        boolean valid = constraints.isScheduleValid(schedule, events);
        setStatus(buildScheduleStatusMessage(settings, valid));
    }

    private void updateScheduleScore() {
        double finalScore = new Fitness().calculateScore(schedule, waiters, events);
        scheduleScoreLabel.setText("Final Schedule (Fitness: " + String.format(Locale.US, "%.3f", finalScore) + ")");
    }

    private String buildScheduleStatusMessage(AlgorithmSettings settings, boolean valid) {
        if (!valid) {
            return "Schedule created with " + settings.description()
                    + ", but some events still need backup manpower or constraints failed.";
        }
        return "Schedule created with " + settings.description()
                + ". All hard constraints passed, including graph path weight.";
    }

    private int readMaxRuns() {
        try {
            int maxRuns = Integer.parseInt(maxRunsField.getText().trim());
            if (maxRuns > 0) {
                return maxRuns;
            }
        } catch (NumberFormatException ignored) {
        }
        maxRunsField.setText("100");
        return 100;
    }

    private int readPopulationSize() {
        try {
            int populationSize = Integer.parseInt(populationSizeField.getText().trim());
            if (populationSize > 1) {
                return populationSize;
            }
        } catch (NumberFormatException ignored) {
        }
        populationSizeField.setText("100");
        return 100;
    }

    private double readMutationRate() {
        try {
            double mutationRate = Double.parseDouble(mutationRateField.getText().trim());
            if (mutationRate >= 0 && mutationRate <= 1) {
                return mutationRate;
            }
        } catch (NumberFormatException ignored) {
        }
        mutationRateField.setText("0.05");
        return 0.05;
    }

    private int readWaiterAmount(JTextField field, int defaultValue) {
        try {
            int amount = Integer.parseInt(field.getText().trim());
            if (amount >= 0) {
                return amount;
            }
        } catch (NumberFormatException ignored) {
        }
        field.setText(String.valueOf(defaultValue));
        return defaultValue;
    }

    private int readEventAmount(JTextField field, int defaultValue) {
        try {
            int amount = Integer.parseInt(field.getText().trim());
            if (amount >= 0) {
                return amount;
            }
        } catch (NumberFormatException ignored) {
        }
        field.setText(String.valueOf(defaultValue));
        return defaultValue;
    }

    private int readMaxWorkDays() {
        try {
            int maxWorkDays = Integer.parseInt(maxWorkDaysField.getText().trim());
            if (maxWorkDays >= 1 && maxWorkDays <= 31) {
                return maxWorkDays;
            }
        } catch (NumberFormatException ignored) {
        }
        maxWorkDaysField.setText("23");
        return 23;
    }

    private int readMinExperience() {
        return readAtLeast(minExperienceField, 1, 1);
    }

    private int readMaxExperience() {
        int minExperience = readMinExperience();
        int maxExperience = readAtLeast(maxExperienceField, minExperience, 9);
        maxExperienceField.setText(String.valueOf(maxExperience));
        return maxExperience;
    }

    private int readMaxEventDay() {
        return readBetween(maxEventDayField, 1, 31, 31);
    }

    private int readMinGuests() {
        return readAtLeast(minGuestsField, 1, 180);
    }

    private int readMaxGuests() {
        int minGuests = readMinGuests();
        int maxGuests = readAtLeast(maxGuestsField, minGuests, 560);
        maxGuestsField.setText(String.valueOf(maxGuests));
        return maxGuests;
    }

    private int readAtLeast(JTextField field, int minimum, int defaultValue) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value >= minimum) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        field.setText(String.valueOf(defaultValue));
        return defaultValue;
    }

    private int readBetween(JTextField field, int minimum, int maximum, int defaultValue) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value >= minimum && value <= maximum) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        field.setText(String.valueOf(defaultValue));
        return defaultValue;
    }

    private void refreshWaitersTable() {
        waitersTableModel.setRowCount(0);
        for (Waiter waiter : waiters) {
            waitersTableModel.addRow(waiterRow(waiter));
        }
    }

    private Object[] waiterRow(Waiter waiter) {
        return new Object[]{
                waiter.getId(),
                waiter.getFullName(),
                waiter.getPhoneNumber(),
                waiter.getExperienceLevel(),
                waiter.getHomeVertex().getDisplayName(),
                waiter.getMaxWorkDays()
        };
    }

    private void refreshEventsTable() {
        eventsTableModel.setRowCount(0);
        for (Event event : events) {
            eventsTableModel.addRow(eventRow(event));
        }
    }

    private Object[] eventRow(Event event) {
        return new Object[]{
                event.getId(),
                event.getDayOfMonth(),
                event.getVenue().getName(),
                event.getVenue().getVertex().getDisplayName(),
                event.getGuestAmount(),
                event.getRequiredWaiters()
        };
    }

    private void refreshAssignmentsTable() {
        clearAssignmentsTable();
        for (Assignment assignment : schedule.getAssignments()) {
            assignmentsTableModel.addRow(assignmentRow(assignment));
        }
    }

    private Object[] assignmentRow(Assignment assignment) {
        int pathWeight = assignment.getWaiter().getId() == 0
                ? 0
                : Graph.Dijkstra(
                        assignment.getWaiter().getHomeVertex(),
                        assignment.getEvent().getVenue().getVertex()
                );
        return new Object[]{
                assignment.getEvent().getDayOfMonth(),
                assignment.getEvent().getVenue().getName(),
                assignment.getWaiter().getFullName(),
                pathWeight
        };
    }

    private void refreshLoadedData() {
        refreshWaitersTable();
        refreshEventsTable();
        clearAssignmentsTable();
        scheduleScoreLabel.setText("Final Schedule (Fitness: 0.000)");
    }

    private void clearAssignmentsTable() {
        assignmentsTableModel.setRowCount(0);
    }

    private void setStatus(String message) {
        statusArea.setText(message);
    }

    private void showLogWindow() {
        closeExistingLogWindow();
        logFrame = new JFrame("Scheduling Run Logs");
        logFrame.setMinimumSize(new Dimension(750, 450));
        logFrame.setLayout(new BorderLayout());
        logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createLogTable();
        addLogWindowCloseHandler();
        logFrame.setLocationRelativeTo(frame);
        logFrame.setVisible(true);
        startLogRefreshTimer();
        refreshLogWindow();
    }

    private void createLogTable() {
        logTableModel = new DefaultTableModel(logColumns(), 0);
        shownLogRows = 0;
        logFrame.add(new JScrollPane(new JTable(logTableModel)), BorderLayout.CENTER);
    }

    private void addLogWindowCloseHandler() {
        logFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                stopLogRefreshTimer();
            }
        });
    }

    private String[] logColumns() {
        return new String[]{"Timestamp", "Where", "Log"};
    }

    private void startLogRefreshTimer() {
        logRefreshTimer = new Timer(400, event -> refreshLogWindow());
        logRefreshTimer.start();
    }

    private void refreshLogWindow() {
        if (logTableModel == null) {
            return;
        }

        List<AppLogger.LogEntry> logs = AppLogger.getLogs();
        while (shownLogRows < logs.size()) {
            AppLogger.LogEntry log = logs.get(shownLogRows);
            logTableModel.addRow(new Object[]{
                    log.getTimestamp(),
                    log.getSource(),
                    log.getMessage()
            });
            shownLogRows++;
        }
    }

    private void closeExistingLogWindow() {
        stopLogRefreshTimer();
        if (logFrame != null) {
            logFrame.dispose();
        }
    }

    private void stopLogRefreshTimer() {
        if (logRefreshTimer != null) {
            logRefreshTimer.stop();
            logRefreshTimer = null;
        }
    }

    private static class AlgorithmSettings {
        private final int maxRuns;
        private final int populationSize;
        private final double mutationRate;

        private AlgorithmSettings(int maxRuns, int populationSize, double mutationRate) {
            this.maxRuns = maxRuns;
            this.populationSize = populationSize;
            this.mutationRate = mutationRate;
        }

        private String description() {
            return "max runs: " + maxRuns
                    + ", population size: " + populationSize
                    + ", mutation rate: " + mutationRate;
        }
    }

    private static class SimulationSettings {
        private final int northWaiters;
        private final int centerWaiters;
        private final int southWaiters;
        private final int maxWorkDays;
        private final int northEvents;
        private final int centerEvents;
        private final int southEvents;
        private final int minExperience;
        private final int maxExperience;
        private final int maxEventDay;
        private final int minGuests;
        private final int maxGuests;

        private SimulationSettings(int northWaiters, int centerWaiters, int southWaiters,
                                   int maxWorkDays, int northEvents, int centerEvents,
                                   int southEvents, int minExperience, int maxExperience,
                                   int maxEventDay, int minGuests, int maxGuests) {
            this.northWaiters = northWaiters;
            this.centerWaiters = centerWaiters;
            this.southWaiters = southWaiters;
            this.maxWorkDays = maxWorkDays;
            this.northEvents = northEvents;
            this.centerEvents = centerEvents;
            this.southEvents = southEvents;
            this.minExperience = minExperience;
            this.maxExperience = maxExperience;
            this.maxEventDay = maxEventDay;
            this.minGuests = minGuests;
            this.maxGuests = maxGuests;
        }
    }
}
