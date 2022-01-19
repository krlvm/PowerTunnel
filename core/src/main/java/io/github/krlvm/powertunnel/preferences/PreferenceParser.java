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

import com.google.gson.*;
import io.github.krlvm.powertunnel.exceptions.PreferenceParseException;
import io.github.krlvm.powertunnel.i18n.I18NBundle;

import java.util.*;

public class PreferenceParser {

    public static final String FILE = "preferences.json";

    public static List<PreferenceGroup> parsePreferences(String source, String json, I18NBundle bundle) throws PreferenceParseException {
        final JsonArray array;
        try {
            array = JsonParser.parseString(json).getAsJsonArray();
        } catch (IllegalStateException | JsonSyntaxException ex) {
            throw new PreferenceParseException(source, "Malformed preferences JSON", ex);
        }

        final List<PreferenceGroup> groups = new ArrayList<>();
        final List<Preference> ungrouped = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) throw new PreferenceParseException(source, "Malformed preferences structure");
            final JsonObject jso = element.getAsJsonObject();
            if(jso.has(PreferencesGroupSchemaFields.ID)) {
                if (!jso.has(PreferencesGroupSchemaFields.PREFERENCES))
                    throw new PreferenceParseException(
                            source,
                            "One of preference groups is incomplete (missing 'preferences')"
                    );
                final JsonElement jsoList = jso.get(PreferencesGroupSchemaFields.PREFERENCES);
                if(!jsoList.isJsonArray()) throw new PreferenceParseException(source, "Preferences list should be array");

                final String title = jso.has(PreferencesGroupSchemaFields.TITLE) ? jso.get(PreferencesGroupSchemaFields.TITLE).getAsString() :
                        bundle.get(jso.get(PreferencesGroupSchemaFields.ID).getAsString());
                final String description = jso.has(PreferencesGroupSchemaFields.DESCRIPTION) ? jso.get(PreferencesGroupSchemaFields.DESCRIPTION).getAsString() :
                        bundle.get(jso.get(PreferencesGroupSchemaFields.ID).getAsString() + ".desc", null);
                groups.add(new PreferenceGroup(
                        title,
                        description,
                        parsePreferenceList(source, jsoList.getAsJsonArray(), bundle)
                ));
            } else {
                ungrouped.add(parsePreference(source, jso, bundle));
            }
        }
        if(!ungrouped.isEmpty()) {
            groups.add(0, new PreferenceGroup(null, null, ungrouped));
        }
        return groups;
    }

    public static List<Preference> parsePreferenceList(String source, JsonArray array, I18NBundle bundle) throws PreferenceParseException {
        final List<Preference> preferences = new ArrayList<>();
        for (JsonElement object : array) {
            if (!object.isJsonObject())
                throw new PreferenceParseException(source, "Malformed preferences list structure");
            preferences.add(parsePreference(source, object.getAsJsonObject(), bundle));
        }
        return preferences;
    }

    public static Preference parsePreference(String source, JsonObject jso, I18NBundle bundle) throws PreferenceParseException {
        if (!jso.has(PreferencesSchemaFields.KEY) || !jso.has(PreferencesSchemaFields.TYPE))
            throw new PreferenceParseException(source, "One of preferences is incomplete (missing 'key' and (or) 'type')");

        final String rawType = jso.get(PreferencesSchemaFields.TYPE).getAsString();
        final PreferenceType type;
        try {
            type = PreferenceType.valueOf(rawType.toUpperCase().replace("İ", "I"));
        } catch (IllegalArgumentException ex) {
            throw new PreferenceParseException(source, "Unsupported preference type: '" + rawType + "'", ex);
        }
        final String key = jso.get(PreferencesSchemaFields.KEY).getAsString();

        Map<String, String> items = null;
        if(type == PreferenceType.SELECT) {
            if(!jso.has(PreferencesSchemaFields.ITEMS)) {
                throw new PreferenceParseException(source, "Preference with type 'select' doesn't have items list");
            }
            items = new LinkedHashMap<>();

            final JsonElement jsoItemsList = jso.get(PreferencesSchemaFields.ITEMS);
            if(!jsoItemsList.isJsonArray()) throw new PreferenceParseException(source, "Select Preference item list should be array");
            final JsonArray itemsArray = jsoItemsList.getAsJsonArray();
            for (JsonElement o : itemsArray) {
                if(!o.isJsonObject())
                    throw new PreferenceParseException(source, "Malformed select preference items structure");
                final JsonObject ijo = o.getAsJsonObject();
                if(!ijo.has(PreferencesSelectItemSchemaFields.KEY))
                    throw new PreferenceParseException(source, "One of select preferences items is incomplete (missing 'key')");

                final String ikey = ijo.get(PreferencesSelectItemSchemaFields.KEY).getAsString();
                final String name = ijo.has(PreferencesSelectItemSchemaFields.NAME) ? ijo.get(PreferencesSelectItemSchemaFields.NAME).getAsString() :
                        bundle.get(key + ".item." + ikey);
                items.put(ikey, name);
            }
        }

        final JsonElement defaultValue = getElementOrNull(jso, PreferencesSchemaFields.DEFAULT_VALUE);
        final JsonElement dependencyValue = getElementOrNull(jso, PreferencesSchemaFields.DEPENDENCY_VALUE);

        final String title = jso.has(PreferencesSchemaFields.TITLE) ? jso.get(PreferencesSchemaFields.TITLE).getAsString() :
                bundle.get(jso.get(PreferencesSchemaFields.KEY).getAsString());
        final String description = jso.has(PreferencesSchemaFields.DESCRIPTION) ? jso.get(PreferencesSchemaFields.DESCRIPTION).getAsString() :
                bundle.get(jso.get(PreferencesSchemaFields.KEY).getAsString() + ".desc", null);

        return new Preference(
                key,
                title,
                description,
                defaultValue != null ? defaultValue.getAsString() :
                        (type == PreferenceType.SELECT ? items.keySet().iterator().next() : type.getDefaultValue()),
                type,
                getStringOrNull(jso, PreferencesSchemaFields.DEPENDENCY),
                dependencyValue != null ? dependencyValue.getAsString() : "true",
                items
        );
    }

    private static String getStringOrNull(JsonObject jso, String key) {
        return jso.has(key) ? jso.get(key).getAsString() : null;
    }

    private static JsonElement getElementOrNull(JsonObject jso, String key) {
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
        static final String ID = "group";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String PREFERENCES = "preferences";
    }

    static class PreferencesSelectItemSchemaFields {
        static final String KEY = "key";
        static final String NAME = "name";
    }
}
