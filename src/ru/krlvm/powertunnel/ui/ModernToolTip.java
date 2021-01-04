package ru.krlvm.powertunnel.ui;

import javax.swing.*;

public class ModernToolTip extends JToolTip {

    public ModernToolTip(JComponent component) {
        super();
        setComponent(component);
    }

    public static class Label extends JLabel {

        public Label(String text, String tooltip) {
            super(text);
            setToolTipText("<html>" + tooltip.replace("\n", "<br>") + "</html>");
        }

        @Override
        public JToolTip createToolTip() {
            return new ModernToolTip(this);
        }
    }

    public static class Checkbox extends JCheckBox {

        public Checkbox(String text, String tooltip) {
            super(text);
            setToolTipText("<html>" + tooltip.replace("\n", "<br>") + "</html>");
        }

        @Override
        public JToolTip createToolTip() {
            return new ModernToolTip(this);
        }
    }
}
