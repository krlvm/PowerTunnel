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
