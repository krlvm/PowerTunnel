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
import io.github.krlvm.powertunnel.mitm.MITMAuthority;
import io.github.krlvm.powertunnel.sdk.ServerListener;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import io.github.krlvm.powertunnel.sdk.types.PowerTunnelPlatform;
import io.github.krlvm.powertunnel.sdk.types.VersionInfo;
import org.jetbrains.annotations.NotNull;
import org.littleshoot.proxy.mitm.Authority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.UUID;

public abstract class DesktopApp implements ServerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopApp.class);

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
            io.github.krlvm.powertunnel.BuildConstants.VERSION_CODE
    );

    protected final Configuration configuration;

    protected PowerTunnel server;
    protected ProxyAddress address;

    public DesktopApp(Configuration configuration, boolean start) {
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
                configuration.getBoolean("transparent", true),
                MITMAuthority.create(
                        new File("cert"),
                        configuration.get("certificate", UUID.randomUUID().toString()).toCharArray()
                )
        );
        this.server.registerServerListener(PLUGIN_INFO, this);
        try {
            this.server.start();
        } catch (ProxyStartException ex) {
            LOGGER.error("Failed to start PowerTunnel: {}", ex.getMessage(), ex);
            return ex;
        }
        return null;
    }

    public void stop() {
        if(this.server == null) {
            LOGGER.warn("Attempted to stop server when it is not running");
            return;
        }
        this.server.stop();
        this.server = null;
    }

    public ProxyStatus getStatus() {
        return server != null ? server.getStatus() : ProxyStatus.NOT_RUNNING;
    }

    public boolean isRunning() {
        return getStatus() == ProxyStatus.RUNNING;
    }

    @Override
    public void beforeProxyStatusChanged(@NotNull ProxyStatus status) {}

    @Override
    public void onProxyStatusChanged(@NotNull ProxyStatus status) {}

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
