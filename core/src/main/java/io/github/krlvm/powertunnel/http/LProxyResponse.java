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

import java.lang.reflect.Field;

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
        try {
            Field contentField;
            if (httpObject.getClass().getSimpleName().equals("DefaultHttpContent") || httpObject.getClass().getSimpleName().equals("DefaultFullHttpResponse")) {
                contentField = httpObject.getClass().getDeclaredField("content");
            } else {
                contentField = httpObject.getClass().getSuperclass().getDeclaredField("content");
            }
            boolean accessibility = contentField.isAccessible();
            contentField.setAccessible(true);
            contentField.set(httpObject, Unpooled.copiedBuffer(raw.getBytes()));
            contentField.setAccessible(accessibility);
            httpObject.headers().set(HttpHeaderNames.CONTENT_LENGTH, raw.getBytes().length);
        } catch (ReflectiveOperationException ex) {
            // TODO: Handle "Failed to set response content" error
            ex.printStackTrace();
        }
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
}
