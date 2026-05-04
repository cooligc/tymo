package io.cooligc.tymo;

public interface LockProvider {
    boolean tryAcquireLock(String taskName, String instanceId, long maxLockMillis);
    void releaseLock(String taskName, String instanceId);
}
