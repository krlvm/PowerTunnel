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

package io.github.krlvm.powertunnel.desktop.system;

import io.github.krlvm.powertunnel.desktop.system.windows.WindowsProxyHandler;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;

public class SystemProxy {

    private static final Handler HANDLER;
    static {
        if(SystemUtility.IS_WINDOWS) {
            HANDLER = new WindowsProxyHandler();
        } else {
            HANDLER = new DummyHandler();
        }
    }

    public static boolean isSupported() {
        return HANDLER != null && !(HANDLER instanceof DummyHandler);
    }

    public static void enableProxy(ProxyAddress address) {
        HANDLER.enableProxy(address);
    }

    public static void disableProxy() {
        HANDLER.disableProxy();
    }

    public interface Handler {
        void enableProxy(ProxyAddress address);
        void disableProxy();
    }

    public static final class DummyHandler implements Handler {
        @Override
        public void enableProxy(ProxyAddress address) {}
        @Override
        public void disableProxy() {}
    }
}