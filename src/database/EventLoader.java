package database;

import model.Event;
import model.Venue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// טוען אירועים ואולמות ממסד הנתונים
public class EventLoader {
    // מחזיר את כל האירועים לאחר טעינה מהמסד
    public List<Event> loadEvents() {
        List<Event> events = new ArrayList<>();
        try (Connection connection = new DatabaseConnection().connect();
             PreparedStatement statement = connection.prepareStatement(eventsSql());
             ResultSet resultSet = statement.executeQuery()) {
            fillEvents(events, resultSet);
            return events;
        } catch (SQLException exception) {
            throw new RuntimeException("Could not load events from SQL Server.", exception);
        }
    }

    // מחזיר את שאילתת הטעינה של האירועים והאולמות
    private String eventsSql() {
        return """
                SELECT
                    Events.Id,
                    Events.DayOfMonth,
                    Events.GuestAmount,
                    Venues.Id AS VenueId,
                    Venues.Name AS VenueName
                FROM Events
                INNER JOIN Venues ON Events.VenueId = Venues.Id
                ORDER BY Events.DayOfMonth, Venues.Name
                """;
    }

    // עובר על תוצאות השאילתה ויוצר אובייקטים של אירועים
    private void fillEvents(List<Event> events, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            events.add(createEvent(resultSet));
        }
    }

    // יוצר אירוע אחד מתוך שורת תוצאה
    private Event createEvent(ResultSet resultSet) throws SQLException {
        return new Event(
                resultSet.getInt("Id"),
                resultSet.getInt("DayOfMonth"),
                createVenue(resultSet),
                resultSet.getInt("GuestAmount")
        );
    }

    // יוצר אולם אחד מתוך שורת תוצאה
    private Venue createVenue(ResultSet resultSet) throws SQLException {
        return new Venue(
                resultSet.getInt("VenueId"),
                resultSet.getString("VenueName")
        );
    }
}
