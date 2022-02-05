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

import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;

public interface ProxyServer {

    // region Utilities

    /**
     * Returns proxy server response builder with given content
     * and status code 200 OK
     *
     * @param content response content
     * @return proxy server response builder
     */
    ProxyResponse.Builder getResponseBuilder(String content);

    /**
     * Returns proxy server response builder with given content
     * and status code
     *
     * @param content response content
     * @param code response status code
     * @return proxy server response builder
     */
    ProxyResponse.Builder getResponseBuilder(String content, int code);

    // endregion

    // region Address

    /**
     * Sets proxy server address
     * @param address proxy server address
     * @throws UnknownHostException if hostname resolving failed
     */
    void setAddress(@NotNull ProxyAddress address) throws UnknownHostException;

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
    @Nullable ProxyCredentials getAuthorizationCredentials();

    // endregion

    // region Upstream Proxy Server

    /**
     * Sets upstream proxy server to specified server
     * or disables it if provided proxy address is null
     *
     * @param proxy upstream proxy server address
     */
    void setUpstreamProxyServer(@NotNull UpstreamProxyServer proxy);

    /**
     * Returns upstream proxy server or null
     * if upstream proxy server is not specified
     *
     * @return upstream proxy server address
     */
    @Nullable UpstreamProxyServer getUpstreamProxyServer();

    // endregion

    // region Upstream Proxy Server Address

    /**
     * Sets upstream proxy server address to specified server
     * or disables it if provided proxy address is null
     *
     * @param address upstream proxy server address
     */
    void setUpstreamProxyServerAddress(@NotNull ProxyAddress address);

    /**
     * Returns upstream proxy server address or null
     * if upstream proxy server is not specified
     *
     * @return upstream proxy server address
     */
    @Nullable ProxyAddress getUpstreamProxyServerAddress();

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
    @Nullable ProxyCredentials getUpstreamProxyServerAuthorizationCredentials();

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

    // region Allow Requests To Origin Server

    /**
     * Sets whether it is allowed to send requests to origin server
     * @param allow whether it is allowed to send requests to origin server
     */
    void setAllowRequestsToOriginServer(boolean allow);

    /**
     * Returns whether it is allowed to send requests to origin server
     * @return whether it is allowed to send requests to origin server
     */
    boolean isAllowRequestsToOriginServer();

    // endregion

    // region MITM

    /**
     * Sets whether MITM is enabled
     * @param enabled whether MITM is enabled
     */
    void setMITMEnabled(boolean enabled);

    /**
     * Returns whether MITM is enabled
     * @return whether MITM is enabled
     */
    boolean isMITMEnabled();

    // endregion

    // region DNS Configuration

    /**
     * Returns base DNS servers
     * @return base DNS servers
     */
    List<String> getDNSServers();

    /**
     * Returns DNS domains search path
     * @return DNS domains search path
     */
    @Nullable String getDNSDomainsSearchPath();

    // region Hostnames Availability

    /**
     * Returns whether hostnames are available
     * @return whether hostnames are available
     */
    @Deprecated
    boolean areHostnamesAvailable();

    // endregion
}
