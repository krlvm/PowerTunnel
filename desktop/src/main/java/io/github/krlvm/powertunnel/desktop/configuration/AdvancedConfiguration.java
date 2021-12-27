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

package io.github.krlvm.powertunnel.desktop.configuration;

import io.github.krlvm.powertunnel.configuration.ConfigurationStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AdvancedConfiguration extends ConfigurationStore {

    private final Collection<String> immutableKeys;

    public AdvancedConfiguration() {
        this(new ArrayList<>());
    }

    public AdvancedConfiguration(Collection<String> immutableKeys) {
        this.immutableKeys = immutableKeys;
    }

    public void protect(String key) {
        this.immutableKeys.add(key);
    }

    public void protect(String key, String value) {
        this.immutableKeys.add(key);
        set(key, value);
    }

    public void protectInt(String key, int value) {
        this.immutableKeys.add(key);
        setInt(key, value);
    }

    public void protectBoolean(String key, boolean value) {
        this.immutableKeys.add(key);
        setBoolean(key, value);
    }

    @Override
    protected Set<Map.Entry<String, String>> entries() {
        return super.entries().stream()
                .filter(entry -> !immutableKeys.contains(entry.getKey()))
                .collect(Collectors.toSet());
    }

    public Collection<String> getImmutableKeys() {
        return immutableKeys;
    }
}
