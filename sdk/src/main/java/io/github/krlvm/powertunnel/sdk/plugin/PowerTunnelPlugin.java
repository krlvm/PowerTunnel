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
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class PowerTunnelPlugin implements ServerListener {

    private PowerTunnelServer server;

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
     * Returns attached PowerTunnel server instance
     *
     * @return attached PowerTunnel server instance
     */
    @Nullable
    public PowerTunnelServer getServer() {
        return this.server;
    }

    /**
     * Throws an exception if PowerTunnel server is not initialized
     */
    protected void validateServer() {
        if(getServer() == null) throw new IllegalStateException("Server is not initialized yet");
    }

    public int registerProxyListener(@NotNull ProxyListener listener) {
        validateServer();
        return getServer().registerProxyListener(this, listener);
    }
    public int registerProxyListener(@NotNull ProxyListener listener, int priority) {
        validateServer();
        return getServer().registerProxyListener(this, listener, priority);
    }
}
