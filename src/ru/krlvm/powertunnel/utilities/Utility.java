package ru.krlvm.powertunnel.utilities;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.frames.LogFrame;

/**
 * PowerTunnel Utilities
 * Main utility class
 *
 * @author krlvm
 */
public class Utility {

    /**
     * Simple print assistant, that printing dually
     * to System.out and MainFrame
     *
     * With String.format automatically
     *
     * @param message - message to print
     * @param args - arguments to format
     */
    public static void print(String message, Object... args) {
        print(String.format(message, args));
    }

    /**
     * Simple print assistant, that printing dually
     * to System.out and MainFrame
     *
     * @param message - message to print
     */
    public static void print(String message) {
        if(message == null) {
            print();
            return;
        }
        System.out.println(message);
        if(!PowerTunnel.FULL_OUTPUT_MIRRORING) {
            LogFrame.print(message);
        }
    }

    /**
     * Prints a line break
     */
    public static void print() {
        print("");
    }
}
