package io.cooligc.tymo;

import java.util.Objects;

public class LockManager {
    private final LockProvider provider;

    public LockManager() {
        this(new SqlLockProvider());
    }

    public LockManager(LockProvider provider) {
        this.provider = Objects.requireNonNull(provider, "LockProvider cannot be null");
    }

    public boolean tryAcquireLock(String taskName, String instanceId, long maxLockMillis) {
        return provider.tryAcquireLock(taskName, instanceId, maxLockMillis);
    }

    public void releaseLock(String taskName, String instanceId) {
        provider.releaseLock(taskName, instanceId);
    }

    public static LockManager sql() {
        return new LockManager(new SqlLockProvider());
    }

    public static LockManager noSql() {
        return new LockManager(new NoSqlLockProvider());
    }
}
