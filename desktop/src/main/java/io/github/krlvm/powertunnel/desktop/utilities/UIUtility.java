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

package io.github.krlvm.powertunnel.desktop.utilities;

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.ui.I18N;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;

public class UIUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIUtility.class);

    public static int showYesNoDialog(JFrame parent, String message) {
        final Object[] options = { I18N.get("yes"), I18N.get("no") };
        return JOptionPane.showOptionDialog(
                parent,
                message,
                BuildConstants.NAME,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    public static int showYesNoCancelDialog(JFrame parent, String message) {
        final Object[] options = { I18N.get("yes"), I18N.get("no"), I18N.get("cancel") };
        return JOptionPane.showOptionDialog(
                parent,
                message,
                BuildConstants.NAME,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    public static void showInfoDialog(JFrame parent, String message) {
        showInfoDialog(parent, BuildConstants.NAME, message);
    }

    public static void showInfoDialog(JFrame parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorDialog(JFrame parent, String message) {
        showErrorDialog(parent, BuildConstants.NAME, message);
    }

    public static void showErrorDialog(JFrame parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void setTooltip(JComponent component, String tooltip) {
        component.setToolTipText("<html>" + tooltip.replace("\n", "<br>") + "</html>");
    }

    public static void tweakLook() {
        if(!SystemUtility.OLD_OS) {
            UIManager.put("ToolTip.background", new ColorUIResource(255, 255, 255)); // The color is #fff7c8.
            UIManager.put("ToolTip.border", BorderFactory.createLineBorder(new Color(135,135,135)));
        }
    }

    public static void setAWTName() {
        Toolkit xToolkit = Toolkit.getDefaultToolkit();
        try {
            java.lang.reflect.Field awtAppClassNameField =
                    xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, BuildConstants.NAME);
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("Failed to set AWT app name: {}", ex.getMessage(), ex);
        }
    }

    public static String getCenteredLabel(String text) {
        return "<html><div style='text-align: center;'>" + text + "</div></html>";
    }

    public static JEditorPane getLabelWithHyperlinkSupport(String html) {
        return getLabelWithHyperlinkSupport(html, null, 0);
    }

    public static JEditorPane getLabelWithHyperlinkSupport(String html, String styles) {
        return getLabelWithHyperlinkSupport(html, styles, 0);
    }

    /**
     * Creates JEditorPane with hyperlink support
     *
     * @param html HTML code
     * @param padding visual padding
     * @return JEditorPane with hyperlink action support
     */
    public static JEditorPane getLabelWithHyperlinkSupport(String html, String styles, int padding) {
        final JEditorPane pane = new JEditorPane();
        setEditorPaneContent(pane, html, styles, padding, true);
        return pane;
    }

    public static void setEditorPaneContent(JEditorPane pane, String html, String styles, int padding, boolean setBackground) {
        final JLabel label = new JLabel();
        final Color background = label.getBackground();
        final Font font = label.getFont();

        padding *= SwingDPI.getScaleFactor();

        String style = "font-family:" + font.getFamily() + ";" +
                "font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
                "font-size:" + font.getSize() + "pt;";
        if(setBackground) style += "background-color:#" + Integer.toHexString(background.getRGB()).substring(2) + ";";
        if(styles != null) style += styles;

        pane.setContentType("text/html");
        pane.setText("<html><body style=\"" + style + "\">" + html + "</body></html>");

        pane.addHyperlinkListener(e -> {
            if (!e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) return;
            try {
                Desktop.getDesktop().browse(e.getURL().toURI());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        pane.setEditable(false);
        pane.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
        pane.setBackground(label.getBackground());
        pane.setHighlighter(null);
    }
}
