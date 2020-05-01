package org.processmining.Guido.server;

import org.processmining.Guido.ConformanceChecker;

public class Database {
    private static ConformanceChecker cc;

    static {
        cc = new ConformanceChecker(true);
    }

    public static ConformanceChecker getConformanceChecker() {
        return cc;
    }
}
