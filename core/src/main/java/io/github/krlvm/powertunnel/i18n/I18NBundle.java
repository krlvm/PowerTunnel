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

package io.github.krlvm.powertunnel.i18n;

import java.util.ResourceBundle;

public class I18NBundle {

    private final ResourceBundle bundle;

    public I18NBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public String get(String key) {
        return get(key, "Missing locale");
    }

    public String get(String key, String defaultValue) {
        if(bundle == null || !bundle.containsKey(key)) return defaultValue;
        return bundle.getString(key);
    }

    public static String getLocalePath(String lang) {
        return "locale/messages_" + (lang != null ? lang : "en");
    }
    public static String getLocaleFilePath(String lang) {
        return getLocalePath(lang) + ".properties";
    }
}
