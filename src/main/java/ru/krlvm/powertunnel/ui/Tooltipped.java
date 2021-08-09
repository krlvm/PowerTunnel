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

package ru.krlvm.powertunnel.ui;

import javax.swing.*;

public class Tooltipped {

    public static class Label extends JLabel {
        public Label(String text, String tooltip) {
            super(text);
            setTooltip(this, tooltip);
        }
    }
    public static class TextField extends JTextField {
        public TextField(String tooltip) {
            setTooltip(this, tooltip);
        }
        public TextField(String text, String tooltip) {
            super(text);
            setTooltip(this, tooltip);
        }
    }
    public static class Checkbox extends JCheckBox {
        public Checkbox(String text, String tooltip) {
            super(text);
            setTooltip(this, tooltip);
        }
    }

    private static void setTooltip(JComponent component, String tooltip) {
        component.setToolTipText("<html>" + tooltip.replace("\n", "<br>") + "</html>");
    }
}