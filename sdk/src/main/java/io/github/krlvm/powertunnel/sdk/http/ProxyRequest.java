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

package io.github.krlvm.powertunnel.sdk.http;

import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProxyRequest extends ProxyMessage {

    @NotNull HttpMethod getMethod();
    void setMethod(@NotNull HttpMethod method);
    void setMethod(@NotNull String method);

    default boolean isEncrypted() {
        return getMethod() == HttpMethod.CONNECT;
    }

    default String getHost() {
        if (address() != null) {
            return address().getHost();
        } else if (isEncrypted() && getUri() != null) {
            return FullAddress.fromString(getUri()).getHost();
        } else if (headers().contains("Host")) {
            return headers().get("Host");
        }
        return null;
    }

    boolean isDataPacket();

    void setBlocked(boolean blocked);
    boolean isBlocked();

    @NotNull String getUri();
    void setUri(@NotNull String uri);

    @Nullable ProxyResponse getResponse();
    void setResponse(@NotNull ProxyResponse response);

    @NotNull
    void setContent(byte[] content);
}