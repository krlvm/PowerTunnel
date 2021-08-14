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

package io.github.krlvm.powertunnel.desktop.frames;

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.application.DesktopApp;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.ui.PluginInfoRenderer;
import io.github.krlvm.powertunnel.plugin.PluginLoader;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PluginsFrame extends AppFrame {

    private static final int PADDING = 8;
    private final DefaultListModel<PluginInfo> model = new DefaultListModel<>();

    public PluginsFrame() {
        super("Plugins");

        GridBagConstraints gbc = new GridBagConstraints();

        final JList<PluginInfo> list = new JList<>(model);
        list.setCellRenderer(new PluginInfoRenderer());


        final JButton homepageButton = new JButton("Visit homepage");
        final JButton settingsButton = new JButton("Configure");

        final JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.add(homepageButton, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        controlPanel.add(settingsButton, gbc);


        final JRootPane root = getRootPane();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(new JScrollPane(list), gbc);
        gbc.gridy = 1;
        gbc.weightx = gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        root.add(controlPanel, gbc);

        setSize(350, 400);
        frameInitialized();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GraphicalApp.getInstance().pluginsFrame = null;
            }
        });

        update();
    }

    private void update() {
        model.removeAllElements();

        final File[] plugins = DesktopApp.LOADED_PLUGINS != null ? DesktopApp.LOADED_PLUGINS : PluginLoader.enumeratePlugins();
        for (File plugin : plugins) {
            final InputStream in;
            try {
                in = PluginLoader.getJarEntry(plugin, PluginLoader.PLUGIN_MANIFEST);
            } catch (IOException ex) {
                System.err.printf("Failed to open plugin '%s' jar file: %s%n", plugin.getName(), ex.getMessage());
                ex.printStackTrace();
                continue;
            }
            if(in == null) {
                System.err.printf("Failed to find manifest of plugin '%s'%n", plugin.getName());
                continue;
            }
            try {
                model.addElement(PluginLoader.parsePluginInfo(plugin.getName(), in));
            } catch (IOException ex) {
                System.err.printf("Failed to read manifest of '%s': %s%n", plugin.getName(), ex.getMessage());
                ex.printStackTrace();
            } catch (PluginLoadException ex) {
                System.err.printf("Failed to parse manifest of '%s': %s%n", plugin.getName(), ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    System.err.printf("Failed to close plugin manifest InputStream: %s%n", ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }
}
