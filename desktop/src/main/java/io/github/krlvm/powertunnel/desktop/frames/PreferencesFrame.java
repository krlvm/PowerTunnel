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

import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.preferences.Preference;
import io.github.krlvm.powertunnel.preferences.PreferenceGroup;
import io.github.krlvm.powertunnel.preferences.PreferenceType;
import io.github.krlvm.powertunnel.sdk.configuration.Configuration;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreferencesFrame extends AppFrame {

    private final File configurationFile;
    private final Configuration configuration;

    public PreferencesFrame(
            PluginInfo pluginInfo,
            File configurationFile,
            Configuration configuration,
            List<PreferenceGroup> preferences
    ) {
        super(pluginInfo.getName() + " preferences");

        this.configurationFile = configurationFile;
        this.configuration = configuration;

        final JRootPane root = getRootPane();
        root.setLayout(new GridBagLayout());

        for (PreferenceGroup group : preferences) {
            final List<JComponent> list = new ArrayList<>();

            for (Preference preference : group.getPreferences()) {
                final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));

                if(preference.getType() != PreferenceType.CHECKBOX && preference.getType() != PreferenceType.SWITCH) {
                    final JLabel label = new JLabel(preference.getTitle() + ":");
                    UIUtility.setTooltip(label, preference.getDescription());
                    panel.add(label);
                }

                final JComponent value;
                switch (preference.getType()) {
                    case SWITCH:
                    case CHECKBOX: {
                        value = new JCheckBox(preference.getTitle(), Boolean.parseBoolean(preference.getDefaultValue()));
                        break;
                    }
                    case NUMBER: {
                        value = new JTextField(preference.getDefaultValue());
                        // TODO: check input
                        break;
                    }
                    case STRING: {
                        value = new JTextField(preference.getDefaultValue());
                        break;
                    }
                    case SELECT: {
                        final List<String> names = new ArrayList<>();
                        for (Preference.SelectItem item : preference.getItems()) {
                            names.add(item.getName());
                        }
                        value = new JComboBox<>(names.toArray());
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unsupported type: " + preference.getType());
                }
                panel.add(value);

                list.add(panel);
            }

            root.add(createBlock(group.getTitle(), list), gbc);
        }

        frameInitialized();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void save() {
        try {
            configuration.save(configurationFile);
        } catch (IOException ex) {
            UIUtility.showErrorDialog(this, "Failed to save preferences: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        dispose();
    }

    private JPanel createBlock(String title, List<JComponent> components) {
        final JPanel panel = new JPanel(new GridBagLayout());

        if(title != null) {
            final TitledBorder border = BorderFactory.createTitledBorder(title);
            border.setTitleJustification(TitledBorder.LEADING);
            panel.setBorder(border);
        }

        for (JComponent component : components) {
            if(component == null) continue;
            panel.add(component, gbc);
        }

        return panel;
    }

    private static final GridBagConstraints gbc;
    static {
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
    }
}
