package com.rdchandrahas.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutionManager {
    // Default: Run in background (Async)
    private static boolean async = true;
    private static boolean multiThreadingEnabled = true;

    private static ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    /**
     * Toggles between background threading and blocking execution.
     * @param enabled true for background threads, false for blocking UI thread.
     */
    public static void setAsync(boolean enabled) {
        async = enabled;
        System.out.println("Execution Mode: " + (async ? "Background Threads" : "Blocking/Sync"));
    }

    /**
     * Toggles between multi-threaded execution and single-thread execution.
     * @param enabled true for multi-threading, false for single background thread.
     */
    public static void setMultiThreading(boolean enabled) {
        if (multiThreadingEnabled == enabled) return;
        
        multiThreadingEnabled = enabled;
        ExecutorService oldExecutor = executor;
        
        if (multiThreadingEnabled) {
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        } else {
            executor = Executors.newSingleThreadExecutor();
        }
        
        if (oldExecutor != null && !oldExecutor.isShutdown()) {
            oldExecutor.shutdown();
        }
        
        System.out.println("Multi-threading Mode: " + (multiThreadingEnabled ? "Enabled" : "Disabled"));
    }

    public static boolean isMultiThreadingEnabled() {
        return multiThreadingEnabled;
    }

    public static void submit(Runnable task) {
        if (async) {
            // Run in background thread (UI remains responsive)
            executor.submit(task);
        } else {
            // Run immediately on current thread (UI freezes)
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}