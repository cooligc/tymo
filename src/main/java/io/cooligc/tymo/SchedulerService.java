package io.cooligc.tymo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    private final LockManager lockManager;
    private final String instanceId = "instance-" + System.currentTimeMillis();

    public SchedulerService() {
        this(new LockManager());
    }

    public SchedulerService(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void start() {
        logger.info("Scheduler started with instance ID: {}", instanceId);
    }

    public void registerJobs() throws Exception {
        registerJobs("");
    }

    public void registerJobs(String packageName) throws Exception {
        String scanPackage = packageName == null ? "" : packageName.trim();
        String target = scanPackage.isEmpty() ? "<root>" : scanPackage;
        logger.info("Starting job registration for package: {}", target);
        List<Class<?>> jobClasses = findJobClasses(scanPackage);
        logger.info("Found {} job classes", jobClasses.size());
        for (Class<?> clazz : jobClasses) {
            logger.info("Registering job class: {}", clazz.getName());
            Object instance = clazz.getDeclaredConstructor().newInstance();
            registerTasks(instance);
        }
        if (jobClasses.isEmpty()) {
            logger.warn("No job classes found for package {}", target);
        }
    }

    private List<Class<?>> findJobClasses(String packageName) throws IOException {
        List<Class<?>> jobClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName == null ? "" : packageName.replace('.', '/');
        if (!path.isEmpty() && !path.endsWith("/")) {
            path += "/";
        }

        Enumeration<URL> resources = classLoader.getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if ("file".equals(resource.getProtocol())) {
                String decodedPath = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
                File directory = new File(decodedPath);
                if (directory.exists()) {
                    scanDirectory(directory, packageName == null ? "" : packageName, jobClasses, classLoader);
                }
            } else if ("jar".equals(resource.getProtocol())) {
                String resourcePath = resource.getPath();
                int bangIndex = resourcePath.indexOf('!');
                if (bangIndex > 0) {
                    String jarPath = resourcePath.substring(0, bangIndex);
                    if (jarPath.startsWith("file:")) {
                        jarPath = jarPath.substring(5);
                    }
                    jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8);
                    try (JarFile jar = new JarFile(jarPath)) {
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (!entry.isDirectory() && name.endsWith(".class")) {
                                String className = name.substring(0, name.length() - 6).replace('/', '.');
                                if (packageName == null || packageName.isEmpty() || className.startsWith(packageName + ".")) {
                                    tryAddJobClass(className, classLoader, jobClasses);
                                }
                            }
                        }
                    }
                }
            }
        }
        return jobClasses;
    }

    private void scanDirectory(File directory, String packageName, List<Class<?>> jobClasses, ClassLoader classLoader)
            throws IOException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String subPackage = packageName.isEmpty() ? file.getName() : packageName + "." + file.getName();
                    scanDirectory(file, subPackage, jobClasses, classLoader);
                } else if (file.isFile() && file.getName().endsWith(".class")) {
                    String className = packageName.isEmpty()
                            ? file.getName().substring(0, file.getName().length() - 6)
                            : packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    tryAddJobClass(className, classLoader, jobClasses);
                }
            }
        }
    }

    private void tryAddJobClass(String className, ClassLoader classLoader, List<Class<?>> jobClasses) {
        try {
            Class<?> clazz = Class.forName(className, false, classLoader);
            if (clazz.isAnnotationPresent(Job.class)) {
                jobClasses.add(clazz);
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Skip classes that can't be loaded or depend on missing classes
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
                        boolean hasLock = lockManager.tryAcquireLock(taskName, instanceId, annotation.maxLockTime());
                        if (!hasLock) {
                            logger.debug("Skipping task {} because lock is held by another instance", taskName);
                            return;
                        }

                        try {
                            method.setAccessible(true);
                            method.invoke(instance);
                            logger.info("Executed task: {}", taskName);
                        } catch (Exception e) {
                            logger.error("Error executing task: " + taskName, e);
                        } finally {
                            lockManager.releaseLock(taskName, instanceId);
                        }
                    };

                    ScheduledExecutorService pool = pools.computeIfAbsent(annotation.poolName(),
                            k -> new ScheduledThreadPoolExecutor(5)); // pool size 5
                    pool.scheduleWithFixedDelay(task, 0, annotation.fixedDelay(), TimeUnit.MILLISECONDS);
                    logger.info("Scheduled task: {} with fixed delay {} ms and max lock time {} ms in pool {}",
                            taskName, annotation.fixedDelay(), annotation.maxLockTime(), annotation.poolName());
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