package ru.krlvm.powertunnel.filter;

import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.HttpFiltersAdapter;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.HttpUtility;
import ru.krlvm.powertunnel.utilities.Utility;
import ru.krlvm.powertunnel.webui.PowerTunnelMonitor;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of LittleProxy filter
 *
 * @author krlvm
 */
public class ProxyFilter extends HttpFiltersAdapter {

    public ProxyFilter(HttpRequest originalRequest) {
        super(originalRequest);
        //Allow us to modify 'HOST' request header
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    /**
     * Filtering client to proxy request:
     * 1) Check if website is in the user whitelist - GOTO 3)
     * 2) Check if website is in the user blacklist - block request
     * 3) Check if website is in the government blacklist - if it's true goto 4)
     * 4) Try to circumvent DPI
     */
    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;

            if(PowerTunnel.isWebUIEnabled() && PowerTunnelMonitor.checkUri(request.getUri())) {
                Utility.print(" [i] Accepted Web UI connection");
                return PowerTunnelMonitor.getResponse(request.getUri());
            }
            if(!request.headers().contains("Host")) {
                Utility.print(" [i] Invalid packet received: Host header not found");
                return PowerTunnel.ALLOW_INVALID_HTTP_PACKETS ? null :
                        HttpUtility.getStub("Bad request");
            }
            String host = HttpUtility.formatHost(request.headers().get("Host"));

            PowerTunnel.addToJournal(host);
            Utility.print("[i] %s / %s", request.method(), host);

            if(!PowerTunnel.isUserWhitelisted(host) && PowerTunnel.isUserBlacklisted(host)) {
                Utility.print(" [!] Access denied by user: " + host);
                return HttpUtility.getStub("This website is blocked by user");
            }

            if(request.method() == HttpMethod.CONNECT && !PowerTunnel.APPLY_HTTP_TRICKS_TO_HTTPS) {
                return null;
            }
            if(PowerTunnel.isBlockedByGovernment(host)) {
                circumventDPI(request);
                Utility.print(" [+] Trying to bypass DPI: " + host);
            }
        }

        return null;
    }

    /**
     * There's no need in this when LittleProxy running in transparent mode
     *
    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) httpObject;
            if(request.headers().contains("Via")) {
                request.headers().remove("Via");
            }
        }
        return null;
    }*/

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof DefaultHttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) httpObject;
            //Utility.print("\n\n----------------------------\n" + request.toString() + "\n----------------------------\n\n");
            if(response.getStatus().code() == 302 && PowerTunnel.isIspStub(response.headers().get("Location"))) {
                Utility.print(" [!] Detected ISP 302-redirect to the stub");
                return HttpUtility.getStub("Thrown out ISP redirect to the stub");
            }
        }

        return httpObject;
    }

    /**
     * DPI circumvention algorithm for HTTP requests
     *
     * @param request - original HttpRequest
     */
    private static void circumventDPI(HttpRequest request) {
        String host = request.headers().get("Host");
        if(PowerTunnel.MIX_HOST_CASE) {
            if(PowerTunnel.COMPLETE_MIX_HOST_CASE) {
                StringBuilder modified = new StringBuilder();
                for (int i = 0; i < host.length(); i++) {
                    char c = host.toCharArray()[i];
                    if (i % 2 == 0) {
                        c = Character.toUpperCase(c);
                    }
                    modified.append(c);
                }
                host = modified.toString();
            } else {
                host = host.substring(0, host.length()-1) + host.substring(host.length()-1).toUpperCase();
            }
        }
        if(PowerTunnel.PAYLOAD_LENGTH > 0) {
            request.headers().remove("Host");
            for (int i = 0; i < PowerTunnel.PAYLOAD_LENGTH; i++) {
                request.headers().add("X-Padding" + i, PAYLOAD.get(i));
            }
        }
        if(request.method() != HttpMethod.CONNECT && PowerTunnel.isHTTPMethodTricksEnabled()) {
            String method = request.method().name();
            if(PowerTunnel.LINE_BREAK_BEFORE_GET) {
                method = "\r\n" + method;
            }
            if(PowerTunnel.ADDITIONAL_SPACE_AFTER_GET) {
                method = method + " ";
            }
            request.setMethod(new HttpMethod(method));
        }
        if(PowerTunnel.DOT_AFTER_HOST_HEADER) {
            host = host + ".";
            request.headers().remove("Host");
        }
        if(!request.headers().contains("Host")) {
            String hostHeader = PowerTunnel.MIX_HOST_HEADER_CASE ? "hOSt" : "Host";
            request.headers().add(hostHeader, host);
        }
    }

    public static final List<String> PAYLOAD = new LinkedList<>();
}
