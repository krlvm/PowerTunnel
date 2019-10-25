package ru.krlvm.powertunnel.frames;

import ru.krlvm.swingdpi.ScalableJFrame;

import javax.swing.*;

/**
 * Base implementation of JFrame (SwingDPI/ScalableJFrame)
 */
public abstract class ControlFrame extends ScalableJFrame {

    public ControlFrame() {
        this(null);
    }

    public ControlFrame(String title) {
        super(title);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    /**
     * Monotonous actions will be called after
     * frame complete initialization
     */
    protected void controlFrameInitialized() {
        setLocationRelativeTo(null);
    }
}
