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

package io.github.krlvm.powertunnel.sdk.proxy;

import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import org.jetbrains.annotations.NotNull;

public interface ProxyListener {

    void onClientToProxyRequest(@NotNull ProxyRequest request);
    void onProxyToServerRequest(@NotNull ProxyRequest request);

    void onServerToProxyResponse(@NotNull ProxyResponse response);
    void onProxyToClientResponse(@NotNull ProxyResponse response);

    Boolean onResolutionRequest(@NotNull DNSRequest request);

    Integer onGetChunkSize(@NotNull FullAddress address);
    Boolean isFullChunking(@NotNull FullAddress address);

    Boolean isMITMAllowed(@NotNull FullAddress address);
    Object onGetSNI(@NotNull String hostname);

    int PRIORITY_HIGH   = -5;
    int PRIORITY_NORMAL =  0;
    int PRIORITY_LOW    =  5;
}
