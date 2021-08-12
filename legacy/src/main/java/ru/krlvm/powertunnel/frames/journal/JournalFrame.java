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

        JButton addToWhitelist = new JButton("Whitelist");
        addToWhitelist.addActionListener(e -> {
            String value = swingList.getSelectedValue();
            if(value != null) {
                PowerTunnel.addToUserWhitelist(value.split(": ")[1]);
                refill();
            }
        });

        JButton addToBlacklist = new JButton("Blacklist");
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
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        getRootPane().setDefaultButton(addToBlacklist);

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
}
