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

package io.github.krlvm.powertunnel;

import io.github.krlvm.powertunnel.http.LProxyResponse;
import io.github.krlvm.powertunnel.sdk.http.ProxyRequest;
import io.github.krlvm.powertunnel.sdk.plugin.PowerTunnelPlugin;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAdapter;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyAddress;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyServer;
import io.github.krlvm.powertunnel.sdk.proxy.ProxyStatus;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;

import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class TestBootstrap {

    public static void main(String[] args) {
        final Server server = new Server();
        server.registerPlugin(new TestPlugin());
        server.start();
    }

    public static class TestPlugin extends PowerTunnelPlugin {

        @Override
        public void onProxyInitialization(@NotNull ProxyServer proxy) {
            try {
                proxy.setAddress(new ProxyAddress("127.0.0.1", 8085));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            this.registerProxyListener(new ProxyAdapter() {
                @Override
                public void onClientToProxyRequest(@NotNull ProxyRequest request) {
                    System.out.println("onClientToProxyRequest");
                    String body = "PowerTunnel Test Plugin";
                    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                    ByteBuf content = Unpooled.copiedBuffer(bytes);
                    HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
                    response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
                    request.setResponse(new LProxyResponse(response));
                }
            });
        }

        @Override
        public void beforeProxyStatusChanged(@NotNull ProxyStatus status) {

        }

        @Override
        public void onProxyStatusChanged(@NotNull ProxyStatus status) {

        }
    }
}
