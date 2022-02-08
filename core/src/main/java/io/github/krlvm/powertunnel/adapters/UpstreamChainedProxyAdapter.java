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

package io.github.krlvm.powertunnel.adapters;

import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyCredentials;
import io.github.krlvm.powertunnel.sdk.proxy.UpstreamProxyServer;
import io.github.krlvm.powertunnel.sdk.types.UpstreamProxyType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class UpstreamChainedProxyAdapter extends ChainedProxyAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpstreamChainedProxyAdapter.class);

    private final InetSocketAddress address;
    private final String authorizationCode;

    /**
     * Available only in case caching is not enabled
     */
    private ProxyAddress upstreamProxyAddress;

    private final ChainedProxyType type;

    /**
     * Creates a new adapter from given UpstreamProxyServer
     * Caching enabled
     *
     * @param proxy upstream proxy server
     * @throws UnknownHostException if hostname resolving failed
     */
    public UpstreamChainedProxyAdapter(UpstreamProxyServer proxy) throws UnknownHostException {
        this(proxy.getAddress(), proxy.getCredentials(), proxy.getType(), true);
    }

    /**
     * Creates a new adapter from given UpstreamProxyServer
     *
     * @param proxy upstream proxy server
     * @param caching whether InetSocketAddress should be cached
     * @throws UnknownHostException if hostname resolving failed
     */
    public UpstreamChainedProxyAdapter(UpstreamProxyServer proxy, boolean caching) throws UnknownHostException {
        this(proxy.getAddress(), proxy.getCredentials(), proxy.getType(), caching);
    }

    /**
     * Creates a new adapter from given ProxyAddress
     * Caching is enabled
     *
     * @param address proxy server address
     * @param credentials upstream proxy server credentials
     * @throws UnknownHostException if hostname resolving failed
     */
    public UpstreamChainedProxyAdapter(ProxyAddress address, ProxyCredentials credentials, UpstreamProxyType type) throws UnknownHostException {
        this(address, credentials, type, true);
    }

    /**
     * Creates a new adapter from given ProxyAddress
     *
     * @param address proxy server address
     * @param credentials upstream proxy server credentials
     * @param caching whether InetSocketAddress should be cached
     * @throws UnknownHostException if hostname resolving failed
     */
    public UpstreamChainedProxyAdapter(ProxyAddress address, ProxyCredentials credentials, UpstreamProxyType type, boolean caching) throws UnknownHostException {
        this(address, credentials != null ? credentials.toAuthorizationCode() : null, type, caching);
    }

    /**
     * Creates a new adapter from given ProxyAddress
     * Caching is enabled
     *
     * @param address upstream proxy server address
     * @param authorizationCode upstream proxy server authorization code
     * @throws UnknownHostException if hostname resolving failed
     */
    public UpstreamChainedProxyAdapter(ProxyAddress address, String authorizationCode, UpstreamProxyType type) throws UnknownHostException {
        this(address, authorizationCode, type, true);
    }

    /**
     * Creates a new adapter from given ProxyAddress
     * Caching is enabled
     *
     * @param address upstream proxy server address
     * @param authorizationCode upstream proxy server authorization code
     * @param caching whether InetSocketAddress should be cached
     * @throws UnknownHostException if hostname resolving failed
     */
    public UpstreamChainedProxyAdapter(ProxyAddress address, String authorizationCode, UpstreamProxyType type, boolean caching) throws UnknownHostException {
        this(caching ? address.resolve() : null, authorizationCode, type);
        if(!caching) this.upstreamProxyAddress = address;
    }

    /**
     * Creates a new adapter from given InetSocketAddress
     *
     * Caching is enabled, to resolve the address each time it needed,
     * use constructor which accepts ProxyAddress
     *
     * @param address upstream proxy server address
     * @param credentials upstream proxy server credentials
     */
    public UpstreamChainedProxyAdapter(InetSocketAddress address, ProxyCredentials credentials, UpstreamProxyType type) {
        this(address, credentials != null ? credentials.toAuthorizationCode() : null, null);
    }

    /**
     * Creates a new adapter from given InetSocketAddress
     *
     * Caching is enabled, to resolve the address each time it needed,
     * use constructor which accepts ProxyAddress
     *
     * @param address upstream proxy server address
     * @param authorizationCode upstream proxy server authorization code
     */
    public UpstreamChainedProxyAdapter(InetSocketAddress address, String authorizationCode, UpstreamProxyType type) {
        this.address = address;
        this.authorizationCode = authorizationCode == null ? null : "Basic " + authorizationCode;
        this.type = ChainedProxyType.valueOf(type != null ? type.name() : UpstreamProxyType.HTTP.name());
    }

    @Override
    public InetSocketAddress getChainedProxyAddress() {
        try {
            return address != null ? address : this.upstreamProxyAddress.resolve();
        } catch (UnknownHostException ex) {
            LOGGER.error("Failed to resolve upstream proxy address");
            return null;
        }
    }

    @Override
    public void filterRequest(HttpObject httpObject) {
        if (this.authorizationCode != null && httpObject instanceof HttpRequest) {
            ((HttpRequest) httpObject).headers().add(HttpHeaderNames.PROXY_AUTHORIZATION, this.authorizationCode);
        }
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public ChainedProxyType getChainedProxyType() {
        return type;
    }
}