package ru.krlvm.powertunnel.utilities;

import java.io.IOException;

public class SystemUtility {

    public static final String OS = System.getProperty("os.name").toLowerCase();
    //Java Swing looks perfectly with Windows 2000/XP
    public static final boolean OLD_OS = OS.contains("2003") || OS.contains("xp") || OS.contains("2000");
    public static final boolean IS_WINDOWS = OS.contains("windows");

    public static Process executeWindowsCommand(String command) throws IOException {
        return executeWindowsCommand("cmd", command);
    }

    public static Process executeWindowsCommand(String handler, String command) throws IOException {
        return Runtime.getRuntime().exec(handler + ".exe /C " + command);
    }
}
