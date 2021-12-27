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

package io.github.krlvm.powertunnel.desktop.types;

import java.util.function.Consumer;

public class ConsoleCommand {

    private final String pluginId;
    private final String name;
    private final String usage;
    private final String description;

    private final Consumer<String[]> handler;

    public ConsoleCommand(String pluginId, String name, String usage, String description, Consumer<String[]> handler) {
        this.pluginId = pluginId;
        this.name = name;
        this.usage = usage;
        this.description = description;
        this.handler = handler;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getName() {
        return name;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public Consumer<String[]> getHandler() {
        return handler;
    }
}
