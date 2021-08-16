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

package io.github.krlvm.powertunnel.desktop.system.windows;

import io.github.krlvm.powertunnel.desktop.system.SystemProxy;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;

import java.io.IOException;

public class WindowsProxyHandler implements SystemProxy.Handler {

    public static boolean USE_IE = SystemUtility.OLD_OS;

    @Override
    public void enableProxy(ProxyAddress address) {
        executeAndApply(
                "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 1 /f",
                "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d " + address.getHost() + ":" + address.getPort() + " /f"
        );
    }

    @Override
    public void disableProxy() {
        executeAndApply("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 0 /f");
    }

    public static void executeAndApply(final String... commands) {
        new Thread(() -> {
            try {
                for (String command : commands) {
                    SystemUtility.executeWindowsCommand(command);
                }
                if (!USE_IE) {
                    Wininet wininet = Wininet.INSTANCE;
                    wininet.InternetSetOptionW(0,
                            wininet.INTERNET_OPTION_SETTINGS_CHANGED, null, 0);
                    wininet.InternetSetOptionW(0,
                            wininet.INTERNET_OPTION_REFRESH, null, 0);
                } else {
                    // We need to start Internet Explorer to apply the changes
                    SystemUtility.executeWindowsCommand("start iexplore && ping -n 5 127.0.0.1 > NUL && taskkill /f /im iexplore.exe");
                }
                System.out.println("System proxy initialization has been finished");
            } catch (IOException ex) {
                System.err.println("Failed to setup system proxy: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }
}
