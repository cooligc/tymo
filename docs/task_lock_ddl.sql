-- DDL for the task_lock table used by SqlLockProvider.
-- This schema is a pre-requisite for developers who want to configure the SQL lock store manually.
CREATE TABLE IF NOT EXISTS task_lock (
    task_name VARCHAR(255) PRIMARY KEY,
    locked_by VARCHAR(255),
    lock_time BIGINT NOT NULL
);
