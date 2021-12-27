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

package io.github.krlvm.powertunnel.sdk.plugin;

import io.github.krlvm.powertunnel.sdk.PowerTunnelServer;
import io.github.krlvm.powertunnel.sdk.ServerListener;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public abstract class PowerTunnelPlugin {

    private PowerTunnelServer server;
    private PluginInfo info;

    private Configuration configuration;

    /**
     * Attaches PowerTunnel server instance to current server instance
     * Should be called only once after plugin initialization
     *
     * @param server PowerTunnel server instance
     */
    public final void attachServer(@NotNull PowerTunnelServer server) {
        if(this.server != null) throw new IllegalStateException("A server is already attached");
        this.server = server;
    }

    /**
     * Attaches plugin info to current plugin instance
     * @param info plugin info
     */
    public void attachInfo(@NotNull PluginInfo info) {
        if(this.info != null) throw new IllegalStateException("PluginInfo is already attached");
        this.info = info;
    }

    /**
     * Returns attached PowerTunnel server instance
     * @return attached PowerTunnel server instance
     */
    public PowerTunnelServer getServer() {
        return this.server;
    }

    /**
     * Returns information about the plugin
     * @return plugin information
     */
    public PluginInfo getInfo() {
        return info;
    }

    /**
     * Throws an exception if PowerTunnel server is not initialized
     */
    protected void validateServer() {
        if(getServer() == null) throw new IllegalStateException("Server is not initialized yet");
    }

    public void registerProxyListener(@NotNull ProxyListener listener) {
        validateServer();
        getServer().registerProxyListener(getInfo(), listener);
    }
    public void registerProxyListener(@NotNull ProxyListener listener, int priority) {
        validateServer();
        getServer().registerProxyListener(getInfo(), listener, priority);
    }

    public void registerServerListener(@NotNull ServerListener listener) {
        validateServer();
        getServer().registerServerListener(getInfo(), listener);
    }

    public Configuration readConfiguration() {
        validateServer();
        if(configuration == null) configuration = getServer().readConfiguration(getInfo());
        return configuration;
    }

    public void saveConfiguration() throws IOException {
        if(configuration == null) return;
        getServer().saveConfiguration(getInfo(), configuration);
    }

    public String readTextFile(@NotNull String filename) throws IOException {
        validateServer();
        return getServer().readTextFile(filename);
    }

    public void saveTextFile(@NotNull String filename, @NotNull String content) throws IOException {
        validateServer();
        getServer().saveTextFile(filename, content);
    }


    /**
     * Called after proxy server creation
     * and before proxy server start
     *
     * @param proxy PowerTunnel Proxy Server
     */
    public void onProxyInitialization(@NotNull ProxyServer proxy) {}

    @Override
    public String toString() {
        return "PowerTunnelPlugin{" +
                "info=" + info +
                '}';
    }
}
