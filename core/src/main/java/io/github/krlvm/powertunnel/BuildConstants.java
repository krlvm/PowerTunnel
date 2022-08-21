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

package io.github.krlvm.powertunnel;

public class BuildConstants {
    public static final String NAME = "PowerTunnel";
    public static final String REPO = "https://github.com/krlvm/PowerTunnel";
    public static final String VERSION = "2.5.2";
    public static final int VERSION_CODE = 110;
    public static final int SDK = 110;

    public static boolean IS_RELEASE = isReleaseVersion(VERSION);
    public static final boolean DEBUG = !IS_RELEASE;

    public static boolean isReleaseVersion(final String version) {
        return !(
                version.contains("-dev") ||
                version.contains("-alpha") ||
                version.contains("-beta") ||
                version.contains("-preview") ||
                version.contains("-rc")
        );
    }
}
