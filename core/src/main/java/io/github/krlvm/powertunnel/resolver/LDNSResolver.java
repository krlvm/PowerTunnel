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
import org.littleshoot.proxy.HostResolver;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class LDNSResolver implements HostResolver {

    private final DNSResolver resolver;
    private final HostResolver fallbackResolver;

    public LDNSResolver(DNSResolver resolver, HostResolver fallbackResolver) {
        this.resolver = resolver;
        this.fallbackResolver = fallbackResolver;
    }

    @Override
    public InetSocketAddress resolve(String host, int port) throws UnknownHostException {
        try {
            return resolver.resolve(host, port);
        } catch (UnknownHostException ex) {
            // TODO: Use Logger, print hostname to debug output
            System.out.printf("DNS Resolver [%s] failed to resolve hostname: %s%n", resolver.getClass().getSimpleName(), ex.getMessage());
            ex.printStackTrace();

            if(this.fallbackResolver != null) return this.fallbackResolver.resolve(host, port);
            return null;
        }
    }
}
