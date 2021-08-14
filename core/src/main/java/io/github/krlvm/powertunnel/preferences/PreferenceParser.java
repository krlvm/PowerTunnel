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

    public static final String FILE = "preferences.json";

    public static List<PreferenceGroup> parsePreferences(String source, String json) throws PreferenceParseException {
        final JSONArray array;
        try {
            array = new JSONArray(json);
        } catch (JSONException ex) {
            throw new PreferenceParseException(source, "Malformed preferences JSON", ex);
        }

        final List<PreferenceGroup> groups = new ArrayList<>();
        final List<Preference> ungrouped = new ArrayList<>();
        for (Object object : array) {
            if (!(object instanceof JSONObject))
                throw new PreferenceParseException(source, "Malformed preferences structure");
            final JSONObject jso = ((JSONObject) object);
            if(jso.has(PreferencesGroupSchemaFields.TITLE)) {
                if (!jso.has(PreferencesGroupSchemaFields.TITLE) || !jso.has(PreferencesGroupSchemaFields.PREFERENCES))
                    throw new PreferenceParseException(
                            source,
                            "One of preference groups is incomplete (missing 'group' and (or) 'preferences')"
                    );
                final Object jsoList = jso.get(PreferencesGroupSchemaFields.PREFERENCES);
                if(!(jsoList instanceof JSONArray)) throw new PreferenceParseException(source, "Preferences list should be array");
                groups.add(new PreferenceGroup(
                        getStringOrNull(jso, PreferencesGroupSchemaFields.TITLE),
                        getStringOrNull(jso, PreferencesGroupSchemaFields.DESCRIPTION),
                        parsePreferenceList(source, ((JSONArray) jsoList))
                ));
            } else {
                ungrouped.add(parsePreference(source, jso));
            }
        }
        if(!ungrouped.isEmpty()) {
            groups.add(0, new PreferenceGroup(null, null, ungrouped));
        }
        return groups;
    }

    public static List<Preference> parsePreferenceList(String source, JSONArray array) throws PreferenceParseException {
        final List<Preference> preferences = new ArrayList<>();
        for (Object object : array) {
            if (!(object instanceof JSONObject))
                throw new PreferenceParseException(source, "Malformed preferences list structure");
            preferences.add(parsePreference(source, ((JSONObject) object)));
        }
        return preferences;
    }

    public static Preference parsePreference(String source, JSONObject jso) throws PreferenceParseException {
        if (!jso.has(PreferencesSchemaFields.KEY) || !jso.has(PreferencesSchemaFields.TYPE))
            throw new PreferenceParseException(source, "One of preferences is incomplete (missing 'key' and (or) 'type')");

        final String rawType = jso.getString(PreferencesSchemaFields.TYPE);
        final PreferenceType type;
        try {
            type = PreferenceType.valueOf(rawType.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new PreferenceParseException(source, "Unsupported preference type: '" + rawType + '"', ex);
        }

        List<Preference.SelectItem> items = null;
        if(type == PreferenceType.SELECT) {
            if(!jso.has(PreferencesSchemaFields.ITEMS)) {
                throw new PreferenceParseException(source, "Preference with type 'select' doesn't have items list");
            }
            items = new ArrayList<>();

            final Object jsoItemsList = jso.get(PreferencesSchemaFields.ITEMS);
            if(!(jsoItemsList instanceof JSONArray)) throw new PreferenceParseException(source, "Select Preference item list should be array");
            final JSONArray itemsArray = ((JSONArray) jsoItemsList);
            for (Object o : itemsArray) {
                if(!(o instanceof JSONObject))
                    throw new PreferenceParseException(source, "Malformed select preference items structure");
                final JSONObject ijo = ((JSONObject) o);
                if(!ijo.has(PreferencesSelectItemSchemaFields.KEY) || !ijo.has(PreferencesSelectItemSchemaFields.NAME))
                    throw new PreferenceParseException(source, "One of select preferences items is incomplete (missing 'key' and (or) 'name')");
                items.add(new Preference.SelectItem(ijo.getString(PreferencesSelectItemSchemaFields.KEY), PreferencesSelectItemSchemaFields.NAME));
            }
        }

        final Object defaultValue = getObjectOrNull(jso, PreferencesSchemaFields.DEFAULT_VALUE);
        final Object dependencyValue = getObjectOrNull(jso, PreferencesSchemaFields.DEPENDENCY_VALUE);

        return new Preference(
                jso.getString(PreferencesSchemaFields.KEY),
                getStringOrNull(jso, PreferencesSchemaFields.TITLE),
                getStringOrNull(jso, PreferencesSchemaFields.DESCRIPTION),
                defaultValue != null ? defaultValue.toString() : type.getDefaultValue(),
                type,
                getStringOrNull(jso, PreferencesSchemaFields.DEPENDENCY),
                dependencyValue != null ? dependencyValue.toString() : null,
                items
        );
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

        static final String ITEMS = "items";
    }

    static class PreferencesGroupSchemaFields {
        static final String TITLE = "group";
        static final String DESCRIPTION = "description";
        static final String PREFERENCES = "preferences";
    }

    static class PreferencesSelectItemSchemaFields {
        static final String KEY = "key";
        static final String NAME = "name";
    }
}
