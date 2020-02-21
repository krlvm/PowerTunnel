package ru.krlvm.powertunnel.ui;

import ru.krlvm.powertunnel.utilities.UIUtility;

import javax.swing.*;
import java.awt.*;

public class ModernTooltip extends JToolTip {

    public ModernTooltip(JComponent component) {
        super();
        setComponent(component);
        if(!UIUtility.OLD_STYLE) {
            setBackground(Color.WHITE);
        }
    }
}
