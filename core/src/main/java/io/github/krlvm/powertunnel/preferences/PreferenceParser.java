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

import io.github.krlvm.powertunnel.exceptions.PreferenceParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PreferenceParser {

    public static List<Preference> parsePreferences(String source, String json) throws PreferenceParseException {
        final JSONArray array;
        try {
            array = new JSONArray(json);
        } catch (JSONException ex) {
            throw new PreferenceParseException(source, "Malformed preferences JSON", ex);
        }

        final List<Preference> preferences = new ArrayList<>();
        for (Object object : array) {
            if (!(object instanceof JSONObject))
                throw new PreferenceParseException(source, "Malformed preferences structure");
            final JSONObject jso = ((JSONObject) object);

            if (!jso.has(PreferencesSchemaFields.KEY) || !jso.has(PreferencesSchemaFields.TYPE))
                throw new PreferenceParseException(source, "One of preferences is incomplete");

            final String rawType = jso.getString(PreferencesSchemaFields.TYPE);
            final PreferenceType type;
            try {
                type = PreferenceType.valueOf(rawType);
            } catch (IllegalArgumentException ex) {
                throw new PreferenceParseException(source, "Unsupported preference type: '" + rawType + '"', ex);
            }

            final Object defaultValue = getObjectOrNull(jso, PreferencesSchemaFields.DEFAULT_VALUE);
            final Object dependencyValue = getObjectOrNull(jso, PreferencesSchemaFields.DEPENDENCY_VALUE);

            preferences.add(new Preference(
                    getStringOrNull(jso, PreferencesSchemaFields.KEY),
                    getStringOrNull(jso, PreferencesSchemaFields.TITLE),
                    getStringOrNull(jso, PreferencesSchemaFields.DESCRIPTION),
                    defaultValue != null ? defaultValue.toString() : type.getDefaultValue(),
                    type,
                    getStringOrNull(jso, PreferencesSchemaFields.DEPENDENCY),
                    dependencyValue != null ? dependencyValue.toString() : null
            ));
        }
        return preferences;
    }

    private static String getStringOrNull(JSONObject jso, String key) {
        return jso.has(key) ? jso.getString(key) : null;
    }

    private static Object getObjectOrNull(JSONObject jso, String key) {
        return jso.has(key) ? jso.get(key) : null;
    }


    static class PreferencesSchemaFields {
        static final String KEY = "key";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String TYPE = "type";
        static final String DEFAULT_VALUE = "defaultValue";

        static final String DEPENDENCY = "dependency";
        static final String DEPENDENCY_VALUE = "dependencyValue";
    }
}
