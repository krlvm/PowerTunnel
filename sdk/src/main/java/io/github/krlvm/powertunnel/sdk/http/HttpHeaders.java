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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface HttpHeaders {

    @Nullable String get(String name);
    void set(@NotNull String name, @NotNull String value);

    @Nullable Integer getInt(String name);
    void setInt(@NotNull String name, int value);

    @Nullable Short getShort(String name);
    void setShort(@NotNull String name, short value);

    @NotNull Set<String> names();
    boolean contains(@NotNull String name);
    void remove(@NotNull String name);

    boolean isEmpty();
    int size();
}
