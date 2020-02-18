package ru.krlvm.powertunnel.data;

import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends DataStore {

    private final String KEY_VALUE_SEPARATOR = "=";
    private Map<String, String> temporaryValues = new HashMap<>();
    private Map<String, String> options = new HashMap<>();

    public Settings() {
        super("settings");
    }

    public void loadSettings() throws IOException {
        options.clear();
        load();
        for (String line : loadedLines) {
            if(line.contains(KEY_VALUE_SEPARATOR)) {
                String[] array = line.split(KEY_VALUE_SEPARATOR);
                String key = array[0];
                String value = array[1];
                if(key.endsWith(".int")) {
                    try {
                        Integer.parseInt(value);
                    } catch (NumberFormatException ignore) {
                        Utility.print("[x] Malformed INT value: '%s'", value);
                        continue;
                    }
                } else if(key.endsWith(".bool")) {
                    if (!value.equals("true") && !value.equals("false")) {
                        Utility.print("[x] Malformed BOOLEAN value: '%s'", value);
                        continue;
                    }
                }
                options.put(key, value);
            } else {
                Debugger.debug("Malformed settings line: '%s'", line);
            }
        }
    }

    public boolean isTemporary(String key) {
        return temporaryValues.containsKey(key);
    }

    public void setTemporaryValue(String key, String value) {
        temporaryValues.put(key, value);
    }

    public String getOption(String key, String defaultValue) {
        if(temporaryValues.containsKey(key)) {
            return temporaryValues.get(key);
        }
        if(options.containsKey(key)) {
            return options.get(key);
        }
        return defaultValue;
    }

    public void setOption(String key, String value) {
        options.put(key, value);
    }

    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            lines.add(entry.getKey() + KEY_VALUE_SEPARATOR + entry.getValue());
        }
        write(lines);
    }

    @Override
    public String getFileFormat() {
        return "ini";
    }


    public static final String SERVER_IP_ADDRESS = "server.ip";
    public static final String SERVER_PORT = "server.port.int";
    public static final String AUTO_PROXY_SETUP_ENABLED = "server.auto-setup.bool";
    public static final String FULL_CHUNKING = "https.chunking.full.bool";
    public static final String CHUNK_SIZE = "https.chunking.size.int";
    public static final String PAYLOAD_LENGTH = "http.payload.length.int";
    public static final String MIX_HOST_CASE = "http.mix-host-case.bool";
    public static final String USE_DNS_SEC = "dns-sec.enabled.bool";
    public static final String GOVERNMENT_BLACKLIST_MIRROR = "powertunnel.government-blacklist-mirror";
    public static final String DISABLE_JOURNAL = "powertunnel.journal.enabled";
}
