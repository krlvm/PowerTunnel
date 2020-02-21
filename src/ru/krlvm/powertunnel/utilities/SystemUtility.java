package ru.krlvm.powertunnel.utilities;

import java.io.IOException;

public class SystemUtility {

    //Java Swing looks perfectly with Windows 2000/XP
    public static final boolean OLD_OS = System.getProperty("os.name").toLowerCase().contains("xp") ||
            System.getProperty("os.name").toLowerCase().contains("2000");

    public static Process executeWindowsCommand(String command) throws IOException {
        return executeWindowsCommand("cmd", command);
    }

    public static Process executeWindowsCommand(String handler, String command) throws IOException {
        return Runtime.getRuntime().exec(handler + ".exe /C " + command);
    }
}
