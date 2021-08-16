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

import io.github.krlvm.powertunnel.desktop.BuildConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateNotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotifier.class);
    public static boolean ENABLED = true;
    public static String NEW_VERSION = null;

    public static boolean checkAndNotify(String product, String repo, boolean loadChangelog) {
        final UpdateInfo info;
        try {
            info = checkForUpdates(product, repo, loadChangelog);
        } catch (IOException ex) {
            LOGGER.warn("Failed to check for updates: {}", ex.getMessage(), ex);
            return false;
        }

        if (info.getVersion() == null) {
            LOGGER.warn("Failed to parse update info");
            return false;
        }

        if (info.getVersionCode() < BuildConstants.VERSION_CODE) {
            LOGGER.info("No updates are available");
            return true;
        }

        NEW_VERSION = info.getVersion();

        System.out.println();
        LOGGER.info("An update is available: {}", info.getVersion());
        LOGGER.info("Changelog: {}", info.getReleasePage());
        LOGGER.info("Download: {}", info.getDownloadUrl());
        System.out.println();

        return true;
    }

    public static UpdateInfo checkForUpdates(String product, String repo, boolean loadChangelog) throws IOException {
        final String[] arr = fetch(repo + "/VERSION").split(";");

        final String version = arr[0];
        final int versionCode;
        try {
            versionCode = Integer.parseInt(arr[0]);
        } catch (NumberFormatException ex) {
            return new UpdateInfo(null, 0, null, null, null);
        }
        final String changelog = loadChangelog ? fetch(repo + "/CHANGELOG") : null;

        return new UpdateInfo(
                arr[0],
                versionCode,
                changelog,
                repo + "/releases/tag/" + version,
                repo + "/releases/download/" + version + "/" + product + ".jar"
        );
    }


    private static String fetch(String address) throws IOException {
        return fetch(address, null);
    }

    private static String fetch(String address, String separator) throws IOException {
        final URL url = new URL(address);
        try(final BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            final StringBuilder builder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                builder.append(line);
                if(separator != null) builder.append(separator);
            }
            return builder.toString();
        }
    }
}
