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

package io.github.krlvm.powertunnel.desktop.managers;

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import io.github.krlvm.powertunnel.desktop.application.GraphicalApp;
import io.github.krlvm.powertunnel.desktop.ui.I18N;
import ru.krlvm.swingdpi.SwingDPI;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class TrayManager {

    private final GraphicalApp app;

    private TrayIcon trayIcon;

    private Font HEADER_FONT;
    private Font FONT;

    public TrayManager(GraphicalApp app) {
        this.app = app;
    }

    public void load() throws AWTException {
        initializeFonts();
        final ActionListener openAppListener = e -> app.showFrame();

        PopupMenu popup = new PopupMenu();

        popup.add(getItem(BuildConstants.NAME, null, true));
        popup.add(getItem(I18N.get("tray.version") + " " + BuildConstants.VERSION, null, true));
        popup.add(getItem("(c) krlvm, 2019-2021", null, true));

        popup.addSeparator();

        popup.add(getItem(I18N.get("tray.open"), openAppListener));

        popup.addSeparator();

        popup.add(getItem(I18N.get("main.plugins"), e -> app.showPluginsFrame()));
        popup.add(getItem(I18N.get("main.options"), e -> app.showOptionsFrame()));

        popup.addSeparator();

        popup.add(getItem(I18N.get("tray.exit"), e -> app.dispose()));

        trayIcon = new TrayIcon(GraphicalApp.ICON, BuildConstants.NAME, popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(BuildConstants.NAME);
        trayIcon.addActionListener(openAppListener);

        freeFonts();

        SystemTray.getSystemTray().add(trayIcon);
    }

    public void unload() {
        SystemTray.getSystemTray().remove(trayIcon);
        trayIcon = null;
    }

    public void showNotification(String message) {
        if(!isLoaded()) return;
        trayIcon.displayMessage(BuildConstants.NAME, message, TrayIcon.MessageType.NONE);
    }

    public boolean isLoaded() {
        return trayIcon != null;
    }


    private MenuItem getItem(String title, ActionListener listener) {
        return getItem(title, listener, false);
    }

    private MenuItem getItem(String title, ActionListener listener, boolean header) {
        final MenuItem item = new MenuItem(title);
        if(header) {
            item.setFont(HEADER_FONT);
            item.setEnabled(false);
        } else {
            item.setFont(FONT);
        }
        if(listener != null) item.addActionListener(listener);
        return item;
    }


    // region Fonts

    private void initializeFonts() {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.SIZE, 12* SwingDPI.getScaleFactor());
        FONT = Font.getFont(attributes);

        attributes.put(TextAttribute.FAMILY, Font.DIALOG);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        HEADER_FONT = Font.getFont(attributes);
    }

    private void freeFonts() {
        FONT = null;
        HEADER_FONT = null;
    }

    // endregion
}
