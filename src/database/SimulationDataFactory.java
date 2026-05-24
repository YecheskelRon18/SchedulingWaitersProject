package database;

import model.Event;
import model.LocationV;
import model.Venue;
import model.Waiter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimulationDataFactory {
    private static final String[] FIRST_NAMES = {
            "Noam", "Amit", "Daniel", "Maya", "Tamar", "Yonatan",
            "Shira", "Omer", "Yael", "Eitan", "Lior", "Adi",
            "Roni", "Itay", "Neta", "Gal", "Hila", "Ori",
            "Dana", "Yair"
    };

    private static final String[] LAST_NAMES = {
            "Cohen", "Levi", "Mizrahi", "Peretz", "Biton", "Dahan",
            "Friedman", "Azoulay", "Malka", "Avraham", "David", "Ben-David",
            "Goldberg", "Shapira", "Katz", "Mor", "Bar", "Atias",
            "Sasson", "Harel"
    };

    public List<Waiter> createWaiters(int northAmount, int centerAmount, int southAmount,
                                      int maxWorkDays, int minExperience,
                                      int maxExperience) {
        List<Waiter> waiters = new ArrayList<>();
        Random random = new Random();
        int waiterId = 1;

        waiterId = addWaitersFromVertices(waiters, random, waiterId, northAmount,
                northVertices(), maxWorkDays, minExperience, maxExperience);
        waiterId = addWaitersFromVertices(waiters, random, waiterId, centerAmount,
                centerVertices(), maxWorkDays, minExperience, maxExperience);
        addWaitersFromVertices(waiters, random, waiterId, southAmount,
                southVertices(), maxWorkDays, minExperience, maxExperience);
        return waiters;
    }

    private int addWaitersFromVertices(List<Waiter> waiters, Random random, int firstId,
                                       int amount, LocationV[] vertices, int maxWorkDays,
                                       int minExperience, int maxExperience) {
        for (int i = 0; i < amount; i++) {
            int waiterId = firstId + i;
            Waiter waiter = createWaiter(waiterId, random,
                    randomVertex(random, vertices), maxWorkDays,
                    minExperience, maxExperience);
            waiters.add(waiter);
        }
        return firstId + amount;
    }

    private Waiter createWaiter(int waiterId, Random random, LocationV homeVertex,
                                int maxWorkDays, int minExperience, int maxExperience) {
        return new Waiter(
                waiterId,
                randomFirstName(random),
                randomLastName(random),
                phoneNumberFor(waiterId),
                randomExperience(random, minExperience, maxExperience),
                homeVertex,
                maxWorkDays
        );
    }

    public List<Event> createEvents(int northAmount, int centerAmount, int southAmount,
                                    int maxEventDay, int minGuests, int maxGuests) {
        List<Event> events = new ArrayList<>();
        Random random = new Random();
        int eventId = 1;

        eventId = addEventsFromVertices(events, random, eventId, northAmount,
                northVertices(), "North", maxEventDay, minGuests, maxGuests);
        eventId = addEventsFromVertices(events, random, eventId, centerAmount,
                centerVertices(), "Center", maxEventDay, minGuests, maxGuests);
        addEventsFromVertices(events, random, eventId, southAmount,
                southVertices(), "South", maxEventDay, minGuests, maxGuests);
        return events;
    }

    private int addEventsFromVertices(List<Event> events, Random random, int firstId,
                                      int amount, LocationV[] vertices, String districtName,
                                      int maxEventDay, int minGuests, int maxGuests) {
        for (int i = 0; i < amount; i++) {
            int eventId = firstId + i;
            LocationV location = randomVertex(random, vertices);
            Venue venue = new Venue(eventId, venueName(districtName, location, eventId), location);
            events.add(new Event(eventId, randomDay(random, maxEventDay),
                    venue, guestAmount(random, minGuests, maxGuests)));
        }
        return firstId + amount;
    }

    private String venueName(String districtName, LocationV location, int eventId) {
        return districtName + " " + location.getDisplayName() + " venue " + eventId;
    }

    private int randomDay(Random random, int maxEventDay) {
        return 1 + random.nextInt(maxEventDay);
    }

    private int guestAmount(Random random, int minGuests, int maxGuests) {
        return minGuests + random.nextInt(maxGuests - minGuests + 1);
    }

    private int randomExperience(Random random, int minExperience, int maxExperience) {
        return minExperience + random.nextInt(maxExperience - minExperience + 1);
    }

    private LocationV randomVertex(Random random) {
        LocationV[] vertices = simulationHomeVertices();
        return vertices[random.nextInt(vertices.length)];
    }

    private LocationV randomVertex(Random random, LocationV[] vertices) {
        return vertices[random.nextInt(vertices.length)];
    }

    private LocationV[] simulationHomeVertices() {
        return new LocationV[]{
                LocationV.GOLAN_HEIGHTS,
                LocationV.KINNERET,
                LocationV.SAFED,
                LocationV.AKKO,
                LocationV.JEZREEL,
                LocationV.HAIFA,
                LocationV.HADERA,
                LocationV.SHARON,
                LocationV.TEL_AVIV_JAFFA,
                LocationV.PETAH_TIKVA,
                LocationV.RAMLA,
                LocationV.REHOVOT,
                LocationV.JERUSALEM,
                LocationV.ASHKELON,
                LocationV.BEERSHEBA
        };
    }

    private LocationV[] northVertices() {
        return new LocationV[]{
                LocationV.GOLAN_HEIGHTS,
                LocationV.KINNERET,
                LocationV.SAFED,
                LocationV.AKKO,
                LocationV.JEZREEL,
                LocationV.HAIFA,
                LocationV.HADERA
        };
    }

    private LocationV[] centerVertices() {
        return new LocationV[]{
                LocationV.SHARON,
                LocationV.TEL_AVIV_JAFFA,
                LocationV.PETAH_TIKVA,
                LocationV.RAMLA,
                LocationV.REHOVOT
        };
    }

    private LocationV[] southVertices() {
        return new LocationV[]{
                LocationV.JERUSALEM,
                LocationV.ASHKELON,
                LocationV.BEERSHEBA
        };
    }

    private String randomFirstName(Random random) {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    private String randomLastName(Random random) {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private String phoneNumberFor(int waiterId) {
        return "050-" + String.format("%07d", waiterId);
    }
}
