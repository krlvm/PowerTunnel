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
    private JCheckBox allowInvalidPackets;
    private JCheckBox mixHostCase;
    private JCheckBox useDnsSec;
    private JTextField dnsOverHttps;
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

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
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
        actionButtonsPanel.add(cancel);
        actionButtonsPanel.add(ok);

        JPanel resetPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.SETTINGS.reset();
                adjustSettings();
                JOptionPane.showMessageDialog(OptionsFrame.this, "Settings has been successfully reset",
                        "PowerTunnel", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        resetPane.add(reset);
        buttonsPanel.add(resetPane, BorderLayout.WEST);
        buttonsPanel.add(actionButtonsPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;

        autoSetup = new TooltipCheckBox("Auto system proxy setup (Windows)",
                "Automatically setting up system proxy server configuration.<br>At the moment available only on the Windows systems.<br>Can require automatic Internet Explorer start for a few seconds.");
        panel.add(autoSetup, gbc);

        fullChunking = new TooltipCheckBox("HTTPS: Full chunking mode",
                "Enables full chunking mode.<br>Can led to higher CPU utilization, some websites from<br>the government blacklist may not accept connections,<br>but more efficient than the default (quiet) method.");
        panel.add(fullChunking, gbc);

        JPanel chunkPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        chunkSize = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        JLabel chunkLabel = new TooltipLabel("Chunk size:", "Count of fragments HTTP packets be divided");
        chunkPane.add(chunkLabel);
        chunkPane.add(chunkSize, gbc);
        panel.add(chunkPane, gbc);

        payload = new TooltipCheckBox("HTTP: Send additional 21KB payload",
                "When it enabled, PowerTunnel adding 21KB of useless data before the Host header");
        panel.add(payload, gbc);

        allowInvalidPackets = new TooltipCheckBox("HTTP: Allow invalid packets (recommended)",
                "When this option is disabled, HTTP packets without Host header throws out");
        panel.add(allowInvalidPackets, gbc);

        mixHostCase = new TooltipCheckBox("HTTP: Mix host case",
                "When it enabled, PowerTunnel mixing case of the host of the website you're trying to connect.<br>Some websites, especially working on the old web servers, may not accept connection.");
        panel.add(mixHostCase, gbc);

        useDnsSec = new TooltipCheckBox("Use DNSSec mode (server restart required)",
                "Enables validating DNS server responses with<br>the Google DNS servers and protects you from the DNS substitution.<br>Can slow down your connection a bit.<br>Make sure you restart the server<br>after changing this option.");
        panel.add(useDnsSec, gbc);

        JPanel dohPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        dnsOverHttps = new JTextField(PowerTunnel.DOH_ADDRESS);
        dnsOverHttps.setPreferredSize(new Dimension(400, ((int) dnsOverHttps.getPreferredSize().getHeight())));
        JLabel dohLabel = new TooltipLabel("DoH resolver (server restart required):", "DNS over HTTPS server address<br>Compatible DoH addresses is listed in the repository readme");
        dohPane.add(dohLabel);
        dohPane.add(dnsOverHttps, gbc);
        panel.add(dohPane, gbc);

        JPanel mirrorPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
        blacklistMirror = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        JLabel blacklistLabel = new TooltipLabel("Government blacklist mirror:", "URL address from government blacklist automatically loads");
        mirrorPane.add(blacklistLabel);
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

        chunkSize.setPreferredSize(new Dimension(dohPane.getWidth()-chunkLabel.getWidth(), chunkSize.getHeight()));
        blacklistMirror.setPreferredSize(new Dimension(dohPane.getWidth()-blacklistLabel.getWidth(), blacklistMirror.getHeight()));
        dnsOverHttps.setPreferredSize(new Dimension(dnsOverHttps.getWidth()+15, dnsOverHttps.getHeight()));
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

        allowInvalidPackets.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS));
        allowInvalidPackets.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ALLOW_INVALID_HTTP_PACKETS));

        mixHostCase.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.MIX_HOST_CASE));
        mixHostCase.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.MIX_HOST_CASE));

        useDnsSec.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.USE_DNS_SEC));
        useDnsSec.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.USE_DNS_SEC));

        dnsOverHttps.setText(PowerTunnel.SETTINGS.getOption(Settings.DOH_ADDRESS));
        dnsOverHttps.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.DOH_ADDRESS));

        blacklistMirror.setText(PowerTunnel.SETTINGS.getOption(Settings.GOVERNMENT_BLACKLIST_MIRROR));
        blacklistMirror.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.GOVERNMENT_BLACKLIST_MIRROR));

        enableJournal.setSelected(!PowerTunnel.SETTINGS.getBooleanOption(Settings.DISABLE_JOURNAL));
        enableJournal.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.DISABLE_JOURNAL));
    }

    private void save() {
        PowerTunnel.SETTINGS.setBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED, autoSetup.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS, allowInvalidPackets.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.FULL_CHUNKING, fullChunking.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.CHUNK_SIZE, chunkSize.getText());
        PowerTunnel.SETTINGS.setOption(Settings.PAYLOAD_LENGTH, payload.isSelected() ? "21" : "0");
        PowerTunnel.SETTINGS.setBooleanOption(Settings.MIX_HOST_CASE, mixHostCase.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.USE_DNS_SEC, useDnsSec.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.DOH_ADDRESS, dnsOverHttps.getText());
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
