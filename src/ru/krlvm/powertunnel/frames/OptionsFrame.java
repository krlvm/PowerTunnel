package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.data.Settings;
import ru.krlvm.powertunnel.ui.TooltipCheckBox;
import ru.krlvm.powertunnel.ui.TooltipLabel;
import ru.krlvm.powertunnel.updater.UpdateNotifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OptionsFrame extends ControlFrame {

    private JLabel updateLabel;
    private JButton updateButton;

    /* ------------------------------------ */
    private JCheckBox autoSetup;
    private JCheckBox fullChunking;
    private JTextField chunkSize;
    private JCheckBox payload;
    private JCheckBox mixHostCase;
    private JCheckBox useDnsSec;
    private JTextField blacklistMirror;
    private JCheckBox enableJournal;
    /* ------------------------------------ */

    public OptionsFrame() {
        super("Options");
        JRootPane root = getRootPane();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel updatePanel = new JPanel(new BorderLayout());
        updateLabel = new JLabel("<html><b>No updates available</b></html>");
        updateLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        updateLabel.setEnabled(UpdateNotifier.ENABLED);
        updateButton = new JButton("Check for updates");
        updateButton.setEnabled(UpdateNotifier.ENABLED);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                UpdateNotifier.checkAndNotify();
            }
        });
        updatePanel.add(updateButton, BorderLayout.WEST);
        updatePanel.add(updateLabel, BorderLayout.EAST);
        updatePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        if(!UpdateNotifier.ENABLED) {
            updateLabel.setText("<html><b>Update checking is disabled</b></html>");
        }

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                setVisible(false);
            }
        });
        buttonsPanel.add(cancel);
        buttonsPanel.add(ok);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        autoSetup = new TooltipCheckBox("Auto system proxy setup",
                "Automatically setting up system proxy server configuration.<br>At the moment available only on the Windows systems.<br>May need Internet Explorer starting for a few seconds.");
        panel.add(autoSetup, gbc);

        fullChunking = new TooltipCheckBox("HTTPS: Full chunking mode",
                "Enables full chunking mode.<br>Can led to higher CPU utilization, some websites from<br>the government blacklist may not accept connections,<br>but more efficient than the default (quiet) method.");
        panel.add(fullChunking, gbc);

        JPanel chunkPane = new JPanel(new FlowLayout());
        chunkSize = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        chunkSize.setPreferredSize(new Dimension(50, ((int) chunkSize.getPreferredSize().getHeight())));
        chunkPane.add(new TooltipLabel("Chunk size:", "Count of fragments HTTP packets be divided"));
        chunkPane.add(chunkSize, gbc);
        panel.add(chunkPane, gbc);

        payload = new TooltipCheckBox("HTTP: Send payload",
                "When it enabled, PowerTunnel adding 21KB of useless data before the Host header");
        panel.add(payload, gbc);

        mixHostCase = new TooltipCheckBox("HTTP: Mix host case",
                "When it enabled, PowerTunnel mixing case of the host of the website you're trying to connect.<br>Some websites, especially working on the old webservers, may not accept connection.");
        panel.add(mixHostCase, gbc);

        useDnsSec = new TooltipCheckBox("Use DNSSec mode",
                "Enables validating DNS server responses with<br>the Google DNS servers and protects you from the DNS substitution.<br>Can slow down your connection a bit.");
        panel.add(useDnsSec, gbc);

        JPanel mirrorPane = new JPanel(new FlowLayout());
        blacklistMirror = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        blacklistMirror.setPreferredSize(new Dimension(200, ((int) blacklistMirror.getPreferredSize().getHeight())));
        mirrorPane.add(new TooltipLabel("Government blacklist mirror:", "URL address from government blacklist automatically loads"));
        mirrorPane.add(blacklistMirror, gbc);
        panel.add(mirrorPane, gbc);

        enableJournal = new TooltipCheckBox("Enable PowerTunnel Journal",
                "Enables PowerTunnel Journal, collecting<br>all websites you've been visited with timestamps.<br>This data doesn't sending anywhere.");
        panel.add(enableJournal, gbc);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.WEST);
        container.add(updatePanel, BorderLayout.SOUTH);

        root.add(container, BorderLayout.NORTH);
        root.add(buttonsPanel, BorderLayout.EAST);
        root.setDefaultButton(ok);
        setResizable(false);
        pack();
        controlFrameInitialized();
    }

    private void adjustSettings() {
        autoSetup.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED));
        autoSetup.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.AUTO_PROXY_SETUP_ENABLED));

        fullChunking.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.FULL_CHUNKING));
        fullChunking.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.FULL_CHUNKING));

        chunkSize.setText(PowerTunnel.SETTINGS.getOption(Settings.CHUNK_SIZE));
        chunkSize.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.CHUNK_SIZE));

        payload.setSelected(PowerTunnel.SETTINGS.getIntOption(Settings.PAYLOAD_LENGTH) != 0);
        payload.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.FULL_CHUNKING));

        mixHostCase.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.MIX_HOST_CASE));
        mixHostCase.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.MIX_HOST_CASE));

        useDnsSec.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.USE_DNS_SEC));
        useDnsSec.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.USE_DNS_SEC));

        blacklistMirror.setText(PowerTunnel.SETTINGS.getOption(Settings.GOVERNMENT_BLACKLIST_MIRROR));
        blacklistMirror.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.GOVERNMENT_BLACKLIST_MIRROR));

        enableJournal.setSelected(!PowerTunnel.SETTINGS.getBooleanOption(Settings.DISABLE_JOURNAL));
        enableJournal.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.DISABLE_JOURNAL));
    }

    private void save() {
        PowerTunnel.SETTINGS.setBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED, autoSetup.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.FULL_CHUNKING, fullChunking.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.CHUNK_SIZE, chunkSize.getText());
        PowerTunnel.SETTINGS.setOption(Settings.PAYLOAD_LENGTH, payload.isSelected() ? "21" : "0");
        PowerTunnel.SETTINGS.setBooleanOption(Settings.MIX_HOST_CASE, mixHostCase.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.USE_DNS_SEC, useDnsSec.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.GOVERNMENT_BLACKLIST_MIRROR, blacklistMirror.getText());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.DISABLE_JOURNAL, !enableJournal.isSelected());
        PowerTunnel.loadSettings();
    }

    public void updateAvailable(String version) {
        updateButton.setText("Update info");
        updateLabel.setText("<html><b>An update available: v" + version + "</b></html>");
    }

    @Override
    public void setVisible(boolean b) {
        if(!isVisible() && b) {
            adjustSettings();
        }
        super.setVisible(b);
    }
}
