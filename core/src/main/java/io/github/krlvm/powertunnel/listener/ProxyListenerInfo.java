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

package io.github.krlvm.powertunnel.listener;

import io.github.krlvm.powertunnel.sdk.plugin.PluginInfo;

public class ProxyListenerInfo {

    private final PluginInfo pluginInfo;
    private final int priority;

    public ProxyListenerInfo(PluginInfo pluginInfo, int priority) {
        this.pluginInfo = pluginInfo;
        this.priority = priority;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public int getPriority() {
        return priority;
    }
}
