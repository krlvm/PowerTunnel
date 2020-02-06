package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.UIUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrayManager {

    private boolean loaded = false;
    private TrayIcon trayIcon;

    public void load() throws AWTException {
        PopupMenu popup = new PopupMenu();
        MenuItem item;

        item = new MenuItem(PowerTunnel.NAME + " is" + PowerTunnel.getStatus().toString());
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("(c) krlvm, 2019-2020");
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.handleClosing();
            }
        });
        popup.add(item);

        trayIcon = new TrayIcon(UIUtility.UI_ICON, PowerTunnel.NAME, popup);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(PowerTunnel.NAME + " " + PowerTunnel.VERSION);
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.showMainFrame();
            }
        });
        SystemTray.getSystemTray().add(trayIcon);
        loaded = true;
    }

    public void unload() {
        SystemTray.getSystemTray().remove(trayIcon);
        loaded = false;
    }

    public void showNotification(String message) {
        trayIcon.displayMessage(PowerTunnel.NAME, message, TrayIcon.MessageType.INFO);
    }

    public boolean isLoaded() {
        return loaded;
    }
}
