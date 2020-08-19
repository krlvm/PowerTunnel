package ru.krlvm.powertunnel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.jitsi.dnssec.validator.ValidatingResolver;
import org.littleshoot.proxy.*;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.xbill.DNS.*;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.enums.SNITrick;
import ru.krlvm.powertunnel.enums.ServerStatus;
import ru.krlvm.powertunnel.exceptions.DataStoreException;
import ru.krlvm.powertunnel.exceptions.PTUnknownHostException;
import ru.krlvm.powertunnel.data.Settings;
import ru.krlvm.powertunnel.filter.ProxyFilter;
import ru.krlvm.powertunnel.frames.*;
import ru.krlvm.powertunnel.frames.journal.BlacklistFrame;
import ru.krlvm.powertunnel.frames.journal.JournalFrame;
import ru.krlvm.powertunnel.frames.journal.UserListFrame;
import ru.krlvm.powertunnel.frames.journal.WhitelistFrame;
import ru.krlvm.powertunnel.pac.PACGenerator;
import ru.krlvm.powertunnel.pac.PACScriptStore;
import ru.krlvm.powertunnel.system.MirroredOutputStream;
import ru.krlvm.powertunnel.system.SystemProxy;
import ru.krlvm.powertunnel.system.TrayManager;
import ru.krlvm.powertunnel.system.windows.WindowsProxyHandler;
import ru.krlvm.powertunnel.updater.UpdateNotifier;
import ru.krlvm.powertunnel.utilities.*;
import ru.krlvm.powertunnel.webui.PowerTunnelMonitor;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * PowerTunnel Bootstrap class
 *
 * This class initializes PowerTunnel, loads government blacklist,
 * user lists, holds journal and controls the LittleProxy Server
 *
 * @author krlvm
 */
public class PowerTunnel {

    public static final String NAME = "PowerTunnel";
    public static final String VERSION = "1.13";
    public static final int VERSION_CODE = 30;
    public static final String REPOSITORY_URL = "https://github.com/krlvm/PowerTunnel";

    private static final String HEADER =
            NAME + " version " + VERSION + "\n" +
            "Simple, scalable, cross-platform and effective solution against government censorship\n" +
            REPOSITORY_URL + "\n" +
            "(c) krlvm, 2019-2020\n";

    private static HttpProxyServer SERVER;
    private static ServerStatus STATUS = ServerStatus.NOT_RUNNING;
    public static String SERVER_IP_ADDRESS = "127.0.0.1";
    public static int SERVER_PORT = 8085;

    private static boolean AUTO_PROXY_SETUP_ENABLED = true;
    private static boolean PROXY_PAC_ENABLED = false;
    private static DataStore PROXY_PAC_STORE = null;

    private static boolean ALLOW_REQUESTS_TO_ORIGIN_SERVER = false;

    public static final Settings SETTINGS = new Settings();
    /* Optional settings */
    public static boolean ALLOW_INVALID_HTTP_PACKETS = true;
    public static boolean CHUNKING_ENABLED = true;
    public static boolean FULL_CHUNKING = false;
    public static int CHUNK_SIZE = 2;
    public static int PAYLOAD_LENGTH = 0; //21 recommended
    public static boolean USE_DNS_SEC = false;
    public static String DNS_SERVER = null;
    public static boolean APPLY_HTTP_TRICKS_TO_HTTPS = false;
    public static boolean MIX_HOST_CASE = false;
    public static boolean COMPLETE_MIX_HOST_CASE = false;
    public static boolean MIX_HOST_HEADER_CASE = true;
    public static boolean DOT_AFTER_HOST_HEADER = true;
    private static String GOVERNMENT_BLACKLIST_MIRROR = null;
    public static boolean LINE_BREAK_BEFORE_GET = false;
    public static boolean ADDITIONAL_SPACE_AFTER_GET = false;
    public static SNITrick SNI_TRICK = null;
    public static String SNI_TRICK_FAKE_HOST;
    /* ----------------- */

    public static boolean FULL_OUTPUT_MIRRORING = false;

    private static final Map<String, String> JOURNAL = new LinkedHashMap<>();
    private static final SimpleDateFormat JOURNAL_DATE_FORMAT = new SimpleDateFormat("[HH:mm]: ");

    public static boolean ENABLE_JOURNAL = false;
    public static boolean ENABLE_LOGS = false;

    private static final Set<String> GOVERNMENT_BLACKLIST = new HashSet<>();
    private static final Set<String> ISP_STUB_LIST = new HashSet<>();
    private static final Set<String> USER_BLACKLIST = new LinkedHashSet<>();
    private static final Set<String> USER_WHITELIST = new LinkedHashSet<>();

    private static TrayManager trayManager;
    private static MainFrame frame;
    public static LogFrame logFrame;
    public static JournalFrame journalFrame;
    public static OptionsFrame optionsFrame;
    public static UserListFrame[] USER_FRAMES;
    
