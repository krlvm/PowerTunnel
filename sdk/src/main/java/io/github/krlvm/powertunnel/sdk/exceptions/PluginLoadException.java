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

package io.github.krlvm.powertunnel.sdk.exceptions;

public class PluginLoadException extends ProxyStartException {

    private final String jarFile;

    public PluginLoadException(String jarFile, String message, Throwable cause) {
        super(message, cause);
        this.jarFile = jarFile;
    }

    public PluginLoadException(String jarFile, String message) {
        super(message + " [" + jarFile + "]");
        this.jarFile = jarFile;
    }

    public String getJarFile() {
        return jarFile;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " (" + jarFile + ")";
    }

    @Override
    public String getLocalizedMessage() {
        return super.getLocalizedMessage() + " (" + jarFile + ")";
    }
}
