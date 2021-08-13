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
import io.github.krlvm.powertunnel.sdk.proxy.ProxyListener;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
    public int onGetChunkSize(final @NotNull String hostname) {
        Object result = callProxyListeners(listener -> listener.onGetChunkSize(hostname));
        return result != null ? ((int) result) : 0;
    }

    @Override
    public boolean isFullChunking(@NotNull String hostname) {
        Object result = callProxyListeners(listener -> listener.isFullChunking(hostname));
        return result != null && ((boolean) result);
    }

    @Override
    public String onGetSNI(@NotNull String hostname) {
        final String sni = ((String) callProxyListeners(listener -> listener.onGetSNI(hostname)));
        return sni != null ? sni : hostname;
    }

    private Object callProxyListeners(ProxyListenerCallback callback) {
        Object result = null;
        for (Map.Entry<ProxyListenerInfo, ProxyListener> entry : proxyListeners.entrySet()) {
            try {
                result = callback.call(entry.getValue());
            } catch (Exception ex) {
                LOGGER.error(
                        "An error occurred in ProxyListener of '{}' [{}, priority={}]: {}",
                        entry.getKey().getPluginInfo().getId(),
                        entry.getValue().getClass().getSimpleName(), entry.getKey().getPriority(),
                        ex.getMessage(),
                        ex
                );
            }
        }
        return result;
    }
}
