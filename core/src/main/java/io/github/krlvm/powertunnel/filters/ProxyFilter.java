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

package io.github.krlvm.powertunnel.filters;

import io.github.krlvm.powertunnel.http.LProxyRequest;
import io.github.krlvm.powertunnel.http.LProxyResponse;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.littleshoot.proxy.HttpFiltersAdapter;

public class ProxyFilter extends HttpFiltersAdapter {

    private final ProxyListener listener;
    private FullAddress address;

    public ProxyFilter(ProxyListener listener, HttpRequest originalRequest) {
        this(listener, originalRequest, null);
    }

    public ProxyFilter(ProxyListener listener, HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
        this.listener = listener;
    }

    @Override
    public void saveAddress(String hostAndPort, boolean isCONNECT) {
        this.address = FullAddress.fromString(hostAndPort, isCONNECT ? 443 : 80);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if(!(httpObject instanceof HttpRequest)) return null;
        LProxyRequest req = new LProxyRequest(((HttpRequest) httpObject), address);
        listener.onClientToProxyRequest(req);
        return req.getLittleProxyResponse();
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        if(!(httpObject instanceof HttpRequest)) return null;
        LProxyRequest req = new LProxyRequest(((HttpRequest) httpObject), address);
        listener.onProxyToServerRequest(req);
        return req.getLittleProxyResponse();
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if(!(httpObject instanceof HttpResponse)) return httpObject;
        LProxyResponse res = new LProxyResponse(((HttpResponse) httpObject), address);
        listener.onServerToProxyResponse(res);
        return res.getLittleProxyObject();
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        if(!(httpObject instanceof HttpResponse)) return httpObject;
        LProxyResponse res = new LProxyResponse(((HttpResponse) httpObject), address);
        listener.onProxyToClientResponse(res);
        return res.getLittleProxyObject();
    }

    @Override
    public int chunkSize() {
        return listener.onGetChunkSize(address);
    }

    @Override
    public boolean fullChunking() {
        return listener.isFullChunking(address);
    }

    @Override
    public String mitmGetSNI(String hostname) {
        return (String)listener.onGetSNI(hostname);
    }

    @Override
    public boolean proxyToServerAllowMitm() {
        return listener.isMITMAllowed(address);
    }

    static {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }
}
