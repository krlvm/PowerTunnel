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
import io.github.krlvm.powertunnel.desktop.configuration.ServerConfiguration;
import io.github.krlvm.powertunnel.desktop.ui.I18N;
import io.github.krlvm.powertunnel.desktop.ui.PluginInfoRenderer;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.desktop.utilities.Utility;
import io.github.krlvm.powertunnel.exceptions.PreferenceParseException;
import io.github.krlvm.powertunnel.i18n.I18NBundle;
import io.github.krlvm.powertunnel.plugin.PluginLoader;
import io.github.krlvm.powertunnel.preferences.PreferenceGroup;
import io.github.krlvm.powertunnel.preferences.PreferenceParser;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.exceptions.PluginLoadException;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import io.github.krlvm.powertunnel.utilities.JarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PluginsFrame extends AppFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginsFrame.class);

    private static final int PADDING = 8;
    private final DefaultListModel<PluginInfo> model = new DefaultListModel<>();

    private final JList<PluginInfo> list;

    public PluginsFrame() {
        super(I18N.get("plugins.title"));

        GridBagConstraints gbc = new GridBagConstraints();

        list = new JList<>(model);
        list.setCellRenderer(new PluginInfoRenderer());


        final JButton homepageButton = new JButton(I18N.get("plugins.homepage"));
        homepageButton.addActionListener(e -> withSelectedValue(pluginInfo -> {
            if(pluginInfo.getHomepage() != null) Utility.launchBrowser(pluginInfo.getHomepage());
        }));
        homepageButton.setEnabled(false);

        final JButton disableButton = new JButton(I18N.get("plugins.disable"));
        disableButton.addActionListener(e -> withSelectedValue(pluginInfo -> {
            if(isPluginEnabled(pluginInfo)) {
                disablePlugin(pluginInfo);
                disableButton.setText(I18N.get("plugins.enable"));
            } else {
                enablePlugin(pluginInfo);
                disableButton.setText(I18N.get("plugins.disable"));
            }
            if(GraphicalApp.getInstance().isRunning()) {
                final int result = UIUtility.showYesNoDialog(PluginsFrame.this, I18N.get("preferences.restartProxyPrompt"));
                if (result == JOptionPane.YES_OPTION) {
                    SwingUtilities.invokeLater(() -> {
                        GraphicalApp.getInstance().stop();
                        GraphicalApp.getInstance().start();
                    });
                }
            }
        }));
        //disableButton.setEnabled(!GraphicalApp.getInstance().isRunning());

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                showMenu(event);
            }
            @Override
            public void mouseReleased(MouseEvent event) {
                showMenu(event);
            }
            private void showMenu(MouseEvent event) {
                if (!event.isPopupTrigger()) return;
                showAdditionalConfigurationContextMenu(event.getComponent(), event.getX(), event.getY());
            }
        });
        list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
                    showAdditionalConfigurationContextMenu(PluginsFrame.this, list.getX(), list.getY());
                }
            }
        });

        final JButton settingsButton = new JButton(I18N.get("plugins.settings"));
        settingsButton.addActionListener(e -> withSelectedValue(this::openPreferences));

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            homepageButton.setEnabled(list.getSelectedValue().getHomepage() != null);
            disableButton.setText(I18N.get("plugins." + (isPluginEnabled(list.getSelectedValue()) ? "disable" : "enable")));
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
                I18N.get("plugins.note") +
                "<br><a href=\"https://github.com/krlvm/PowerTunnel-Plugins/blob/master/README.md\">" + I18N.get("plugins.visitRegistry") + "</a>" +
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

    private void showAdditionalConfigurationContextMenu(Component component, int x, int y) {
        withSelectedValue(pluginInfo -> {
            if (pluginInfo.getConfigurationFiles().length == 0) return;

            JPopupMenu menu = new JPopupMenu();

            JMenuItem item = new JMenuItem(I18N.get("plugins.additionalConfiguration"));
            item.setEnabled(false);
            menu.add(item);
            item = new JMenuItem(" " + pluginInfo.getName());
            item.setEnabled(false);
            menu.add(item);

            menu.addSeparator();

            for (String targetFile : pluginInfo.getConfigurationFiles()) {
                item = new JMenuItem(targetFile);
                item.addActionListener(e -> {
                    File file = new File("configs" + File.separator + targetFile.replace("/", File.separator));
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException ex) {
                            LOGGER.error("Failed to create configuration '{}' for plugin '{}'", targetFile, pluginInfo.getId(), ex);
                            UIUtility.showErrorDialog(PluginsFrame.this, "Failed to initialize configuration file: " + ex.getMessage());
                            return;
                        }
                    }
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ex) {
                        LOGGER.error("Failed to externally open configuration file '{}' of plugin '{}'", targetFile, pluginInfo.getId(), ex);
                        UIUtility.showErrorDialog(PluginsFrame.this, "Failed to launch text editor: " + ex.getMessage());
                    }
                });
                menu.add(item);
            }

            menu.show(component, x, y);
        });
    }

    private boolean isPluginEnabled(PluginInfo plugin) {
        return !GraphicalApp.getInstance().getConfiguration().get("disabled_plugins", "")
                .contains(";" + plugin.getSource());
    }
    private void enablePlugin(PluginInfo plugin) {
        final Configuration configuration = GraphicalApp.getInstance().getConfiguration();
        final String val = configuration.get("disabled_plugins", "");
        configuration.set("disabled_plugins", val.replace(";" + plugin.getSource(), ""));
        pluginToggleSaveConfiguration();
    }
    private void disablePlugin(PluginInfo plugin) {
        final ServerConfiguration configuration = GraphicalApp.getInstance().getConfiguration();
        final String val = configuration.get("disabled_plugins", "");
        configuration.set("disabled_plugins", val + ";" + plugin.getSource());
        pluginToggleSaveConfiguration();
    }
    private void pluginToggleSaveConfiguration() {
        try {
            GraphicalApp.getInstance().getConfiguration().save();
        } catch (IOException ex) {
            LOGGER.warn("Failed to save configuration: {}", ex.getMessage(), ex);
            UIUtility.showErrorDialog(this, "Failed to save configuration");
        }
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

        try(final JarLoader loader = new JarLoader(PluginLoader.getPluginFile(pluginInfo.getSource()))) {
            loader.open(PreferenceParser.FILE, (in) -> {
                if(in == null) {
                    UIUtility.showInfoDialog(this, I18N.get("plugins.notConfigurable"));
                    return;
                }
                getJarLocaleBundleInputStream(loader, (_in) -> {
                    final PropertyResourceBundle bundle;
                    if(_in != null) {
                        try {
                            bundle = new PropertyResourceBundle(new InputStreamReader(_in, StandardCharsets.UTF_8));
                        } catch (IOException ex) {
                            UIUtility.showErrorDialog(
                                    this, I18N.get("plugins.failedToRead"),
                                    "Failed to read plugin locale: " + ex.getMessage()
                            );
                            System.err.printf("Failed to read '%s' locale: %s%n", pluginInfo.getName(), ex.getMessage());
                            ex.printStackTrace();
                            return;
                        }
                    } else {
                        bundle = null;
                    }
                    openPreferences(pluginInfo, in, new I18NBundle(bundle));
                });
            }, true);
        } catch (IOException ex) {
            UIUtility.showErrorDialog(
                    this, I18N.get("plugins.failedToRead"),
                    "Failed to open plugin jar file: " + ex.getMessage()
            );
            System.err.printf("Failed to open plugin '%s' jar file: %s%n", pluginInfo.getName(), ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void getJarLocaleBundleInputStream(JarLoader loader, Consumer<InputStream> consumer) throws IOException {
        loader.open(I18NBundle.getLocaleFilePath(I18N.getLang()), (in) -> {
            if(in == null) {
                loader.open(I18NBundle.getLocaleFilePath(null), consumer::accept, true);
            } else {
                consumer.accept(in);
            }
        }, true);
    }

    private void openPreferences(PluginInfo pluginInfo, InputStream in, I18NBundle bundle) {
        final String json;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            json = reader.lines().collect(Collectors.joining(""));
        } catch (IOException ex) {
            UIUtility.showErrorDialog(
                    this, I18N.get("plugins.failedToOpen"),
                    "Failed to read preferences schema: " + ex.getMessage()
            );
            ex.printStackTrace();
            return;
        }

        final List<PreferenceGroup> preferences;
        try {
            preferences = PreferenceParser.parsePreferences(pluginInfo.getSource(), json, bundle);
        } catch (PreferenceParseException ex) {
            UIUtility.showErrorDialog(
                    this, I18N.get("plugins.failedToOpen"),
                    "Failed to parse preferences: " + ex.getMessage()
            );
            ex.printStackTrace();
            return;
        }

        if(preferences.isEmpty()) {
            UIUtility.showInfoDialog(this, I18N.get("plugins.emptyPreferences"));
            return;
        }

        final File configsDir = new File("configs");
        if(!configsDir.exists()) configsDir.mkdir();
        final File configurationFile = new File("configs", pluginInfo.getId() + Configuration.EXTENSION);
        final ConfigurationStore configuration = new ConfigurationStore();
        try {
            configuration.read(configurationFile);
        } catch (IOException ex) {
            UIUtility.showErrorDialog(
                    this, I18N.get("plugins.failedToOpen"),
                    "Failed to load configuration: " + ex.getMessage()
            );
            ex.printStackTrace();
            return;
        }

        new PreferencesFrame(
                pluginInfo.getName() + " " + I18N.get("preferences.subtitle"),
                pluginInfo.getId(), configurationFile, configuration, preferences
        ).showFrame(this);
    }
}
