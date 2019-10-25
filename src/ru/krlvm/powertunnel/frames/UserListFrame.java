package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class UserListFrame extends ControlFrame {

    private DefaultListModel<String> model;

    public UserListFrame() {
        setTitle(PowerTunnel.NAME + " | " + type());
        setSize(1000, 500);

        final JList<String> swingList = new JList<>(getElements());
        swingList.setModel(new DefaultListModel<String>());
        model = ((DefaultListModel<String>) swingList.getModel());

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String value = swingList.getSelectedValue();
                if(value != null) {
                    userActed(swingList.getSelectedValue());
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(swingList));
        getContentPane().add(panel);
        getContentPane().add(removeButton, "Last");

        refill();
        controlFrameInitialized();
    }

    public void refill() {
        model.removeAllElements();
        for (String b : getElements()) {
            model.addElement(b);
        }
    }

    public abstract String type();
    protected abstract void userActed(String address);
    protected abstract String[] getElements();
}
