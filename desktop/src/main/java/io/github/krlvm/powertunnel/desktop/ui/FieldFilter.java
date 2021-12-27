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

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.regex.Pattern;

public abstract class FieldFilter extends DocumentFilter {

    protected abstract boolean isAllowed(String s);

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        final Document doc = fb.getDocument();
        final StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.insert(offset, string);

        if (isAllowed(sb.toString())) {
            super.insertString(fb, offset, string, attr);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        final Document doc = fb.getDocument();
        final StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.replace(offset, offset + length, text);

        if (isAllowed(sb.toString())) {
            super.replace(fb, offset, length, text, attrs);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        final Document doc = fb.getDocument();
        final StringBuilder sb = new StringBuilder();
        sb.append(doc.getText(0, doc.getLength()));
        sb.delete(offset, offset + length);

        if (isAllowed(sb.toString())) {
            super.remove(fb, offset, length);
            return;
        }
        Toolkit.getDefaultToolkit().beep();
    }


    public static class Number extends FieldFilter {
        @Override
        protected boolean isAllowed(String s) {
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }

    public static class IP extends FieldFilter {
        private static final Pattern PATTERN = Pattern.compile("(\\d+\\.?)+");
        @Override
        protected boolean isAllowed(String s) {
            return s.isEmpty() || PATTERN.matcher(s).matches();
        }
    }
}