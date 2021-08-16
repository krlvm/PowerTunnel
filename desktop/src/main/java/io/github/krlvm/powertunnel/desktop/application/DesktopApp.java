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

package io.github.krlvm.powertunnel.desktop.application;

import io.github.krlvm.powertunnel.PowerTunnel;
import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.configuration.ServerConfiguration;
import io.github.krlvm.powertunnel.mitm.MITMAuthority;
import io.github.krlvm.powertunnel.plugin.PluginLoader;
import io.github.krlvm.powertunnel.sdk.ServerListener;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.proxy.*;
import io.github.krlvm.powertunnel.sdk.types.PowerTunnelPlatform;
import io.github.krlvm.powertunnel.sdk.types.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class DesktopApp implements ServerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopApp.class);

    public static final File CONFIGURATION_FILE = new File("config.ini");
    public static File[] LOADED_PLUGINS = null;

    public static final VersionInfo VERSION = new VersionInfo(BuildConstants.VERSION, BuildConstants.VERSION_CODE);
    private static final PluginInfo PLUGIN_INFO = new PluginInfo(
            "desktop-app",
            BuildConstants.VERSION,
            BuildConstants.VERSION_CODE,
            BuildConstants.NAME,
            BuildConstants.DESCRIPTION,
            "krlvm",
            BuildConstants.REPO,
            null,
            io.github.krlvm.powertunnel.BuildConstants.VERSION_CODE,
            null
    );

    protected final ServerConfiguration configuration;

    protected PowerTunnel server;
    protected ProxyAddress address;

    private Exception initializationException = null;

    public DesktopApp(ServerConfiguration configuration, boolean start) {
        this.configuration = configuration;
        this.address = new ProxyAddress(
                configuration.get("ip", "127.0.0.1"),
                configuration.getInt("port", 8085)
        );

        if(start) start();
    }

    public void start() {
        startInternal();
    }

    protected ProxyStartException startInternal() {
        if(this.server != null) {
            LOGGER.warn("Attempted to start server when it is already running");
            return null;
        }
        this.server = new PowerTunnel(
                address,
                PowerTunnelPlatform.DESKTOP,
                Paths.get(""),
                configuration.getBoolean("transparent_mode", true),
                MITMAuthority.create(
                        new File("cert"),
                        configuration.get("cert_password", UUID.randomUUID().toString()).toCharArray()
                ),
                getHardcodedSettings()
        );
        this.server.registerServerListener(PLUGIN_INFO, this);
        try {
            if(LOADED_PLUGINS == null) LOADED_PLUGINS = PluginLoader.enumeratePlugins();
            PluginLoader.loadPlugins(LOADED_PLUGINS, this.server);
        } catch (PluginLoadException ex) {
            LOGGER.error("Failed to load plugin ({}): {}", ex.getJarFile(), ex.getMessage(), ex);
            return ex;
        }
        try {
            this.server.start();
        } catch (ProxyStartException ex) {
            LOGGER.error("Failed to start PowerTunnel: {}", ex.getMessage(), ex);
            return ex;
        }
        return null;
    }

    public void stop() {
        stop(true);
    }
    public void stop(boolean graceful) {
        if(this.server == null) {
            LOGGER.warn("Attempted to stop server when it is not running");
            return;
        }
        this.server.stop(graceful);
        this.server = null;
    }

    public ProxyStatus getStatus() {
        return server != null ? server.getStatus() : ProxyStatus.NOT_RUNNING;
    }

    public boolean isRunning() {
        return getStatus() == ProxyStatus.RUNNING;
    }

    @Override
    public void beforeProxyStatusChanged(@NotNull ProxyStatus status) {
        if (status == ProxyStatus.STARTING) {
            try {
                final ProxyServer proxy = server.getProxyServer();
                assert proxy != null;

                if (configuration.getBoolean("upstream_proxy_enabled", false)) {
                    ProxyCredentials credentials = null;
                    if (configuration.getBoolean("upstream_proxy_auth_enabled", false)) {
                        credentials = new ProxyCredentials(
                                configuration.get("upstream_proxy_auth_username", ""),
                                configuration.get("upstream_proxy_auth_password", "")
                        );
                    }
                    proxy.setUpstreamProxyServer(new UpstreamProxyServer(
                            new ProxyAddress(
                                    configuration.get("upstream_proxy_host", ""),
                                    configuration.getInt("upstream_proxy_port", 0)
                            ),
                            credentials
                    ));
                }

                proxy.setAllowRequestsToOriginServer(configuration.getBoolean("allow_requests_to_origin_server", true));
            } catch (Exception ex) {
                initializationException = ex;
            }
        } else if(status == ProxyStatus.RUNNING) {
            if(initializationException != null) {
                stop(false);
                onUnexpectedProxyInitializationError(initializationException);
                initializationException = null;
            }
        }
    }

    @Override
    public void onProxyStatusChanged(@NotNull ProxyStatus status) {}

    protected void onUnexpectedProxyInitializationError(Exception ex) {
        LOGGER.error("Unexpected error occurred when initializing proxy server: {}", ex.getMessage(), ex);
    }

    private Map<String, String> getHardcodedSettings() {
        return configuration.getImmutableKeys().stream()
                .collect(Collectors.toMap(key -> key, key -> configuration.get(key, null)));
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public PowerTunnel getServer() {
        return server;
    }

    public ProxyAddress getAddress() {
        return address;
    }

    public void setAddress(ProxyAddress address) {
        this.address = address;
    }
}
