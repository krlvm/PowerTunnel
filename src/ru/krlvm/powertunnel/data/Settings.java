package ru.krlvm.powertunnel.data;

import ru.krlvm.powertunnel.filter.ProxyFilter;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings extends DataStore {

    public static final String KEY_VALUE_SEPARATOR = "=";

    private boolean hasChanged = false;
    private final Map<String, String> temporaryValues = new HashMap<>();
    private final Map<String, String> options = new HashMap<>();

    public Settings() {
        super("settings");
    }

    public void loadSettings() throws IOException {
        options.clear();
        /*filteredLoad(new Filter() {
            @Override
            public boolean accept(String line) {
                return !line.startsWith(ROOT_CA_PASSWORD);
            }
        });*/
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
                Debugger.debug("[Settings] Malformed settings line: '%s'", line);
            }
        }
        addDefaults();
    }

    private void addDefaults() {
        for (Map.Entry<String, String> entry : defaultValues.entrySet()) {
            if(!options.containsKey(entry.getKey())) {
                String key = entry.getKey(), value = entry.getValue();
                options.put(key, value);

                if(!temporaryValues.containsKey(key)) {
                    valueUpdated(key, value);
                }
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

        valueUpdated(key, value);
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

        valueUpdated(key, value);
        value = null;
    }

    public void setIntOption(String key, int value) {
        options.put(key, String.valueOf(value));
    }

    public void setBooleanOption(String key, boolean value) {
        options.put(key, String.valueOf(value));
    }

    public void unload(String key) {
        temporaryValues.remove(key);
        options.remove(key);
        if(loadedLines != null) {
            for (String line : loadedLines) {
                if(line.startsWith(key)) {
                    loadedLines.remove(key);
                }
            }
        }
    }

    public void save() throws IOException {
        if(!hasChanged) return;
        List<String> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            lines.add(entry.getKey() + KEY_VALUE_SEPARATOR + entry.getValue());
        }
        write(lines);
    }

    private static void valueUpdated(String key, String value) {
        if(key.equals(PAYLOAD_LENGTH)) {
            int len = Integer.parseInt(value);
            ProxyFilter.PAYLOAD.clear();
            for(int i = 0; i < len; i++) {
                ProxyFilter.PAYLOAD.add(new String(new char[1000])
                        .replace("\0", String.valueOf(i % 10)).intern());
            }
        }
    }

    @Override
    public String getFileExtension() {
        return "ini";
    }


    public static final String SERVER_IP_ADDRESS = "server.ip";
    public static final String SERVER_PORT = "server.port.int";
    public static final String AUTO_PROXY_SETUP_ENABLED = "server.auto-setup.bool";
    public static final String PROXY_PAC_ENABLED = "server.proxy-pac.bool";
    public static final String ALLOW_INVALID_HTTP_PACKETS = "http.invalid-packets.allow";
    public static final String ENABLE_CHUNKING = "https.chunking.enabled.bool";
    public static final String FULL_CHUNKING = "https.chunking.full.bool";
    public static final String CHUNK_SIZE = "https.chunking.size.int";
    public static final String SNI_TRICK = "https.sni-trick.int";
    public static final String SNI_TRICK_FAKE_HOST = "https.sni-trick.fake.host";
    public static final String PAYLOAD_LENGTH = "http.payload.length.int";
    public static final String MIX_HOST_CASE = "http.mix-host-case.bool";
    public static final String COMPLETE_MIX_HOST_CASE = "http.mix-host-case.complete.bool";
    public static final String MIX_HOST_HEADER_CASE = "http.mix-host-header-case.bool";
    public static final String DOT_AFTER_HOST_HEADER = "http.dot-after-host-header.bool";
    public static final String LINE_BREAK_BEFORE_GET = "http.line-break-before-get.bol";
    public static final String ADDITIONAL_SPACE_AFTER_GET = "http.space-after-get.bool";
    public static final String USE_DNS_SEC = "dns.dnssec.enabled.bool";
    public static final String DNS_ADDRESS = "dns.doh.address";
    public static final String GOVERNMENT_BLACKLIST_MIRROR = "powertunnel.government-blacklist-mirror";
    public static final String ENABLE_JOURNAL = "powertunnel.journal.enabled.bool";
    public static final String ENABLE_LOGS = "powertunnel.logs.enabled.bool";
    public static final String ALLOW_REQUESTS_TO_ORIGIN_SERVER = "server.allow-requests-to-origin-server.bool";
    public static final String ROOT_CA_PASSWORD = "powertunnel.cert.password";
    public static final String APPLY_HTTP_TRICKS_TO_HTTPS = "powertunnel.filter.http-https.bool";

    private static final Map<String, String> defaultValues = new HashMap<>();
    static {
        defaultValues.put(SERVER_IP_ADDRESS, "127.0.0.1");
        defaultValues.put(SERVER_PORT, "8085");
        defaultValues.put(AUTO_PROXY_SETUP_ENABLED, "true");
        defaultValues.put(PROXY_PAC_ENABLED, "false");
        defaultValues.put(ALLOW_INVALID_HTTP_PACKETS, "true");
        defaultValues.put(ENABLE_CHUNKING, "true");
        defaultValues.put(FULL_CHUNKING, "false");
        defaultValues.put(CHUNK_SIZE, "2");
        defaultValues.put(PAYLOAD_LENGTH, "0");
        defaultValues.put(MIX_HOST_CASE, "false");
        defaultValues.put(COMPLETE_MIX_HOST_CASE, "false");
        defaultValues.put(MIX_HOST_HEADER_CASE, "true");
        defaultValues.put(DOT_AFTER_HOST_HEADER, "true");
        defaultValues.put(LINE_BREAK_BEFORE_GET, "false");
        defaultValues.put(ADDITIONAL_SPACE_AFTER_GET, "false");
        defaultValues.put(SNI_TRICK, "0");
        defaultValues.put(SNI_TRICK_FAKE_HOST, "w3.org");
        defaultValues.put(USE_DNS_SEC, "false");
        defaultValues.put(DNS_ADDRESS, "");
        defaultValues.put(GOVERNMENT_BLACKLIST_MIRROR, "");
        defaultValues.put(ENABLE_JOURNAL, "false");
        defaultValues.put(ENABLE_LOGS, "false");
        defaultValues.put(ALLOW_REQUESTS_TO_ORIGIN_SERVER, "true");
        defaultValues.put(APPLY_HTTP_TRICKS_TO_HTTPS, "false");
    }
}
