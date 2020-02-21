package ru.krlvm.powertunnel.ui;

import ru.krlvm.powertunnel.utilities.SystemUtility;

import javax.swing.*;
import java.awt.*;

public class ModernTooltip extends JToolTip {

    public ModernTooltip(JComponent component) {
        super();
        setComponent(component);
        if(!SystemUtility.OLD_OS) {
            setBackground(Color.WHITE);
        }
    }
}
