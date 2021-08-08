package ru.krlvm.powertunnel.utilities;

import ru.krlvm.swingdpi.SwingDPI;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for working with Java AWT tray
 * Uses SwingDPI to scale tray popup menu
 *
 * @author krlvm
 */
public class TrayUtility {

    public static Font headerFont;
    private static Font font;

    public static void initializeFonts() {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.SIZE, 12* SwingDPI.getScaleFactor());
        font = Font.getFont(attributes);

        attributes.put(TextAttribute.FAMILY, Font.DIALOG);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        headerFont = Font.getFont(attributes);
    }

    public static void freeFonts() {
        font = null;
        headerFont = null;
    }

    public static MenuItem getItem(String title, ActionListener listener) {
        return getItem(title, listener, false);
    }

    public static MenuItem getItem(String title, ActionListener listener, boolean header) {
        MenuItem item = new MenuItem(title);
        if(header) {
            item.setFont(headerFont);
            item.setEnabled(false);
        } else {
            item.setFont(font);
        }
        if(listener != null) {
            item.addActionListener(listener);
        }
        return item;
    }
}
