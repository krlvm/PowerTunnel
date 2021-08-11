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

import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class LProxyResponse extends LProxyMessage<HttpResponse> implements ProxyResponse {

    public LProxyResponse(HttpResponse response) {
        super(response);
    }

    @Override
    public int code() {
        return httpObject.status().code();
    }

    @Override
    public void setCode(int code) {
        httpObject.setStatus(HttpResponseStatus.valueOf(code));
    }

    @Override
    public void setRaw(@NotNull String raw) {
        LProxyMessage.setHttpObjectContent(httpObject, raw);
    }

    @Override
    public @NotNull String raw() {
        if(!(httpObject instanceof FullHttpResponse)) throw new IllegalStateException("Can't get raw content of HttpResponse chunk");
        return this.httpObject.toString();
    }

    @Override
    protected HttpHeaders getHeaders() {
        return httpObject.headers();
    }

    public static class Builder implements ProxyResponse.Builder {

        private final HttpResponse response;

        public Builder(String content) {
            this(HttpResponseStatus.OK, content);
        }

        public Builder(String content, int code) {
            this(HttpResponseStatus.valueOf(code), content);
        }

        public Builder(HttpResponseStatus status, String content) {
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, status,
                    Unpooled.copiedBuffer(content.getBytes(StandardCharsets.UTF_8))
            );
        }

        @Override
        public ProxyResponse.Builder code(int code) {
            response.setStatus(HttpResponseStatus.valueOf(code));
            return this;
        }

        @Override
        public ProxyResponse.Builder content(String content) {
            LProxyMessage.setHttpObjectContent(response, content);
            return this;
        }

        @Override
        public ProxyResponse.Builder header(String name, String value) {
            response.headers().set(name, value);
            return this;
        }

        @Override
        public ProxyResponse.Builder header(String name, int value) {
            response.headers().setInt(name, value);
            return this;
        }

        @Override
        public ProxyResponse.Builder header(String name, short value) {
            response.headers().setShort(name, value);
            return this;
        }

        @Override
        public ProxyResponse build() {
            return new LProxyResponse(response);
        }
    }
}
