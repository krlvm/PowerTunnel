package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LogFrame extends ControlFrame {

    private static LogFrame instance = null;

    private final JTextArea logArea;
    private final JTextField addressInput;
    private final JPanel panel;
    private final JButton addToBlockList;
    private final JButton addToWhiteList;

    public LogFrame() {
        super("Log");
        setSize(900, 450);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        logArea.setFont(logArea.getFont().deriveFont(14F));
        SwingDPI.scaleFont(logArea);

        panel = new JPanel(new BorderLayout());

        addressInput = new JTextField();
        addressInput.setBackground(Color.WHITE);
        panel.add(addressInput, BorderLayout.CENTER);

        addToWhiteList = new JButton("Whitelist");
        addToWhiteList.addActionListener(e -> PowerTunnel.addToUserWhitelist(readInput()));
        panel.add(addToWhiteList, BorderLayout.WEST);

        addToBlockList = new JButton("Blacklist");
        addToBlockList.addActionListener(e -> PowerTunnel.addToUserBlacklist(readInput()));
        panel.add(addToBlockList, BorderLayout.EAST);

        getContentPane().add(new JScrollPane(logArea));
        getContentPane().add(panel, "Last");

        getRootPane().setDefaultButton(addToBlockList);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeInput();
            }
        });
        resizeInput();

        controlFrameInitialized();
        addressInput.requestFocus();

        instance = this;
    }

    private void resizeInput() {
        addressInput.setSize(new Dimension(panel.getWidth()- addToBlockList.getWidth()- addToWhiteList.getWidth(), panel.getHeight()));
    }

    private String readInput() {
        String text = addressInput.getText();
        addressInput.setText("");
        return text;
    }

    public static void print(String s) {
        if(instance == null) {
            return;
        }
        instance.logArea.append(s);
        if(!s.endsWith("\n")) {
            instance.logArea.append("\r\n");
        }
        instance.logArea.setCaretPosition(instance.logArea.getDocument().getLength());
    }
}
