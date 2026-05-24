package util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppLogger {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final List<LogEntry> logs = new ArrayList<>();

    public static synchronized void clear() {
        logs.clear();
    }

    public static synchronized void log(String source, String message) {
        logs.add(new LogEntry(LocalTime.now().format(TIME_FORMAT), source, message));
    }

    public static synchronized List<LogEntry> getLogs() {
        return new ArrayList<>(logs);
    }

    public static class LogEntry {
        private final String timestamp;
        private final String source;
        private final String message;

        public LogEntry(String timestamp, String source, String message) {
            this.timestamp = timestamp;
            this.source = source;
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getSource() {
            return source;
        }

        public String getMessage() {
            return message;
        }
    }
}
