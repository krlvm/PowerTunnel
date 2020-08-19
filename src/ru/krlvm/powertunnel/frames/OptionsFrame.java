package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.data.Settings;
import ru.krlvm.powertunnel.enums.SNITrick;
import ru.krlvm.powertunnel.ui.TextRightClickPopup;
import ru.krlvm.powertunnel.ui.TooltipCheckBox;
import ru.krlvm.powertunnel.ui.TooltipLabel;
import ru.krlvm.powertunnel.updater.UpdateNotifier;
import ru.krlvm.powertunnel.utilities.SystemUtility;
import ru.krlvm.powertunnel.utilities.UIUtility;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class OptionsFrame extends ControlFrame {

    private final int[] maxSize;

    private final JLabel updateLabel;
    private final JButton updateButton;
    private final JScrollPane scroll;

    /* ------------------------------------ */
    private final JCheckBox autoSetup;
    private final JCheckBox proxyPac; // server restart
    private final JCheckBox chunking;
    private final JCheckBox fullChunking;
    private final JTextField chunkSize;
    private final JCheckBox enableSniTricks;  // server restart
    private final JComboBox<String> sniTrick;
    private final JTextField fakeSniHost;
    private final JCheckBox applyHttpHttps;
    private final JCheckBox payload;
    private final JCheckBox allowInvalidPackets;
    private final JCheckBox mixHostCase;
    private final JCheckBox completeMixHostCase;
    private final JCheckBox mixHostHeaderCase;
    private final JCheckBox dotAfterHost;
    private final JCheckBox lineBreakGet;
    private final JCheckBox spaceGet;
    private final JCheckBox useDnsSec;  // server restart
    private final JTextField dnsAddress;  // server restart
    private final JTextField blacklistMirror;
    private final JCheckBox allowRequestsToOriginServer;
    private final JCheckBox enableJournal;
    private final JCheckBox enableLogs;
    /* ------------------------------------ */

    //Restart required - previous values
    private boolean proxyPacVal;
    private boolean eraseSniVal;
    private boolean useDnsSecVal;
    private String dnsOverHttpsVal;
    private boolean allowRequestsToOriginServerVal;

    public OptionsFrame() {
        super("Options");
        JPanel root = new JPanel();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JPanel updatePanel = new JPanel(new BorderLayout());
        updateLabel = new JLabel("<html><b>No updates available</b></html>");
        updateLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        updateButton = new JButton("Check for updates");
        updateButton.addActionListener(e -> UpdateNotifier.checkAndNotify());
        updatePanel.add(updateButton, BorderLayout.WEST);
        updatePanel.add(updateLabel, BorderLayout.EAST);
        updatePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        if(!UpdateNotifier.ENABLED) {
            updateButton.setEnabled(false);
            updateLabel.setEnabled(false);
            updateLabel.setText("<html><b>Check for updates is disabled</b></html>");
        }

        JPanel buttonsPanel = new JPanel(new BorderLayout());
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> setVisible(false));
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            save();
            setVisible(false);
        });
        actionButtonsPanel.add(cancel);
        actionButtonsPanel.add(ok);

        JPanel resetPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reset = new JButton("Reset settings");
        reset.addActionListener(e -> {
            PowerTunnel.SETTINGS.reset();
            adjustSettings();
            JOptionPane.showMessageDialog(OptionsFrame.this, "Settings has been successfully reset",
                    PowerTunnel.NAME, JOptionPane.INFORMATION_MESSAGE);
        });
        resetPane.add(reset);
        buttonsPanel.add(resetPane, BorderLayout.WEST);
        buttonsPanel.add(actionButtonsPanel, BorderLayout.EAST);

        JPanel panel = new JPanel(new GridBagLayout());

        autoSetup = new TooltipCheckBox("Auto system proxy setup",
                "Automatically setting up system proxy server configuration.\nAt the moment available only on the Windows.\n\nWindows: automatic Internet Explorer start for a few seconds can be required.");

        proxyPac = new TooltipCheckBox("Generate Proxy Auto Configuration file (PAC, server restart required)",
                "Can increase performance by using proxy only for blocked resources.\nYou should fill the government blacklist to use this option.\nCan slowdown connections with big blacklists.");

        panel.add(generateBlock("Proxy connection",
                (SystemUtility.IS_WINDOWS ? autoSetup : null),
                proxyPac
        ), gbc);

        chunking = new TooltipCheckBox("HTTPS: Enable chunking",
                "Fragments your HTTPS packets");

        fullChunking = new TooltipCheckBox("HTTPS: Full chunking mode (requires chunking enabled)",
                "Enables full chunking mode.\nCan led to higher CPU utilization, some websites from\nthe government blacklist may not accept connections,\nbut more efficient than the default (quiet) method.");

        JPanel chunkPane = newOptionPanel();
        chunkSize = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        TextRightClickPopup.register(chunkSize);
        JLabel chunkLabel = new TooltipLabel("Chunk size:", "Count of fragments HTTP packets be divided");
        chunkPane.add(chunkLabel);
        chunkPane.add(chunkSize, gbc);

        JPanel sniPane = newOptionPanel();
        enableSniTricks = new TooltipCheckBox("HTTPS: Enable SNI tricks (requires further setup, server restart required)",
                "When it enabled, PowerTunnel does some magic with Server Name Indication in your HTTPS requests");
        enableSniTricks.setBorder(null);
        JEditorPane sniWikiRef = UIUtility.getLabelWithHyperlinkSupport("<a href=\"" + SNITrick.SUPPORT_REFERENCE + "\">Read more...</a>", null);
        sniTrick = new JComboBox<>(new String[] { "Spoil SNI", "Erase SNI", "Fake SNI" });
        sniTrick.setSelectedIndex(0);
        sniTrick.setLightWeightPopupEnabled(false);
        sniPane.add(enableSniTricks);
        sniPane.add(sniWikiRef, gbc);
        sniPane.add(sniTrick, gbc);

        JPanel fakeSniPane = newOptionPanel();
        fakeSniHost = new JTextField(String.valueOf(PowerTunnel.SNI_TRICK_FAKE_HOST));
        TextRightClickPopup.register(fakeSniHost);
        JLabel fakeSniLabel = new TooltipLabel("Fake SNI host:", "The fake SNI host sends instead of host of the blocked website you want connect to,\nthe fake host usually has to be a government resource host,\nor host of any not blocked website.\nUsed in combination with 'fake' SNI trick.");
        fakeSniPane.add(fakeSniLabel);
        fakeSniPane.add(fakeSniHost, gbc);

        applyHttpHttps = new TooltipCheckBox("HTTPS: Apply HTTP tricks to HTTPS packets",
                "When this option is enabled, selected HTTP tricks will be applied to HTTPS too");

        payload = new TooltipCheckBox("HTTP: Send additional 21KB payload",
                "When it enabled, PowerTunnel adding 21KB of useless data before the Host header");

        mixHostCase = new TooltipCheckBox("HTTP: Mix host case",
                "When it enabled, PowerTunnel mixes case of the host header value of the website you're trying to connect.\nSome websites, especially working on the old web servers, may not accept connection.");

        completeMixHostCase = new TooltipCheckBox("HTTP: Complete mix host case",
                "When it enabled, PowerTunnel mixes case of the host header completely, not just the last letter.");

        mixHostHeaderCase = new TooltipCheckBox("HTTP: Mix host header case",
                "When it enabled, PowerTunnel mixes case of the host header.\nSome websites, especially working on the old web servers, may not accept connection.");

        dotAfterHost = new TooltipCheckBox("HTTP: Dot after host",
                "When it enabled, PowerTunnel adds a dot after the host header.");

        lineBreakGet = new TooltipCheckBox("HTTP: Line break before the GET method",
                "When it enabled, PowerTunnel adds a line break before the GET method.");

        spaceGet = new TooltipCheckBox("HTTP: Space after the GET method",
                "When it enabled, PowerTunnel adds a space after the GET method.");

        panel.add(generateBlock("DPI circumvention",
                chunking,
                fullChunking,
                chunkPane,
                sniPane,
                fakeSniPane,
                applyHttpHttps,
                payload,
                mixHostCase,
                completeMixHostCase,
                mixHostHeaderCase,
                dotAfterHost,
                lineBreakGet,
                spaceGet
        ), gbc);

        JPanel dohPane = newOptionPanel();
        dnsAddress = new JTextField(PowerTunnel.DNS_SERVER);
        TextRightClickPopup.register(dnsAddress);
        dnsAddress.setPreferredSize(new Dimension(400, ((int) dnsAddress.getPreferredSize().getHeight())));
        JLabel dohLabel = new TooltipLabel("DNS or DoH resolver (server restart required):", "DNS or DNS over HTTPS resolver address\nAddresses starts with 'https://' automatically recognizes as a DoH resolvers\nCompatible DoH addresses is listed in the repository readme");
        dohPane.add(dohLabel);
        dohPane.add(dnsAddress, gbc);

        useDnsSec = new TooltipCheckBox("Enable DNSSec (server restart required)",
                "Enables validating DNS server responses with\nthe Google DNS servers and protects you from the DNS substitution.\nCan slow down your connection a bit.\nMake sure you restart the server\nafter changing this option.");

        panel.add(generateBlock("Domain name resolving",
                dohPane,
                useDnsSec
        ), gbc);

        JPanel mirrorPane = newOptionPanel();
        blacklistMirror = new JTextField(String.valueOf(PowerTunnel.CHUNK_SIZE));
        TextRightClickPopup.register(blacklistMirror);
        JLabel blacklistLabel = new TooltipLabel("Government blacklist mirror:", "URL address from government blacklist automatically loads");
        mirrorPane.add(blacklistLabel);
        mirrorPane.add(blacklistMirror, gbc);

        allowInvalidPackets = new TooltipCheckBox("Allow invalid packets (recommended)",
                "When this option is disabled, HTTP packets without Host header throws out");

        allowRequestsToOriginServer = new TooltipCheckBox("Allow requests to origin server (server restart required)",
                "Experimental option, can fix some connectivity issues.");

        enableJournal = new TooltipCheckBox("Enable PowerTunnel Journal (restart required)",
                "Enables PowerTunnel Journal, collecting\nall websites you've been visited with timestamps.\nThis data doesn't sending anywhere.");

        enableLogs = new TooltipCheckBox("Enable PowerTunnel Logs (restart required)",
                "Enables PowerTunnel Logs that need for troubleshooting and debugging\nfrom the user interface.");

        panel.add(generateBlock("Proxy settings",
                mirrorPane,
                allowInvalidPackets,
                allowRequestsToOriginServer,
                enableJournal,
                enableLogs
        ), gbc);

        JPanel container = new JPanel(new BorderLayout());
        container.add(panel, BorderLayout.WEST);
        container.add(updatePanel, BorderLayout.SOUTH);

        root.add(container, BorderLayout.NORTH);
        root.add(buttonsPanel, BorderLayout.SOUTH);

        scroll = new JScrollPane(root);
        scroll.setBorder(null);
        add(scroll);

        getRootPane().setDefaultButton(ok);
        setResizable(true);

        pack(); // calculate the first size estimate
        fakeSniHost.setPreferredSize(new Dimension(fakeSniPane.getWidth()-fakeSniLabel.getWidth(), fakeSniHost.getHeight()));
        chunkSize.setPreferredSize(new Dimension(chunkPane.getWidth()-chunkLabel.getWidth(), chunkSize.getHeight()));
        blacklistMirror.setPreferredSize(new Dimension(mirrorPane.getWidth()-blacklistLabel.getWidth(), blacklistMirror.getHeight()));
        dnsAddress.setPreferredSize(new Dimension(dohPane.getWidth()-dohLabel.getWidth(), dnsAddress.getHeight()));

        pack();
        maxSize = new int[] { getWidth(), getHeight() };

        Dimension screenResolution = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenResolution.getWidth();
        double screenHeight = screenResolution.getHeight();
        setSize(
                (int)(Math.min(getWidth(), screenWidth)),
                (int)(Math.min(getHeight(), screenHeight-(screenHeight*0.1)))
        );
        handleResize();

        controlFrameInitialized();

        SwingUtilities.invokeLater(() -> addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                SwingUtilities.invokeLater(() -> handleResize());
            }
        }));
    }

    private void adjustSettings() {
        autoSetup.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED));
        autoSetup.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.AUTO_PROXY_SETUP_ENABLED));

        proxyPac.setSelected(proxyPacVal = PowerTunnel.SETTINGS.getBooleanOption(Settings.PROXY_PAC_ENABLED));
        proxyPac.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.PROXY_PAC_ENABLED));

        chunking.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ENABLE_CHUNKING));
        chunking.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ENABLE_CHUNKING));

        fullChunking.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.FULL_CHUNKING));
        fullChunking.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.FULL_CHUNKING));

        chunkSize.setText(PowerTunnel.SETTINGS.getOption(Settings.CHUNK_SIZE));
        chunkSize.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.CHUNK_SIZE));

        enableSniTricks.setSelected(eraseSniVal = PowerTunnel.SETTINGS.getIntOption(Settings.SNI_TRICK) != 0);
        enableSniTricks.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.SNI_TRICK));

        fakeSniHost.setText(PowerTunnel.SETTINGS.getOption(Settings.SNI_TRICK_FAKE_HOST));
        fakeSniHost.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.SNI_TRICK_FAKE_HOST));

        applyHttpHttps.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.APPLY_HTTP_TRICKS_TO_HTTPS));
        applyHttpHttps.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.APPLY_HTTP_TRICKS_TO_HTTPS));

        payload.setSelected(PowerTunnel.SETTINGS.getIntOption(Settings.PAYLOAD_LENGTH) != 0);
        payload.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.FULL_CHUNKING));

        allowInvalidPackets.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS));
        allowInvalidPackets.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.ALLOW_INVALID_HTTP_PACKETS));

        mixHostCase.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.MIX_HOST_CASE));
        mixHostCase.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.MIX_HOST_CASE));

        completeMixHostCase.setSelected(PowerTunnel.SETTINGS.getBooleanOption(Settings.COMPLETE_MIX_HOST_CASE));
        completeMixHostCase.setEnabled(!PowerTunnel.SETTINGS.isTemporary(Settings.COMPLETE_MIX_HOST_CASE));

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
                proxyPacVal != proxyPac.isSelected() ||
                useDnsSecVal != useDnsSec.isSelected() ||
                !dnsOverHttpsVal.equals(dnsAddress.getText()) ||
                allowRequestsToOriginServerVal != allowRequestsToOriginServer.isSelected() ||
                eraseSniVal != enableSniTricks.isSelected()
        );

        PowerTunnel.SETTINGS.setBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED, autoSetup.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.PROXY_PAC_ENABLED, proxyPac.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS, allowInvalidPackets.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.ENABLE_CHUNKING, chunking.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.FULL_CHUNKING, fullChunking.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.CHUNK_SIZE, chunkSize.getText());
        PowerTunnel.SETTINGS.setIntOption(Settings.SNI_TRICK, determineSniTrick());
        PowerTunnel.SETTINGS.setOption(Settings.SNI_TRICK_FAKE_HOST, fakeSniHost.getText());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.APPLY_HTTP_TRICKS_TO_HTTPS, applyHttpHttps.isSelected());
        PowerTunnel.SETTINGS.setOption(Settings.PAYLOAD_LENGTH, payload.isSelected() ? "21" : "0");
        PowerTunnel.SETTINGS.setBooleanOption(Settings.MIX_HOST_CASE, mixHostCase.isSelected());
        PowerTunnel.SETTINGS.setBooleanOption(Settings.COMPLETE_MIX_HOST_CASE, completeMixHostCase.isSelected());
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
                new Thread(PowerTunnel::restartServer, "Server Restart Thread").start();
            }
        }
    }

    private int determineSniTrick() {
        if(!enableSniTricks.isSelected()) {
            return 0;
        }
        return sniTrick.getSelectedIndex()+1;
    }

    public void updateAvailable(String version) {
        updateButton.setText("Update details");
        updateLabel.setText("<html><b>Update available: v" + version + "</b></html>");
    }

    @Override
    public void setVisible(boolean b) {
        if(!isVisible() && b) {
            adjustSettings();
        }
        super.setVisible(b);
    }

    private void handleResize() {
        int newWidth = getWidth();
        setSize(Math.min(newWidth, maxSize[0]), Math.min(getHeight(), maxSize[1]));
        if(newWidth == maxSize[0]) {
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        } else {
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }
    }
    
    private JPanel newOptionPanel() {
        return new JPanel(new FlowLayout(FlowLayout.LEADING));
    }

    private JPanel generateBlock(String title, JComponent... components) {
        JPanel panel = new JPanel(new GridBagLayout());

        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleJustification(TitledBorder.LEADING);
        panel.setBorder(border);

        for (JComponent component : components) {
            if(component == null) continue;
            panel.add(component, gbc);
        }

        panel.setSize(panel.getWidth(), panel.getHeight()-20);

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
