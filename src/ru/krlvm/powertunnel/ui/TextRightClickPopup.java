package ru.krlvm.powertunnel.ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Cut/Copy/Paste/Delete context menu
 * for Java Swing Inputs
 */
public class TextRightClickPopup {

    public static void register(JTextComponent input) {
        JPopupMenu popup = new JPopupMenu();

        UndoManager undoManager = new UndoManager();
        input.getDocument().addUndoableEditListener(undoManager);

        Action undoAction = new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoManager.canUndo()) {
                    undoManager.undo();
                }
            }
        };
        Action redoAction = new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(undoManager.canRedo()) {
                    undoManager.redo();
                }
            }
        };

        Action copyAction = new AbstractAction("Copy") {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.copy();
            }
        };
        Action cutAction = new AbstractAction("Cut") {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.cut();
            }
        };
        Action pasteAction = new AbstractAction("Paste") {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.paste();
            }
        };
        Action deleteAction = new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.replaceSelection("");
            }
        };

        Action selectAllAction = new AbstractAction("Select All") {
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
