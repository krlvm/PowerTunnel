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
import io.github.krlvm.powertunnel.sdk.PowerTunnelServer;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Server implements PowerTunnelServer {

    private LittleProxyServer server;
    private ProxyStatus status = ProxyStatus.NOT_RUNNING;
    private final ProxyAddress address;

    private final List<PowerTunnelPlugin> plugins = new ArrayList<>();
    private final List<ProxyListener> listeners = new ArrayList<>();
    private static final int DEFAULT_LISTENER_PRIORITY = 0;

    public Server(ProxyAddress address) {
        this.address = address;
    }

    @Override
    public void start() throws ProxyStartException {
        if(this.server != null) throw new IllegalStateException("Proxy Server is already running");
        this.server = new LittleProxyServer();

        setStatus(ProxyStatus.STARTING);
        for (PowerTunnelPlugin plugin : plugins) plugin.onProxyInitialization(this.server);
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
        if(this.server == null) throw new IllegalStateException("Proxy Server has not been created");

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
            this.server.start(new CoreProxyListener());
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
        for (PowerTunnelPlugin plugin : plugins) plugin.beforeProxyStatusChanged(status);
        this.status = status;
        for (PowerTunnelPlugin plugin : plugins) plugin.onProxyStatusChanged(status);
    }

    @Override
    public int registerProxyListener(@NotNull PowerTunnelPlugin plugin, @NotNull ProxyListener listener) {
        return this.registerProxyListener(plugin, listener, DEFAULT_LISTENER_PRIORITY);
    }

    @Override
    public int registerProxyListener(@NotNull PowerTunnelPlugin plugin, @NotNull ProxyListener listener, int priority) {
        listeners.add(listener);
        return 0;
    }

    @Override
    public void unregisterProxyListener(int id) {
        // TODO: Listeners ID
    }

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

    private class CoreProxyListener implements ProxyListener {
        @Override
        public void onClientToProxyRequest(@NotNull ProxyRequest request) {
            for (ProxyListener listener : listeners) {
                listener.onClientToProxyRequest(request);
            }
        }
        @Override
        public void onProxyToServerRequest(@NotNull ProxyRequest request) {
            for (ProxyListener listener : listeners) {
                listener.onProxyToServerRequest(request);
            }
        }
        @Override
        public void onServerToProxyResponse(@NotNull ProxyResponse response) {
            for (ProxyListener listener : listeners) {
                listener.onServerToProxyResponse(response);
            }
        }
        @Override
        public void onProxyToClientResponse(@NotNull ProxyResponse response) {
            for (ProxyListener listener : listeners) {
                listener.onProxyToClientResponse(response);
            }
        }
    }
}
