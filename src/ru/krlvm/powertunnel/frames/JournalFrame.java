package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;

public class JournalFrame extends ControlFrame {

    private Thread refillThread;
    private DefaultListModel<String> model;

    public JournalFrame() {
        super(PowerTunnel.NAME + " | Journal");
        setSize(1200, 700);

        final JList<String> swingList = new JList<>(getVisited());
        swingList.setModel(new DefaultListModel<String>());
        model = ((DefaultListModel<String>) swingList.getModel());
        refill();

        JButton addToWhitelist = new JButton("Allow");
        addToWhitelist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = swingList.getSelectedValue();
                if(value != null) {
                    PowerTunnel.addToUserWhitelist(value.split(": ")[1]);
                    refill();
                }
            }
        });

        JButton addToBlacklist = new JButton("Block");
        addToBlacklist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = swingList.getSelectedValue();
                if(value != null) {
                    PowerTunnel.addToUserBlacklist(value.split(": ")[1]);
                    refill();
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(swingList));
        getContentPane().add(panel);

        panel = new JPanel();
        panel.add(addToWhitelist, BorderLayout.WEST);
        panel.add(addToBlacklist, BorderLayout.EAST);
        getContentPane().add(panel, "Last");
        getRootPane().setDefaultButton(addToBlacklist);

        refillThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            refill();
                        }
                    });
                }
            }
        }, "Visited refill thread");
        refillThread.start();

        controlFrameInitialized();
    }

    private void refill() {
        model.removeAllElements();
        for (String b : getVisited()) {
            model.addElement(b);
        }
    }

    private String[] getVisited() {
        LinkedList<String> list = new LinkedList<>();
        for (Map.Entry<String, String> entry : PowerTunnel.getJournal().entrySet()) {
            list.add(entry.getValue() + entry.getKey());
        }
        return list.toArray(new String[0]);
    }

    public void stopRefilling() {
        refillThread.interrupt();
    }
}
