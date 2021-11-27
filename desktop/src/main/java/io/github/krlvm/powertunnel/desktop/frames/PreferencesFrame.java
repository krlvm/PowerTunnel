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
import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.ui.I18N;
import io.github.krlvm.powertunnel.desktop.ui.*;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.preferences.Preference;
import io.github.krlvm.powertunnel.preferences.PreferenceGroup;
import io.github.krlvm.powertunnel.preferences.PreferenceType;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PreferencesFrame extends AppFrame {

    protected static Map<String, PreferencesFrame> OPENED_IDS = new HashMap<>();

    private final String id;
    private final File configurationFile;
    private final ConfigurationStore configuration;
    private final List<PreferenceGroup> preferences;

    private final ScrollablePanel root = new ScrollablePanel();
    private final JScrollPane scroll = new JScrollPane(root);

    private Set<JEditorPane> groupDescriptions = new HashSet<>();

    public PreferencesFrame(
            String title,
            String id,
            File configurationFile,
            ConfigurationStore configuration,
            List<PreferenceGroup> preferences
    ) {
        super(title);
        this.id = id;

        this.configurationFile = configurationFile;
        this.configuration = configuration;
        this.preferences = preferences;

        root.setLayout(new GridBagLayout());
        root.setBorder(BORDER);

        final ActionListener actionlistener = e -> updateDependencies();
        final ItemListener itemListener = e -> updateDependencies();

        final int len = preferences.size();
        for(int i = 0; i < len; i++) {
            final PreferenceGroup group = preferences.get(i);
            final List<JComponent> list = new ArrayList<>();

            for (Preference preference : group.getPreferences()) {
                final JPanel panel = new JPanel(new GridBagLayout());
                final GridBagConstraints c = new GridBagConstraints();

                c.gridx = 0;
                if(preference.getType() != PreferenceType.CHECKBOX && preference.getType() != PreferenceType.SWITCH) {
                    c.ipadx = SwingDPI.scale(5);
                    final JLabel label = new JLabel(preference.getTitle() + ":");
                    if(preference.getDescription() != null) UIUtility.setTooltip(label, preference.getDescription());
                    panel.add(label, c);
                    c.gridx++;
                }

                c.weightx = 1;
                c.fill = GridBagConstraints.HORIZONTAL;
                final JComponent value;
                switch (preference.getType()) {
                    case SWITCH:
                    case CHECKBOX: {
                        value = new JCheckBox(preference.getTitle(), getBooleanOption(preference));
                        ((JCheckBox) value).addItemListener(itemListener);
                        if(preference.getDescription() != null) UIUtility.setTooltip(value, preference.getDescription());
                        break;
                    }
                    case STRING:
                    case NUMBER: {
                        final String val;
                        if(preference.getType() == PreferenceType.NUMBER) {
                            try {
                                val = String.valueOf(getIntOption(preference));
                            } catch (NumberFormatException ex) {
                                ex.printStackTrace();
                                showResetPrompt(
                                        I18N.get("preferences.incorrectConfiguration"),
                                        null
                                );
                                onFailedToInitialize();
                                return;
                            }
                        } else {
                            val = getStringOption(preference);
                        }
                        final JTextField field = new JTextField(val);
                        if (preference.getType() == PreferenceType.NUMBER) {
                            ((PlainDocument) field.getDocument()).setDocumentFilter(new FieldFilter.Number());
                            field.addKeyListener(new KeyAdapter() {
                                @Override
                                public void keyPressed(KeyEvent e) {
                                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                                        field.setText(String.valueOf(Integer.parseInt(field.getText()) - 1));
                                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                                        field.setText(String.valueOf(Integer.parseInt(field.getText()) + 1));
                                    }
                                }
                            });
                        }
                        TextRightClickPopup.register(field);
                        value = field;
                        break;
                    }
                    case SELECT: {
                        final List<Preference.SelectPreferenceItem> models = preference.getItemsAsModels();
                        final JComboBox<Preference.SelectPreferenceItem> comboBox = new JComboBox<>(models.toArray(new Preference.SelectPreferenceItem[0]));
                        comboBox.setSelectedItem(models.stream().filter(m -> m.getKey().equals(getStringOption(preference)))
                                .findFirst().orElse(models.get(0))
                        );
                        comboBox.setLightWeightPopupEnabled(false);
                        comboBox.setRenderer(new SelectPreferenceRenderer());
                        comboBox.addItemListener(itemListener);
                        ComboBoxScroll.register(comboBox);
                        value = comboBox;
                        break;
                    }
                    default:
                        throw new IllegalStateException("Unsupported type: " + preference.getType());
                }
                preference.binding = value;
                panel.add(value, c);

                list.add(panel);
            }

            insertComponent(createBlock(group, list));
            if(len > 1 && i != len-1) {
                insertComponent(Box.createVerticalStrut(SwingDPI.scale(4)));
            }
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                final int result = UIUtility.showYesNoCancelDialog(PreferencesFrame.this, I18N.get("preferences.exitConfirmation"));
                if (result == JOptionPane.YES_OPTION) {
                    save();
                    dispose();
                } else if(result == JOptionPane.NO_OPTION) {
                    dispose();
                }
            }
            @Override
            public void windowClosed(WindowEvent e) {
                OPENED_IDS.remove(id);
            }
        });
        OPENED_IDS.put(id, this);

        if(!GraphicalApp.getInstance().getConfiguration().getImmutableKeys().isEmpty()) {
            final JPanel notePanel = new JPanel();
            notePanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            notePanel.add(new JLabel("<html>" + I18N.get("preferences.note") + "</html>"));
            insertComponent(notePanel);
        }

        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll);

        requestSpacing();
        frameInitialized();
    }

    public void insertComponent(Component component) {
        root.add(component, gbc);
    }

    @Override
    protected void frameInitialized() {
        final JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBorder(BorderFactory.createEmptyBorder(PADDING, 0, 0, 0));

        final JButton resetButton = new JButton(I18N.get("reset"));
        resetButton.addActionListener(e -> showResetPrompt(I18N.get("preferences.resetConfirmation"), this));

        final JButton cancelButton = new JButton(I18N.get("cancel"));
        cancelButton.addActionListener(e -> dispose());

        final JButton saveButton = new JButton(I18N.get("save"));
        saveButton.addActionListener(e -> {
            save();
            dispose();
        });

        final JPanel westWrapper = new JPanel(new FlowLayout());
        westWrapper.add(resetButton);

        final JPanel eastWrapper = new JPanel(new FlowLayout());
        eastWrapper.add(cancelButton);
        eastWrapper.add(saveButton);

        actionPanel.add(westWrapper, BorderLayout.WEST);
        actionPanel.add(eastWrapper, BorderLayout.EAST);
        insertComponent(actionPanel);

        saveButton.requestFocus();
        saveButton.requestFocusInWindow();
        getRootPane().setDefaultButton(saveButton);

        updateDependencies();

        pack();
        final int width = getWidth();
        groupDescriptions.forEach(groupDescription -> groupDescription.setVisible(true));
        groupDescriptions.clear();
        groupDescriptions = null;
        root.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
        pack();

        final Dimension screenResolution = Toolkit.getDefaultToolkit().getScreenSize();
        final double screenWidth = screenResolution.getWidth();
        final double screenHeight = screenResolution.getHeight();

        setSize(
                (int) (Math.min(width + SwingDPI.scale(75), 0.9 * screenWidth)),
                (int) (Math.min(
                        getHeight() + (PADDING * 0.5) + (0.1 * (getWidth() - width)),
                        0.9 * screenHeight
                ))
        );

        super.frameInitialized();
    }

    protected void requestSpacing() {
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        root.add(Box.createHorizontalGlue(), c);
    }

    private void onMalformedPreferences() {
        UIUtility.showErrorDialog(this, "Malformed preferences schema");
        dispose();
    }

    private String getStringOption(Preference preference) {
        if(isProtected(preference)) return GraphicalApp.getInstance().getConfiguration().get(getFullId(preference), preference.getDefaultValue());
        return configuration.get(preference.getKey(), preference.getDefaultValue());
    }
    private int getIntOption(Preference preference) {
        if(isProtected(preference)) return GraphicalApp.getInstance().getConfiguration().getInt(getFullId(preference), Integer.parseInt(preference.getDefaultValue()));
        return configuration.getInt(preference.getKey(), Integer.parseInt(preference.getDefaultValue()));
    }
    private boolean getBooleanOption(Preference preference) {
        if(isProtected(preference)) return GraphicalApp.getInstance().getConfiguration().getBoolean(getFullId(preference), Boolean.parseBoolean(preference.getDefaultValue()));
        return configuration.getBoolean(preference.getKey(), Boolean.parseBoolean(preference.getDefaultValue()));
    }

    private String getBindingValue(Preference preference) {
        final JComponent component = ((JComponent) preference.binding);
        switch (preference.getType()) {
            case SWITCH:
            case CHECKBOX: {
                return String.valueOf(((JCheckBox) component).isSelected());
            }
            case NUMBER:
            case STRING: {
                return ((JTextField) component).getText();
            }
            case SELECT: {
                final JComboBox<Preference.SelectPreferenceItem> cb = ((JComboBox<Preference.SelectPreferenceItem>) component);
                if(cb.getSelectedItem() == null) {
                    if(preference.getDefaultValue().equals("true")) return preference.getItems().keySet().iterator().next();
                    return preference.getItems().get(preference.getDefaultValue());
                }
                return ((Preference.SelectPreferenceItem) cb.getSelectedItem()).getKey();
            }
            default:
                throw new IllegalStateException("Unsupported type: " + preference.getType());
        }
    }

    private void showResetPrompt(String message, JFrame parent) {
        final int result = UIUtility.showYesNoDialog(parent, message);
        if (result == JOptionPane.YES_OPTION) {
            reset();
            dispose();
        }
    }
    private void reset() {
        configuration.clear();
        saveConfiguration();
    }

    private void save() {
        for (PreferenceGroup group : preferences) {
            for (Preference preference : group.getPreferences()) {
                configuration.set(preference.getKey(), getBindingValue(preference));
            }
        }
        saveConfiguration();
    }
    private void saveConfiguration() {
        try {
            configuration.save(configurationFile);
        } catch (IOException ex) {
            UIUtility.showErrorDialog(this, I18N.get("preferences.failedToSave") + ": " + ex.getMessage());
            System.err.println("Failed to save preferences: " + ex.getMessage());
            ex.printStackTrace();
        }

        if(GraphicalApp.getInstance().isRunning()) {
            final int result = UIUtility.showYesNoDialog(PreferencesFrame.this, I18N.get("preferences.restartProxyPrompt"));
            if (result == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> {
                    GraphicalApp.getInstance().stop();
                    GraphicalApp.getInstance().start();
                });
            } else {
                dispose();
            }
        }
    }

    private void updateDependencies() {
        for (PreferenceGroup group : preferences) {
            for (Preference preference : group.getPreferences()) {
                if(preference.binding == null) continue;
                ((JComponent) preference.binding).setEnabled(isSatisfied(preference));
            }
        }
    }
    private boolean isSatisfied(Preference preference) {
        if(isProtected(preference)) return false;

        if(preference.binding == null) return true;
        final String dependency = preference.getDependency();
        if(dependency == null) return true;
        final Preference target = PreferenceGroup.findPreference(preferences, dependency);
        if(target == null) return true;
        if(target.binding == null) return true;

        return getBindingValue(target).equals(preference.getDependencyValue()) && isSatisfied(target);
    }
    private boolean isProtected(Preference preference) {
        return GraphicalApp.getInstance().getConfiguration().getImmutableKeys().contains(getFullId(preference));
    }
    private String getFullId(Preference preference) {
        return (id.isEmpty() ? preference.getKey() : id + ".") + preference.getKey();
    }


    private JPanel createBlock(PreferenceGroup group, List<JComponent> components) {
        gbc.ipady = SwingDPI.scale(3);
        final JPanel panel = new JPanel(new GridBagLayout());

        if(group != null && group.getTitle() != null) {
            final TitledBorder border = BorderFactory.createTitledBorder(group.getTitle());
            border.setTitleJustification(TitledBorder.LEADING);
            panel.setBorder(BorderFactory.createCompoundBorder(border, GROUP_BORDER));
            if(group.getDescription() != null) {
                final JEditorPane descriptionPane = UIUtility.getLabelWithHyperlinkSupport(group.getDescription());
                panel.add(descriptionPane, gbc);
                descriptionPane.setVisible(false);
                groupDescriptions.add(descriptionPane);
            }
        }

        for (JComponent component : components) {
            if(component == null) continue;
            panel.add(component, gbc);
        }

        return panel;
    }
    private static final Border GROUP_BORDER = BorderFactory.createEmptyBorder(2, 4, 2, 4);
    private static final GridBagConstraints gbc = new GridBagConstraints();
    static {
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
    }
}
