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

public class Preference {

    private final String key;
    private final String title;
    private final String description;
    private final String defaultValue;
    private final PreferenceType type;

    private final String dependency;
    private final String dependencyValue;

    public Preference(String key, String title, String description, String defaultValue, PreferenceType type) {
        this(key, title, description, defaultValue, type, null, null);
    }

    public Preference(
            String key,
            String title,
            String description,
            String defaultValue,
            PreferenceType type,
            String dependency,
            String dependencyValue
    ) {
        this.key = key;
        this.title = title;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;

        this.dependency = dependency;
        this.dependencyValue = dependencyValue;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public PreferenceType getType() {
        return type;
    }

    public String getDependency() {
        return dependency;
    }

    public String getDependencyValue() {
        return dependencyValue;
    }
}