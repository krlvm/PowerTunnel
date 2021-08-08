package ru.krlvm.powertunnel.utilities;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.frames.LogFrame;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.*;

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
        if(LOGGER == null) {
            System.out.println(message);
        } else {
            LOGGER.info(message);
        }
        if(PowerTunnel.isUIEnabled() && (LOGGER != null || !PowerTunnel.FULL_OUTPUT_MIRRORING)) {
            LogFrame.print(message);
        }
    }

    /**
     * Prints a line break
     */
    public static void print() {
        print("");
    }

    public static void initializeLogger() {
        LOGGER = Logger.getLogger("PT");
        try {
            FileHandler handler = new FileHandler("powertunnel.log");
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return String.valueOf(record.getLevel()) + ':' +
                            record.getMessage() + '\n';
                }
            });
            LOGGER.addHandler(handler);
        } catch (Exception ex) {
            LOGGER = null;
            print("[x] Failed to initialize logger: " + ex.getMessage());
            Debugger.debug(ex);
        }
    }

    //From Apache Commons-IO
    public static void closeQuietly(Closeable is) {
        if(is != null) {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

    public static Logger LOGGER;
}
