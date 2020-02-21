package ru.krlvm.powertunnel.data;

import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends DataStore {


    private static final String KEY_VALUE_SEPARATOR = "=";

    private boolean hasChanged = false;
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
                String value;
                if(array.length > 1) {
                    value = array[1];
                } else {
                    value = "";
                }
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
        addDefaults();
    }

    private void addDefaults() {
        for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
            if(!options.containsKey(entry.getKey())) {
                options.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void reset() {
        options.clear();
        addDefaults();
    }

    public boolean isTemporary(String key) {
        return temporaryValues.containsKey(key);
    }

    public void setTemporaryValue(String key, String value) {
        temporaryValues.put(key, value);
    }

    public String getOption(String key) {
        if(temporaryValues.containsKey(key)) {
            return temporaryValues.get(key);
        }
        if(options.containsKey(key)) {
            return options.get(key);
        }
        return "undefined"; //better to return null though
    }

    public int getIntOption(String key) {
        return Integer.parseInt(getOption(key));
    }

    public boolean getBooleanOption(String key) {
        return Boolean.parseBoolean(getOption(key));
    }

    public void setOption(String key, String value) {
        if(temporaryValues.containsKey(key)) {
            return;
        }
        options.put(key, value);
        hasChanged = true;
    }

    public void setIntOption(String key, int value) {
        options.put(key, String.valueOf(value));
    }

    public void setBooleanOption(String key, boolean value) {
        options.put(key, String.valueOf(value));
    }

    public void save() throws IOException {
        if(!hasChanged) return;
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

    private static final Map<String, String> defaultValues = new HashMap<>();
    static {
        defaultValues.put(SERVER_IP_ADDRESS, "127.0.0.1");
        defaultValues.put(SERVER_PORT, "8085");
        defaultValues.put(AUTO_PROXY_SETUP_ENABLED, "true");
        defaultValues.put(FULL_CHUNKING, "false");
        defaultValues.put(CHUNK_SIZE, "2");
        defaultValues.put(PAYLOAD_LENGTH, "0");
        defaultValues.put(MIX_HOST_CASE, "false");
        defaultValues.put(USE_DNS_SEC, "false");
        defaultValues.put(GOVERNMENT_BLACKLIST_MIRROR, "");
        defaultValues.put(DISABLE_JOURNAL, "false");
    }
}
