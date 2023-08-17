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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LProxyMessage<T> implements ProxyMessage {

    protected T httpObject;
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
}
