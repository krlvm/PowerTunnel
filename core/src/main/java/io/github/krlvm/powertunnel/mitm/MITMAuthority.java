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

package io.github.krlvm.powertunnel.mitm;

import org.littleshoot.proxy.mitm.Authority;

import java.io.File;

public class MITMAuthority {

    public static final String CERTIFICATE_ALIAS = "powertunnel-root-ca";

    public static Authority create(File certificateDirectory, String password) {
        return create(certificateDirectory, password.toCharArray());
    }

    public static Authority create(File certificateDirectory, char[] password) {
        if(!certificateDirectory.exists()) certificateDirectory.mkdir();
        return new Authority(certificateDirectory, CERTIFICATE_ALIAS, password,
                "PowerTunnel Root CA",
                "PowerTunnel",
                "PowerTunnel",
                "PowerTunnel",
                "PowerTunnel"
        );
    }
}
