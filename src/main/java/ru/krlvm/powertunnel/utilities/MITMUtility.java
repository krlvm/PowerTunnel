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

package ru.krlvm.powertunnel.utilities;

import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.data.Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class MITMUtility {

    private static final String CERTIFICATE_ALIAS = "powertunnel-root-ca";

    public static CertificateSniffingMitmManager initMitmManager() throws RootCertificateException, IOException {
        char[] password;
        List<String> values = PowerTunnel.SETTINGS.filteredLoad(line -> line.startsWith(Settings.ROOT_CA_PASSWORD));
        if(values != null) {
            password = values.get(0).split(Settings.KEY_VALUE_SEPARATOR)[1].toCharArray();
        } else {
            password = UUID.randomUUID().toString().toCharArray();
            PowerTunnel.SETTINGS.setOption(Settings.ROOT_CA_PASSWORD, new String(password));
            PowerTunnel.SETTINGS.save();
            //PowerTunnel.SETTINGS.unload(Settings.ROOT_CA_PASSWORD);
        }
        try {
            return new CertificateSniffingMitmManager(new Authority(new File("."),
                    CERTIFICATE_ALIAS, password,
                    "PowerTunnel Root CA",
                    "PowerTunnel",
                    "PowerTunnel",
                    "PowerTunnel",
                    "PowerTunnel"));
        } finally {
            password = null;
        }
    }

    public static void copyCertificateWithExtensionChange() {
        if(!SystemUtility.IS_WINDOWS) return;
        try {
            Files.copy(Paths.get(CERTIFICATE_ALIAS + ".pem"), Paths.get(CERTIFICATE_ALIAS + ".cer"));
            Debugger.debug("Certificate copied");
        } catch (IOException ex) {
            Utility.print("[x] Failed to copy '%s.pem' to '%s.cer', copy and rename it manually to install Root CA");
            Debugger.debug("Failed to copy certificate", ex);
        }
    }
}
