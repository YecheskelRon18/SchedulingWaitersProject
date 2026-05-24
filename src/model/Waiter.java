package model;

public class Waiter {
    private static final int DAYS_IN_MONTH = 31;

    private int id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private int experienceLevel;
    private int maxWorkDays;
    private LocationV homeVertex;

    public Waiter(int id, String firstName, String lastName, String phoneNumber, int experienceLevel,
                  LocationV homeVertex, int maxWorkDays) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.experienceLevel = experienceLevel;
        this.homeVertex = homeVertex;
        this.maxWorkDays = maxWorkDays;
    }

    public static Waiter createManPowerExtra() {
        return new Waiter(0, "ManPower", "Extra", "N/A", 1,
                LocationV.ManPowerLocation, DAYS_IN_MONTH);
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    public LocationV getHomeVertex() {
        return homeVertex;
    }

    public int getMaxWorkDays() {
        return maxWorkDays;
    }

    public boolean[] getAvailableDays() {
        return new boolean[DAYS_IN_MONTH + 1];
    }

    public void addAvailableDay(int dayOfMonth) {
    }

    public boolean isAvailable(int dayOfMonth) {
        return isValidDay(dayOfMonth);
    }

    public String getAvailableDaysText() {
        return String.valueOf(maxWorkDays);
    }

    private boolean isValidDay(int dayOfMonth) {
        return dayOfMonth >= 1 && dayOfMonth <= DAYS_IN_MONTH;
    }
}
