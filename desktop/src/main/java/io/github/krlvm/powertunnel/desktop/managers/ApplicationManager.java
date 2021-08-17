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

package io.github.krlvm.powertunnel.desktop.managers;

import io.github.krlvm.powertunnel.desktop.utilities.Utility;
import io.github.krlvm.powertunnel.plugin.PluginLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApplicationManager {

    private static final String[] PREINSTALLED_PLUGINS = {
            "LibertyTunnel.jar",
            "DNSResolver.jar"
    };

    public static void extractPlugins() throws IOException {
        final Path pluginsDir = Paths.get(PluginLoader.PLUGINS_DIR);
        for (String plugin : PREINSTALLED_PLUGINS) {
            Utility.extractResource(pluginsDir.resolve(plugin), "plugins/" + plugin);
        }
    }
}
