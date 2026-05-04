package io.cooligc.tymo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NoSqlLockProvider implements LockProvider {
    private static final Logger logger = LoggerFactory.getLogger(NoSqlLockProvider.class);

    private static class LockEntry {
        final String lockedBy;
        final long lockTime;

        LockEntry(String lockedBy, long lockTime) {
            this.lockedBy = lockedBy;
            this.lockTime = lockTime;
        }
    }

    private final ConcurrentMap<String, LockEntry> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquireLock(String taskName, String instanceId, long maxLockMillis) {
        long now = System.currentTimeMillis();
        LockEntry entry = locks.compute(taskName, (key, existing) -> {
            if (existing == null || existing.lockedBy.equals(instanceId) || (now - existing.lockTime) > maxLockMillis) {
                return new LockEntry(instanceId, now);
            }
            return existing;
        });

        boolean acquired = entry != null && entry.lockedBy.equals(instanceId);
        if (!acquired) {
            logger.debug("NoSQL lock not acquired for task {}: held by {}", taskName, entry == null ? "none" : entry.lockedBy);
        }
        return acquired;
    }

    @Override
    public void releaseLock(String taskName, String instanceId) {
        locks.computeIfPresent(taskName, (key, existing) -> {
            if (existing.lockedBy.equals(instanceId)) {
                return null;
            }
            return existing;
        });
    }
}
