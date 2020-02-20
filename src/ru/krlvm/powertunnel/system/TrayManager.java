package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.UIUtility;
import ru.krlvm.swingdpi.SwingDPI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class TrayManager {

    private TrayIcon trayIcon;

    public void load() throws AWTException {
        PopupMenu popup = new PopupMenu();

        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.SIZE, 12*SwingDPI.getScaleFactor());
        Font font = Font.getFont(attributes);
        attributes.put(TextAttribute.FAMILY, Font.DIALOG);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        Font headerFont = Font.getFont(attributes);

        MenuItem item;

        item = new MenuItem(PowerTunnel.NAME + " " + PowerTunnel.VERSION);
        item.setFont(headerFont);
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("(c) krlvm, 2019-2020");
        item.setFont(headerFont);
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("");
        item.setFont(font);
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("Open " + PowerTunnel.NAME);
        item.setFont(font);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.showMainFrame();
            }
        });
        popup.add(item);

        item = new MenuItem("");
        item.setFont(font);
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("Logs");
        item.setFont(font);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.logFrame.setVisible(true);
            }
        });
        popup.add(item);

        item = new MenuItem("Journal");
        item.setFont(font);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.journalFrame.setVisible(true);
            }
        });
        popup.add(item);

        item = new MenuItem("Blacklist");
        item.setFont(font);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.USER_FRAMES[0].setVisible(true);
            }
        });
        popup.add(item);

        item = new MenuItem("Whitelist");
        item.setFont(font);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.USER_FRAMES[1].setVisible(true);
            }
        });
        popup.add(item);

        item = new MenuItem("Options");
        item.setFont(font);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PowerTunnel.optionsFrame.setVisible(true);
            }
        });
        popup.add(item);

        item = new MenuItem("");
        item.setFont(font);
        item.setEnabled(false);
        popup.add(item);

        item = new MenuItem("Exit");
        item.setFont(headerFont);
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
    }

    public void unload() {
        SystemTray.getSystemTray().remove(trayIcon);
        trayIcon = null;
    }

    public void showNotification(String message) {
        if(!isLoaded()) return;
        trayIcon.displayMessage(PowerTunnel.NAME, message, TrayIcon.MessageType.INFO);
    }

    public boolean isLoaded() {
        return trayIcon != null;
    }
}
