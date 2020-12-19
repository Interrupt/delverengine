package com.interrupt.utils;

public final class OSUtils {
    private static String OS;

    static {
        OS = System.getProperty("os.name");
        if (OS == null) {
            OS = "unknown";
        }
        OS = OS.toLowerCase();
    }

    public static boolean isWindows() {
        return OS.startsWith("windows");
    }

    public static boolean isMac() {
        return OS.startsWith("mac");
    }

    public static boolean isIOS() {
        return OS.startsWith("ios");
    }

    public static boolean isAndroid() {
        String vendor = System.getProperty("java.vendor");
        if (vendor == null) {
            vendor = "unknown";
        }
        vendor = vendor.toLowerCase();

        return vendor.contains("android");
    }

    public static boolean isMobile() {
        return isAndroid() || isIOS();
    }

    public static boolean isDesktop() {
        return !isMobile();
    }
}