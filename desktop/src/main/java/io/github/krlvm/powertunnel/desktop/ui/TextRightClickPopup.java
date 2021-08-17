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

import io.github.krlvm.powertunnel.desktop.i18n.I18N;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class TextRightClickPopup {

    public static void register(JTextComponent input) {
        final JPopupMenu popup = new JPopupMenu();

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                input.requestFocus();
                input.requestFocusInWindow();
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        final UndoManager undoManager = new UndoManager();
        input.getDocument().addUndoableEditListener(undoManager);

        final Action undoAction = new AbstractAction(I18N.get("menu.undo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        };
        final Action redoAction = new AbstractAction(I18N.get("menu.redo")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        };

        final Action copyAction = new AbstractAction(I18N.get("menu.copy")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.copy();
            }
        };
        final Action cutAction = new AbstractAction(I18N.get("menu.cut")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.cut();
            }
        };
        final Action pasteAction = new AbstractAction(I18N.get("menu.paste")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.paste();
            }
        };
        final Action deleteAction = new AbstractAction(I18N.get("menu.delete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.replaceSelection("");
            }
        };

        final Action selectAllAction = new AbstractAction(I18N.get("menu.selectAll")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.selectAll();
            }
        };


        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK), undoAction);
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.SHIFT_MASK | KeyEvent.CTRL_MASK), redoAction);

        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK), cutAction);
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK), copyAction);
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK), pasteAction);
        //deleteAction

        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK), selectAllAction);

        /* ----------------------- */
        popup.add(undoAction);
        popup.add(redoAction);

        popup.addSeparator();

        popup.add(cutAction);
        popup.add(copyAction);
        popup.add(pasteAction);
        popup.add(deleteAction);

        popup.addSeparator();

        popup.add(selectAllAction);
        /* ----------------------- */

        input.setComponentPopupMenu(popup);
    }
}
