package ru.krlvm.powertunnel.ui;

import javax.swing.*;
import java.awt.*;

public class ModernTooltip extends JToolTip {

    public ModernTooltip(JComponent component) {
        super();
        setComponent(component);
        setBackground(Color.WHITE);
    }
}
