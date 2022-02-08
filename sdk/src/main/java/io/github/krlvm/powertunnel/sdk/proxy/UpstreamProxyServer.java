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

package io.github.krlvm.powertunnel.sdk.proxy;

import io.github.krlvm.powertunnel.sdk.types.UpstreamProxyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UpstreamProxyServer {

    private ProxyAddress address;
    private ProxyCredentials credentials;
    private UpstreamProxyType type;

    private InetSocketAddress inetSocketAddress;

    public UpstreamProxyServer(@NotNull ProxyAddress address) {
        this(address, null);
    }

    public UpstreamProxyServer(@NotNull ProxyAddress address, @Nullable ProxyCredentials credentials) {
        this(address, credentials, null);
    }

    public UpstreamProxyServer(@NotNull ProxyAddress address, @Nullable ProxyCredentials credentials, @Nullable UpstreamProxyType type) {
        this.address = address;
        this.credentials = credentials;
        this.type = type != null ? type : UpstreamProxyType.HTTP;
    }

    public @NotNull ProxyAddress getAddress() {
        return address;
    }

    public void setAddress(@NotNull ProxyAddress address) {
        this.address = address;
    }

    public void setAddress(@NotNull InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    public @Nullable ProxyCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(@Nullable ProxyCredentials credentials) {
        this.credentials = credentials;
    }

    @NotNull
    public UpstreamProxyType getType() {
        return type;
    }

    public void setType(@NotNull UpstreamProxyType type) {
        this.type = type;
    }

    public InetSocketAddress resolve() throws UnknownHostException {
        return this.isResolved() ? this.inetSocketAddress : this.address.resolve();
    }

    public boolean isResolved() {
        return this.inetSocketAddress != null;
    }
}
