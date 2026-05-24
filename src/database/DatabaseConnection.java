package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// אחראי על פתיחת חיבור למסד הנתונים
public class DatabaseConnection {
    // פרטי החיבור למסד הנתונים
    private static final String URL = "jdbc:sqlserver://localhost;databaseName=SchedulingWaitersProjectDB;encrypt=true;trustServerCertificate=true";
    private static final String USER = "ProjectUser";
    private static final String PASSWORD = "ProjectPass123";

    // יוצר ומחזיר חיבור פעיל למסד הנתונים
    public Connection connect() throws SQLException {
        if (USER.isEmpty()) {
            return DriverManager.getConnection(URL);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
