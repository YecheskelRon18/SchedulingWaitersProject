package database;

import model.Waiter;
import model.LocationV;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// טוען את המלצרים ואת הזמינות שלהם ממסד הנתונים
public class WaiterLoader {
    private final int maxWorkDays;

    public WaiterLoader(int maxWorkDays) {
        this.maxWorkDays = maxWorkDays;
    }

    // מחזיר את כל המלצרים אחרי טעינת הפרטים והזמינות
    public List<Waiter> loadWaiters() {
        Map<Integer, Waiter> waitersById = new HashMap<>();
        try (Connection connection = new DatabaseConnection().connect();
             PreparedStatement waitersStatement = connection.prepareStatement(waitersSql());
             ResultSet waitersResult = waitersStatement.executeQuery()) {
            fillWaiters(waitersById, waitersResult);
            loadAvailability(connection, waitersById);
            return new ArrayList<>(waitersById.values());
        } catch (SQLException exception) {
            throw new RuntimeException("Could not load waiters from SQL Server.", exception);
        }
    }

    // מחזיר את שאילתת הטעינה של המלצרים
    private String waitersSql() {
        return """
                SELECT Id, FirstName, LastName, PhoneNumber, ExperienceLevel
                FROM Waiters
                ORDER BY Id
                """;
    }

    // מחזיר את שאילתת הטעינה של ימי הזמינות
    private String availabilitySql() {
        return """
                SELECT WaiterId, DayOfMonth
                FROM WaiterAvailability
                ORDER BY WaiterId, DayOfMonth
                """;
    }

    // עובר על תוצאות השאילתה ויוצר אובייקטים של מלצרים
    private void fillWaiters(Map<Integer, Waiter> waitersById, ResultSet waitersResult) throws SQLException {
        while (waitersResult.next()) {
            Waiter waiter = createWaiter(waitersResult);
            waitersById.put(waiter.getId(), waiter);
        }
    }

    // יוצר מלצר בודד משורת תוצאה של מסד הנתונים
    private Waiter createWaiter(ResultSet waitersResult) throws SQLException {
        return new Waiter(
                waitersResult.getInt("Id"),
                waitersResult.getString("FirstName"),
                waitersResult.getString("LastName"),
                waitersResult.getString("PhoneNumber"),
                waitersResult.getInt("ExperienceLevel"),
                LocationV.TEL_AVIV_JAFFA,
                maxWorkDays
        );
    }

    // טוען את ימי הזמינות ומחבר אותם למלצרים המתאימים
    private void loadAvailability(Connection connection, Map<Integer, Waiter> waitersById) throws SQLException {
        try (PreparedStatement availabilityStatement = connection.prepareStatement(availabilitySql());
             ResultSet availabilityResult = availabilityStatement.executeQuery()) {
            while (availabilityResult.next()) {
                addAvailability(waitersById, availabilityResult);
            }
        }
    }

    // מוסיף יום זמינות אחד למלצר המתאים
    private void addAvailability(Map<Integer, Waiter> waitersById, ResultSet availabilityResult) throws SQLException {
        int waiterId = availabilityResult.getInt("WaiterId");
        Waiter waiter = waitersById.get(waiterId);
        if (waiter != null) {
            waiter.addAvailableDay(availabilityResult.getInt("DayOfMonth"));
        }
    }
}
