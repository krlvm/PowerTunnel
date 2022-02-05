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

import io.github.krlvm.powertunnel.adapters.ProxyFiltersSourceAdapter;
import io.github.krlvm.powertunnel.adapters.UpstreamChainedProxyAdapter;
import io.github.krlvm.powertunnel.http.LProxyResponse;
import io.github.krlvm.powertunnel.managers.ProxyAuthenticationManager;
import io.github.krlvm.powertunnel.managers.UpstreamProxyChainedProxyManager;
import io.github.krlvm.powertunnel.resolver.LDNSResolver;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.proxy.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class LittleProxyServer implements ProxyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LittleProxyServer.class);

    private DefaultHttpProxyServer server;
    private HttpProxyServerBootstrap bootstrap;
    private boolean isRunning = false;

    private ProxyCredentials credentials;
    private UpstreamProxyServer upstreamProxyServer;

    private final boolean allowFallbackResolver;
    private final Authority mitmAuthority;

    private boolean mitmEnabled = false;
    private boolean isFullRequest = false, isFullResponse = false;

    private final List<String> dnsServers;
    private final String dnsDomainsSearchPath;

    private boolean areHostnamesAvailable = true;

    protected LittleProxyServer(boolean transparent, boolean allowFallbackDnsResolver, Authority mitmAuthority,
                                List<String> dnsServers, String dnsDomainsSearchPath) {
        this.bootstrap = DefaultHttpProxyServer.bootstrap()
                .withTransparent(transparent)
                .withAllowRequestToOriginServer(true);
        this.allowFallbackResolver = allowFallbackDnsResolver;
        this.mitmAuthority = mitmAuthority;
        this.dnsServers = dnsServers;
        this.dnsDomainsSearchPath = dnsDomainsSearchPath;
    }

    /**
     * Starts LittleProxy server
     */
    public void start(ProxyListener listener) throws ProxyStartException {
        ensureBootstrapAvailable();

        LOGGER.info("Starting LittleProxy Server...");
        this.bootstrap.withServerResolver(new LDNSResolver(listener, this.allowFallbackResolver));
        if(this.upstreamProxyServer != null) {
            try {
                this.bootstrap.withChainProxyManager(new UpstreamProxyChainedProxyManager(
                        new UpstreamChainedProxyAdapter(this.upstreamProxyServer)
                ));
            } catch (UnknownHostException ex) {
                throw new ProxyStartException("Failed to resolve upstream proxy server address", ex);
            }
        }
        if(mitmEnabled) {
            try {
                this.bootstrap.withManInTheMiddle(new CertificateSniffingMitmManager(mitmAuthority));
            } catch (RootCertificateException ex) {
                throw new ProxyStartException("Failed to initialize MITM manager: " + ex.getMessage(), ex);
            }
        }

        this.bootstrap.withFiltersSource(new ProxyFiltersSourceAdapter(listener, isFullRequest, isFullResponse));

        this.server = ((DefaultHttpProxyServer) this.bootstrap.start());
        LOGGER.info("LittleProxy Server is listening at {}", getAddress());

        this.isRunning = true;
        this.bootstrap = null;
    }

    /**
     * Gracefully shutdowns LittleProxy server
     */
    public void stop() {
        this.stop(true);
    }

    /**
     * Immediately shutdowns LittleProxy server
     */
    public void kill() {
        this.stop(false);
    }

    public void stop(boolean graceful) {
        ensureServerAvailable();

        LOGGER.info("Stopping LittleProxy Server{}...", graceful ? "" : " (non-graceful)");
        if(graceful) {
            this.server.stop();
        } else {
            this.server.abort();
        }
        this.server = null;

        LOGGER.info("LittleProxy Server has stopped");
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Returns LittleProxy server instance if it's initialized
     * @return LittleProxy server instance
     */
    public @Nullable HttpProxyServer getServer() {
        return server;
    }

    /**
     * Returns LittleProxy server bootstrap instance, if proxy server
     * has already started returns null
     * @return LittleProxy server bootstrap instance
     */
    public @Nullable HttpProxyServerBootstrap getBootstrap() {
        return bootstrap;
    }

    private void ensureServerAvailable() {
        if(server == null) throw new IllegalStateException("LittleProxy Server has not been started");
    }

    private void ensureBootstrapAvailable() {
        if(bootstrap == null) throw new IllegalStateException("LittleProxy Server has already started");
    }

    @Override
    public ProxyResponse.Builder getResponseBuilder(String content) {
        return new LProxyResponse.Builder(content);
    }

    @Override
    public ProxyResponse.Builder getResponseBuilder(String content, int code) {
        return new LProxyResponse.Builder(content, code);
    }

    @Override
    public void setAddress(@NotNull ProxyAddress address) throws UnknownHostException {
        ensureBootstrapAvailable();
        bootstrap.withAddress(address.resolve());
    }

    @Override
    public @NotNull ProxyAddress getAddress() {
        ensureServerAvailable();
        final InetSocketAddress address = server.getListenAddress();
        return new ProxyAddress(address.getHostString(), address.getPort());
    }

    @Override
    public void setInetSocketAddress(@NotNull InetSocketAddress address) {
        ensureBootstrapAvailable();
        bootstrap.withAddress(address);
    }

    @Override
    public @NotNull InetSocketAddress getInetSocketAddress() {
        ensureServerAvailable();
        return server.getListenAddress();
    }

    @Override
    public void setPort(int port) {
        ensureBootstrapAvailable();
        bootstrap.withPort(port);
    }

    @Override
    public int getPort() {
        ensureServerAvailable();
        return server.getListenAddress().getPort();
    }

    @Override
    public void setAlias(@NotNull String alias) {
        ensureBootstrapAvailable();
        bootstrap.withProxyAlias(alias);
    }

    @Override
    public @NotNull String getAlias() {
        ensureServerAvailable();
        return server.getProxyAlias();
    }

    @Override
    public void setAuthorizationCredentials(@Nullable ProxyCredentials credentials) {
        ensureBootstrapAvailable();
        bootstrap.withProxyAuthenticator(credentials == null ? null : new ProxyAuthenticationManager(credentials));
        this.credentials = credentials;
    }

    @Override
    public @Nullable ProxyCredentials getAuthorizationCredentials() {
        return this.credentials;
    }

    @Override
    public void setUpstreamProxyServer(@NotNull UpstreamProxyServer proxy) {
        ensureBootstrapAvailable();
        this.upstreamProxyServer = proxy;
    }

    @Override
    public @Nullable UpstreamProxyServer getUpstreamProxyServer() {
        return this.upstreamProxyServer;
    }

    @Override
    public void setUpstreamProxyServerAddress(@NotNull ProxyAddress address) {
        ensureBootstrapAvailable();
        if(this.upstreamProxyServer == null) setUpstreamProxyServer(new UpstreamProxyServer(address, null));
    }

    @Override
    public @Nullable ProxyAddress getUpstreamProxyServerAddress() {
        return this.upstreamProxyServer != null ? this.upstreamProxyServer.getAddress() : null;
    }

    @Override
    public void setUpstreamProxyServerAuthorizationCredentials(@Nullable ProxyCredentials credentials) {
        ensureBootstrapAvailable();
        if(this.upstreamProxyServer == null) throw new IllegalStateException("Upstream proxy server is not set");
        this.upstreamProxyServer.setCredentials(credentials);
    }

    @Override
    public @Nullable ProxyCredentials getUpstreamProxyServerAuthorizationCredentials() {
        return this.upstreamProxyServer != null ? this.upstreamProxyServer.getCredentials() : null;
    }

    @Override
    public void setMaxChunkSize(int maxChunkSize) {
        ensureBootstrapAvailable();
        bootstrap.withMaxChunkSize(maxChunkSize);
    }

    @Override
    public int getMaxChunkSize() {
        ensureServerAvailable();
        return server.getMaxChunkSize();
    }

    @Override
    public void setFullRequest(boolean value) {
        ensureBootstrapAvailable();
        this.isFullRequest = value;
    }

    @Override
    public boolean isFullRequest() {
        return this.isFullRequest;
    }

    @Override
    public void setFullResponse(boolean value) {
        ensureBootstrapAvailable();
        this.isFullResponse = value;
    }

    @Override
    public boolean isFullResponse() {
        return this.isFullResponse;
    }

    @Override
    public void setAllowRequestsToOriginServer(boolean allow) {
        ensureBootstrapAvailable();
        bootstrap.withAllowRequestToOriginServer(allow);
    }

    @Override
    public boolean isAllowRequestsToOriginServer() {
        ensureServerAvailable();
        return server.isAllowRequestsToOriginServer();
    }

    @Override
    public void setMITMEnabled(boolean enabled) {
        ensureBootstrapAvailable();
        mitmEnabled = enabled;
    }

    @Override
    public boolean isMITMEnabled() {
        return mitmEnabled;
    }

    public void setHostnamesAvailability(boolean availability) {
        this.areHostnamesAvailable = availability;
    }

    @Override
    public List<String> getDNSServers() {
        return Collections.unmodifiableList(dnsServers);
    }

    @Override
    public @Nullable String getDNSDomainsSearchPath() {
        return dnsDomainsSearchPath;
    }

    @Override
    public boolean areHostnamesAvailable() {
        return areHostnamesAvailable;
    }
}
