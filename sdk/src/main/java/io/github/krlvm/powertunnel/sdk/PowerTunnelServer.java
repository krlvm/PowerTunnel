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

package io.github.krlvm.powertunnel.sdk;

import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import io.github.krlvm.powertunnel.sdk.types.PowerTunnelPlatform;
import io.github.krlvm.powertunnel.sdk.types.VersionInfo;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public interface PowerTunnelServer {

    /**
     * Starts proxy server
     * @throws ProxyStartException if proxy server failed to start
     */
    void start() throws ProxyStartException;

    /**
     * Stops proxy server
     */
    void stop();

    /**
     * Check whether proxy server is running
     *
     * @return whether proxy server is currently running
     */
    boolean isRunning();

    /**
     * Returns proxy server status
     *
     * @return proxy server status
     */
    @NotNull ProxyStatus getStatus();

    /**
     * Registers proxy server listener
     *
     * @param pluginInfo registrant plugin
     * @param listener proxy server listener
     * @return ID of registered listener
     */
    void registerProxyListener(@NotNull PluginInfo pluginInfo, @NotNull ProxyListener listener);

    /**
     * Registers proxy server listener
     *
     * @param pluginInfo registrant plugin
     * @param listener proxy server listeners
     * @param priority proxy server listener priority
     */
    void registerProxyListener(@NotNull PluginInfo pluginInfo, @NotNull ProxyListener listener, int priority);

    /**
     * Unregisters proxy server listener
     * @param listener registered proxy server listener
     */
    void unregisterProxyListener(@NotNull ProxyListener listener);

    /**
     * Registers server listener
     *
     * @param pluginInfo registrant plugin
     * @param listener proxy listener
     */
    void registerServerListener(@NotNull PluginInfo pluginInfo, @NotNull ServerListener listener);

    /**
     * Unregisters server listener
     * @param listener registered server listener
     */
    void unregisterServerListener(@NotNull ServerListener listener);

    /**
     * Reads plugin configuration
     *
     * @param pluginInfo plugin info
     * @return configuration store
     */
    @NotNull Configuration readConfiguration(@NotNull PluginInfo pluginInfo);

    /**
     * Saves plugin configuration
     *
     * @param pluginInfo plugin info
     * @param configuration configuration store
     */
    void saveConfiguration(@NotNull PluginInfo pluginInfo, @NotNull Configuration configuration);

    /**
     * Reads file in configurations directory as plain text
     *
     * @param filename file name
     * @return plain text
     * @throws IOException on file read error
     */
    @NotNull String readTextFile(@NotNull String filename) throws IOException;

    /**
     * Saves file in configurations directory as plain text
     *
     * @param filename file name
     * @param text plain text to write
     * @throws IOException on file read error
     */
    @NotNull void saveTextFile(@NotNull String filename, @NotNull String text) throws IOException;

    /**
     * Returns proxy server
     * If proxy server was not created, returns null
     *
     * @return proxy server
     */
    ProxyServer getProxyServer();

    /**
     * Returns PowerTunnel platform
     * @return PowerTunnel platform
     */
    PowerTunnelPlatform getPlatform();

    /**
     * Returns PowerTunnel Core version info
     * @return Core version info
     */
    VersionInfo getVersion();
}
