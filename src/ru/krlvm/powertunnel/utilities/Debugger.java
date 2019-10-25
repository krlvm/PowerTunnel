package ru.krlvm.powertunnel.utilities;

/**
 * PowerTunnel Debugger
 *
 * @author krlvm
 */
public class Debugger {

    private static boolean debug = false;

    /**
     * Enable or disable debug
     *
     * @param debug - true/false
     */
    public static void setDebug(boolean debug) {
        if(Debugger.debug == debug) {
            return;
        }
        String state;
        if(!debug) {
            state = "disabled";
        } else {
            state = "enabled";
        }
        Utility.print("[Debug] Debug is %s now", state);
        Debugger.debug = debug;
    }

    /**
     * Retrieve is debug enabled
     *
     * @return true if debug enabled or false if it isn't
     */
    public static boolean isDebug() {
        return debug;
    }

    /**
     * Debug message
     *
     * @param message - message
     * @param args - arguments for formatting
     */
    public static void debug(String message, Object... args) {
        if(debug) {
            Utility.print("[Debug] " + message, args);
        }
    }
}
