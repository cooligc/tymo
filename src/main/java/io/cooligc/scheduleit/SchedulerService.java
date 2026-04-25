package io.cooligc.scheduleit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final ConcurrentHashMap<String, ScheduledExecutorService> pools = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> instances = new ConcurrentHashMap<>();
    private final LockManager lockManager = new LockManager();
    private final String instanceId = "instance-" + System.currentTimeMillis();

    public void start() {
        logger.info("Scheduler started with instance ID: {}", instanceId);
    }

    public void registerJobs() throws Exception {
        List<Class<?>> jobClasses = findJobClasses("com.example.schedulerha");
        for (Class<?> clazz : jobClasses) {
            Object instance = clazz.getDeclaredConstructor().newInstance();
            registerTasks(instance);
        }
    }

    private List<Class<?>> findJobClasses(String packageName) throws IOException, ClassNotFoundException {
        List<Class<?>> jobClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                File directory = new File(resource.getFile());
                if (directory.exists()) {
                    scanDirectory(directory, packageName, jobClasses);
                }
            } else if (resource.getProtocol().equals("jar")) {
                String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                try (JarFile jar = new JarFile(jarPath)) {
                    Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(path) && name.endsWith(".class") && !entry.isDirectory()) {
                            String className = name.substring(0, name.length() - 6).replace('/', '.');
                            try {
                                Class<?> clazz = Class.forName(className);
                                if (clazz.isAnnotationPresent(Job.class)) {
                                    jobClasses.add(clazz);
                                }
                            } catch (ClassNotFoundException e) {
                                // Skip classes that can't be loaded
                            }
                        }
                    }
                }
            }
        }
        return jobClasses;
    }

    private void scanDirectory(File directory, String packageName, List<Class<?>> jobClasses) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Job.class)) {
                        jobClasses.add(clazz);
                    }
                }
            }
        }
    }

    public void registerTasks(Object... taskInstances) {
        for (Object instance : taskInstances) {
            String instanceKey = instance.getClass().getName() + "-" + System.identityHashCode(instance);
            instances.put(instanceKey, instance);
            Class<?> clazz = instance.getClass();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(ScheduledTask.class)) {
                    ScheduledTask annotation = method.getAnnotation(ScheduledTask.class);
                    if (annotation.fixedDelay() <= 0) {
                        throw new IllegalArgumentException("fixedDelay must be positive");
                    }

                    String taskName = clazz.getName() + "." + method.getName();
                    Runnable task = () -> {
                        if (lockManager.tryAcquireLock(taskName, instanceId)) {
                            try {
                                method.setAccessible(true);
                                method.invoke(instance);
                                logger.info("Executed task: {}", taskName);
                            } catch (Exception e) {
                                logger.error("Error executing task: " + taskName, e);
                            } finally {
                                lockManager.releaseLock(taskName, instanceId);
                            }
                        } else {
                            logger.debug("Task {} locked by another instance", taskName);
                        }
                    };

                    ScheduledExecutorService pool = pools.computeIfAbsent(annotation.poolName(),
                            k -> new ScheduledThreadPoolExecutor(5)); // pool size 5
                    pool.scheduleWithFixedDelay(task, 0, annotation.fixedDelay(), TimeUnit.MILLISECONDS);
                    logger.info("Scheduled task: {} with fixed delay {} ms in pool {}", taskName, annotation.fixedDelay(), annotation.poolName());
                }
            }
        }
    }

    public void stop() {
        pools.values().forEach(ScheduledExecutorService::shutdown);
        try {
            for (ScheduledExecutorService pool : pools.values()) {
                if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        logger.info("Scheduler stopped");
    }
}