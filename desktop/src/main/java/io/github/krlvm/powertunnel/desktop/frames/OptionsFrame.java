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
import io.github.krlvm.powertunnel.desktop.application.DesktopApp;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.ui.I18N;
import io.github.krlvm.powertunnel.desktop.updater.UpdateNotifier;
import io.github.krlvm.powertunnel.desktop.utilities.SystemUtility;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.preferences.Preference;
import io.github.krlvm.powertunnel.preferences.PreferenceGroup;
import io.github.krlvm.powertunnel.preferences.PreferenceType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OptionsFrame extends PreferencesFrame {

    private static List<PreferenceGroup> getPreferences() {
        final List<PreferenceGroup> groups = new ArrayList<>();
        List<Preference> preferences;

        {
            preferences = new ArrayList<>();

            if (SystemUtility.IS_WINDOWS) {
                preferences.add(pref(
                        "auto_proxy_setup",
                        "true",
                        PreferenceType.SWITCH
                ));
            }
            preferences.add(pref(
                    "upstream_proxy_enabled",
                    "false",
                    PreferenceType.SWITCH
            ));
            preferences.add(pref(
                    "upstream_proxy_host",
                    "",
                    PreferenceType.STRING,
                    "upstream_proxy_enabled", "true", null
            ));
            preferences.add(pref(
                    "upstream_proxy_port",
                    "8080",
                    PreferenceType.NUMBER,
                    "upstream_proxy_enabled", "true", null
            ));

            preferences.add(pref(
                    "upstream_proxy_auth_enabled",
                    "false",
                    PreferenceType.SWITCH,
                    "upstream_proxy_enabled", "true", null
            ));
            preferences.add(pref(
                    "upstream_proxy_auth_username",
                    "",
                    PreferenceType.STRING,
                    "upstream_proxy_auth_enabled", "true", null
            ));
            preferences.add(pref(
                    "upstream_proxy_auth_password",
                    "",
                    PreferenceType.STRING,
                    "upstream_proxy_auth_enabled", "true", null
            ));

            groups.add(new PreferenceGroup(I18N.get("options.group.proxyConnection"), null, preferences));
        }

        {
            preferences = new ArrayList<>();

            preferences.add(pref(
                    "proxy_auth_enabled",
                    "false",
                    PreferenceType.SWITCH
            ));
            preferences.add(pref(
                    "proxy_auth_username",
                    "",
                    PreferenceType.STRING,
                    "proxy_auth_enabled", "true", null
            ));
            preferences.add(pref(
                    "proxy_auth_password",
                    "",
                    PreferenceType.STRING,
                    "proxy_auth_enabled", "true", null
            ));

            preferences.add(pref(
                    "transparent_mode",
                    "true",
                    PreferenceType.SWITCH
            ));
            preferences.add(pref(
                    "strict_dns",
                    "false",
                    PreferenceType.SWITCH
            ));
            preferences.add(pref(
                    "allow_requests_to_origin_server",
                    "true",
                    PreferenceType.SWITCH
            ));

            groups.add(new PreferenceGroup(I18N.get("options.group.proxySettings"), null, preferences));
        }

        return groups;
    }

    private static Preference pref(String key, String defaultValue, PreferenceType type) {
        return pref(key, defaultValue, type, null, null, null);
    }
    private static Preference pref(String key, String defaultValue, PreferenceType type, String dependency, String dependencyValue, Map<String, String> items) {
        return new Preference(
                key,
                I18N.get("options." + key),
                I18N.get("options." + key + ".desc", null),
                defaultValue,
                type,
                dependency,
                dependencyValue,
                items
        );
    }

    public OptionsFrame(ConfigurationStore configuration) {
        super(I18N.get("options.title"), "",
                DesktopApp.CONFIGURATION_FILE, configuration,
                getPreferences()
        );

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                resetAppOptionsFrame();
            }
        });
    }

    @Override
    protected void frameInitialized() {
        final JPanel notePanel = new JPanel();
        notePanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        notePanel.add(new JLabel(I18N.get("options.note")));
        insertComponent(notePanel);

        if(UpdateNotifier.ENABLED) {
            final JPanel updatePanel = new JPanel(new BorderLayout());
            final JLabel updateLabel = new JLabel();
            final JButton updateButton = new JButton(I18N.get("updater.checkForUpdates"));

            updateButton.addActionListener(e -> {
                updateButton.setEnabled(false);
                setUpdateInfo(updateLabel, true);
                new Thread(() -> {
                    if(!UpdateNotifier.checkAndNotify(BuildConstants.NAME, BuildConstants.REPO, true)) {
                        UIUtility.showErrorDialog(OptionsFrame.this, I18N.get("updater.failedToCheck"));
                    }
                    setUpdateInfo(updateLabel, false);
                    updateButton.setEnabled(true);
                }, "App Update Checker").start();
            });

            updatePanel.add(updateLabel, BorderLayout.WEST);
            updatePanel.add(updateButton, BorderLayout.EAST);
            updatePanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            insertComponent(updatePanel);

            setUpdateInfo(updateLabel, false);
        }

        super.frameInitialized();
    }

    private void setUpdateInfo(JLabel label, boolean checking) {
        label.setText("<html><b>" +
                (checking ? I18N.get("updater.checkingForUpdates") : (UpdateNotifier.NEW_VERSION == null ?
                        I18N.get("updater.noUpdates"): I18N.get("updater.updateAvailable") + ": " + UpdateNotifier.NEW_VERSION
                )) +
                "</b></html>"
        );
    }

    @Override
    protected void onFailedToInitialize() {
        super.onFailedToInitialize();
        resetAppOptionsFrame();
    }

    private void resetAppOptionsFrame() {
        GraphicalApp.getInstance().optionsFrame = null;
    }
}
