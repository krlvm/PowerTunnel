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