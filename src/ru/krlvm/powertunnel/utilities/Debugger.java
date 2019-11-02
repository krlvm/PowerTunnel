package ru.krlvm.powertunnel.utilities;

/**
 * PowerTunnel Debugger
 *
 * @author krlvm
 */
public class Debugger {

    private static boolean DEBUG_ENABLED = false;

    /**
     * Enable or disable debug
     *
     * @param debug - true/false
     */
    public static void setDebug(boolean debug) {
        if(Debugger.DEBUG_ENABLED == debug) {
            return;
        }
        String state;
        if(!debug) {
            state = "disabled";
        } else {
            state = "enabled";
        }
        Utility.print("[Debug] Debug %s", state);
        Debugger.DEBUG_ENABLED = debug;
    }

    /**
     * Retrieve is debug enabled
     *
     * @return true if debug enabled or false if it isn't
     */
    public static boolean isDebug() {
        return DEBUG_ENABLED;
    }

    /**
     * Debug message
     *
     * @param message - message
     * @param args - arguments for formatting
     */
    public static void debug(String message, Object... args) {
        if(DEBUG_ENABLED) {
            Utility.print("[Debug] " + message, args);
        }
    }

    /**
     * Debug an exception - print stacktrace
     *
     * @param ex - exception
     */
    public static void debug(Exception ex) {
        if(DEBUG_ENABLED) {
            ex.printStackTrace();
        }
    }
}
