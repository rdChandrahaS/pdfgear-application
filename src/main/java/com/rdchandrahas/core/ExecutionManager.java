package com.rdchandrahas.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutionManager {
    private static final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    public static void submit(Runnable task) {
        executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}