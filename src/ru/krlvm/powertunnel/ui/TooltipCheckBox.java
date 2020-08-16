package ru.krlvm.powertunnel.ui;

import javax.swing.*;

public class TooltipCheckBox extends JCheckBox {

    public TooltipCheckBox(String text, String tooltip) {
        super(text);
        setToolTipText("<html>" + tooltip.replace("\n", "<br>") + "</html>");
    }

    @Override
    public JToolTip createToolTip() {
        return new ModernTooltip(this);
    }
}
