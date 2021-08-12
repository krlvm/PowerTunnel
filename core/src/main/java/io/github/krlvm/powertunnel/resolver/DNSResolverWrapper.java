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

package io.github.krlvm.powertunnel.resolver;

import io.github.krlvm.powertunnel.sdk.proxy.DNSResolver;
import org.jetbrains.annotations.NotNull;
import org.littleshoot.proxy.HostResolver;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class DNSResolverWrapper implements DNSResolver {

    private final HostResolver resolver;

    public DNSResolverWrapper(@NotNull HostResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public @NotNull InetSocketAddress resolve(@NotNull String host, int port) throws UnknownHostException {
        return resolver.resolve(host, port);
    }
}
