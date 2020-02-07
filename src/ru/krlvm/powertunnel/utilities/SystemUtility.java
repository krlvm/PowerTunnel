package ru.krlvm.powertunnel.utilities;

import java.io.IOException;

public class SystemUtility {

    public static Process executeWindowsCommand(String command) throws IOException {
        return executeWindowsCommand("cmd", command);
    }

    public static Process executeWindowsCommand(String handler, String command) throws IOException {
        return Runtime.getRuntime().exec(handler + ".exe /C " + command);
    }
}
