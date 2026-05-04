package io.cooligc.tymo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlLockProvider implements LockProvider {
    private static final Logger logger = LoggerFactory.getLogger(SqlLockProvider.class);
    public static final String TASK_LOCK_DDL = "CREATE TABLE IF NOT EXISTS task_lock (" +
            "task_name VARCHAR(255) PRIMARY KEY, " +
            "locked_by VARCHAR(255), " +
            "lock_time BIGINT NOT NULL" +
            ")";

    private final String dbUrl = "jdbc:h2:./scheduler-ha;DB_CLOSE_DELAY=-1";
    private final String user = "sa";
    private final String password = "";

    public SqlLockProvider() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             Statement stmt = conn.createStatement()) {
            stmt.execute(TASK_LOCK_DDL);
            logger.info("Database initialized and task_lock table ensured");
        } catch (SQLException e) {
            logger.error("Error initializing SQL lock provider", e);
        }
    }

    @Override
    public boolean tryAcquireLock(String taskName, String instanceId, long maxLockMillis) {
        long now = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
            conn.setAutoCommit(false);

            PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE task_lock SET locked_by = ?, lock_time = ? " +
                            "WHERE task_name = ? AND (locked_by = ? OR lock_time < ? OR locked_by IS NULL)");
            updateStmt.setString(1, instanceId);
            updateStmt.setLong(2, now);
            updateStmt.setString(3, taskName);
            updateStmt.setString(4, instanceId);
            updateStmt.setLong(5, now - maxLockMillis);

            int updatedRows = updateStmt.executeUpdate();
            if (updatedRows == 1) {
                conn.commit();
                return true;
            }

            PreparedStatement selectStmt = conn.prepareStatement("SELECT 1 FROM task_lock WHERE task_name = ?");
            selectStmt.setString(1, taskName);
            boolean exists = selectStmt.executeQuery().next();

            if (!exists) {
                try (PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO task_lock (task_name, locked_by, lock_time) VALUES (?, ?, ?)") ) {
                    insertStmt.setString(1, taskName);
                    insertStmt.setString(2, instanceId);
                    insertStmt.setLong(3, now);
                    insertStmt.executeUpdate();
                    conn.commit();
                    return true;
                } catch (SQLException e) {
                    conn.rollback();
                    logger.debug("Lock insert failed for {}: {}", taskName, e.getMessage());
                    return false;
                }
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            logger.error("Error acquiring SQL lock for task: {}", taskName, e);
            return false;
        }
    }

    @Override
    public void releaseLock(String taskName, String instanceId) {
        try (Connection conn = DriverManager.getConnection(dbUrl, user, password);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE task_lock SET locked_by = NULL, lock_time = 0 WHERE task_name = ? AND locked_by = ?")) {
            stmt.setString(1, taskName);
            stmt.setString(2, instanceId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error releasing SQL lock for task: {}", taskName, e);
        }
    }
}
