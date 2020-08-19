package ru.krlvm.powertunnel.frames.journal;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.frames.ControlFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class JournalFrame extends ControlFrame {

    private final DefaultListModel<String> model;

    //private static final int REFILL_INTERVAL = 5; // in seconds
    //private final ScheduledExecutorService refillExecutor;

    public JournalFrame() {
        super("Journal");
        setSize(1000, 500);

        final JList<String> swingList = new JList<>(getVisited());
        swingList.setModel(new DefaultListModel<>());
        model = ((DefaultListModel<String>) swingList.getModel());
        refill();

        JButton addToWhitelist = new JButton("Allow");
        addToWhitelist.addActionListener(e -> {
            String value = swingList.getSelectedValue();
            if(value != null) {
                PowerTunnel.addToUserWhitelist(value.split(": ")[1]);
                refill();
            }
        });

        JButton addToBlacklist = new JButton("Block");
        addToBlacklist.addActionListener(e -> {
            String value = swingList.getSelectedValue();
            if(value != null) {
                PowerTunnel.addToUserBlacklist(value.split(": ")[1]);
                refill();
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

        //refillExecutor = Executors.newSingleThreadScheduledExecutor();
        //refillExecutor.scheduleAtFixedRate(() ->
        //        SwingUtilities.invokeLater(() -> {
        //            if(isVisible()) {
        //                refill();
        //            }
        //        }), REFILL_INTERVAL, REFILL_INTERVAL, TimeUnit.SECONDS
        //);

        controlFrameInitialized();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if(b) {
            refill();
        }
    }

    public void refill() {
        model.removeAllElements();
        for (String b : getVisited()) {
            model.addElement(b);
        }
    }

    public static String[] getVisited() {
        LinkedList<String> list = new LinkedList<>();
        for (Map.Entry<String, String> entry : PowerTunnel.getJournal().entrySet()) {
            list.add(entry.getValue() + entry.getKey());
        }
        return list.toArray(new String[0]);
    }

    //public void stopRefilling() {
    //    refillExecutor.shutdown();
    //}
}
