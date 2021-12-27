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

import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;

public class DNSRequest {

    private final String host;
    private final int port;

    private InetSocketAddress response;

    public DNSRequest(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }


    /**
     * Returns response
     * If there's no DNS Resolvers registered, returns null
     *
     * @return response
     */
    public @Nullable InetSocketAddress getResponse() {
        return response;
    }

    /**
     * Sets response
     * If response is set to null, the request will be resolved
     * using default DNS Resolver
     *
     * @param response
     */
    public void setResponse(@Nullable InetSocketAddress response) {
        this.response = response;
    }
}
