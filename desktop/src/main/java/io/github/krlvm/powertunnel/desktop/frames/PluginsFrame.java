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

import io.github.krlvm.powertunnel.configuration.ConfigurationStore;
import io.github.krlvm.powertunnel.desktop.application.DesktopApp;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.ui.PluginInfoRenderer;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.desktop.utilities.Utility;
import io.github.krlvm.powertunnel.exceptions.PreferenceParseException;
import io.github.krlvm.powertunnel.plugin.PluginLoader;
import io.github.krlvm.powertunnel.preferences.PreferenceGroup;
import io.github.krlvm.powertunnel.preferences.PreferenceParser;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.utility.JarLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PluginsFrame extends AppFrame {

    private static final int PADDING = 8;
    private final DefaultListModel<PluginInfo> model = new DefaultListModel<>();

    private final JList<PluginInfo> list;

    public PluginsFrame() {
        super("Plugins");

        GridBagConstraints gbc = new GridBagConstraints();

        list = new JList<>(model);
        list.setCellRenderer(new PluginInfoRenderer());


        final JButton homepageButton = new JButton("Homepage");
        homepageButton.addActionListener(e -> withSelectedValue(pluginInfo -> {
            if(pluginInfo.getHomepage() != null) Utility.launchBrowser(pluginInfo.getHomepage());
        }));
        homepageButton.setEnabled(false);

        final JButton disableButton = new JButton("Disable");
        disableButton.addActionListener(e -> withSelectedValue(pluginInfo -> {
            if(isPluginEnabled(pluginInfo)) {
                disablePlugin(pluginInfo);
                disableButton.setText("Enable");
            } else {
                enablePlugin(pluginInfo);
                disableButton.setText("Disable");
            }
        }));
        disableButton.setEnabled(!GraphicalApp.getInstance().isRunning());

        final JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(e -> withSelectedValue(this::openPreferences));
        settingsButton.setEnabled(!GraphicalApp.getInstance().isRunning());

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            homepageButton.setEnabled(list.getSelectedValue().getHomepage() != null);
            disableButton.setText(isPluginEnabled(list.getSelectedValue()) ? "Disable" : "Enable");
        });

        final JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, 0, 0, 0));
        controlPanel.add(disableButton, gbc);
        controlPanel.add(homepageButton, gbc);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        controlPanel.add(settingsButton, gbc);

        final JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        notePanel.add(UIUtility.getLabelWithHyperlinkSupport("<html><center>" +
                "Plugins can't be configured when server is running<br>" +
                "Verify before installing as they may be malicious<br>" +
                "<a href=\"https://github.com/krlvm/PowerTunnel-Plugins\">Visit official plugins registry</a>" +
                "</center></html>")
        );

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
        root.add(notePanel, gbc);

        gbc.gridy = 2;
        root.add(controlPanel, gbc);

        setSize(new Dimension(325, 375));
        setResizable(false);
        frameInitialized();

        list.requestFocus();
        list.requestFocusInWindow();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                GraphicalApp.getInstance().pluginsFrame = null;
            }
        });

        update();
    }

    private boolean isPluginEnabled(PluginInfo plugin) {
        return !GraphicalApp.getInstance().getConfiguration().get("disabled_plugins", "")
                .contains(";" + plugin.getSource());
    }
    private void enablePlugin(PluginInfo plugin) {
        final Configuration configuration = GraphicalApp.getInstance().getConfiguration();
        final String val = configuration.get("disabled_plugins", "");
        configuration.set("disabled_plugins", val.replace(";" + plugin.getSource(), ""));
    }
    private void disablePlugin(PluginInfo plugin) {
        final Configuration configuration = GraphicalApp.getInstance().getConfiguration();
        final String val = configuration.get("disabled_plugins", "");
        configuration.set("disabled_plugins", val + ";" + plugin.getSource());
    }

    private void withSelectedValue(Consumer<PluginInfo> consumer) {
        PluginInfo value = list.getSelectedValue();
        if(value != null) consumer.accept(value);
    }

    private void update() {
        model.removeAllElements();

        final File[] plugins = DesktopApp.LOADED_PLUGINS != null ? DesktopApp.LOADED_PLUGINS : PluginLoader.enumeratePlugins();
        for (File plugin : plugins) {
            try {
                JarLoader.open(plugin, PluginLoader.PLUGIN_MANIFEST, (in) -> {
                    try {
                        model.addElement(PluginLoader.parsePluginInfo(plugin.getName(), in));
                    } catch (IOException ex) {
                        System.err.printf("Failed to read manifest of '%s': %s%n", plugin.getName(), ex.getMessage());
                        ex.printStackTrace();
                    } catch (PluginLoadException ex) {
                        System.err.printf("Failed to parse manifest of '%s': %s%n", plugin.getName(), ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            } catch (IOException ex) {
                System.err.printf("Failed to open plugin '%s' jar file: %s%n", plugin.getName(), ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void openPreferences(PluginInfo pluginInfo) {
        if(PreferencesFrame.OPENED_IDS.containsKey(pluginInfo.getId())) {
            PreferencesFrame.OPENED_IDS.get(pluginInfo.getId()).showFrame();
            return;
        }

        try {
            JarLoader.open(PluginLoader.getPluginFile(pluginInfo.getSource()), PreferenceParser.FILE, (in) -> {
                if(in == null) {
                    UIUtility.showInfoDialog(this, "Plugin is not configurable");
                    return;
                }
                openPreferences(pluginInfo, in);
            }, true);
        } catch (IOException ex) {
            UIUtility.showErrorDialog(
                    this, "Failed to read plugin preferences",
                    "Failed to open plugin jar file: " + ex.getMessage()
            );
            System.err.printf("Failed to open plugin '%s' jar file: %s%n", pluginInfo.getName(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void openPreferences(PluginInfo pluginInfo, InputStream in) {
        final String json;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            json = reader.lines().collect(Collectors.joining(""));
        } catch (IOException ex) {
            UIUtility.showErrorDialog(
                    this, "Failed to open plugin preferences",
                    "Failed to read preferences schema: " + ex.getMessage()
            );
            ex.printStackTrace();
            return;
        }

        final List<PreferenceGroup> preferences;
        try {
            preferences = PreferenceParser.parsePreferences(pluginInfo.getSource(), json);
        } catch (PreferenceParseException ex) {
            UIUtility.showErrorDialog(
                    this, "Failed to open plugin preferences",
                    "Failed to parse preferences: " + ex.getMessage()
            );
            return;
        }

        if(preferences.isEmpty()) {
            UIUtility.showInfoDialog(this, "Plugin preferences is empty");
            return;
        }

        final File configurationFile = new File(PluginLoader.PLUGINS_DIR + File.separator + pluginInfo.getId() + Configuration.EXTENSION);
        final ConfigurationStore configuration = new ConfigurationStore();
        try {
            configuration.read(configurationFile);
        } catch (IOException ex) {
            UIUtility.showErrorDialog(
                    this, "Failed to open plugin preferences",
                    "Failed to load configuration: " + ex.getMessage()
            );
            ex.printStackTrace();
            return;
        }

        new PreferencesFrame(
                pluginInfo.getName() + " preferences",
                pluginInfo.getId(), configurationFile, configuration, preferences
        ).showFrame(this);
    }
}
