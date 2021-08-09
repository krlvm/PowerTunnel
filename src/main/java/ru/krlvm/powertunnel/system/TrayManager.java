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

package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.TrayUtility;
import ru.krlvm.powertunnel.utilities.UIUtility;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrayManager {

    private TrayIcon trayIcon;

    public void load() throws AWTException {
        TrayUtility.initializeFonts();

        PopupMenu popup = new PopupMenu();

        popup.add(TrayUtility.getItem(PowerTunnel.NAME, null, true));
        popup.add(TrayUtility.getItem("Version " + PowerTunnel.VERSION, null, true));
        popup.add(TrayUtility.getItem("(c) krlvm, 2019-2021", null, true));

        popup.addSeparator();

        popup.add(TrayUtility.getItem("Open " + PowerTunnel.NAME, e -> PowerTunnel.showMainFrame()));

        popup.addSeparator();

        if(PowerTunnel.ENABLE_LOGS) {
            popup.add(TrayUtility.getItem("Logs", e -> PowerTunnel.logFrame.showFrame()));
        }

        if(PowerTunnel.ENABLE_JOURNAL) {
            popup.add(TrayUtility.getItem("Journal", e -> PowerTunnel.journalFrame.showFrame()));
        }

        popup.add(TrayUtility.getItem("Blacklist", e -> PowerTunnel.USER_FRAMES[0].showFrame()));
        popup.add(TrayUtility.getItem("Whitelist", e -> PowerTunnel.USER_FRAMES[1].showFrame()));
        popup.add(TrayUtility.getItem("Options", e -> PowerTunnel.optionsFrame.showFrame()));

        popup.addSeparator();

        popup.add(TrayUtility.getItem("Exit", e -> PowerTunnel.handleClosing()));

        trayIcon = new TrayIcon(UIUtility.UI_ICON, PowerTunnel.NAME, popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(PowerTunnel.NAME + " " + PowerTunnel.VERSION);
        trayIcon.addActionListener(e -> PowerTunnel.showMainFrame());
        SystemTray.getSystemTray().add(trayIcon);

        TrayUtility.freeFonts();
    }

    public void unload() {
        SystemTray.getSystemTray().remove(trayIcon);
        trayIcon = null;
    }

    public void showNotification(String message) {
        if(!isLoaded()) return;
        trayIcon.displayMessage(PowerTunnel.NAME, message, TrayIcon.MessageType.NONE);
    }

    public boolean isLoaded() {
        return trayIcon != null;
    }
}
