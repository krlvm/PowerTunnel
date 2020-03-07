package ru.krlvm.powertunnel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import ru.krlvm.powertunnel.data.DataStore;
import ru.krlvm.powertunnel.data.DataStoreException;
import ru.krlvm.powertunnel.filter.ProxyFilter;
import ru.krlvm.powertunnel.patches.PatchManager;
import ru.krlvm.powertunnel.utilities.URLUtility;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * LibertyTunnel Bootstrap class
 *
 * LibertyTunnel: PowerTunnel with nothing extra
 *
 * This class initializes LibertyTunnel, loads government blacklist
 * and controls a LittleProxy Server
 *
 * @author krlvm
 */
public class PowerTunnel {

    public static final String NAME = "LibertyTunnel";
    public static final String VERSION = "1.0";
    public static final int VERSION_CODE = 10; //base version code
    public static final String REPOSITORY_URL = "https://github.com/krlvm/PowerTunnel/tree/libertytunnel";

    private static HttpProxyServer SERVER;
    private static boolean RUNNING = false;
    public static String SERVER_IP_ADDRESS = "127.0.0.1";
    public static int SERVER_PORT = 8085;

    public static boolean FULL_CHUNKING = false;
    public static int DEFAULT_CHUNK_SIZE = 2;
    public static int PAYLOAD_LENGTH = 0; //21 recommended

    public static boolean MIX_HOST_CASE = false;

    private static final Set<String> GOVERNMENT_BLACKLIST = new HashSet<>();
    private static final Set<String> ISP_STUB_LIST = new HashSet<>();

    public static void main(String[] args) {
        //Parse launch arguments
        //java -jar PowerTunnel.jar (-args)
        if(args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (!arg.startsWith("-")) {
                    continue;
                }
                arg = arg.replaceFirst("-", "").toLowerCase();
                switch (arg) {
                    case "help": {
                        System.out.println("Available params:\n" +
                                " -help - display help\n" +
                                " -full-chunking - enables chunking the whole packets\n" +
                                " -mix-host-case - enables 'Host' header case mix (unstable)\n" +
                                " -send-payload [length] - method to bypass HTTP blocking, 21 is recommended\n" +
                                " -chunk-size [size] - sets size of one chunk\n" +
                                " -ip [IP Address] - sets IP Address\n" +
                                " -port [Port] - sets port\n");
                        System.exit(0);
                        break;
                    }
                    case "full-chunking": {
                        FULL_CHUNKING = true;
                        System.out.println("[#] Full-chunking mode enabled");
                        break;
                    }
                    case "mix-host-case": {
                        MIX_HOST_CASE = true;
                        System.out.println("[#] Enabled case mix for 'Host' header");
                        break;
                    }
                    default: {
                        if (args.length < i + 1) {
                            System.out.println("[!] Invalid input for option '" + arg + "'");
                        } else {
                            String value = args[i + 1];
                            switch (arg) {
                                case "ip": {
                                    SERVER_IP_ADDRESS = value;
                                    System.out.println("[#] IP address set to '" + SERVER_IP_ADDRESS + "'");
                                    break;
                                }
                                case "port": {
                                    try {
                                        SERVER_PORT = Integer.parseInt(value);
                                        System.out.println("[#] Port set to '" + SERVER_PORT + "'");
                                    } catch (NumberFormatException ex) {
                                        System.out.println("[x] Invalid port, using default");
                                    }
                                    break;
                                }
                                case "send-payload": {
                                    try {
                                        PAYLOAD_LENGTH = Integer.parseInt(value);
                                        assert PAYLOAD_LENGTH > 0;
                                        System.out.println("[#] Payload length set to '" + PAYLOAD_LENGTH + "'");
                                    } catch (AssertionError | NumberFormatException ex) {
                                        System.out.println("[x] Invalid payload length, using '21'");
                                        PAYLOAD_LENGTH = 21;
                                    }
                                    break;
                                }
                                case "chunk-size": {
                                    try {
                                        DEFAULT_CHUNK_SIZE = Integer.parseInt(value);
                                        System.out.println("[#] Chunk size set to '" + DEFAULT_CHUNK_SIZE + "'");
                                    } catch (NumberFormatException ex) {
                                        System.out.println("[x] Invalid chunk size number, using default");
                                    }
                                    break;
                                }
                                default: {
                                    //it is an argument
                                    //System.out.println("[?] Unknown option: '%s'", arg);
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        System.out.println(NAME + " version " + VERSION);
        System.out.println("Simple, scalable, cross-platform and effective solution against government censorship");
        System.out.println(REPOSITORY_URL);
        System.out.println("Base PowerTunnel version: 1.7.2 | https://github.com/krlvm/PowerTunnel");
        System.out.println("(c) krlvm, 2019-2020");
        System.out.println();

        //Allow us to modify 'HOST' request header
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        //Load patches
        System.out.println("[#] Loaded '" + PatchManager.load() + "' patches");
        safeBootstrap();
    }

    public static void safeBootstrap() {
        try {
            PowerTunnel.bootstrap();
        } catch (UnknownHostException ex) {
            System.out.println("[x] Cannot use IP-Address '" + SERVER_IP_ADDRESS + "': " + ex.getMessage());
            System.out.println("[!] Program halted");
        } catch (DataStoreException ex) {
            System.out.println("[x] Failed to load data store: " + ex.getMessage());
            ex.printStackTrace();
            ex.getMessage();
        }
    }

    /**
     * PowerTunnel bootstrap
     */
    public static void bootstrap() throws DataStoreException, UnknownHostException {
        //Load data
        try {
            GOVERNMENT_BLACKLIST.addAll(new DataStore(DataStore.GOVERNMENT_BLACKLIST).load());
            ISP_STUB_LIST.addAll(new DataStore(DataStore.ISP_STUB_LIST).load());
            System.out.println("[i] Loaded '" + GOVERNMENT_BLACKLIST.size() + "' government blocked sites");
            System.out.println();
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
        System.out.println("[.] Starting LittleProxy server on " + SERVER_IP_ADDRESS + ":" + SERVER_PORT);
        SERVER = DefaultHttpProxyServer.bootstrap().withFiltersSource(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                return new ProxyFilter(originalRequest);
            }
        }).withAddress(new InetSocketAddress(InetAddress.getByName(SERVER_IP_ADDRESS), SERVER_PORT))
                .withTransparent(true).start();
        RUNNING = true;
        System.out.println("[.] Server started");
        System.out.println();
    }

    /**
     * Stops LittleProxy server
     */
    public static void stopServer() {
        System.out.println();
        System.out.println("[.] Stopping server...");
        SERVER.stop();
        System.out.println("[.] Server stopped");
        System.out.println();
        RUNNING = false;
    }

    /**
     * Save data and goodbye
     */
    public static void stop() {
        stopServer();
        GOVERNMENT_BLACKLIST.clear();
        ISP_STUB_LIST.clear();
    }

    /**
     * Retrieve is LittleProxy server is running
     *
     * @return true if it is or false if it isn't
     */
    public static boolean isRunning() {
        return RUNNING;
    }

    /*
    Government blacklist block
     */

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
}