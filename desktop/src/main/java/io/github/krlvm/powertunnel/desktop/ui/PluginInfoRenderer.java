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

package io.github.krlvm.powertunnel.desktop.ui;

import io.github.krlvm.powertunnel.desktop.utilities.UIUtility;
import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

// https://stackoverflow.com/a/26476427
public class PluginInfoRenderer extends JPanel implements ListCellRenderer<PluginInfo> {

    private static final int PADDING = 2;
    private static final Border BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
    );

    private final JEditorPane infoLabel = new JEditorPane();

    public PluginInfoRenderer() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BORDER);
        add(infoLabel);
    }

    @Override
    public Dimension getMinimumSize() {
        return SwingDPI.scale(100, 72);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public Component getListCellRendererComponent(
            JList<? extends PluginInfo> list,
            PluginInfo value,
            int index,
            boolean isSelected,
            boolean cellHasFocus
    ) {
        setComponentOrientation(list.getComponentOrientation());

        Color backgroundColor = null;
        Color foregroundColor = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
            backgroundColor = UIManager.getColor("List.dropCellBackground");
            foregroundColor = UIManager.getColor("List.dropCellForeground");

            isSelected = true;
        }

        if (isSelected) {
            setBackground(backgroundColor == null ? list.getSelectionBackground() : backgroundColor);
            setForeground(foregroundColor == null ? list.getSelectionForeground() : foregroundColor);

            setBackground(Color.LIGHT_GRAY);
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        UIUtility.setEditorPaneContent(infoLabel, String.format(
                "<b>%s</b><br>%sVersion %s%s",
                value.getName(),
                value.getDescription() != null ? value.getDescription() + "<br>" : "",
                value.getVersion(),
                value.getAuthor() != null ? "<br><i>by " + value.getAuthor() + "</i>" : ""
        ), null, 0, false);
        infoLabel.setBackground(getBackground());
        infoLabel.setForeground(getForeground());

        setEnabled(list.isEnabled());

        return this;
    }
}