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

package io.github.krlvm.powertunnel.listener;

import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.proxy.DNSRequest;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import io.github.krlvm.powertunnel.sdk.types.FullAddress;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CoreProxyListener implements ProxyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreProxyListener.class);

    private final Map<ProxyListenerInfo, ProxyListener> proxyListeners;

    public CoreProxyListener(Map<ProxyListenerInfo, ProxyListener> proxyListeners) {
        this.proxyListeners = proxyListeners;
    }

    @Override
    public void onClientToProxyRequest(@NotNull ProxyRequest request) {
        callProxyListeners((ProxyListenerCallback.Void) listener -> listener.onClientToProxyRequest(request));
    }

    @Override
    public void onProxyToServerRequest(@NotNull ProxyRequest request) {
        callProxyListeners((ProxyListenerCallback.Void) listener -> listener.onProxyToServerRequest(request));
    }

    @Override
    public void onServerToProxyResponse(@NotNull ProxyResponse response) {
        callProxyListeners((ProxyListenerCallback.Void) listener -> listener.onServerToProxyResponse(response));
    }

    @Override
    public void onProxyToClientResponse(@NotNull ProxyResponse response) {
        callProxyListeners((ProxyListenerCallback.Void) listener -> listener.onProxyToClientResponse(response));
    }

    @Override
    public Boolean onResolutionRequest(@NotNull DNSRequest request) {
        final Object result = callProxyListeners(listener -> listener.onResolutionRequest(request), false);
        return ((Boolean) result);
    }

    @Override
    public Integer onGetChunkSize(final @NotNull FullAddress address) {
        final Object result = callProxyListeners(listener -> listener.onGetChunkSize(address));
        return result != null ? ((int) result) : 0;
    }

    @Override
    public Boolean isFullChunking(@NotNull FullAddress address) {
        final Object result = callProxyListeners(listener -> listener.isFullChunking(address));
        return result != null && ((boolean) result);
    }

    @Override
    public Boolean isMITMAllowed(@NotNull FullAddress address) {
        final Object result = callProxyListeners(listener -> listener.isMITMAllowed(address));
        return result == null || ((boolean) result);
    }

    @Override
    public Object onGetSNI(@NotNull String hostname) {
        final Object result = callProxyListeners(listener -> listener.onGetSNI(hostname), null, Void.TYPE);
        return result != Void.TYPE ? result : hostname;
    }

    private Object callProxyListeners(ProxyListenerCallback callback) {
        return callProxyListeners(callback, null);
    }

    private Object callProxyListeners(ProxyListenerCallback callback, Object errVal) {
        return callProxyListeners(callback, errVal, null);
    }

    private Object callProxyListeners(ProxyListenerCallback callback, Object errVal, Object defObj) {
        Object result = defObj;
        for (Map.Entry<ProxyListenerInfo, ProxyListener> entry : proxyListeners.entrySet()) {
            Object res;
            try {
                res = callback.call(entry.getValue());
            } catch (Exception ex) {
                LOGGER.error(
                        "An error occurred in ProxyListener of '{}' [{}, priority={}]: {}",
                        entry.getKey().getPluginInfo().getId(),
                        entry.getValue().getClass().getSimpleName(), entry.getKey().getPriority(),
                        ex.getMessage(),
                        ex
                );
                res = errVal;
            }
            if(res != defObj) result = res;
        }
        return result;
    }
}
