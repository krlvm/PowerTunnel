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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class LHttpHeaders implements HttpHeaders {

    private final io.netty.handler.codec.http.HttpHeaders headers;

    public LHttpHeaders(io.netty.handler.codec.http.HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public @Nullable String get(String name) {
        return headers.get(name);
    }

    @Override
    public void set(@NotNull String name, @NotNull String value) {
        headers.set(name, value);
    }

    @Override
    public @Nullable Integer getInt(String name) {
        return headers.getInt(name);
    }

    @Override
    public void setInt(@NotNull String name, int value) {
        headers.setInt(name, value);
    }

    @Override
    public @Nullable Short getShort(String name) {
        return headers.getShort(name);
    }

    @Override
    public void setShort(@NotNull String name, short value) {
        headers.setShort(name, value);
    }

    @Override
    public @NotNull Set<String> names() {
        return headers.names();
    }

    @Override
    public boolean contains(@NotNull String name) {
        return headers.contains(name);
    }

    @Override
    public void remove(@NotNull String name) {
        headers.remove(name);
    }

    @Override
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    @Override
    public int size() {
        return headers.size();
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
