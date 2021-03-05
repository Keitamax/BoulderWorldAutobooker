package com.max.autobooker.utils;

/**
 * @author Maxime Rocchia
 */
public class OsUtils {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }
}
