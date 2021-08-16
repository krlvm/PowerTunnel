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

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.Main;
import io.github.krlvm.powertunnel.desktop.configuration.ServerConfiguration;
import io.github.krlvm.powertunnel.desktop.frames.MainFrame;
import io.github.krlvm.powertunnel.desktop.frames.OptionsFrame;
import io.github.krlvm.powertunnel.desktop.frames.PluginsFrame;
import io.github.krlvm.powertunnel.desktop.managers.TrayManager;
import io.github.krlvm.powertunnel.desktop.system.SystemProxy;
import io.github.krlvm.powertunnel.desktop.ui.JPanelCallback;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.sdk.exceptions.ProxyStartException;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GraphicalApp extends DesktopApp {

    private static GraphicalApp instance;

    public static final Image ICON = Toolkit.getDefaultToolkit().getImage(
            Main.class.getResource("/icon" + (BuildConstants.IS_RELEASE ? "" : "_dev") + ".png")
    );

    private final TrayManager trayManager;
    private final MainFrame frame;

    public PluginsFrame pluginsFrame = null;
    public OptionsFrame optionsFrame = null;

    public GraphicalApp(ServerConfiguration configuration, boolean start, boolean minimized, boolean tray) {
        super(configuration, start);
        instance = this;

        frame = new MainFrame(this);
        if(tray) {
            trayManager = new TrayManager(this);
            try {
                trayManager.load();
            } catch (AWTException ex) {
                System.err.println("Tray is not available: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            trayManager = null;
        }

        if(!minimized) {
            showFrame();
        } else {
            if(!isTrayAvailable()) System.err.println("Can't run minimized when tray icon is disabled");
        }
    }

    @Override
    public void start() {
        final ProxyStartException ex = startInternal();
        if(ex == null) {
            try {
                configuration.save();
            } catch (IOException e) {
                System.err.println("Failed to save configuration: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        UIUtility.showErrorDialog(getVisibleMainFrame(), "Failed to start server", ex.getMessage());
        JOptionPane.showMessageDialog(getVisibleMainFrame(), ex.getMessage(), "Failed to start server", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void beforeProxyStatusChanged(@NotNull ProxyStatus status) {
        super.beforeProxyStatusChanged(status);
        frame.update();
    }

    @Override
    public void onProxyStatusChanged(@NotNull ProxyStatus status) {
        super.beforeProxyStatusChanged(status);
        frame.update();

        if(!configuration.getBoolean("auto_proxy_setup", true)) return;
        if(status == ProxyStatus.RUNNING) {
            SystemProxy.enableProxy(address);
        } else if(status == ProxyStatus.STOPPING) {
            SystemProxy.disableProxy();
        }
    }

    @Override
    protected void onUnexpectedProxyInitializationError(Exception ex) {
        super.onUnexpectedProxyInitializationError(ex);
        Thread.dumpStack();
        UIUtility.showErrorDialog(getVisibleMainFrame(), "Unexpected error during proxy server initialization: " + ex.getMessage());
    }

    public boolean isTrayAvailable() {
        return trayManager != null && trayManager.isLoaded();
    }

    public void showNotification(String message) {
        if(isTrayAvailable()) trayManager.showNotification(message);
    }

    private JFrame getVisibleMainFrame() {
        return frame;
    }

    public void showFrame() {
        frame.showFrame();
    }

    public void showPluginsFrame() {
        if(pluginsFrame == null) pluginsFrame = new PluginsFrame();
        pluginsFrame.showFrame(getVisibleMainFrame());
    }

    public void showOptionsFrame() {
        if(optionsFrame == null) optionsFrame = new OptionsFrame(configuration);
        optionsFrame.showFrame(getVisibleMainFrame());
    }

    public void extendButtonsPanel(JPanelCallback callback) {
        frame.getExtensibleButtonsPanel(callback);
    }

    public void dispose() {
        new Thread(() -> {
            if (isRunning()) stop();
            if (isTrayAvailable()) trayManager.unload();
            System.exit(0);
        }, "App Shutdown Thread").start();
    }

    public static GraphicalApp getInstance() {
        return instance;
    }
}
