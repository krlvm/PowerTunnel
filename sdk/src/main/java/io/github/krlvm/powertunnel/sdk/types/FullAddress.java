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

package io.github.krlvm.powertunnel.sdk.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class FullAddress implements Cloneable {

    private final String host;
    private final int port;

    public FullAddress(@NotNull String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress resolve() throws UnknownHostException {
        return new InetSocketAddress(InetAddress.getByName(this.host), this.port);
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Nullable
    public static FullAddress fromString(String input, int defaultPort) {
        if(!input.contains(":")) return new FullAddress(input, defaultPort);

        final String[] arr = input.split(":");
        try {
            return new FullAddress(arr[0], Integer.parseInt(arr[1]));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Nullable
    public static FullAddress fromString(String input) {
        return fromString(input, -1);
    }
}
