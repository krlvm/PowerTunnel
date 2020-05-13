package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.UIUtility;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class AdvancedMainFrame extends MainFrame {

    private JLabel header;
    private JButton stateButton;
    private JTextField[] inputs;

    public AdvancedMainFrame() {
        JRootPane root = getRootPane();
        root.setLayout(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(8,0,0,0);

        header = new JLabel(getHeaderText());

        final JTextField ipInput = new JTextField();
        Insets insets = ipInput.getInsets();
        ipInput.setPreferredSize(new Dimension(SwingDPI.scale(200)+insets.left+insets.right,
                SwingDPI.scale(22)+insets.top+insets.bottom));
        ipInput.setToolTipText("IP Address");
        //ipInput.setHorizontalAlignment(SwingConstants.CENTER);
        ipInput.setText(String.valueOf(PowerTunnel.SERVER_IP_ADDRESS));

        final JTextField portInput = new JTextField();
        insets = portInput.getInsets();
        portInput.setPreferredSize(SwingDPI.scale(75+insets.left+insets.right,
                22+insets.top+insets.bottom));
        portInput.setToolTipText("Port");
        //portInput.setHorizontalAlignment(SwingConstants.CENTER);
        portInput.setText(String.valueOf(PowerTunnel.SERVER_PORT));

        inputs = new JTextField[]{ipInput, portInput};

        stateButton = new JButton("Start server");
        stateButton.setPreferredSize(new Dimension((int)stateButton.getPreferredSize().getWidth(), (int)portInput.getPreferredSize().getHeight()));
        stateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (PowerTunnel.getStatus() == ServerStatus.RUNNING) {
                            PowerTunnel.stopServer();
                        } else {
                            try {
                                PowerTunnel.SERVER_IP_ADDRESS = ipInput.getText();
                                PowerTunnel.SERVER_PORT = Integer.parseInt(portInput.getText());
                                String error = PowerTunnel.safeBootstrap();
                                if (error != null) {
                                    JOptionPane.showMessageDialog(AdvancedMainFrame.this, error,
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(AdvancedMainFrame.this, "Invalid port",
                                        "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }).start();
            }
        });

        JButton logButton = new JButton("Logs");
        logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.logFrame.showFrame();
            }
        });
        logButton.setEnabled(PowerTunnel.ENABLE_LOGS);

        JButton journalButton = new JButton("Journal");
        journalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.journalFrame.showFrame();
            }
        });
        journalButton.setEnabled(PowerTunnel.ENABLE_JOURNAL);

        JButton userBlacklist = new JButton("Blacklist");
        userBlacklist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.USER_FRAMES[0].showFrame();
            }
        });

        JButton userWhitelist = new JButton("Whitelist");
        userWhitelist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.USER_FRAMES[1].showFrame();
            }
        });

        JButton options = new JButton("Options");
        options.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.optionsFrame.showFrame();
            }
        });

        JButton reload = new JButton("Reload");
        reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PowerTunnel.loadLists();
                    JOptionPane.showMessageDialog(AdvancedMainFrame.this,
                            "Government blacklist and user lists has been reloaded",
                            PowerTunnel.NAME, JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(AdvancedMainFrame.this,
                            "An error occurred while reloading lists: " + ex.getMessage(),
                            PowerTunnel.NAME, JOptionPane.INFORMATION_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        JButton about = new JButton("About");
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.optionsFrame.showFrame();
            }
        });

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        //panel.add(new JLabel("IP Address:"));
        panel.add(ipInput);
        //panel.add(new JLabel("Port:"));
        panel.add(portInput);
        panel.add(stateButton);
        pane.add(header, gbc);
        pane.add(panel, gbc);
        root.add(pane, BorderLayout.NORTH);

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JPanel generalButtonsPane = new JPanel(new GridLayout(2, 1));

        JPanel firstButtonsRow = new JPanel();
        firstButtonsRow.add(logButton);
        firstButtonsRow.add(journalButton);
        firstButtonsRow.add(userBlacklist);
        firstButtonsRow.add(userWhitelist);

        JPanel secondButtonsRow = new JPanel();
        secondButtonsRow.add(reload);
        secondButtonsRow.add(options);
        secondButtonsRow.add(about);

        generalButtonsPane.add(firstButtonsRow);
        generalButtonsPane.add(secondButtonsRow);

        pane.add(generalButtonsPane, gbc);
        pane.add(UIUtility.getLabelWithHyperlinkSupport("<a href=\"" + PowerTunnel.REPOSITORY_URL + "/issues\">Submit a bug</a> | <a href=\"https://github.com/krlvm/PowerTunnel-Android\">Android version</a> | " + "<a href=\"" + PowerTunnel.REPOSITORY_URL + "/wiki\">Help</a><br>" +
                "<b><a style=\"color: black\" href=\"" + PowerTunnel.REPOSITORY_URL + "\">" + PowerTunnel.REPOSITORY_URL + "</a>" +
                "</b><br><br>(c) krlvm, 2019-2020", "text-align: center"), gbc);

        pack();
        setResizable(false);
        controlFrameInitialized();
        setVisible(true);

        stateButton.requestFocus();
        stateButton.requestFocusInWindow();
        root.setDefaultButton(stateButton);

        //save data
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(PowerTunnel.getStatus() != ServerStatus.NOT_RUNNING && PowerTunnel.getTray().isLoaded()) {
                    PowerTunnel.getTray().showNotification(PowerTunnel.NAME + " is still working in tray mode");
                    return;
                }
                PowerTunnel.handleClosing();
            }
        });
    }

    @Override
    public void update() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean running = PowerTunnel.getStatus() == ServerStatus.RUNNING;
                stateButton.setText((running ? "Stop" : "Start") + " server");
                header.setText(getHeaderText());
                boolean activateUI = !(PowerTunnel.getStatus() == ServerStatus.STARTING || PowerTunnel.getStatus() == ServerStatus.STOPPING);
                stateButton.setEnabled(activateUI);
                for (JTextField input : inputs) {
                    input.setEditable(PowerTunnel.getStatus() == ServerStatus.NOT_RUNNING);
                }
            }
        });
    }

    private String getHeaderText() {
        return getCenteredLabel("<b>" + PowerTunnel.NAME + " v" + PowerTunnel.VERSION + "</b><br>Server " + PowerTunnel.getStatus() + "</div></html>");
    }

    private String getCenteredLabel(String text) {
        return "<html><div style='text-align: center;'>" + text + "</div></html>";
    }
}
