package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.data.Settings;
import ru.krlvm.powertunnel.ui.TooltipCheckBox;
import ru.krlvm.powertunnel.ui.TooltipLabel;
import ru.krlvm.powertunnel.updater.UpdateNotifier;
import ru.krlvm.powertunnel.utilities.SystemUtility;
import ru.krlvm.powertunnel.utilities.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OptionsFrame extends ControlFrame {

    private final JLabel updateLabel;
    private final JButton updateButton;

    /* ------------------------------------ */
    private final JCheckBox autoSetup;
    private final JCheckBox chunking;
    private final JCheckBox fullChunking;
    private final JTextField chunkSize;
    private final JCheckBox eraseSni;     // server restart
    private final JCheckBox payload;
    private final JCheckBox allowInvalidPackets;
    private final JCheckBox mixHostCase;
    private final JCheckBox mixHostHeaderCase;
    private final JCheckBox dotAfterHost;
    private final JCheckBox lineBreakGet;
    private final JCheckBox spaceGet;
    private final JCheckBox useDnsSec;    // server restart
    private final JTextField dnsAddress;  // server restart
    private final JTextField blacklistMirror;
    private final JCheckBox allowRequestsToOriginServer;
    private final JCheckBox enableJournal;
    private final JCheckBox enableLogs;
    /* ------------------------------------ */

    //Restart required - previous values
    private boolean eraseSniVal;
    private boolean useDnsSecVal;
    private String dnsOverHttpsVal;
    private boolean allowRequestsToOriginServerVal;

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

        autoSetup = new TooltipCheckBox("Auto system proxy setup",
                "Automatically setting up system proxy server configuration.<br>At the moment available only on the Windows systems.<br>Can require automatic Internet Explorer start for a few seconds.");
        if(SystemUtility.OS.contains("windows")) {
            panel.add(autoSetup, gbc);
        }

        chunking = new TooltipCheckBox("HTTPS: Enable chunking",
                "Fragments your HTTPS packets");
        panel.add(chunking, gbc);

        fullChunking = new TooltipCheckBox("HTTPS: Full chunking mode (requires chunking enabled)",
                "Enables full chunking mode.<br>Can led to higher CPU utilization, some websites from<br>the government blacklist may not accept connections,<br>but more efficient than the default (quiet) method.");
        panel.add(fullChunking, gbc);

        JPanel chunkPane = newOptionPanel();
        chunkSize = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        JLabel chunkLabel = new TooltipLabel("Chunk size:", "Count of fragments HTTP packets be divided");
        chunkPane.add(chunkLabel);
        chunkPane.add(chunkSize, gbc);
        panel.add(chunkPane, gbc);

        JPanel eraseSniPane = newOptionPanel();
        eraseSni = new TooltipCheckBox("HTTPS: Erase SNI (requires further setup, server restart required)",
                "When it enabled, PowerTunnel removes Server Name Indication from your HTTPS requests");
        JEditorPane eraseSniWiki = UIUtility.getLabelWithHyperlinkSupport("<a href=\"https://github.com/krlvm/PowerTunnel/wiki/SNI-Erasing\">Read more...</a>", null);
        eraseSni.setBorder(null);
        eraseSniPane.add(eraseSni);
        eraseSniPane.add(eraseSniWiki, gbc);
        panel.add(eraseSniPane, gbc);

        payload = new TooltipCheckBox("HTTP: Send additional 21KB payload",
                "When it enabled, PowerTunnel adding 21KB of useless data before the Host header");
        panel.add(payload, gbc);

        allowInvalidPackets = new TooltipCheckBox("HTTP: Allow invalid packets (recommended)",
                "When this option is disabled, HTTP packets without Host header throws out");
        panel.add(allowInvalidPackets, gbc);

        mixHostCase = new TooltipCheckBox("HTTP: Mix host case",
                "When it enabled, PowerTunnel mixes case of the host header value of the website you're trying to connect.<br>Some websites, especially working on the old web servers, may not accept connection.");
        panel.add(mixHostCase, gbc);

        mixHostHeaderCase = new TooltipCheckBox("HTTP: Mix host header case",
                "When it enabled, PowerTunnel mixes case of the host header.<br>Some websites, especially working on the old web servers, may not accept connection.");
        panel.add(mixHostHeaderCase, gbc);

        dotAfterHost = new TooltipCheckBox("HTTP: Dot after host",
                "When it enabled, PowerTunnel adds a dot after the host header.");
        panel.add(dotAfterHost, gbc);

        lineBreakGet = new TooltipCheckBox("HTTP: Line break before the GET method",
                "When it enabled, PowerTunnel adds a line break before the GET method.");
        panel.add(lineBreakGet, gbc);

        spaceGet = new TooltipCheckBox("HTTP: Space after the GET method",
                "When it enabled, PowerTunnel adds a space after the GET method.");
        panel.add(spaceGet, gbc);

        useDnsSec = new TooltipCheckBox("Use DNSSec mode (server restart required)",
                "Enables validating DNS server responses with<br>the Google DNS servers and protects you from the DNS substitution.<br>Can slow down your connection a bit.<br>Make sure you restart the server<br>after changing this option.");
        panel.add(useDnsSec, gbc);

        JPanel dohPane = newOptionPanel();
        dnsAddress = new JTextField(PowerTunnel.DNS_SERVER);
        dnsAddress.setPreferredSize(new Dimension(400, ((int) dnsAddress.getPreferredSize().getHeight())));
        JLabel dohLabel = new TooltipLabel("DNS or DoH resolver (server restart required):", "DNS or DNS over HTTPS resolver address<br>Addresses starts with 'https://' automatically recognizes as a DoH resolvers<br>Compatible DoH addresses is listed in the repository readme");
        dohPane.add(dohLabel);
        dohPane.add(dnsAddress, gbc);
        panel.add(dohPane, gbc);

        JPanel mirrorPane = newOptionPanel();
        blacklistMirror = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        JLabel blacklistLabel = new TooltipLabel("Government blacklist mirror:", "URL address from government blacklist automatically loads");
        mirrorPane.add(blacklistLabel);
        mirrorPane.add(blacklistMirror, gbc);
        panel.add(mirrorPane, gbc);

        allowRequestsToOriginServer = new TooltipCheckBox("Allow requests to origin server (server restart required)",
                "Experimental option, can fix some connectivity issues.");
        panel.add(allowRequestsToOriginServer, gbc);

        enableJournal = new TooltipCheckBox("Enable PowerTunnel Journal (restart required)",
                "Enables PowerTunnel Journal, collecting<br>all websites you've been visited with timestamps.<br>This data doesn't sending anywhere.");
        panel.add(enableJournal, gbc);

        enableLogs = new TooltipCheckBox("Enable PowerTunnel Logs (restart required)",
                "Enables PowerTunnel Logs that need for troubleshooting and debugging<br>from the user interface.");
        panel.add(enableLogs, gbc);

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
        dnsAddress.setPreferredSize(new Dimension(dnsAddress.getWidth()+15, dnsAddress.getHeight()));
        pack();

        controlFrameInitialized();
    }

    private void adjustSettings() {
        autoSetup.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED));
        autoSetup.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.AUTO_PROXY_SETUP_ENABLED));

        chunking.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ENABLE_CHUNKING));
        chunking.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ENABLE_CHUNKING));

        fullChunking.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.FULL_CHUNKING));
        fullChunking.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.FULL_CHUNKING));

        chunkSize.setText(PowerTunnel.SETTINGS.getOption(Settings.CHUNK_SIZE));
        chunkSize.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.CHUNK_SIZE));

        eraseSni.setSelected(eraseSniVal = PowerTunnel.SETTINGS.getBooleanOption(Settings.ERASE_SNI));
        eraseSni.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ERASE_SNI));

        payload.setSelected(PowerTunnel.SETTINGS.getIntOption(Settings.PAYLOAD_LENGTH) != 0);
        payload.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.FULL_CHUNKING));

        allowInvalidPackets.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS));
        allowInvalidPackets.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ALLOW_INVALID_HTTP_PACKETS));

        mixHostCase.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.MIX_HOST_CASE));
        mixHostCase.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.MIX_HOST_CASE));

        mixHostHeaderCase.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.MIX_HOST_HEADER_CASE));
        mixHostHeaderCase.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.MIX_HOST_HEADER_CASE));

        dotAfterHost.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.DOT_AFTER_HOST_HEADER));
        dotAfterHost.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.DOT_AFTER_HOST_HEADER));

        lineBreakGet.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.LINE_BREAK_BEFORE_GET));
        lineBreakGet.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.LINE_BREAK_BEFORE_GET));

        spaceGet.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ADDITIONAL_SPACE_AFTER_GET));
        spaceGet.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ADDITIONAL_SPACE_AFTER_GET));

        useDnsSec.setSelected(useDnsSecVal = PowerTunnel.SETTINGS.getBooleanOption(Settings.USE_DNS_SEC));
        useDnsSec.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.USE_DNS_SEC));

        dnsAddress.setText(dnsOverHttpsVal = PowerTunnel.SETTINGS.getOption(Settings.DNS_ADDRESS));
        dnsAddress.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.DNS_ADDRESS));

        blacklistMirror.setText(PowerTunnel.SETTINGS.getOption(Settings.GOVERNMENT_BLACKLIST_MIRROR));
        blacklistMirror.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.GOVERNMENT_BLACKLIST_MIRROR));

        allowRequestsToOriginServer.setSelected(allowRequestsToOriginServerVal = PowerTunnel.SETTINGS.getBooleanOption(Settings.ALLOW_REQUESTS_TO_ORIGIN_SERVER));
        allowRequestsToOriginServer.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ALLOW_REQUESTS_TO_ORIGIN_SERVER));

        enableJournal.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ENABLE_JOURNAL));
        enableJournal.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ENABLE_JOURNAL));

        enableLogs.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ENABLE_LOGS));
        enableLogs.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ENABLE_LOGS));
    }

    private void save() {
        final boolean suggestRestart = (
                useDnsSecVal != useDnsSec.isSelected() ||
                !dnsOverHttpsVal.equals(dnsAddress.getText()) ||
                allowRequestsToOriginServerVal != allowRequestsToOriginServer.isSelected() ||
                eraseSniVal != eraseSni.isSelected()
        );

        PowerTunnel.SETTINGS.setBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED, autoSetup.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS, allowInvalidPackets.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ENABLE_CHUNKING, chunking.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.FULL_CHUNKING, fullChunking.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.CHUNK_SIZE, chunkSize.getText());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ERASE_SNI, eraseSni.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.PAYLOAD_LENGTH, payload.isSelected() ? "21" : "0");
        PowerTunnel.SETTINGS.setBooleanOption(Settings.MIX_HOST_CASE, mixHostCase.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.MIX_HOST_HEADER_CASE, mixHostHeaderCase.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.DOT_AFTER_HOST_HEADER, dotAfterHost.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.LINE_BREAK_BEFORE_GET, lineBreakGet.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ADDITIONAL_SPACE_AFTER_GET, spaceGet.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.USE_DNS_SEC, useDnsSec.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.DNS_ADDRESS, dnsAddress.getText());
        PowerTunnel.SETTINGS.setOption(Settings.GOVERNMENT_BLACKLIST_MIRROR, blacklistMirror.getText());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ALLOW_REQUESTS_TO_ORIGIN_SERVER, allowRequestsToOriginServer.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ENABLE_JOURNAL, enableJournal.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ENABLE_LOGS, enableLogs.isSelected());
        PowerTunnel.loadSettings();

        if (suggestRestart && PowerTunnel.isRunning()) {
            if (JOptionPane.showConfirmDialog(this, "<html>The changes you made requires server restart to take effect.<br>Restart server?</html>", PowerTunnel.NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PowerTunnel.restartServer();
                    }
                }, "Server Restart Thread").start();
            }
        }
    }

    public void updateAvailable(String version) {
        updateButton.setText("Update details");
        updateLabel.setText("<html><b>An update available: v" + version + "</b></html>");
    }

    @Override
    public void setVisible(boolean b) {
        if(!isVisible() && b) {
            adjustSettings();
        }
        super.setVisible(b);
    }
    
    private JPanel newOptionPanel() {
        return new JPanel(new FlowLayout(FlowLayout.LEADING));
    }
}
