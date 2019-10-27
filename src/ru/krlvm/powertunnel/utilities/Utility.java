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
     * Simple print assistant,
     * that printing dually to System.out and MainFrame
     *
     * @param message - message to print
     * @param args - arguments to format
     */
    public static void print(String message, Object... args) {
        if(message == null) {
            print();
            return;
        }
        String print = String.format(message, args);
        System.out.println(print);
        if(!PowerTunnel.FULL_OUTPUT_MIRRORING) {
            LogFrame.print(print);
        }
    }

    /**
     * Prints a line break
     */
    public static void print() {
        print("");
    }
}
