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

import io.github.krlvm.powertunnel.sdk.proxy.DNSRequest;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import org.littleshoot.proxy.DefaultHostResolver;
import org.littleshoot.proxy.HostResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class LDNSResolver implements HostResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(LDNSResolver.class);

    private final ProxyListener listener;
    private final boolean allowFallbackResolver;

    private final HostResolver resolver = new DefaultHostResolver();

    public LDNSResolver(ProxyListener listener, boolean allowFallbackResolver) {
        this.listener = listener;
        this.allowFallbackResolver = allowFallbackResolver;
    }

    @Override
    public InetSocketAddress resolve(String host, int port) throws UnknownHostException {
        final DNSRequest request = new DNSRequest(host, port);
        final Boolean result = listener.onResolutionRequest(request);
        if(result != null && !result) {
            LOGGER.error("Resolution of hostname '{}' failed", host);
            if(!this.allowFallbackResolver) throw new UnknownHostException();
        }
        return request.getResponse() == null ? this.resolver.resolve(host, port) : request.getResponse();
    }
}
