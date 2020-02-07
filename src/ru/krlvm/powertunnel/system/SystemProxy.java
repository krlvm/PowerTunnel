package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.SystemUtility;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.IOException;

/**
 * Configures system's proxy settings
 * Only Windows is supported now
 *
 * TODO: Other systems support
 */
public class SystemProxy {

    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void enableProxy() {
        if (OS.contains("windows")) {
            setupProxy("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 1 /f",
                    "reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyServer /t REG_SZ /d " + PowerTunnel.SERVER_IP_ADDRESS + ":" + PowerTunnel.SERVER_PORT + " /f");

        }
    }

    public static void disableProxy() {
        if (OS.contains("windows")) {
            setupProxy("reg add \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings\" /v ProxyEnable /t REG_DWORD /d 0 /f");
        }
    }

    public static void setupProxy(final String... commands) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String command : commands) {
                        SystemUtility.executeWindowsCommand(command);
                    }
                    //we need to Internet Explorer started for apply these changes
                    try {
                        SystemUtility.executeWindowsCommand("start iexplore && ping -n 5 127.0.0.1 > NUL && taskkill /f /im iexplore.exe").waitFor();
                        Utility.print("[*] System proxy setup has finished");
                    } catch (InterruptedException ex) {
                        throw new IOException(ex);
                    }
                } catch (IOException ex) {
                    Utility.print("[x] Failed to setup system proxy: " + ex.getMessage());
                    Debugger.debug(ex);
                }
            }
        }).start();
    }
}