package ru.krlvm.powertunnel.frames;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.UIUtility;
import ru.krlvm.swingdpi.ScalableJFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Base implementation of JFrame (SwingDPI/ScalableJFrame)
 */
public abstract class ControlFrame extends ScalableJFrame {

    public ControlFrame() {
        this(null);
    }

    public ControlFrame(String title) {
        super(title == null ? PowerTunnel.NAME + " v" + PowerTunnel.VERSION : title + " - " + PowerTunnel.NAME);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setIconImage(UIUtility.UI_ICON);
    }

    public void showFrame() {
        setVisible(true);
        setState(Frame.NORMAL);
        toFront();
        requestFocus();
    }

    /**
     * Monotonous actions will be called after
     * frame complete initialization
     */
    protected void controlFrameInitialized() {
        setLocationRelativeTo(null);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(UIUtility.correct(width), UIUtility.correct(height));
    }
}
