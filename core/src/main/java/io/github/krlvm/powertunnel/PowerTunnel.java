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

import io.github.krlvm.powertunnel.configuration.ConfigurationStore;
import io.github.krlvm.powertunnel.listener.CoreProxyListener;
import io.github.krlvm.powertunnel.listener.ProxyListenerInfo;
import io.github.krlvm.powertunnel.listener.ServerListenerCallback;
import io.github.krlvm.powertunnel.sdk.PowerTunnelServer;
import io.github.krlvm.powertunnel.sdk.ServerListener;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import io.github.krlvm.powertunnel.sdk.types.PowerTunnelPlatform;
import io.github.krlvm.powertunnel.sdk.types.VersionInfo;
import io.github.krlvm.powertunnel.sdk.utiities.TextReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.littleshoot.proxy.mitm.Authority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.*;

public class PowerTunnel implements PowerTunnelServer {

    public static final VersionInfo VERSION = new VersionInfo(BuildConstants.VERSION, BuildConstants.VERSION_CODE);

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerTunnel.class);

    private LittleProxyServer server;
    private ProxyStatus status = ProxyStatus.NOT_RUNNING;
    private final ProxyAddress address;

    private final PowerTunnelPlatform platform;
    private final boolean transparent;
    private final boolean allowFallbackDnsResolver;
    private final List<String> dnsServers;
    private final String dnsDomainsSearchPath;
    private final Authority mitmAuthority;

    private final List<PowerTunnelPlugin> plugins = new ArrayList<>();
    private final File pluginsDir;

    private final Map<String, String> inheritedConfiguration;
    private final File configsDir;

    private final Map<ServerListener, PluginInfo> serverListeners = new HashMap<>();
    private final Map<ProxyListenerInfo, ProxyListener> proxyListeners = new TreeMap<>((o1, o2) -> o1.getPriority() - o2.getPriority());
    private static final int DEFAULT_LISTENER_PRIORITY = 0;

    public PowerTunnel(
            ProxyAddress address,
            PowerTunnelPlatform platform,
            File parentDirectory,
            boolean transparent,
            boolean allowFallbackDnsResolver,
            List<String> dnsServers,
            String dnsDomainsSearchPath,
            Authority mitmAuthority,
            File configsDirectory,
            Map<String, String> inheritedConfiguration
    ) {
        this.address = address;
        this.platform = platform;
        this.transparent = transparent;
        this.allowFallbackDnsResolver = allowFallbackDnsResolver;
        this.dnsServers = dnsServers;
        this.dnsDomainsSearchPath = dnsDomainsSearchPath;
        this.mitmAuthority = mitmAuthority;

        this.inheritedConfiguration = inheritedConfiguration;

        pluginsDir = new File(parentDirectory, "plugins");
        configsDir = configsDirectory == null ? new File(parentDirectory, "configs") : configsDirectory;
        initializeDirectories();
    }

    private void initializeDirectories() {
        if(!pluginsDir.exists()) pluginsDir.mkdir();
        if(!configsDir.exists()) configsDir.mkdir();
    }

    @Override
    public void start() throws ProxyStartException {
        if(this.server != null) throw new IllegalStateException("Proxy Server is already running");
        this.server = new LittleProxyServer(transparent, allowFallbackDnsResolver, mitmAuthority, dnsServers, dnsDomainsSearchPath);

        setStatus(ProxyStatus.STARTING);
        try {
            callPluginsProxyInitializationCallback();
        } catch (ProxyStartException ex) {
            setStatus(ProxyStatus.NOT_RUNNING);
            throw ex;
        }
        try {
            this.startServer();
            setStatus(ProxyStatus.RUNNING);
        } catch (ProxyStartException ex) {
            this.server = null;
            setStatus(ProxyStatus.NOT_RUNNING);
            throw ex;
        }
    }

    @Override
    public void stop() {
        this.stop(true);
    }

    public void stop(boolean graceful) {
        if(this.server == null) throw new IllegalStateException("Proxy Server has not been initialized");

        setStatus(ProxyStatus.STOPPING);
        this.server.stop(graceful);
        setStatus(ProxyStatus.NOT_RUNNING);

        serverListeners.clear();
        proxyListeners.clear();

        this.server = null;

        System.gc();
    }

    private void startServer() throws ProxyStartException {
        try {
            this.server.setAddress(address);
        } catch (UnknownHostException ex) {
            throw new ProxyStartException("Failed to resolve proxy server address", ex);
        }
        try {
            this.server.start(new CoreProxyListener(proxyListeners));
        } catch (RuntimeException ex) {
            if(ex.getCause() != null && ex.getCause() instanceof BindException)
                throw new ProxyStartException("Failed to bind proxy server port", ex);
            throw new ProxyStartException("Unexpected error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isRunning() {
        return status == ProxyStatus.RUNNING;
    }

    @Override
    public @NotNull ProxyStatus getStatus() {
        return status;
    }

    private void setStatus(ProxyStatus status) {
        LOGGER.debug("Proxy server status is changing from {} to {}", this.status.name(), status.name());

        callServerListeners((listener -> listener.beforeProxyStatusChanged(status)));
        this.status = status;
        callServerListeners((listener -> listener.onProxyStatusChanged(status)));

        LOGGER.debug("Proxy server status has changed to {}", status.name());
    }

    // region Proxy Listeners Management

    @Override
    public void registerProxyListener(@NotNull PluginInfo pluginInfo, @NotNull ProxyListener listener) {
        this.registerProxyListener(pluginInfo, listener, DEFAULT_LISTENER_PRIORITY);
    }

    @Override
    public void registerProxyListener(@NotNull PluginInfo pluginInfo, @NotNull ProxyListener listener, int priority) {
        if(proxyListeners.containsValue(listener)) throw new IllegalStateException("Proxy Listener is already registered");
        proxyListeners.put(new ProxyListenerInfo(pluginInfo, priority), listener);
    }

    @Override
    public void unregisterProxyListener(@NotNull ProxyListener listener) {
        if(!proxyListeners.containsValue(listener)) throw new IllegalStateException("Proxy Listener is not registered");
        proxyListeners.values().remove(listener);
    }

    // endregion

    // region Server Listeners Management

    @Override
    public void registerServerListener(@NotNull PluginInfo pluginInfo, @NotNull ServerListener listener) {
        if(serverListeners.containsKey(listener)) throw new IllegalStateException("Server Listener is already registered");
        serverListeners.put(listener, pluginInfo);
    }

    @Override
    public void unregisterServerListener(@NotNull ServerListener listener) {
        if(!serverListeners.containsKey(listener)) throw new IllegalStateException("Server Listener is not registered");
        serverListeners.remove(listener);
    }

    private void callServerListeners(@NotNull ServerListenerCallback callback) {
        for (Map.Entry<ServerListener, PluginInfo> entry : serverListeners.entrySet()) {
            try {
                callback.call(entry.getKey());
            } catch (Exception ex) {
                LOGGER.error(
                        "An error occurred in ServerListener of plugin '{}' [{}]: {}",
                        entry.getValue().getId(), entry.getKey().getClass().getSimpleName(), ex.getMessage(),
                        ex
                );
            }
        }
    }

    // endregion

    @Override
    public @Nullable ProxyServer getProxyServer() {
        return this.server;
    }

    @Override
    public @NotNull Configuration readConfiguration(@NotNull PluginInfo pluginInfo) {
        final ConfigurationStore configuration = new ConfigurationStore();
        try {
            configuration.read(new File(configsDir, pluginInfo.getId() + Configuration.EXTENSION));
        } catch (IOException ex) {
            LOGGER.error("Failed to read configuration of plugin '{}' ('{}')", pluginInfo.getName(), pluginInfo.getId(), ex);
        }
        if(inheritedConfiguration != null) {
            for (Map.Entry<String, String> entry : inheritedConfiguration.entrySet()) {
                if (entry.getKey().startsWith(pluginInfo.getId() + ".")) {
                    configuration.set(entry.getKey().replaceFirst(pluginInfo.getId() + ".", ""), entry.getValue());
                }
            }
        }
        return configuration;
    }

    @Override
    public void saveConfiguration(@NotNull PluginInfo pluginInfo, @NotNull Configuration configuration) {
        if(!(configuration instanceof ConfigurationStore))
            throw new IllegalArgumentException("Unsupported Configuration implementation");
        try {
            ((ConfigurationStore) configuration).save(new File(configsDir, pluginInfo.getId() + Configuration.EXTENSION));
        } catch (IOException ex) {
            LOGGER.error("Failed to save configuration of plugin '{}' ('{}')", pluginInfo.getName(), pluginInfo.getId(), ex);
        }
    }

    @Override
    public @NotNull String readTextFile(@NotNull String filename) throws IOException {
        final File file = new File(configsDir, filename);
        if(!file.exists()) file.createNewFile();
        return TextReader.read(file);
    }

    @Override
    public @NotNull void saveTextFile(@NotNull String filename, @NotNull String text) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File(configsDir, filename)))) {
            writer.write(text);
            writer.flush();
        }
    }

    public void registerPlugin(PowerTunnelPlugin plugin) {
        for (PowerTunnelPlugin p : this.plugins) {
            if (p.getInfo().getId().equals(plugin.getInfo().getId())) {
                LOGGER.warn(
                        "Detected duplicate of plugin '{}' [{}] with version {} ({}) in '{}', version {} ({}) has already been loaded from '{}'",
                        p.getInfo().getName(), p.getInfo().getId(), p.getInfo().getVersion(), p.getInfo().getVersionCode(), p.getInfo().getSource(),
                        plugin.getInfo().getVersion(), plugin.getInfo().getVersionCode(), plugin.getInfo().getSource()
                );
                return;
            }
        }

        this.plugins.add(plugin);
        plugin.attachServer(this);

        final PluginInfo info = plugin.getInfo();
        LOGGER.info(
                "Registered plugin '{}' [{}] v{} ({}) by {}",
                info.getName(), info.getId(), info.getVersion(), info.getVersionCode(), info.getAuthor()
        );
    }

    public List<PowerTunnelPlugin> getPlugins() {
        return plugins;
    }

    private void callPluginsProxyInitializationCallback() throws ProxyStartException {
        for (PowerTunnelPlugin plugin : plugins) {
            try {
                plugin.onProxyInitialization(server);
            } catch (Exception ex) {
                LOGGER.error(
                        "An error occurred when plugin '{}' was handling proxy initialization: {}",
                        plugin.getInfo().getId(), ex.getMessage(),
                        ex
                );
                throw new ProxyStartException(String.format(
                        "Plugin '%s' failed to initialize: %s", plugin.getInfo().getName(), ex.getMessage()
                ));
            }
        }
    }

    @Override
    public PowerTunnelPlatform getPlatform() {
        return platform;
    }

    @Override
    public VersionInfo getVersion() {
        return VERSION;
    }
}
