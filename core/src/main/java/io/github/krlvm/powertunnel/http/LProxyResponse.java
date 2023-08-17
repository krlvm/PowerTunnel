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
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.nio.charset.StandardCharsets;

public class LProxyResponse extends LProxyMessage<HttpResponse> implements ProxyResponse {

    public LProxyResponse(HttpResponse response, FullAddress address) {
        super(response, address);
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
    public boolean isDataPacket() {
        return httpObject instanceof FullHttpResponse;
    }

    @Override
    public byte[] content() {
        if(!isDataPacket()) throw new IllegalStateException("Can't get raw content of HttpResponse chunk");

        final ByteBuf buf = ((FullHttpResponse) httpObject).content();
        return ByteBufUtil.getBytes(buf, 0, buf.readableBytes(), false);
    }

    @Override
    public void setContent(byte[] content) {
        if(!isDataPacket()) throw new IllegalStateException("Can't set raw content of HttpResponse chunk");

        final HttpResponse response = new DefaultFullHttpResponse(
                httpObject.protocolVersion(),
                httpObject.status(),
                Unpooled.wrappedBuffer(content));
        response.headers().set(httpObject.headers());

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);

        httpObject = response;
    }

    @Override
    protected HttpHeaders getHeaders() {
        return httpObject.headers();
    }

    public static class Builder implements ProxyResponse.Builder {

        private final DefaultFullHttpResponse response;

        public Builder(String content) {
            this(HttpResponseStatus.OK, content);
        }

        public Builder(String content, int code) {
            this(HttpResponseStatus.valueOf(code), content);
        }

        public Builder(HttpResponseStatus status, String content) {
            final byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(bytes));
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        @Override
        public ProxyResponse.Builder code(int code) {
            response.setStatus(HttpResponseStatus.valueOf(code));
            return this;
        }

        @Override
        public ProxyResponse.Builder contentType(String contentType) {
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
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
            return new LProxyResponse(response, null);
        }
    }
}
