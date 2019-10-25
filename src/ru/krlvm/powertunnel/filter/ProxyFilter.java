package ru.krlvm.powertunnel.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFiltersAdapter;
import ru.krlvm.powertunnel.PowerTunnel;
import ru.krlvm.powertunnel.utilities.HttpUtility;
import ru.krlvm.powertunnel.utilities.Utility;

/**
 * Implementation of LittleProxy filter
 *
 * @author krlvm
 */
public class ProxyFilter extends HttpFiltersAdapter {

    public ProxyFilter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }

    public ProxyFilter(HttpRequest originalRequest) {
        super(originalRequest);
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
            String host = HttpUtility.formatHost(request.headers().get("Host"));
            PowerTunnel.addToJournal(host);
            Utility.print("[i] %s / %s", request.getMethod(), host);

            if(!PowerTunnel.isUserWhitelisted(host) && PowerTunnel.isUserBlacklisted(host)) {
                Utility.print(" [!] Access denied by user: " + host);
                return HttpUtility.getStub("This website blocked by user");
            }

            if(PowerTunnel.isBlockedByGovernment(host)) {
                circumventDPI(request);
            }
        }

        return null;
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (httpObject instanceof DefaultHttpResponse) {
            DefaultHttpResponse response = (DefaultHttpResponse) httpObject;
            if(response.getStatus().code() == 302 && PowerTunnel.isIspStub(response.headers().get("Location"))) {
                Utility.print(" [!] Detected ISP 302-redirect to the stub");
                return HttpUtility.getStub("Thrown out ISP redirect to the stub");
            }
        }

        return httpObject;
    }

    /**
     * DPI circumvention algorithm
     *
     * @param request - original HttpRequest
     */
    private static void circumventDPI(HttpRequest request) {
        String host = request.headers().get("Host");
        request.headers().remove("Host");
        request.headers().add("hOSt", host + ".");
        Utility.print(" [+] Trying to bypass DPI: " + host);
    }
}
