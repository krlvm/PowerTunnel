package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.UIUtility;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends ControlFrame {

    private JLabel header;
    private JButton stateButton;
    private JTextField[] inputs;

    public MainFrame() {
        super(PowerTunnel.NAME + " v" + PowerTunnel.VERSION);
        float multiplier = SwingDPI.isScaleApplied() ? (SwingDPI.getScaleFactor() / (SwingDPI.getScaleFactor() - 0.25F)) + 0.05F : 1.3F;
        Debugger.debug("Scale multiplier: " + multiplier);
        setSize((int) (324 * (UIUtility.getResidualScaleFactor() * multiplier)),
                (int) (182 * (UIUtility.getResidualScaleFactor() * multiplier)));

        header = new JLabel(getHeaderText());

        final JTextField ipInput = new JTextField();
        ipInput.setPreferredSize(SwingDPI.scale(200, 22));
        ipInput.setToolTipText("IP Address");
        ipInput.setText(String.valueOf(PowerTunnel.SERVER_IP_ADDRESS));

        final JTextField portInput = new JTextField();
        portInput.setPreferredSize(SwingDPI.scale(76, 22));
        portInput.setToolTipText("Port");
        portInput.setText(String.valueOf(PowerTunnel.SERVER_PORT));

        inputs = new JTextField[]{ipInput, portInput};

        stateButton = new JButton("Start server");
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
                                    JOptionPane.showMessageDialog(MainFrame.this, error,
                                            "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(MainFrame.this, "Invalid port",
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
                PowerTunnel.logFrame.setVisible(true);
            }
        });

        JButton journalButton = new JButton("Journal");
        journalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.journalFrame.setVisible(true);
            }
        });

        JButton userBlacklist = new JButton("Blacklist");
        userBlacklist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.USER_FRAMES[0].setVisible(true);
            }
        });

        JButton userWhitelist = new JButton("Whitelist");
        userWhitelist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.USER_FRAMES[1].setVisible(true);
            }
        });

        JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        setContentPane(mainPanel);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(header);
        mainPanel.add(panel, "First");

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(ipInput);
        panel.add(portInput);
        mainPanel.add(panel, "Center");

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(stateButton);
        mainPanel.add(panel, "Center");

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(logButton);
        panel.add(journalButton);
        panel.add(userBlacklist);
        panel.add(userWhitelist);
        mainPanel.add(panel, "Last");

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(UIUtility.getLabelWithHyperlinkSupport("<a href=\"" + PowerTunnel.REPOSITORY_URL + "/issues\">Submit a bug</a> | " + "<a href=\"" + PowerTunnel.REPOSITORY_URL + "/wiki\">Help</a><br>" +
                "<b><a style=\"color: black\" href=\"" + PowerTunnel.REPOSITORY_URL + "\">" + PowerTunnel.REPOSITORY_URL + "</a>" +
                "</b><br><br>(c) krlvm, 2019-2020", "text-align: center"));
        mainPanel.add(panel, "Last");

        getRootPane().setDefaultButton(stateButton);
        setResizable(false);
        controlFrameInitialized();
        setVisible(true);

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
        return getCenteredLabel("<b>" + PowerTunnel.NAME + " v" + PowerTunnel.VERSION + "</b><br>Server is" + PowerTunnel.getStatus() + "</div></html>");
    }

    private String getCenteredLabel(String text) {
        return "<html><div style='text-align: center;'>" + text + "</div></html>";
    }
}
