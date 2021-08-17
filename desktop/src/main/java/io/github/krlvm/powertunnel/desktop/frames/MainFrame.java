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
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.system.SystemProxy;
import io.github.krlvm.powertunnel.desktop.ui.FieldFilter;
import io.github.krlvm.powertunnel.desktop.i18n.I18N;
import io.github.krlvm.powertunnel.desktop.ui.JPanelCallback;
import io.github.krlvm.powertunnel.desktop.ui.TextRightClickPopup;
import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends AppFrame {

    private static final String ABOUT_MESSAGE = String.format("" +
            "%s<br><br>" +
            "<a href=\"%s\">%s</a> is made possible by these open-source projects:" +
            "<br><br>" +
            " • <a href=\"https://github.com/adamfisk/LittleProxy\">LittleProxy</a> - proxy server, <a href=\"https://github.com/mrog/LittleProxy\">forked</a> version<br>" +
            " • <a href=\"https://github.com/ganskef/LittleProxy-mitm\">LittleProxy-MITM</a> - LittleProxy SSL extension<br>" +
            " • <a href=\"https://github.com/dnsjava/dnsjava\">dnsjava</a> - DNS library, DoH realization<br>" +
            " • <a href=\"https://github.com/ibauersachs/dnssecjava\">dnssecjava</a> - DNSSec realization for dnsjava<br>" +
            " • <a href=\"https://github.com/adamfisk/DNSSEC4J\">DNSSEC4J</a> - DNSSec realization for LittleProxy<br>" +
            " • <a href=\"https://github.com/java-native-access/jna\">Java Native Access</a> - accessing system native API<br>" +
            " • <a href=\"https://github.com/krlvm/SwingDPI\">SwingDPI</a> - High DPI scaling" +
            "<br><br>" +
            "Get <a href=\"https://github.com/krlvm/PowerTunnel-Android\">version for Android</a>" +
            "<br><br>" +
            "Version %s [%s]<br>" +
            "Core version: %s [%s]" +
            "<br><br>" +
            "Licensed under the<br>" +
            "<a href=\"https://raw.githubusercontent.com/krlvm/PowerTunnel/master/LICENSE\">GNU General Public License v3</a>" +
            "<br><br>" +
            "(c) krlvm, 2019-2021",

            BuildConstants.DESCRIPTION, BuildConstants.REPO, BuildConstants.NAME,
            BuildConstants.VERSION, BuildConstants.VERSION_CODE,
            io.github.krlvm.powertunnel.BuildConstants.VERSION, io.github.krlvm.powertunnel.BuildConstants.VERSION_CODE
    );
    private final JLabel header;
    private final JButton stateButton;

    private final JTextField ipField;
    private final JTextField portField;

    private final JPanel buttonsPanel;
    private final JPanel extensibleButtonsPanel;

    public MainFrame(GraphicalApp app) {
        super(null, app);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8, 0, 0, 0);

        header = new JLabel();

        // region Proxy Control Pane

        ipField = createField("IP Address", 200, 22);
        ((PlainDocument) ipField.getDocument()).setDocumentFilter(new FieldFilter.IP());
        portField = createField("Port", 75, 22);
        ((PlainDocument) portField.getDocument()).setDocumentFilter(new FieldFilter.Number());

        stateButton = new JButton("Start server");
        stateButton.setPreferredSize(new Dimension(
                (int) stateButton.getPreferredSize().getWidth(),
                (int) portField.getPreferredSize().getHeight()
        ));
        stateButton.addActionListener(e -> new Thread(() -> {
            if (app.getStatus() == ProxyStatus.RUNNING) {
                app.stop();
            } else {
                final String ip = ipField.getText();
                final int port = Integer.parseInt(portField.getText());
                app.getConfiguration().set("ip", ip);
                app.getConfiguration().setInt("port", port);
                app.setAddress(new ProxyAddress(ip, port));

                if(SystemProxy.isSupported() && app.getConfiguration().getBoolean("auto_proxy_setup_ask", true)) {
                    final int result = UIUtility.showYesNoDialog(this, I18N.get("main.configureAutomaticallyPrompt"));
                    app.getConfiguration().setBoolean("auto_proxy_setup", result == JOptionPane.YES_OPTION);
                    app.getConfiguration().setBoolean("auto_proxy_setup_ask", false);
                }

                app.start();
            }
        }).start());

        // endregion

        JPanel proxyControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        proxyControlPanel.add(ipField);
        proxyControlPanel.add(portField);
        proxyControlPanel.add(stateButton);

        // region Second Row Buttons

        final JButton pluginsButton = new JButton(I18N.get("main.plugins"));
        pluginsButton.addActionListener(e -> app.showPluginsFrame());

        final JButton optionsButton = new JButton(I18N.get("main.options"));
        optionsButton.addActionListener(e -> app.showOptionsFrame());

        final JButton aboutButton = new JButton(I18N.get("main.about"));
        aboutButton.addActionListener(e -> {
            JEditorPane message = UIUtility.getLabelWithHyperlinkSupport(ABOUT_MESSAGE, null, 3);
            JOptionPane.showMessageDialog(this, message,
                    "About " + BuildConstants.NAME, JOptionPane.INFORMATION_MESSAGE
            );
        });

        // endregion

        extensibleButtonsPanel = new JPanel();
        extensibleButtonsPanel.setVisible(false);

        final JPanel secondButtonsRow = new JPanel();
        secondButtonsRow.add(pluginsButton);
        secondButtonsRow.add(optionsButton);
        secondButtonsRow.add(aboutButton);

        buttonsPanel = new JPanel(new GridLayout(1, 1));
        buttonsPanel.add(secondButtonsRow);

        final JPanel panel = new JPanel(new GridBagLayout());
        panel.add(header, gbc);
        panel.add(proxyControlPanel, gbc);
        panel.add(buttonsPanel, gbc);
        panel.add(UIUtility.getLabelWithHyperlinkSupport(
                "<a href=\"" + BuildConstants.REPO + "/issues\">" + I18N.get("main.reportBug") + "</a> | <a href=\"https://t.me/powertunnel_dpi\">Telegram Channel</a> | " + "<a href=\"" + BuildConstants.REPO + "/wiki\">" + I18N.get("main.help") + "</a>" +
                        "<br>" +
                        "<b><a style=\"color: black\" href=\"" + BuildConstants.REPO + "\">" + BuildConstants.REPO + "</a></b>" +
                        "<br><br>" +
                        "(c) krlvm, 2019-2021",
                "text-align: center"
        ), gbc);

        final JRootPane root = getRootPane();
        root.setLayout(new BorderLayout());
        root.setBorder(BORDER);
        root.add(panel, BorderLayout.NORTH);
        
        pack();
        setResizable(false);
        frameInitialized();

        stateButton.requestFocus();
        stateButton.requestFocusInWindow();
        root.setDefaultButton(stateButton);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(app.getStatus() != ProxyStatus.NOT_RUNNING && app.isTrayAvailable()) {
                    app.showNotification(BuildConstants.NAME + " " + I18N.get("tray.stillRunning"));
                    return;
                }
                app.dispose();
            }
        });

        update();
    }

    public void update() {
        SwingUtilities.invokeLater(() -> {
            header.setText(getHeaderText());

            final ProxyStatus status = app.getStatus();
            stateButton.setText(I18N.get("main." + (app.isRunning() ? "stop" : "start")));
            stateButton.setEnabled(status != ProxyStatus.STARTING && status != ProxyStatus.STOPPING);

            final boolean notRunning = status == ProxyStatus.NOT_RUNNING;
            ipField.setEnabled(notRunning && !app.getConfiguration().getImmutableKeys().contains("ip"));
            portField.setEnabled(notRunning && !app.getConfiguration().getImmutableKeys().contains("port"));

            final ProxyAddress address = app.getAddress();
            ipField.setText(address.getHost());
            portField.setText(String.valueOf(address.getPort()));

            pack();
        });
    }

    private String getHeaderText() {
        return UIUtility.getCenteredLabel("" +
                "<b>" + BuildConstants.NAME + " v" + BuildConstants.VERSION + "</b>" +
                "<br>" + I18N.get("main.status." + app.getStatus().name())
        );
    }

    public void getExtensibleButtonsPanel(JPanelCallback callback) {
        buttonsPanel.setLayout(new GridLayout(2, 1));
        if(!extensibleButtonsPanel.isVisible()) {
            buttonsPanel.add(extensibleButtonsPanel, 0);
            extensibleButtonsPanel.setVisible(true);
        }
        callback.call(extensibleButtonsPanel);
        pack();
    }

    private static JTextField createField(String tooltip, int width, int height) {
        final JTextField field = new JTextField();
        final Insets insets = field.getInsets();
        field.setPreferredSize(SwingDPI.scale(
                width + insets.left + insets.right,
                height + insets.top + insets.bottom
        ));

        final int padding = ((int) (4 * SwingDPI.getScaleFactor()));
        field.setBorder(BorderFactory.createCompoundBorder(
                field.getBorder(),
                BorderFactory.createEmptyBorder(0, padding, 0, padding)
        ));

        TextRightClickPopup.register(field);
        UIUtility.setTooltip(field, tooltip);

        return field;
    }
}
