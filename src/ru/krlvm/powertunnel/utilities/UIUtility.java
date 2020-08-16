package ru.krlvm.powertunnel.utilities;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.frames.ControlFrame;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

/**
 * Utility for working with Java Swing
 *
 * @author krlvm
 */
public class UIUtility {

    public static final Image UI_ICON
            = Toolkit.getDefaultToolkit().getImage(ControlFrame.class.getResource("/icon.png"));

    /**
     * Retrieves corrected value a window width/height
     * Needed for properly UI behavior on non-Windows operating systems
     *
     * 1,06 - is experimentally discovered Pi for UI scaling
     * in non-Windows guest, such as Linux/macOS
     *
     * @param value - dimension
     * @return corrected dimension
     */
    public static int correct(int value) {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            return value;
        }
        return (int)(value*1.06);
    }

    public static float getResidualScaleFactor() {
        return SwingDPI.isScaleApplied() ? SwingDPI.getScaleFactor() : 1;
    }

    public static void setAWTName() {
        Toolkit xToolkit = Toolkit.getDefaultToolkit();
        try {
            java.lang.reflect.Field awtAppClassNameField =
                    xToolkit.getClass().getDeclaredField("awtAppClassName");
            awtAppClassNameField.setAccessible(true);
            awtAppClassNameField.set(xToolkit, PowerTunnel.NAME);
        } catch (Exception ex) {
            Debugger.debug("Failed to set AWT name: " + ex.getMessage());
            //Debugger.debug(ex);
        }
    }

    public static JEditorPane getLabelWithHyperlinkSupport(String html, String additionalStyles) {
        return getLabelWithHyperlinkSupport(html, additionalStyles, false);
    }

    /**
     * Retrieves JEditorPane with hyperlink action support
     *
     * @param html - HTML code
     * @param padding - visual padding
     * @return JEditorPane with hyperlink action support
     */
    public static JEditorPane getLabelWithHyperlinkSupport(String html, String additionalStyles, boolean padding) {
        //We will copy style from this JLabel
        JLabel label = new JLabel();
        Color background = label.getBackground();
        Font font = label.getFont();
        String style = "font-family:" + font.getFamily() + ";" +
                "font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
                "font-size:" + font.getSize() + "pt;" +
                "background-color:#" + Integer.toHexString(background.getRGB()).substring(2) + ";";
        if(additionalStyles != null) {
            style += additionalStyles;
        }

        //Create message
        JEditorPane pane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" + html + "</body></html>");

        //Handle anchor
        pane.addHyperlinkListener(e -> {
            if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        pane.setEditable(false);
        if(padding) {
            pane.setBorder(new LineBorder(background, (int) (3 * SwingDPI.getScaleFactor())));
        } else {
            pane.setBorder(new EmptyBorder(0,0,0,0));
        }
        pane.setBackground(label.getBackground());
        pane.setHighlighter(null);

        return pane;
    }
}
