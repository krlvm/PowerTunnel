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

import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import org.jetbrains.annotations.NotNull;

public interface ServerListener {

    /**
     * Called before server status changing,
     * e.g. after user stopped the server
     *
     * @param status new server status
     */
    void beforeProxyStatusChanged(@NotNull ProxyStatus status);

    /**
     * Called after server status has changed,
     * e.g. server stopped
     *
     * @param status new server status
     */
    void onProxyStatusChanged(@NotNull ProxyStatus status);
}
