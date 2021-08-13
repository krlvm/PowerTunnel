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

import io.github.krlvm.powertunnel.configuration.ConfigurationStore;
import io.github.krlvm.powertunnel.listener.CoreProxyListener;
import io.github.krlvm.powertunnel.listener.ProxyListenerInfo;
import io.github.krlvm.powertunnel.listener.ServerListenerCallback;
import io.github.krlvm.powertunnel.sdk.PowerTunnelServer;
import io.github.krlvm.powertunnel.sdk.ServerListener;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import io.github.krlvm.powertunnel.sdk.types.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.*;

public class PowerTunnel implements PowerTunnelServer {

    public static final VersionInfo VERSION = new VersionInfo(BuildConstants.VERSION, BuildConstants.VERSION_CODE);

    private LittleProxyServer server;
    private ProxyStatus status = ProxyStatus.NOT_RUNNING;
    private final ProxyAddress address;

    private final List<PowerTunnelPlugin> plugins = new ArrayList<>();

    private final Map<ServerListener, PowerTunnelPlugin> serverListeners = new HashMap<>();
    private final Map<ProxyListener, ProxyListenerInfo> proxyListeners = new LinkedHashMap<>();
    private static final int DEFAULT_LISTENER_PRIORITY = 0;

    public PowerTunnel(ProxyAddress address) {
        this.address = address;
    }

    @Override
    public void start() throws ProxyStartException {
        if(this.server != null) throw new IllegalStateException("Proxy Server is already running");
        this.server = new LittleProxyServer();

        setStatus(ProxyStatus.STARTING);
        callPluginsProxyInitializationCallback();
        try {
            this.startServer();
            setStatus(ProxyStatus.RUNNING);
        } catch (ProxyStartException ex) {
            this.stop(false);
            throw ex;
        }
    }

    @Override
    public void stop() {
        this.stop(true);
    }

    public void stop(boolean graceful) {
        if(this.server == null) throw new IllegalStateException("Proxy Server has not been initialized");

        setStatus(ProxyStatus.STOPPING);
        this.server.stop(graceful);
        setStatus(ProxyStatus.NOT_RUNNING);

        this.server = null;
    }

    private void startServer() throws ProxyStartException {
        try {
            this.server.setAddress(address);
        } catch (UnknownHostException ex) {
            throw new ProxyStartException("Failed to resolve proxy server address", ex);
        }
        try {
            this.server.start(new CoreProxyListener(proxyListeners));
        } catch (BindException ex) {
            throw new ProxyStartException("Failed to bind proxy server port", ex);
        }
    }

    @Override
    public boolean isRunning() {
        return status == ProxyStatus.RUNNING;
    }

    @Override
    public @NotNull ProxyStatus getStatus() {
        return status;
    }

    private void setStatus(ProxyStatus status) {
        callServerListeners((listener -> listener.beforeProxyStatusChanged(status)));
        this.status = status;
        callServerListeners((listener -> listener.onProxyStatusChanged(status)));
    }

    // region Proxy Listeners Management

    @Override
    public void registerProxyListener(@NotNull PowerTunnelPlugin plugin, @NotNull ProxyListener listener) {
        this.registerProxyListener(plugin, listener, DEFAULT_LISTENER_PRIORITY);
    }

    @Override
    public void registerProxyListener(@NotNull PowerTunnelPlugin plugin, @NotNull ProxyListener listener, int priority) {
        if(proxyListeners.containsKey(listener)) throw new IllegalStateException("Proxy Listener is already registered");
        // TODO: Sort listeners by priority
        proxyListeners.put(listener, new ProxyListenerInfo(plugin, priority));
    }

    @Override
    public void unregisterProxyListener(@NotNull ProxyListener listener) {
        if(!proxyListeners.containsKey(listener)) throw new IllegalStateException("Proxy Listener is not registered");
    }

    // endregion

    // region Server Listeners Management

    @Override
    public void registerServerListener(@NotNull PowerTunnelPlugin plugin, @NotNull ServerListener listener) {
        if(serverListeners.containsKey(listener)) throw new IllegalStateException("Server Listener is already registered");
        serverListeners.put(listener, plugin);
    }

    @Override
    public void unregisterServerListener(@NotNull ServerListener listener) {
        if(!serverListeners.containsKey(listener)) throw new IllegalStateException("Server Listener is not registered");
        serverListeners.remove(listener);
    }

    private void callServerListeners(@NotNull ServerListenerCallback callback) {
        for (Map.Entry<ServerListener, PowerTunnelPlugin> entry : serverListeners.entrySet()) {
            try {
                callback.call(entry.getKey());
            } catch (Exception ex) {
                // TODO: Use Logger
                System.out.printf(
                        "An error occurred in ServerListener of plugin '%s' [class=%s]: %s%n",
                        entry.getValue().getInfo().getId(), entry.getKey().getClass().getSimpleName(), ex.getMessage()
                );
                ex.printStackTrace();
            }
        }
    }

    // endregion

    @Override
    public @Nullable ProxyServer getProxyServer() {
        return this.server;
    }

    @Override
    public Configuration readConfiguration(@NotNull File file) {
        final ConfigurationStore configuration = new ConfigurationStore();
        try {
            configuration.read(file);
        } catch (IOException ex) {
            // TODO: Handle error
            ex.printStackTrace();
        }
        return configuration;
    }

    public void registerPlugin(PowerTunnelPlugin plugin) {
        this.plugins.add(plugin);
        plugin.attachServer(this);
    }

    public List<PowerTunnelPlugin> getPlugins() {
        return plugins;
    }

    private void callPluginsProxyInitializationCallback() {
        for (PowerTunnelPlugin plugin : plugins) {
            try {
                plugin.onProxyInitialization(server);
            } catch (Exception ex) {
                // TODO: Use Logger
                System.out.printf(
                        "An error occurred when plugin '%s' was handling proxy initialization: %s%n",
                        plugin.getInfo().getId(), ex.getMessage()
                );
            }
        }
    }

    @Override
    public VersionInfo getVersion() {
        return VERSION;
    }
}
