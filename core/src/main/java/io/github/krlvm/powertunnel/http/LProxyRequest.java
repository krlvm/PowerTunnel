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

package io.github.krlvm.powertunnel.http;

import io.github.krlvm.powertunnel.sdk.http.HttpMethod;
import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LProxyRequest extends LProxyMessage<HttpRequest> implements ProxyRequest {

    private ProxyResponse response;

    public LProxyRequest(HttpRequest request, FullAddress address) {
        super(request, address);
    }

    @Override
    public @NotNull HttpMethod getMethod() {
        return HttpMethod.valueOf(httpObject.method().name());
    }

    @Override
    public void setMethod(@NotNull HttpMethod method) {
        httpObject.setMethod(io.netty.handler.codec.http.HttpMethod.valueOf(method.name()));
    }

    @Override
    public void setMethod(@NotNull String method) {
        httpObject.setMethod(new io.netty.handler.codec.http.HttpMethod(method));
    }

    @Override
    public boolean isEncrypted() {
        return httpObject.method() == io.netty.handler.codec.http.HttpMethod.CONNECT;
    }

    @Override
    public void setBlocked(boolean blocked) {
        if (blocked) {
            setResponse(new LProxyResponse.Builder("Access denied by proxy server", 403)
                    .contentType("text/plain")
                    .build()
            );
        } else {
            this.response = null;
        }
    }

    @Override
    public boolean isBlocked() {
        return this.response != null;
    }

    @Override
    public @Nullable ProxyResponse getResponse() {
        return this.response;
    }

    @Override
    public void setResponse(@NotNull ProxyResponse response) {
        this.response = response;
    }

    @Override
    public void setRaw(@NotNull String raw) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String raw() {
        if(!(httpObject instanceof FullHttpRequest)) throw new IllegalStateException("Can't get raw content of HttpRequest chunk");
        return this.httpObject.toString();
    }

    @Override
    protected HttpHeaders getHeaders() {
        return httpObject.headers();
    }

    public HttpResponse getLittleProxyResponse() {
        if(this.response == null) return null;
        return ((LProxyResponse) this.response).getLittleProxyObject();
    }
}
