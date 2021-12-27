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

package io.github.krlvm.powertunnel.preferences;

import java.io.Serializable;
import java.util.List;

public class PreferenceGroup implements Serializable {

    private final String title;
    private final String description;
    private final List<Preference> preferences;

    public PreferenceGroup(String title, String description, List<Preference> preferences) {
        this.title = title;
        this.description = description;
        this.preferences = preferences;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Preference> getPreferences() {
        return preferences;
    }

    public Preference findPreference(String key) {
        for (Preference preference : preferences) {
            if(key.equals(preference.getKey())) return preference;
        }
        return null;
    }


    public static Preference findPreference(List<PreferenceGroup> groups, String key) {
        for (PreferenceGroup group : groups) {
            final Preference preference = group.findPreference(key);
            if(preference != null) return preference;
        }
        return null;
    }
}
