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

package io.github.krlvm.powertunnel.configuration;

import io.github.krlvm.powertunnel.sdk.configuration.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationStore implements Configuration {

    private static final String DELIMITER = ": ";
    private static final int DELIMITER_LENGTH = DELIMITER.length();

    private final Map<String, String> data = new HashMap<>();

    public void read(File file) throws IOException {
        file.createNewFile();
        this.read(new FileReader(file));
    }

    public void read(InputStream in) {
        this.read(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public void read(Reader source) {
        data.clear();

        final BufferedReader reader = new BufferedReader(source);
        reader.lines().forEach((line) -> {
            final int pos = line.indexOf(DELIMITER);
            data.put(
                line.substring(0, pos),
                line.substring(pos + DELIMITER_LENGTH)
            );
        });
        //reader.close();
    }

    public void save(File file) throws IOException {
        int current = 0;
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for(Map.Entry<String, String> entry : data.entrySet()) {
            writer.write(entry.getKey() + DELIMITER + entry.getValue());
            if(++current != data.size()) {
                writer.write("\r\n");
            }
        }
    }


    @Override
    public String get(String key, String defaultValue) {
        if (!data.containsKey(key)) set(key, defaultValue);
        return data.get(key);
    }

    @Override
    public void set(String key, String value) {
        data.put(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        if (!data.containsKey(key)) setInt(key, defaultValue);
        return Integer.parseInt(data.get(key));
    }

    @Override
    public void setInt(String key, int value) {
        data.put(key, String.valueOf(value));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        if (!data.containsKey(key)) setBoolean(key, defaultValue);
        return Boolean.parseBoolean(data.get(key));
    }

    @Override
    public void setBoolean(String key, boolean value) {
        data.put(key, String.valueOf(value));
    }

    @Override
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    @Override
    public void remove(String key) {
        data.remove(key);
    }

    @Override
    public Collection<String> keys() {
        return data.keySet();
    }

    @Override
    public void clear() {
        data.clear();
    }
}
