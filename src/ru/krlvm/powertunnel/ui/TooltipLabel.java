package ru.krlvm.powertunnel.ui;

import javax.swing.*;

public class TooltipLabel extends JLabel {

    public TooltipLabel(String text, String tooltip) {
        super(text);
        setToolTipText("<html>" + tooltip + "</html>");
    }

    @Override
    public JToolTip createToolTip() {
        return new ModernTooltip(this);
    }
}
