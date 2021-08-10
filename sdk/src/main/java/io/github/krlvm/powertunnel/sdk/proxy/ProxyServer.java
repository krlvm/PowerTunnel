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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public interface ProxyServer {

    // region Address

    /**
     * Sets proxy server address
     * @param address proxy server address
     */
    void setAddress(@NotNull ProxyAddress address);

    /**
     * Returns proxy server address
     * @return proxy server address
     */
    @NotNull ProxyAddress getAddress();

    // endregion

    // region InetSocketAddress

    /**
     * Sets proxy server raw address
     * @param address raw address
     */
    void setInetSocketAddress(@NotNull InetSocketAddress address);

    /**
     * Returns proxy server raw address
     * @return proxy server raw address
     */
    @NotNull InetSocketAddress getInetSocketAddress();

    // endregion

    // region Port

    /**
     * Sets proxy server port
     * @param port port
     */
    void setPort(int port);

    /**
     * Returns proxy server port
     * @return proxy server port
     */
    int getPort();

    // endregion

    // region Alias

    /**
     * Sets proxy server alias which is displayed
     * in "Via" header if proxy server is not running
     * in transparent mode
     *
     * @param alias proxy server alias
     */
    void setAlias(@NotNull String alias);

    /**
     * Returns proxy server alias which is displayed
     * in "Via" header if proxy server is not running
     * in transparent mode
     *
     * @return proxy server alias
     */
    @NotNull String getAlias();

    // endregion

    // region Authorization

    /**
     * Sets proxy server authorization enabled with specified credentials
     * or disabled if credentials is null
     *
     * @param credentials proxy server authorization credentials
     */
    void setAuthorizationCredentials(@Nullable ProxyCredentials credentials);

    /**
     * Returns proxy server authorization credentials or null
     * if proxy server authorization is not enabled
     *
     * @return proxy server authorization credentials
     */
    @NotNull ProxyCredentials getAuthorizationCredentials();

    // endregion

    // region Resolver

    /**
     * Sets DNS Resolver
     * @param resolver DNS Resolver
     */
    void setResolver(@NotNull DNSResolver resolver);

    /**
     * Returns DNS Resolver
     * @return DNS Resolver
     */
    @Nullable DNSResolver getResolver();

    // endregion

    // region Upstream Proxy Server

    /**
     * Sets upstream proxy server to specified server
     * or disables it if provided proxy address is null
     *
     * @param proxy upstream proxy server address
     */
    void setUpstreamProxyServer(@NotNull ProxyAddress proxy);

    /**
     * Returns upstream proxy server or null
     * if upstream proxy server is not specified
     *
     * @return upstream proxy server address
     */
    @Nullable ProxyAddress getUpstreamProxyServer();

    // endregion

    // region Upstream Proxy Server Authorization

    /**
     * Sets upstream proxy server authorization enabled with specified credentials
     * or disabled if credentials is null
     *
     * @param credentials upstream proxy server authorization credentials
     */
    void setUpstreamProxyServerAuthorizationCredentials(@Nullable ProxyCredentials credentials);

    /**
     * Returns upstream proxy server authorization credentials or null
     * if upstream proxy server authorization is not enabled
     *
     * @return upstream proxy server authorization credentials
     */
    @NotNull ProxyCredentials getUpstreamProxyServerAuthorizationCredentials();

    // endregion

    // region Max Chunk Size

    /**
     * Sets proxy server max chunk size
     * @param maxChunkSize max chunk size
     */
    void setMaxChunkSize(int maxChunkSize);

    /**
     * Returns proxy server max chunk size
     * @return proxy server max chunk size
     */
    int getMaxChunkSize();

    // endregion

    // region Full Packets

    /**
     * Forces proxy server to collect all HTTPS request chunks
     * so the full packet can be inspected by the listener when
     * MITM is enabled
     *
     * @param value whether to collect all request chunks
     */
    void setFullRequest(boolean value);

    /**
     * Returns whether proxy server collects all HTTPS request chunks
     * @return whether proxy server collects all HTTPS request chunks
     */
    boolean isFullRequest();


    /**
     * Forces proxy server to collect all HTTPS response chunks
     * so the full packet can be inspected by the listener when
     * MITM is enabled
     *
     * @param value whether to collect all request chunks
     */
    void setFullResponse(boolean value);

    /**
     * Returns whether proxy server collects all HTTPS response chunks
     * @return whether proxy server collects all HTTPS response chunks
     */
    boolean isFullResponse();

    // endregion
}
