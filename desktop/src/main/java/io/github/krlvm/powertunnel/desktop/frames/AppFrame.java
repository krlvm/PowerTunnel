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

package io.github.krlvm.powertunnel.desktop.frames;

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import ru.krlvm.swingdpi.ScalableJFrame;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Base implementation of JFrame (SwingDPI/ScalableJFrame)
 */
public abstract class AppFrame extends ScalableJFrame {

    public static final int PADDING = 8;
    public static final Border BORDER = BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING);

    protected final GraphicalApp app;

    public AppFrame() {
        this(null, null);
    }

    public AppFrame(GraphicalApp app) {
        this(null, app);
    }

    public AppFrame(String title) {
        this(title, null);
    }

    public AppFrame(String title, GraphicalApp app) {
        super((title != null ? title + " - " : "") + BuildConstants.NAME);
        this.app = app;

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setIconImage(GraphicalApp.ICON);
    }

    public void showFrame() {
        setVisible(true);
        setState(Frame.NORMAL);
        toFront();
        requestFocus();
    }

    protected void frameInitialized() {
        setLocationRelativeTo(null);
    }
}
