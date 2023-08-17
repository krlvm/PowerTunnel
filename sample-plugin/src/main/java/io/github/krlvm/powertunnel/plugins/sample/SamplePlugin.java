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

package io.github.krlvm.powertunnel.plugins.sample;

import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.http.ProxyResponse;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAdapter;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class SamplePlugin extends PowerTunnelPlugin {

    @Override
    public void onProxyInitialization(ProxyServer proxy) {
        proxy.setMITMEnabled(true);
        proxy.setFullResponse(true);
        proxy.setFullRequest(true);

        this.registerProxyListener(new ProxyAdapter() {
            @Override
            public void onClientToProxyRequest(@NotNull ProxyRequest request) {
                if (request.isEncrypted()) return;
                if (!request.getHost().equals("github.com")) return;

                final ProxyResponse response = getServer()
                        .getProxyServer()
                        .getResponseBuilder("PowerTunnel Test Plugin")
                        .header("X-PT-Test", "OK")
                        .build();
                request.setResponse(response);
            }

            @Override
            public void onProxyToClientResponse(@NotNull ProxyResponse response) {
                if (!proxy.isMITMEnabled()) return;
                if (!response.isDataPacket()) return;

                if (!"text/html; charset=utf-8".equals(response.headers().get("Content-Type"))) return;

                final byte[] raw = response.content();
                final String str = new String(raw, StandardCharsets.UTF_8);

                response.setContent(("<b>Injected response part</b>" + str).getBytes(StandardCharsets.UTF_8));
            }
        });
    }
}
