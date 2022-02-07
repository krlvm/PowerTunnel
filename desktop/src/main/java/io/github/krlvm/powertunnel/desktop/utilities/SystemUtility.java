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

package io.github.krlvm.powertunnel.desktop.utilities;

import sun.net.dns.ResolverConfiguration;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SystemUtility {

    public static final String OS = System.getProperty("os.name").toLowerCase();
    public static final boolean IS_WINDOWS = OS.contains("windows");
    public static final boolean OLD_OS = IS_WINDOWS && (
            OS.contains("2003") ||
            OS.contains("xp") ||
            OS.contains("2000")
    );

    public static final boolean IS_TERMINAL = GraphicsEnvironment.isHeadless();

    public static Process executeWindowsCommand(String command) throws IOException {
        return executeWindowsCommand("cmd", command);
    }

    public static Process executeWindowsCommand(String handler, String command) throws IOException {
        return Runtime.getRuntime().exec(handler + ".exe /C " + command);
    }

    public static List<String> getDNSServers() {
        try {
            Class.forName("sun.net.dns.ResolverConfiguration");
            List<String> list = null;
            try {
                list = ResolverConfiguration.open().nameservers();
            } catch (Throwable ignored) {}
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (ClassNotFoundException ex) {
            return new ArrayList<>();
        }
    }
}
