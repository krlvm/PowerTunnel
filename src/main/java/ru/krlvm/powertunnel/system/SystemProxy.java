package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.system.windows.WindowsProxyHandler;
import ru.krlvm.powertunnel.utilities.SystemUtility;

/**
 * Configures system's proxy settings
 */
public class SystemProxy {

    private static SystemProxyHandler handler;

    private static void findHandler() {
        if(handler != null) {
            return;
        }
        if(SystemUtility.OS.contains("windows")) {
            handler = new WindowsProxyHandler();
        }
        if(handler == null) {
            handler = new DummyProxyHandler();
        }
    }

    public static boolean isCompatible() {
        return !(handler instanceof DummyProxyHandler);
    }

    public static void enableProxy() {
        findHandler();
        handler.enableProxy();
    }

    public static void disableProxy() {
        findHandler();
        handler.disableProxy();
    }

    public interface SystemProxyHandler {
        void enableProxy();
        void disableProxy();
    }

    public static final class DummyProxyHandler implements SystemProxyHandler {
        @Override
        public void enableProxy() {}
        @Override
        public void disableProxy() {}
    }
}