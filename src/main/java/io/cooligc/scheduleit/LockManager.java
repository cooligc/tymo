package io.cooligc.scheduleit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LockManager {
    private static final Logger logger = LoggerFactory.getLogger(LockManager.class);
    private final String dbUrl = "jdbc:h2:./scheduler-ha;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
    private final String user = "sa";
    private final String password = "";

    public LockManager() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS task_locks (" +
                    "task_name VARCHAR(255) PRIMARY KEY, " +
                    "locked_by VARCHAR(255), " +
                    "lock_time BIGINT)");
            logger.info("Database initialized");
        } catch (SQLException e) {
            logger.error("Error initializing database", e);
        }
    }

    public boolean tryAcquireLock(String taskName, String instanceId) {
        long now = System.currentTimeMillis();
        long lockTimeout = 30000; // 30 seconds

        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            conn.setAutoCommit(false);

            // Try to acquire lock
            PreparedStatement selectStmt = conn.prepareStatement(
                    "SELECT locked_by, lock_time FROM task_locks WHERE task_name = ?");
            selectStmt.setString(1, taskName);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                String lockedBy = rs.getString("locked_by");
                long lockTime = rs.getLong("lock_time");

                if (lockedBy.equals(instanceId) || (now - lockTime) > lockTimeout) {
                    // Own lock or expired, update
                    PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE task_locks SET locked_by = ?, lock_time = ? WHERE task_name = ?");
                    updateStmt.setString(1, instanceId);
                    updateStmt.setLong(2, now);
                    updateStmt.setString(3, taskName);
                    updateStmt.executeUpdate();
                    conn.commit();
                    return true;
                } else {
                    return false; // Locked by another instance
                }
            } else {
                // No lock, insert
                PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO task_locks (task_name, locked_by, lock_time) VALUES (?, ?, ?)");
                insertStmt.setString(1, taskName);
                insertStmt.setString(2, instanceId);
                insertStmt.setLong(3, now);
                insertStmt.executeUpdate();
                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error acquiring lock for task: " + taskName, e);
            return false;
        }
    }

    public void releaseLock(String taskName, String instanceId) {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE task_locks SET locked_by = NULL, lock_time = 0 WHERE task_name = ? AND locked_by = ?")) {
            stmt.setString(1, taskName);
            stmt.setString(2, instanceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error releasing lock for task: " + taskName, e);
        }
    }
}