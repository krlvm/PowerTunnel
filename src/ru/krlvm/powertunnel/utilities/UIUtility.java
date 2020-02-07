package ru.krlvm.powertunnel.utilities;

import ru.krlvm.powertunnel.frames.ControlFrame;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
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

    /**
     * Retrieves JEditorPane with hyperlink action support
     *
     * @param html - HTML code
     * @return JEditorPane with hyperlink action support
     */
    public static JEditorPane getLabelWithHyperlinkSupport(String html, String additionalStyles) {
        //We will copy style from this JLabel
        JLabel label = new JLabel();
        Font font = label.getFont();
        String style = "font-family:" + font.getFamily() + ";" +
                "font-weight:" + (font.isBold() ? "bold" : "normal") + ";" +
                "font-size:" + font.getSize() + "pt;";
        if(additionalStyles != null) {
            style += additionalStyles;
        }

        //Create message
        JEditorPane pane = new JEditorPane("text/html", "<html><body style=\"" + style + "\">" + html + "</body></html>");

        //Handle anchor
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        pane.setEditable(false);
        pane.setBackground(label.getBackground());
        pane.setHighlighter(null);

        return pane;
    }
}
