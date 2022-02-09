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
import io.github.krlvm.powertunnel.desktop.managers.ConsoleHandler;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.mitm.MITMAuthority;
import io.github.krlvm.powertunnel.plugin.PluginLoader;
import io.github.krlvm.powertunnel.sdk.ServerListener;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.*;
import io.github.krlvm.powertunnel.sdk.types.PowerTunnelPlatform;
import io.github.krlvm.powertunnel.sdk.types.UpstreamProxyType;
import io.github.krlvm.powertunnel.sdk.types.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
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

    private static DesktopApp instance;

    protected final ServerConfiguration configuration;
    protected ConsoleHandler consoleHandler;

    protected PowerTunnel server;
    protected ProxyAddress address;

    private final Path certificateDirectory = Paths.get("cert");
    private Exception initializationException = null;

    public DesktopApp(ServerConfiguration configuration) {
        instance = this;

        this.configuration = configuration;
        this.address = new ProxyAddress(
                configuration.get("ip", "127.0.0.1"),
                configuration.getInt("port", 8085)
        );

        registerAppCommands();
    }

    public void start() {
        startInternal();
    }

    protected ProxyStartException startInternal() {
        final ProxyStartException ex = _startInternal();
        if(ex != null) this.server = null;
        return ex;
    }

    private ProxyStartException _startInternal() {
        if(this.server != null) {
            LOGGER.warn("Attempted to start server when it is already running");
            return null;
        }
        this.server = new PowerTunnel(
                address,
                PowerTunnelPlatform.DESKTOP,
                new File("."),
                configuration.getBoolean("transparent_mode", true),
                !configuration.getBoolean("strict_dns", false),
                SystemUtility.getDNSServers(),
                null,
                MITMAuthority.create(
                        certificateDirectory.toFile(),
                        configuration.get("cert_password", UUID.randomUUID().toString()).toCharArray()
                ),
                null,
                getHardcodedSettings()
        );
        this.server.registerServerListener(PLUGIN_INFO, this);
        try {
            if(LOADED_PLUGINS == null) LOADED_PLUGINS = PluginLoader.enumeratePlugins();
            PluginLoader.loadPlugins(Arrays.stream(LOADED_PLUGINS).filter(plugin ->
                    !configuration.get("disabled_plugins", "").contains(";" + plugin.getName())
            ).toArray(File[]::new), this.server);
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
        //if(UpdateNotifier.ENABLED) {
        //    this.server.getPlugins().stream().filter(plugin -> plugin.getInfo().getHomepage().startsWith("https://github.com/krlvm/"))
        //            .forEach(plugin -> UpdateNotifier.checkAndNotify(plugin.getInfo().getName(), plugin.getInfo().getHomepage(), true));
        //}

        if(this.server.getProxyServer().isMITMEnabled() && SystemUtility.IS_WINDOWS) {
            final Path target = certificateDirectory.resolve(MITMAuthority.CERTIFICATE_ALIAS + ".cer");
            if(!target.toFile().exists()) {
                try {
                    Files.copy(
                            certificateDirectory.resolve(MITMAuthority.CERTIFICATE_ALIAS + ".pem"),
                            target
                    );
                } catch (IOException ex) {
                    LOGGER.warn("Failed to copy the certificate from .pem to .cer: {}", ex.getMessage(), ex);
                }
            }
        }

        LOGGER.info("Serving at {}:{}", address.getHost(), address.getPort());

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
                                    configuration.getInt("upstream_proxy_port", 8080)
                            ),
                            credentials,
                            UpstreamProxyType.valueOf(configuration.get("upstream_proxy_protocol", "HTTP"))
                    ));
                }

                if (configuration.getBoolean("proxy_auth_enabled", false)) {
                    proxy.setAuthorizationCredentials(new ProxyCredentials(
                            configuration.get("proxy_auth_username", ""),
                            configuration.get("proxy_auth_password", "")
                    ));
                }

                proxy.setAllowRequestsToOriginServer(configuration.getBoolean("allow_requests_to_origin_server", true));
            } catch (Exception ex) {
                initializationException = ex;
            }
        } else if(status == ProxyStatus.RUNNING) {
            if(initializationException != null) {
                onUnexpectedProxyInitializationError(initializationException);
                initializationException = null;
            }
        }
    }

    @Override
    public void onProxyStatusChanged(@NotNull ProxyStatus status) {
        if (status == ProxyStatus.STOPPING || status == ProxyStatus.NOT_RUNNING) {
            if (consoleHandler != null) consoleHandler.reset();
        }
    }

    protected void onUnexpectedProxyInitializationError(Exception ex) {
        LOGGER.error("Unexpected error occurred when initializing proxy server: {}", ex.getMessage(), ex);
    }

    protected ConsoleHandler getConsoleReader() {
        if (consoleHandler == null) consoleHandler = new ConsoleHandler();
        return consoleHandler;
    }

    public void registerConsoleCommand(
            @NotNull PowerTunnelPlugin plugin,
            @NotNull String command,
            @NotNull Consumer<String[]> handler,
            @Nullable String usage,
            @Nullable String description) {
        getConsoleReader().registerCommand(plugin.getInfo().getId().toLowerCase(), command.toLowerCase(),
                handler, usage, description);
    }

    private void registerAppCommands() {
        getConsoleReader().registerAppCommand("version", args -> {
            System.out.println();
            System.out.println("Running " + BuildConstants.NAME + " version " + BuildConstants.VERSION + " (code " + BuildConstants.VERSION_CODE + ")");
            System.out.println("Core version " + io.github.krlvm.powertunnel.BuildConstants.VERSION + " (code " + io.github.krlvm.powertunnel.BuildConstants.VERSION_CODE + "), SDK " + io.github.krlvm.powertunnel.BuildConstants.SDK);
            System.out.println("(c) krlvm, 2019-2022");
            System.out.println();
        }, "", "print version details");
        if (this instanceof GraphicalApp) {
            getConsoleReader().registerAppCommand("start", args -> {
                if (isRunning()) {
                    System.err.println("Proxy Server is already running");
                } else {
                    start();
                }
            }, "", "start proxy server");
        }
        getConsoleReader().registerAppCommand("stop", args -> {
            if (!isRunning()) {
                System.err.println("Proxy Server is not running");
            } else {
                stop();
            }
        }, "", "shutdown proxy server");
        getConsoleReader().registerAppCommand("exit", args -> {
            System.exit(0);
        }, "", "terminate proxy server and exit");
    }

    private Map<String, String> getHardcodedSettings() {
        return configuration.getImmutableKeys().stream()
                .collect(Collectors.toMap(key -> key, key -> configuration.get(key, null)));
    }

    public ServerConfiguration getConfiguration() {
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

    public static DesktopApp getInstance() {
        return instance;
    }
}
