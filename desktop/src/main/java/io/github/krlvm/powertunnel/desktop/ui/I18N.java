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

import io.github.krlvm.powertunnel.i18n.I18NBundle;
import io.github.krlvm.powertunnel.i18n.UTF8Control;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

    private static I18NBundle bundle;
    private static String locale = null;

    public static String get(String key) {
        return bundle.get(key);
    }

    public static String get(String key, String defaultValue) {
        return bundle.get(key, defaultValue);
    }

    public static void load(String lang) {
        if(bundle != null) throw new IllegalStateException("Bundle is already loaded");
        try {
            bundle = new I18NBundle(getResourceBundle(lang));
            locale = lang;
        } catch (MissingResourceException ex) {
            System.err.printf("Locale '%s' is not supported%n", lang);
            bundle = new I18NBundle(getResourceBundle(null));
        }
    }

    public static String getLocale() {
        return locale;
    }


    private static final String NAME = "messages";
    public static ResourceBundle getResourceBundle(String lang) {
        return ResourceBundle.getBundle(I18NBundle.getLocalePath(lang), new UTF8Control());
    }
}
