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

import io.github.krlvm.powertunnel.sdk.PowerTunnelServer;
import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Server implements PowerTunnelServer {

    private LittleProxyServer server;
    private ProxyStatus status = ProxyStatus.NOT_RUNNING;

    private final List<PowerTunnelPlugin> plugins = new ArrayList<>();
    private final List<ProxyListener> listeners = new ArrayList<>();
    private static final int DEFAULT_LISTENER_PRIORITY = 0;

    @Override
    public void start() {
        if(this.server != null) throw new IllegalStateException("Proxy Server is already running");
        this.server = new LittleProxyServer();

        status = ProxyStatus.STARTING;
        for (PowerTunnelPlugin plugin : plugins) plugin.onProxyInitialization(this.server);
        this.server.start(new CoreProxyListener());
        status = ProxyStatus.RUNNING;
    }

    @Override
    public void stop() {
        if(this.server == null) throw new IllegalStateException("Proxy Server has not been created");

        status = ProxyStatus.STOPPING;
        this.server.stop();
        status = ProxyStatus.NOT_RUNNING;

        this.server = null;
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

    public void registerPlugin(PowerTunnelPlugin plugin) {
        this.plugins.add(plugin);
        plugin.attachServer(this);
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
