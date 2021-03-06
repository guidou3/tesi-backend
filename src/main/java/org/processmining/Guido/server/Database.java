package org.processmining.Guido.server;

import org.processmining.Guido.ConformanceChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Database {
    private static ConformanceChecker cc;
    private static boolean started;

    static {
        cc = new ConformanceChecker();
        started = false;
    }

    public static ConformanceChecker getConformanceChecker() {
        return cc;
    }

    public static boolean hasStarted() {
        return started;
    }

    public static void setStarted(boolean bool) {
        started = bool;
    }
}
