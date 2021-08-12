/*
 * This file is part of PowerTunnel.
 *
 * PowerTunnel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PowerTunnel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerTunnel.  If not, see <https://www.gnu.org/licenses/>.
 */

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