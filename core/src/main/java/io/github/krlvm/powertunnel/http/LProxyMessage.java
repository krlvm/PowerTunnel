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

import io.github.krlvm.powertunnel.sdk.http.HttpHeaders;
import io.github.krlvm.powertunnel.sdk.http.ProxyMessage;
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public abstract class LProxyMessage<T> implements ProxyMessage {

    private static final Logger LOGGER = LoggerFactory.getLogger(LProxyMessage.class);

    protected final T httpObject;
    protected HttpHeaders headers;

    protected final FullAddress address;

    protected LProxyMessage(T httpObject, FullAddress address) {
        this.httpObject = httpObject;
        this.address = address;
    }

    @Override
    public @Nullable FullAddress address() {
        return address;
    }

    @Override
    public @NotNull HttpHeaders headers() {
        if(headers == null) headers = new LHttpHeaders(this.getHeaders());
        return headers;
    }

    protected abstract io.netty.handler.codec.http.HttpHeaders getHeaders();

    public T getLittleProxyObject() {
        return httpObject;
    }


    // TODO: Support for setting HttpRequest content
    public static void setHttpObjectContent(HttpObject httpObject, byte[] bytes) {
        if(!(httpObject instanceof HttpMessage)) return;
        try {
            Field contentField;
            if (httpObject.getClass().getSimpleName().equals("DefaultHttpContent") || httpObject.getClass().getSimpleName().equals("DefaultFullHttpResponse")) {
                contentField = httpObject.getClass().getDeclaredField("content");
            } else {
                contentField = httpObject.getClass().getSuperclass().getDeclaredField("content");
            }
            contentField.setAccessible(true);
            contentField.set(httpObject, Unpooled.copiedBuffer(bytes));
            ((HttpMessage) httpObject).headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
        } catch (ReflectiveOperationException ex) {
            LOGGER.error("Failed to set HttpObject content: {}", ex.getMessage(), ex);
        }
    }
    public static void setHttpObjectContent(HttpObject httpObject, String content) {
        setHttpObjectContent(httpObject, content.getBytes(StandardCharsets.UTF_8));
    }
}