    private static boolean CONSOLE_MODE = false;

    public static void main(String[] args) {
        //Parse launch arguments
        //java -jar PowerTunnel.jar (-args)
        boolean startNow = false;
        boolean[] uiSettings = { true, true, true };
        float scaleFactor = -1F;
        if(args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (!arg.startsWith("-")) {
                    continue;
                }
                arg = arg.replaceFirst("-", "").toLowerCase();
                switch (arg) {
                    case "help": {
                        Utility.print(HEADER);
                        Utility.print(
                            "Available arguments:\n" +
                            " -help                                display help\n" +
                            " -start                               starts server right after load\n" +
                            " -console                             console mode, without UI\n" +
                            " -government-blacklist-from [URL]     automatically fill government blacklist from URL\n" +
                            " -use-dns-sec                         enables DNSSec mode with the Google DNS servers\n" +
                            " -use-dns-server [URL]                overrides DNS settings (DNS over HTTPS supported)\n" +
                            " -disallow-invalid-packets            HTTP packets without Host header will be thrown out (unrecommended)\n" +
                            " -disable-chunking                    HTTPS: disables packet chunking (fragmentation)\n" +
                            " -full-chunking                       HTTPS: enables chunking the whole packets (requires chunking enabled)\n" +
                            " -chunk-size [size]                   HTTPS: sets size of one chunk\n" +
                            " -sni-trick [trick]                   HTTPS: enable SNI tricks: 1 - spoil, 2 - erase, 3 - fake; (requires Root CA installation)\n" +
                            " -sni-trick-fake-host [host]          HTTPS: host that will used with 'fake' SNI Trick\n" +
                            " -line-break-get                      HTTP:  inserts a line break before 'GET' method\n" +
                            " -space-after-get                     HTTP:  inserts a space after 'GET' method\n" +
                            " -apply-http-https                    HTTP:  apply enabled HTTP tricks to HTTPS\n" +
                            " -mix-host-case                       HTTP:  enables 'Host' header value case mix\n" +
                            " -complete-mix-host-case              HTTP:  complete 'Host' header value case mix\n" +
                            " -disable-mix-host-header-case        HTTP:  disables 'Host' header case mix\n" +
                            " -disable-dot-after-host-header       HTTP:  disables dot after host header\n" +
                            " -send-payload [length]               HTTP:  sends payload to bypass blocking, 21 is recommended\n" +
                            " -ip [IP Address]                     sets IP Address\n" +
                            " -port [Port]                         sets port\n" +
                            " -enable-journal                      enables PowerTunnel journal (when UI enabled)\n" +
                            " -enable-logs                         enables PowerTunnel logs (when UI enabled)\n" +
                            " -enable-log-to-file                  enables PowerTunnel logger and log file\n" +
                            " -with-web-ui [appendix]              enables Web UI at http://" + String.format(PowerTunnelMonitor.FAKE_ADDRESS_TEMPLATE, "[appendix]") + "\n" +
                            " -disable-auto-proxy-setup            disables auto proxy setup (supported OS: Windows)\n" +
                            " -enable-proxy-pac                    enables generation of PAC file on startup\n" +
                            " -auto-proxy-setup-win-ie             auto proxy setup using IE instead of native API on Windows\n" +
                            " -full-output-mirroring               fully mirrors system output to the log\n" +
                            " -set-scale-factor [n]                sets DPI scale factor (for testing purposes)\n" +
                            " -disable-tray                        disables tray icon\n" +
                            " -disable-native-lf                   disables native L&F (when UI enabled)\n" +
                            " -disable-ui-scaling                  disables UI scaling (when UI enabled)\n" +
                            " -disable-updater                     disables the update notifier\n" +
                            " -debug                               enables debug"
                        );
                        System.exit(0);
                        break;
                    }
                    case "start": {
                        startNow = true;
                        break;
                    }
                    case "use-dns-sec": {
                        SETTINGS.setTemporaryValue(Settings.USE_DNS_SEC, "true");
                        Utility.print("[#] Enabled DNSSec mode");
                        break;
                    }
                    case "full-output-mirroring": {
                        FULL_OUTPUT_MIRRORING = true;
                        Utility.print("[+] Enabled full output mirroring");
                        break;
                    }
                    case "debug": {
                        Debugger.setDebug(true);
                        break;
                    }
                    case "console": {
                        CONSOLE_MODE = true;
                        break;
                    }
                    case "disable-chunking": {
                        SETTINGS.setTemporaryValue(Settings.ENABLE_CHUNKING, "false");
                        break;
                    }
                    case "full-chunking": {
                        SETTINGS.setTemporaryValue(Settings.FULL_CHUNKING, "true");
                        break;
                    }
                    case "apply-http-https": {
                        SETTINGS.setTemporaryValue(Settings.APPLY_HTTP_TRICKS_TO_HTTPS, "true");
                        break;
                    }
                    case "mix-host-case": {
                        SETTINGS.setTemporaryValue(Settings.MIX_HOST_CASE, "true");
                        break;
                    }
                    case "complete-mix-host-case": {
                        SETTINGS.setTemporaryValue(Settings.COMPLETE_MIX_HOST_CASE, "true");
                        break;
                    }
                    case "disable-mix-host-header-case": {
                        SETTINGS.setTemporaryValue(Settings.MIX_HOST_HEADER_CASE, "false");
                        break;
                    }
                    case "disable-dot-after-host-header": {
                        SETTINGS.setTemporaryValue(Settings.DOT_AFTER_HOST_HEADER, "false");
                        break;
                    }
                    case "line-break-get": {
                        SETTINGS.setTemporaryValue(Settings.LINE_BREAK_BEFORE_GET, "true");
                        break;
                    }
                    case "space-after-get": {
                        SETTINGS.setTemporaryValue(Settings.ADDITIONAL_SPACE_AFTER_GET, "true");
                        break;
                    }
                    case "enable-journal": {
                        SETTINGS.setTemporaryValue(Settings.ENABLE_JOURNAL, "true");
                        break;
                    }
                    case "enable-logs": {
                        SETTINGS.setTemporaryValue(Settings.ENABLE_LOGS, "true");
                        break;
                    }
                    case "enable-log-to-file": {
                        Utility.initializeLogger();
                        break;
                    }
                    case "disable-auto-proxy-setup": {
                        SETTINGS.setTemporaryValue(Settings.AUTO_PROXY_SETUP_ENABLED, "false");
                        break;
                    }
                    case "enable-proxy-pac": {
                        SETTINGS.setTemporaryValue(Settings.PROXY_PAC_ENABLED, "true");
                        break;
                    }
                    case "disallow-invalid-packets": {
                        SETTINGS.setTemporaryValue(Settings.ALLOW_INVALID_HTTP_PACKETS, "false");
                        break;
                    }
                    case "auto-proxy-setup-win-ie": {
                        WindowsProxyHandler.USE_WINDOWS_NATIVE_API = false;
                        break;
                    }
                    case "disable-tray": {
                        uiSettings[2] = false;
                        break;
                    }
                    case "disable-ui-scaling": {
                        uiSettings[0] = false;
                        break;
                    }
                    case "disable-native-lf": {
                        uiSettings[1] = false;
                        break;
                    }
                    case "disable-updater": {
                        UpdateNotifier.ENABLED = false;
                        break;
                    }
                    default: {
                        if (args.length < i + 2) {
                            Utility.print("[!] Invalid input for option '%s'", arg);
                        } else {
                            String value = args[i + 1];
                            switch (arg) {
                                case "government-blacklist-from": {
                                    SETTINGS.setTemporaryValue(Settings.GOVERNMENT_BLACKLIST_MIRROR, value);
                                    Utility.print("[#] Government blacklist mirror: '%s'", value);
                                    break;
                                }
                                case "ip": {
                                    SETTINGS.setTemporaryValue(Settings.SERVER_IP_ADDRESS, value);
                                    Utility.print("[#] IP address set to '%s'", value);
                                    break;
                                }
                                case "port": {
                                    try {
                                        SETTINGS.setTemporaryValue(Settings.SERVER_PORT, value);
                                        Utility.print("[#] Port set to '%s'", value);
                                    } catch (NumberFormatException ex) {
                                        Utility.print("[x] Invalid port, using default");
                                    }
                                    break;
                                }
                                case "use-dns-server": {
                                    SETTINGS.setTemporaryValue(Settings.DNS_ADDRESS, value);
                                    Utility.print("[#] DNS resolver address set to '%s'", value);
                                    break;
                                }
                                case "with-web-ui": {
                                    PowerTunnelMonitor.FAKE_ADDRESS = String.format(PowerTunnelMonitor.FAKE_ADDRESS_TEMPLATE, value);
                                    break;
                                }
                                case "send-payload": {
                                    try {
                                        int payloadLength = Integer.parseInt(value);
                                        assert payloadLength > 0;
                                        SETTINGS.setTemporaryValue(Settings.PAYLOAD_LENGTH, value);
                                        Utility.print("[#] Payload length set to '" + value + "'");
                                    } catch (AssertionError | NumberFormatException ex) {
                                        Utility.print("[x] Invalid payload length, using '21'");
                                        PAYLOAD_LENGTH = 21;
                                    }
                                    break;
                                }
                                case "chunk-size": {
                                    try {
                                        SETTINGS.setTemporaryValue(Settings.CHUNK_SIZE, value);
                                        Utility.print("[#] Chunk size set to '%s'", value);
                                    } catch (NumberFormatException ex) {
                                        Utility.print("[x] Invalid chunk size number, using default");
                                    }
                                    break;
                                }
                                case "erase-sni": {
                                    try {
                                        int id = Integer.parseInt(value);
                                        SNITrick trick = SNITrick.fromID(id);
                                        if(trick == null) {
                                            Utility.print("[x] Unknown SNI trick ID");
                                        } else {
                                            SETTINGS.setTemporaryValue(Settings.SNI_TRICK, value);
                                            Utility.print("[#] SNI trick set to '%s (%s)'", trick.name(), id);
                                        }
                                    } catch (NumberFormatException ex) {
                                        Utility.print("[x] Invalid SNI Trick ID");
                                    }
                                    break;
                                }
                                case "set-scale-factor": {
                                    try {
                                        scaleFactor = Float.parseFloat(value);
                                        assert scaleFactor < 0;
                                    } catch (AssertionError | NumberFormatException ex) {
                                        Utility.print("[x] Invalid scale factor, will be detected automatically");
                                        scaleFactor = -1F;
                                    }
                                    break;
                                }
                                default: {
                                    //it is an argument
                                    //Utility.print("[?] Unknown option: '%s'", arg);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        try {
            SETTINGS.loadSettings();
            loadSettings();
        } catch (IOException ex) {
            Utility.print("[!] Failed to load settings: " + ex.getMessage());
            Debugger.debug(ex);
        }
        if(!CONSOLE_MODE) {
            //Initialize UI
            UIUtility.setAWTName();
            if(uiSettings[1]) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    System.out.println("Failed to set native Look and Feel: " + ex.getMessage());
                    ex.printStackTrace();
                    System.out.println();
                }
            }
            if(uiSettings[0]) {
                if(scaleFactor != -1) {
                    SwingDPI.setScaleFactor(scaleFactor);
                    SwingDPI.setScaleApplied(true);
                } else {
                    SwingDPI.applyScalingAutomatically();
                }
                Debugger.debug("SwingDPI v" + SwingDPI.VERSION + " | Scale factor: " + SwingDPI.getScaleFactor());
            }

            //Initializing main frame and system outputs mirroring
            if(ENABLE_LOGS) {
                logFrame = new LogFrame();
                if (FULL_OUTPUT_MIRRORING) {
                    PrintStream systemOutput = System.out;
                    PrintStream systemErr = System.err;
                    System.setOut(new PrintStream(new MirroredOutputStream(new ByteArrayOutputStream(), systemOutput)));
                    System.setErr(new PrintStream(new MirroredOutputStream(new ByteArrayOutputStream(), systemErr)));
                }
            }

            if(ENABLE_JOURNAL) {
                journalFrame = new JournalFrame();
            }

            trayManager = new TrayManager();
            if(uiSettings[2]) {
                try {
                    trayManager.load();
                } catch (Exception ex) {
                    Utility.print("[x] Tray icon initialization failed");
                    Debugger.debug(ex);
                }
            }

            optionsFrame = new OptionsFrame();
            frame = new AdvancedMainFrame();

            //Initialize UI
            USER_FRAMES = new UserListFrame[] {
                    new BlacklistFrame(), new WhitelistFrame()
            };
        }

        Utility.print(HEADER);

        if(isWebUIEnabled()) {
            try {
                PowerTunnelMonitor.load();
                Utility.print("[x] WebUI is available at http://%s", PowerTunnelMonitor.FAKE_ADDRESS);
            } catch (IOException ex) {
                Utility.print("[x] Cannot load Web UI, now it is disabled: " + ex.getMessage());
                Debugger.debug(ex);
                PowerTunnelMonitor.FAKE_ADDRESS = null;
            }
        }
        if(CONSOLE_MODE || startNow) {
            safeBootstrap();
        }

        Utility.print();
        UpdateNotifier.checkAndNotify();
    }

    public static String safeBootstrap() {
        String error = null;
        try {
            PowerTunnel.bootstrap();
        } catch (PTUnknownHostException ex) {
            Utility.print("[x] Cannot use %s address '%s': %s", ex.getType(), ex.getHost(), ex.getMessage());
            Debugger.debug(ex);
            error = "Cannot use " + ex.getType() + " address '" + ex.getHost() + "'";
        } catch (DataStoreException ex) {
            Utility.print("[x] Failed to load data store: " + ex.getMessage());
            ex.printStackTrace();
            error = "Failed to load data store: " + ex.getMessage();
        }
        if(error != null) {
            setStatus(ServerStatus.NOT_RUNNING);
        }
        return error;
    }

    /**
     * PowerTunnel bootstrap
     */
    public static void bootstrap() throws DataStoreException, PTUnknownHostException {
        setStatus(ServerStatus.STARTING);
        //Load data
        try {
            loadLists();
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }

        //Start server
        startServer();
    }

    /**
     * Starts LittleProxy server
     */
    private static void startServer() throws PTUnknownHostException {
        setStatus(ServerStatus.STARTING);
        SETTINGS.setOption(Settings.SERVER_IP_ADDRESS, SERVER_IP_ADDRESS);
        SETTINGS.setOption(Settings.SERVER_PORT, String.valueOf(SERVER_PORT));
        Utility.print("[.] Starting LittleProxy server on %s:%s", SERVER_IP_ADDRESS, SERVER_PORT);
        HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap().withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new ProxyFilter(originalRequest);
            }
        }).withTransparent(true).withUseDnsSec(USE_DNS_SEC)
                .withAllowRequestToOriginServer(ALLOW_REQUESTS_TO_ORIGIN_SERVER);
        try {
            bootstrap.withAddress(new InetSocketAddress(InetAddress.getByName(SERVER_IP_ADDRESS), SERVER_PORT));
        } catch (UnknownHostException ex) {
            throw new PTUnknownHostException(SERVER_IP_ADDRESS, PTUnknownHostException.Type.SERVER_IP, ex);
        }
        boolean overrideDns = DNS_SERVER != null && !DNS_SERVER.isEmpty();
        boolean doh = DNS_SERVER != null && DNS_SERVER.startsWith("https://");
        if (overrideDns) {
            if(doh) {
                if (DNS_SERVER.endsWith("/")) {
                    DNS_SERVER = DNS_SERVER.substring(0, DNS_SERVER.length() - 1);
                }
                Utility.print("[*] DNS over HTTPS is enabled: '" + DNS_SERVER + "'");
            } else {
                Utility.print("[*] DNS override enabled: '" + DNS_SERVER + "'");
            }
        }
        if (SNI_TRICK != null) {
            try {
                bootstrap.withManInTheMiddle(MITMUtility.mitmManager());
                Utility.print("[*] SNI Tricks is enabled\n" +
                        "    You have to install PowerTunnel Root CA\n" +
                        "    Please, read the following manual: " + SNITrick.SUPPORT_REFERENCE);
            } catch (Exception ex) {
                Utility.print("[x] Failed to initialize MITM Manager for SNI Tricks\n" +
                              "    Follow this link for troubleshooting: " + SNITrick.SUPPORT_REFERENCE);
            }
        }
        if (USE_DNS_SEC) {
            Utility.print("[*] DNSSec is enabled");
        }
        if(overrideDns || USE_DNS_SEC) {
            final Resolver resolver;
            try {
                resolver = getResolver(overrideDns, doh);
            } catch (UnknownHostException ex) {
                throw new PTUnknownHostException(DNS_SERVER, PTUnknownHostException.Type.DNS, ex);
            }
            bootstrap.withServerResolver((host, port) -> {
                try {
                    Lookup lookup = new Lookup(host, Type.A);
                    lookup.setResolver(resolver);
                    Record[] records = lookup.run();
                    if (lookup.getResult() == Lookup.SUCCESSFUL) {
                        return new InetSocketAddress(((ARecord) records[0]).getAddress(), port);
                    } else {
                        throw new UnknownHostException(lookup.getErrorString());
                    }
                } catch (Exception ex) {
                    Utility.print("[x] Failed to resolve '%s': %s", host, ex.getMessage());
                    Debugger.debug(ex);
                    throw new UnknownHostException(String.format("Failed to resolve '%s': %s", host, ex.getMessage()));
                }
            });
        }
        if(PROXY_PAC_ENABLED) {
            if(PROXY_PAC_STORE == null) {
                PROXY_PAC_STORE = new PACScriptStore();
            }
            try {
                PROXY_PAC_STORE.write(PACGenerator.generatePAC(SERVER_IP_ADDRESS, SERVER_PORT, GOVERNMENT_BLACKLIST));
                GOVERNMENT_BLACKLIST.clear();
                GOVERNMENT_BLACKLIST.add("*");
                Utility.print("[i] PAC script is generated, global bypass mode is enabled");
            } catch (IOException ex) {
                Utility.print("[x] Failed to write PAC script: %s", ex.getMessage());
                Debugger.debug(ex);
            }
        }
        try {
            SERVER = bootstrap.start();
            setStatus(ServerStatus.RUNNING);
            Utility.print("[.] Server started");
            if(AUTO_PROXY_SETUP_ENABLED) {
                SystemProxy.enableProxy();
            }
        } catch (Exception ex) {
            Utility.print("[x] Cannot to start server: " + ex.getMessage());
            Debugger.debug(ex);
            setStatus(ServerStatus.NOT_RUNNING);
            if(isUIEnabled()) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(frame, "Cannot to start server: " + ex.getMessage(),
                                NAME, JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
        Utility.print();

        if(!CONSOLE_MODE) {
            frame.update();
        }
    }

    /**
     * Stops LittleProxy server
     */
    public static void stopServer() {
        if(!isRunning()) {
            throw new IllegalStateException("Server not running");
        }
        setStatus(ServerStatus.STOPPING);
        Utility.print();
        Utility.print("[.] Stopping server...");
        SERVER.stop();
        SERVER = null;
        Utility.print("[.] Server stopped");
        Utility.print();
        setStatus(ServerStatus.NOT_RUNNING);

        if(AUTO_PROXY_SETUP_ENABLED) {
            SystemProxy.disableProxy();
        }
    }

    /**
     * Save data and goodbye
     */
    public static void stop() {
        stopServer();
        safeUserListSave();
        clearLists();
    }

    public static void restartServer() {
        stopServer();
        safeBootstrap();
    }

    public static boolean isRunning() {
        return SERVER != null;
    }

    public static void handleClosing() {
        new Thread(() -> {
            if (PowerTunnel.getStatus() == ServerStatus.RUNNING) {
                PowerTunnel.stop();
            } else {
                PowerTunnel.safeUserListSave();
            }
            if(trayManager.isLoaded()) {
                trayManager.unload();
            }
            System.exit(0);
        }).start();
    }

    public static void showMainFrame() {
        frame.showFrame();
    }

    public static boolean isMainFrameVisible() {
        return frame.isVisible();
    }

    public static void setStatus(ServerStatus status) {
        STATUS = status;
        if(!CONSOLE_MODE) {
            frame.update();
        }
    }

    private static void clearLists() {
        GOVERNMENT_BLACKLIST.clear();
        USER_BLACKLIST.clear();
        USER_WHITELIST.clear();
        ISP_STUB_LIST.clear();
    }

    public static void loadLists() throws IOException {
        clearLists();
        for (String address : DataStore.GOVERNMENT_BLACKLIST.load()) {
            addToGovernmentBlacklist(address);
        }
        if(GOVERNMENT_BLACKLIST_MIRROR != null && !GOVERNMENT_BLACKLIST_MIRROR.trim().isEmpty()) {
            Utility.print("[#] Loading government blacklist from the mirror...");
            try {
                URL url = new URL(GOVERNMENT_BLACKLIST_MIRROR);
                InputStream in = url.openStream();
                Scanner scanner = new Scanner(in);
                int before = GOVERNMENT_BLACKLIST.size();
                while (scanner.hasNext()) {
                    addToGovernmentBlacklist(scanner.next());
                }
                in.close();
                scanner.close();
                Utility.print("[#] Loaded '%s' government-blocked websites from the mirror", (GOVERNMENT_BLACKLIST.size() - before));
            } catch (Exception ex) {
                Utility.print("[#] Failed to load government-blocked websites from the mirror: " + ex.getMessage());
                Debugger.debug(ex);
            }
        }
        for (String address : DataStore.USER_BLACKLIST.load()) {
            addToUserBlacklist(address);
        }
        if(!CONSOLE_MODE) {
            USER_FRAMES[0].refill();
        }
        for (String address : DataStore.USER_WHITELIST.load()) {
            addToUserWhitelist(address);
        }
        if(!CONSOLE_MODE) {
            USER_FRAMES[1].refill();
        }
        ISP_STUB_LIST.addAll(DataStore.ISP_STUB_LIST.load());
        Utility.print("[i] Loaded '%s' government blocked sites, '%s' user blocked sites, '%s' user whitelisted sites",
                GOVERNMENT_BLACKLIST.size(), USER_BLACKLIST.size(), USER_WHITELIST.size());
        Utility.print();
    }

    private static Resolver getResolver(boolean override, boolean useDoh) throws UnknownHostException {
        Resolver resolver = null;
        if (useDoh) {
            resolver = new DohResolver(DNS_SERVER);
        } else {
            if(override) {
                String address = DNS_SERVER;
                int port = -1;
                if(IPUtility.hasPort(DNS_SERVER)) {
                    Object[] split = IPUtility.split(DNS_SERVER);
                    if(split != null) {
                        address = ((String) split[0]);
                        port = ((int) split[1]);
                    }
                }
                resolver = new SimpleResolver(address);
                if(port != -1) {
                    resolver.setPort(port);
                }
            }
            if (USE_DNS_SEC) {
                if (resolver == null) {
                    resolver = new SimpleResolver();
                }
                resolver = new ValidatingResolver(resolver);
            }
        }
        return resolver;
    }

    public static void loadSettings() {
        ENABLE_JOURNAL = SETTINGS.getBooleanOption(Settings.ENABLE_JOURNAL);
        ENABLE_LOGS = SETTINGS.getBooleanOption(Settings.ENABLE_LOGS);
        GOVERNMENT_BLACKLIST_MIRROR = SETTINGS.getOption(Settings.GOVERNMENT_BLACKLIST_MIRROR);

        SERVER_IP_ADDRESS = SETTINGS.getOption(Settings.SERVER_IP_ADDRESS);
        SERVER_PORT = SETTINGS.getIntOption(Settings.SERVER_PORT);

        AUTO_PROXY_SETUP_ENABLED = SETTINGS.getBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED);
        PROXY_PAC_ENABLED = SETTINGS.getBooleanOption(Settings.PROXY_PAC_ENABLED);

        ALLOW_INVALID_HTTP_PACKETS = SETTINGS.getBooleanOption(Settings.ALLOW_INVALID_HTTP_PACKETS);
        CHUNKING_ENABLED = SETTINGS.getBooleanOption(Settings.ENABLE_CHUNKING);
        FULL_CHUNKING = SETTINGS.getBooleanOption(Settings.FULL_CHUNKING);
        CHUNK_SIZE = SETTINGS.getIntOption(Settings.CHUNK_SIZE);
        PAYLOAD_LENGTH = SETTINGS.getIntOption(Settings.PAYLOAD_LENGTH);
        MIX_HOST_CASE = SETTINGS.getBooleanOption(Settings.MIX_HOST_CASE);
        COMPLETE_MIX_HOST_CASE = SETTINGS.getBooleanOption(Settings.COMPLETE_MIX_HOST_CASE);
        MIX_HOST_HEADER_CASE = SETTINGS.getBooleanOption(Settings.MIX_HOST_HEADER_CASE);
        DOT_AFTER_HOST_HEADER = SETTINGS.getBooleanOption(Settings.DOT_AFTER_HOST_HEADER);
        LINE_BREAK_BEFORE_GET = SETTINGS.getBooleanOption(Settings.LINE_BREAK_BEFORE_GET);
        ADDITIONAL_SPACE_AFTER_GET = SETTINGS.getBooleanOption(Settings.ADDITIONAL_SPACE_AFTER_GET);
        SNI_TRICK = SNITrick.fromID(SETTINGS.getIntOption(Settings.SNI_TRICK));
        SNI_TRICK_FAKE_HOST = SETTINGS.getOption(Settings.SNI_TRICK_FAKE_HOST);

        USE_DNS_SEC = SETTINGS.getBooleanOption(Settings.USE_DNS_SEC);
        DNS_SERVER = SETTINGS.getOption(Settings.DNS_ADDRESS);

        ALLOW_REQUESTS_TO_ORIGIN_SERVER = SETTINGS.getBooleanOption(Settings.ALLOW_REQUESTS_TO_ORIGIN_SERVER);
        APPLY_HTTP_TRICKS_TO_HTTPS = SETTINGS.getBooleanOption(Settings.APPLY_HTTP_TRICKS_TO_HTTPS);
    }

    public static TrayManager getTray() {
        return trayManager;
    }

    public static boolean isWebUIEnabled() {
        return PowerTunnelMonitor.FAKE_ADDRESS != null;
    }

    public static void safeUserListSave() {
        try {
            saveUserLists();
            Utility.print("[#] Configuration files has been saved");
        } catch (IOException ex) {
            Utility.print("[x] Failed to save data: " + ex.getMessage());
            ex.printStackTrace();
            Utility.print();
        }
    }

    public static boolean isHTTPMethodTricksEnabled() {
        return ADDITIONAL_SPACE_AFTER_GET || LINE_BREAK_BEFORE_GET;
    }

    /**
     * Retrieves proxy server status
     *
     * @return proxy server status
     */
    public static ServerStatus getStatus() {
        return STATUS;
    }

    /**
     * Retrieves is console mode disabled (therefore is the UI enabled)
     *
     * @return is UI enabled
     */
    public static boolean isUIEnabled() {
        return !CONSOLE_MODE;
    }

    /*
    Journal block
     */

    /**
     * Adds website address to journal
     *
     * @param address - website address
     */
    public static void addToJournal(String address) {
        if(CONSOLE_MODE || !ENABLE_JOURNAL) {
            return;
        }
        JOURNAL.put(address, JOURNAL_DATE_FORMAT.format(new Date()));
        journalFrame.refill();
    }

    /**
     * Retrieves the journal
     *
     * @return journal
     */
    public static Map<String, String> getJournal() {
        return JOURNAL;
    }

    /**
     * Clears the journal
     */
    public static void clearJournal() {
        JOURNAL.clear();
    }

    /*
    Government blacklist block
     */

    /**
     * Adds website to the government blacklist
     * and removes from the user whitelist if it's contains in it
     *
     * @param address - website address
     * @return true if address doesn't already contains in the government blacklist or false if it is
     */
    public static boolean addToGovernmentBlacklist(String address) {
        address = URLUtility.clearHost(address.toLowerCase());
        if(GOVERNMENT_BLACKLIST.contains(address)) {
            return false;
        }
        GOVERNMENT_BLACKLIST.add(address);
        return true;
    }

    /**
     * Retrieves the government blacklist
     *
     * @return government blacklist
     */
    public static Set<String> getGovernmentBlacklist() {
        return GOVERNMENT_BLACKLIST;
    }

    /**
     * Determine if 302-redirect location is ISP (Internet Service Provider) stub
     *
     * @param address - redirect location
     * @return true if it's ISP stub or false if it isn't
     */
    public static boolean isIspStub(String address) {
        String host;
        if(address.contains("/")) {
            host = address.substring(0, address.indexOf("/"));
        } else {
            host = address;
        }
        host = URLUtility.clearHost(host).toLowerCase();
        return ISP_STUB_LIST.contains(host);
    }

    /**
     * Retrieves is the website blocked by the government
     *
     * @param address - website address
     * @return is address blocked by the government
     */
    public static boolean isBlockedByGovernment(String address) {
        return URLUtility.checkIsHostContainsInList(address.toLowerCase(), GOVERNMENT_BLACKLIST);
        //return GOVERNMENT_BLACKLIST.contains(address.toLowerCase());
    }

    /*
    User lists block
     */

    /**
     * Writes user black and whitelist to data store
     *
     * @throws IOException - write failure
     * @see DataStore
     */
    public static void saveUserLists() throws IOException {
        DataStore.USER_BLACKLIST.write(USER_BLACKLIST);
        DataStore.USER_WHITELIST.write(USER_WHITELIST);
        SETTINGS.save();
    }

    /**
     * Refills user list frames
     */
    public static void updateUserListFrames() {
        if(CONSOLE_MODE) {
            return;
        }
        for (UserListFrame frame : USER_FRAMES) {
            frame.refill();
        }
    }

    /*
    Blacklist
     */

    /**
     * Adds website to the user blacklist
     * and removes from the user whitelist if it's contains in it
     *
     * @param address - website address
     * @return true if address doesn't already contains in the user blacklist or false if it is
     */
    public static boolean addToUserBlacklist(String address) {
        address = URLUtility.clearHost(address.toLowerCase());
        if(USER_BLACKLIST.contains(address)) {
            return false;
        }
        USER_WHITELIST.remove(address);
        USER_BLACKLIST.add(address);
        updateUserListFrames();
        Utility.print("\n[@] Blacklisted: '%s'", address);
        return true;
    }

    /**
     * Retrieves if user blocked website
     *
     * @param address - website address
     * @return true if user blocked website or false if he isn't
     */
    public static boolean isUserBlacklisted(String address) {
        return URLUtility.checkIsHostContainsInList(address.toLowerCase(), USER_BLACKLIST);
        //return USER_BLACKLIST.contains(address.toLowerCase());
    }

    /**
     * Removes website from the user blacklist
     *
     * @param address - website address
     * @return true if address contained in the user blacklist (and removed) or false if it isn't
     */
    public static boolean removeFromUserBlacklist(String address) {
        address = URLUtility.clearHost(address.toLowerCase());
        if(!USER_BLACKLIST.contains(address)) {
            return false;
        }
        USER_BLACKLIST.remove(address);
        updateUserListFrames();
        Utility.print("\n[@] Removed from the blacklist: '%s'", address);
        return true;
    }

    /**
     * Retrieves the user blacklist
     *
     * @return the user blacklist
     */
    public static Set<String> getUserBlacklist() {
        return USER_BLACKLIST;
    }

    /*
    Whitelist
     */

    /**
     * Adds website to the user whitelist
     * and removes from the user blocklist if it's contains in it
     *
     * @param address - website address
     * @return true if address doesn't already contains in the user whitelist or false if it is
     */
    public static boolean addToUserWhitelist(String address) {
        address = URLUtility.clearHost(address.toLowerCase());
        if(USER_WHITELIST.contains(address)) {
            return false;
        }
        USER_BLACKLIST.remove(address);
        USER_WHITELIST.add(address);
        updateUserListFrames();
        Utility.print("\n[@] Whitelisted: '%s'", address);
        return true;
    }

    /**
     * Retrieve if user whitelisted website
     *
     * @param address - website address
     * @return true if user whitelisted website or false if he isn't
     */
    public static boolean isUserWhitelisted(String address) {
        return URLUtility.checkIsHostContainsInList(address.toLowerCase(), USER_WHITELIST);
        //return USER_WHITELIST.contains(address.toLowerCase());
    }

    /**
     * Removes website from the user whitelist
     *
     * @param address - website address
     * @return true if address contained in the user whitelist (and removed) or false if it isn't
     */
    public static boolean removeFromUserWhitelist(String address) {
        address = URLUtility.clearHost(address.toLowerCase());
        if(!USER_WHITELIST.contains(address)) {
            return false;
        }
        USER_WHITELIST.remove(address);
        updateUserListFrames();
        Utility.print("\n[@] Removed from the whitelist: '%s'", address);
        return true;
    }

    /**
     * Retrieves the user whitelist
     *
     * @return the user whitelist
     */
    public static Set<String> getUserWhitelist() {
        return USER_WHITELIST;
    }
}