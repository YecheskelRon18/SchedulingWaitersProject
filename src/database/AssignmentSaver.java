package database;

import model.Assignment;
import model.Schedule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// שומר את השיבוץ הסופי בטבלת השיבוצים במסד הנתונים
public class AssignmentSaver {
    // מפעיל את תהליך השמירה של כל השיבוצים
    public void saveAssignments(Schedule schedule) {
        String deleteSql = "DELETE FROM Assignments";
        String insertSql = "INSERT INTO Assignments (EventId, WaiterId) VALUES (?, ?)";
        saveAssignments(schedule, deleteSql, insertSql);
    }

    // פותח חיבור למסד ומעביר את פעולת השמירה לביצוע
    private void saveAssignments(Schedule schedule, String deleteSql, String insertSql) {
        try (Connection connection = new DatabaseConnection().connect()) {
            saveInTransaction(schedule, deleteSql, insertSql, connection);
        } catch (SQLException exception) {
            throw new RuntimeException("Could not save assignments to SQL Server.", exception);
        }
    }

    // מבצע את המחיקה וההכנסה של השיבוצים בתוך טרנזקציה אחת
    private void saveInTransaction(Schedule schedule,
                                   String deleteSql,
                                   String insertSql,
                                   Connection connection) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql);
             PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            connection.setAutoCommit(false);
            deleteStatement.executeUpdate();
            addAssignmentsToBatch(schedule, insertStatement);
            insertStatement.executeBatch();
            connection.commit();
        }
    }

    // מוסיף את כל השיבוצים לאצווה לפני שליחה למסד הנתונים
    private void addAssignmentsToBatch(Schedule schedule, PreparedStatement insertStatement) throws SQLException {
        for (Assignment assignment : schedule.getAssignments()) {
            insertStatement.setInt(1, assignment.getEvent().getId());
            insertStatement.setInt(2, assignment.getWaiter().getId());
            insertStatement.addBatch();
        }
    }
}
