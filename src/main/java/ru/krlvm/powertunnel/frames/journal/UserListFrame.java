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

import ru.krlvm.powertunnel.frames.ControlFrame;

import javax.swing.*;
import java.awt.*;

public abstract class UserListFrame extends ControlFrame {

    private final String type;
    private final DefaultListModel<String> model;

    public UserListFrame(String type) {
        super(type);
        this.type = type;
        setSize(1000, 500);

        final JList<String> swingList = new JList<>(getElements());
        swingList.setModel(new DefaultListModel<>());
        model = ((DefaultListModel<String>) swingList.getModel());

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            String value = swingList.getSelectedValue();
            if(value != null) {
                userActed(swingList.getSelectedValue());
            }
        });
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(removeButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(swingList));
        getContentPane().add(panel);
        getContentPane().add(buttonsPanel, "Last");
        getRootPane().setDefaultButton(removeButton);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        removeButton.requestFocus();

        refill();
        controlFrameInitialized();
    }

    public void refill() {
        model.removeAllElements();
        for (String b : getElements()) {
            model.addElement(b);
        }
    }

    public final String type() {
        return type;
    }
    protected abstract void userActed(String address);
    protected abstract String[] getElements();
}
