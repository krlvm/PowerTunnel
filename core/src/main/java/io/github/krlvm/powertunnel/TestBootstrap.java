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

package io.github.krlvm.powertunnel;

import io.github.krlvm.powertunnel.plugin.PluginLoader;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;

public class TestBootstrap {

    private static final ProxyAddress ADDRESS = new ProxyAddress("0.0.0.0", 8085);

    public static void main(String[] args) {
        System.out.println("PowerTunnel Core Preview");
        System.out.println("(c) krlvm, 2019-2021");
        System.out.println();

        final Server server = new Server(ADDRESS);

        System.out.println("Loading plugins...");
        try {
            PluginLoader.loadPlugins(server);
        } catch (PluginLoadException ex) {
            ex.printStackTrace();
        }

        for (PowerTunnelPlugin plugin : server.getPlugins()) {
            final PluginInfo info = plugin.getInfo();
            System.out.printf(" - Loaded %s [%s] v%s by %s%n",
                    info.getName(), info.getId(), info.getVersion(), info.getAuthor());
        }

        System.out.println("Starting proxy server...");
        try {
            server.start();
        } catch (ProxyStartException ex) {
            System.out.println("Failed to start proxy: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("Proxy server is serving at " + ADDRESS);
    }
}
