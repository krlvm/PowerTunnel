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

package io.github.krlvm.powertunnel.desktop.updater;

public class UpdateInfo {

    private final String version;
    private final int versionCode;
    private final String changelog;

    private final String releasePage;
    private final String downloadUrl;

    public UpdateInfo(String version, int versionCode, String changelog, String releasePage, String downloadUrl) {
        this.version = version;
        this.versionCode = versionCode;
        this.changelog = changelog;

        this.releasePage = releasePage;
        this.downloadUrl = downloadUrl;
    }

    public String getVersion() {
        return version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getChangelog() {
        return changelog;
    }

    public String getReleasePage() {
        return releasePage;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
