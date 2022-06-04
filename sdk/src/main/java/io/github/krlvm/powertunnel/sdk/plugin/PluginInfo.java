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

package io.github.krlvm.powertunnel.sdk.plugin;

import java.io.Serializable;

public class PluginInfo implements Serializable {

    private final String id;
    private final String version;
    private final int versionCode;

    private final String name;
    private final String description;
    private final String author;
    private final String homepage;

    private final String[] configurationFiles;

    private final String mainClass;
    private final int targetSdkVersion;

    private final String source;

    /**
     * @param name plugin name
     * @param description plugin description
     * @param version plugin version
     * @param author plugin author
     * @param homepage plugin homepage
     * @param configurationFiles plugin additional configuration files
     * @param mainClass plugin main class
     * @param targetSdkVersion plugin target SDK version
     * @param source source jar
     */
    public PluginInfo(
            String id,
            String version,
            int versionCode,
            String name,
            String description,
            String author,
            String homepage,
            String[] configurationFiles,
            String mainClass,
            int targetSdkVersion,
            String source
    ) {
        this.id = id;
        this.version = version;
        this.versionCode = versionCode;
        this.name = name;
        this.description = description;
        this.author = author;
        this.homepage = homepage;

        this.configurationFiles = configurationFiles;

        this.mainClass = mainClass;
        this.targetSdkVersion = targetSdkVersion;

        this.source = source;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public String getHomepage() {
        return homepage;
    }

    public String[] getConfigurationFiles() {
        return configurationFiles;
    }

    public String getMainClass() {
        return mainClass;
    }

    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public String getSource() {
        return source;
    }
}