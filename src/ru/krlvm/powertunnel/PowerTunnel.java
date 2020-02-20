package ru.krlvm.powertunnel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.data.DataStoreException;
import ru.krlvm.powertunnel.data.Settings;
import ru.krlvm.powertunnel.filter.ProxyFilter;
import ru.krlvm.powertunnel.frames.*;
import ru.krlvm.powertunnel.system.MirroredOutputStream;
import ru.krlvm.powertunnel.system.SystemProxy;
import ru.krlvm.powertunnel.system.TrayManager;
import ru.krlvm.powertunnel.updater.UpdateNotifier;
import ru.krlvm.powertunnel.utilities.Debugger;
import ru.krlvm.powertunnel.utilities.URLUtility;
import ru.krlvm.powertunnel.utilities.Utility;
import ru.krlvm.powertunnel.webui.PowerTunnelMonitor;
import ru.krlvm.swingdpi.SwingDPI;

import javax.swing.*;
import java.awt.*;
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
    public static final String VERSION = "1.9";
    public static final int VERSION_CODE = 18;
    public static final String REPOSITORY_URL = "https://github.com/krlvm/PowerTunnel";

    private static HttpProxyServer SERVER;
    private static ServerStatus STATUS = ServerStatus.NOT_RUNNING;
    public static String SERVER_IP_ADDRESS = "127.0.0.1";
    public static int SERVER_PORT = 8085;
    private static boolean AUTO_PROXY_SETUP_ENABLED = true;

    public static final Settings SETTINGS = new Settings();
    /** Optional settings */
    public static boolean FULL_CHUNKING = false;
    public static int CHUNK_SIZE = 2;
    public static int PAYLOAD_LENGTH = 0; //21 recommended
    public static boolean USE_DNS_SEC = false;
    public static boolean MIX_HOST_CASE = false;
    private static String GOVERNMENT_BLACKLIST_MIRROR = null;
    /** ----------------- */

    public static boolean FULL_OUTPUT_MIRRORING = false;

    private static final Map<String, String> JOURNAL = new LinkedHashMap<>();
    private static final SimpleDateFormat JOURNAL_DATE_FORMAT = new SimpleDateFormat("[HH:mm]: ");
    public static boolean DISABLE_JOURNAL = false;

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
        boolean[] uiSettings = { true, true };
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
                        Utility.print("Available params:\n" +
                                " -help - display help\n" +
                                " -start - starts server right after load\n" +
                                " -console - console mode, without UI\n" +
                                " -government-blacklist-from [URL] - automatically fill government blacklist from URL\n" +
                                " -use-dns-sec - enables DNSSec mode with the Google DNS servers\n" +
                                " -full-chunking - enables chunking the whole packets\n" +
                                " -mix-host-case - enables 'Host' header case mix (unstable)\n" +
                                " -send-payload [length] - to bypass HTTP blocking, 21 is recommended\n" +
                                " -chunk-size [size] - sets size of one chunk\n" +
                                " -ip [IP Address] - sets IP Address\n" +
                                " -port [Port] - sets port\n" +
                                " -with-web-ui [appendix] - enables Web UI at http://" + String.format(PowerTunnelMonitor.FAKE_ADDRESS_TEMPLATE, "[appendix]") + "\n" +
                                " -disable-auto-proxy-setup - disables auto proxy setup on Windows\n" +
                                " -full-output-mirroring - fully mirrors system output to the log\n" +
                                " -set-scale-factor [n] - sets DPI scale factor (for testing purposes)\n" +
                                " -disable-journal - disables journal\n" +
                                " -disable-native-lf - disables native L&F (when UI enabled)\n" +
                                " -disable-ui-scaling - disables UI scaling (when UI enabled)\n" +
                                " -disable-updater - disables the update notifier\n" +
                                " -debug - enable debug");
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
                    case "full-chunking": {
                        SETTINGS.setTemporaryValue(Settings.FULL_CHUNKING, "true");
                        Utility.print("[#] Full-chunking mode enabled");
                        break;
                    }
                    case "mix-host-case": {
                        SETTINGS.setTemporaryValue(Settings.MIX_HOST_CASE, "true");
                        Utility.print("[#] Enabled case mix for the 'Host' header");
                        break;
                    }
                    case "disable-journal": {
                        SETTINGS.setTemporaryValue(Settings.DISABLE_JOURNAL, "true");
                        break;
                    }
                    case "disable-auto-proxy-setup": {
                        SETTINGS.setTemporaryValue(Settings.AUTO_PROXY_SETUP_ENABLED, "false");
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
        if(!CONSOLE_MODE) {
            try {
                SETTINGS.loadSettings();
                loadSettings();
            } catch (IOException ex) {
                Utility.print("[!] Failed to load settings: " + ex.getMessage());
                Debugger.debug(ex);
            }
            //Initialize UI
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
                } else {
                    SwingDPI.applyScalingAutomatically();
                }
                Debugger.debug("SwingDPI v" + SwingDPI.VERSION + " | Scale factor: " + SwingDPI.getScaleFactor());
            }

            trayManager = new TrayManager();
            try {
                trayManager.load();
            } catch (AWTException ex) {
                Utility.print("[x] Tray icon initialization failed");
                Debugger.debug(ex);
            }

            //Initializing main frame and system outputs mirroring
            logFrame = new LogFrame();
            if (FULL_OUTPUT_MIRRORING) {
                PrintStream systemOutput = System.out;
                PrintStream systemErr = System.err;
                System.setOut(new PrintStream(new MirroredOutputStream(new ByteArrayOutputStream(), systemOutput)));
                System.setErr(new PrintStream(new MirroredOutputStream(new ByteArrayOutputStream(), systemErr)));
            }

            journalFrame = new JournalFrame();
            optionsFrame = new OptionsFrame();
            frame = new MainFrame();

            //Initialize UI
            USER_FRAMES = new UserListFrame[] {
                    new BlacklistFrame(), new WhitelistFrame()
            };
        }

        Utility.print(NAME + " version " + VERSION);
        Utility.print("Simple, scalable, cross-platform and effective solution against government censorship");
        Utility.print(REPOSITORY_URL);
        Utility.print("(c) krlvm, 2019-2020");
        Utility.print();

        //Allow us to modify 'HOST' request header
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

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

        UpdateNotifier.checkAndNotify();
    }

    public static String safeBootstrap() {
        String error = null;
        try {
            PowerTunnel.bootstrap();
        } catch (UnknownHostException ex) {
            Utility.print("[x] Cannot use IP-Address '%s': %s", SERVER_IP_ADDRESS, ex.getMessage());
            Debugger.debug(ex);
            Utility.print("[!] Program halted");
            error = "Cannot use IP Address '" + PowerTunnel.SERVER_IP_ADDRESS + "'";
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
    public static void bootstrap() throws DataStoreException, UnknownHostException {
        setStatus(ServerStatus.STARTING);
        //Load data
        try {
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
        } catch (IOException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }

        //Start server
        startServer();
    }

    /**
     * Starts LittleProxy server
     */
    private static void startServer() throws UnknownHostException {
        setStatus(ServerStatus.STARTING);
        SETTINGS.setOption(Settings.SERVER_IP_ADDRESS, SERVER_IP_ADDRESS);
        SETTINGS.setOption(Settings.SERVER_PORT, String.valueOf(SERVER_PORT));
        Utility.print("[.] Starting LittleProxy server on %s:%s", SERVER_IP_ADDRESS, SERVER_PORT);
        SERVER = DefaultHttpProxyServer.bootstrap().withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new ProxyFilter(originalRequest);
            }
        }).withAddress(new InetSocketAddress(InetAddress.getByName(SERVER_IP_ADDRESS), SERVER_PORT))
                .withTransparent(true).withUseDnsSec(USE_DNS_SEC).start();
        setStatus(ServerStatus.RUNNING);
        Utility.print("[.] Server started");
        Utility.print();

        if(AUTO_PROXY_SETUP_ENABLED) {
            SystemProxy.enableProxy();
        }

        if(!CONSOLE_MODE) {
            frame.update();
        }
    }

    /**
     * Stops LittleProxy server
     */
    public static void stopServer() {
        setStatus(ServerStatus.STOPPING);
        Utility.print();
        Utility.print("[.] Stopping server...");
        SERVER.stop();
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
        GOVERNMENT_BLACKLIST.clear();
        USER_BLACKLIST.clear();
        USER_WHITELIST.clear();
        ISP_STUB_LIST.clear();
    }

    public static void handleClosing() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (PowerTunnel.getStatus() == ServerStatus.RUNNING) {
                    PowerTunnel.stop();
                } else {
                    PowerTunnel.safeUserListSave();
                }
                System.exit(0);
            }
        }).start();
    }

    public static void showMainFrame() {
        frame.setVisible(true);
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

    public static void loadSettings() {
        DISABLE_JOURNAL = SETTINGS.getBooleanOption(Settings.DISABLE_JOURNAL);
        GOVERNMENT_BLACKLIST_MIRROR = SETTINGS.getOption(Settings.GOVERNMENT_BLACKLIST_MIRROR);

        SERVER_IP_ADDRESS = SETTINGS.getOption(Settings.SERVER_IP_ADDRESS);
        SERVER_PORT = SETTINGS.getIntOption(Settings.SERVER_PORT);
        AUTO_PROXY_SETUP_ENABLED = SETTINGS.getBooleanOption(Settings.AUTO_PROXY_SETUP_ENABLED);

        FULL_CHUNKING = SETTINGS.getBooleanOption(Settings.FULL_CHUNKING);
        CHUNK_SIZE = SETTINGS.getIntOption(Settings.CHUNK_SIZE);
        PAYLOAD_LENGTH = SETTINGS.getIntOption(Settings.PAYLOAD_LENGTH);
        MIX_HOST_CASE = SETTINGS.getBooleanOption(Settings.MIX_HOST_CASE);
        USE_DNS_SEC = SETTINGS.getBooleanOption(Settings.USE_DNS_SEC);
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
        if(!DISABLE_JOURNAL) {
            return;
        }
        JOURNAL.put(address, JOURNAL_DATE_FORMAT.format(new Date()));
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