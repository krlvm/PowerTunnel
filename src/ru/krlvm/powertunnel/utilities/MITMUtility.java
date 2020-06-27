package ru.krlvm.powertunnel.utilities;

import org.littleshoot.proxy.mitm.Authority;
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager;
import org.littleshoot.proxy.mitm.RootCertificateException;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.data.Settings;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class MITMUtility {

    public static CertificateSniffingMitmManager mitmManager() throws RootCertificateException, IOException {
        char[] password;
        List<String> values = PowerTunnel.SETTINGS.filteredLoad(new DataStore.Filter() {
            @Override
            public boolean accept(String line) {
                return line.startsWith(Settings.ROOT_CA_PASSWORD);
            }
        });
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
                    "powertunnel-root-ca", password,
                    "PowerTunnel Root CA",
                    "PowerTunnel",
                    "PowerTunnel",
                    "PowerTunnel",
                    "PowerTunnel"));
        } finally {
            password = null;
        }
    }
}
