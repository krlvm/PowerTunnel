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
