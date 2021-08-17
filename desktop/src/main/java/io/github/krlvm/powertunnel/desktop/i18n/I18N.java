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

package io.github.krlvm.powertunnel.desktop.i18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

    private static I18NBundle bundle;

    public static String get(String key) {
        return bundle.get(key);
    }

    public static String get(String key, String defaultValue) {
        return bundle.get(key, defaultValue);
    }

    public static void load(String lang) {
        try {
            bundle = new I18NBundle(ResourceBundle.getBundle("locale/messages_" + lang));
        } catch (MissingResourceException ex) {
            System.err.printf("Locale '%s' is not supported%n", lang);
            bundle = new I18NBundle(ResourceBundle.getBundle("locale/messages"));
        }
    }
}
