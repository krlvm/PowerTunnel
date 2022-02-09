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

import io.github.krlvm.powertunnel.sdk.utiities.Base64Compat;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class ProxyCredentials {

    private final String username;
    private final String password;

    public ProxyCredentials(@NotNull String username, @NotNull String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String toAuthorizationCode() {
        final String credential = this.username + ":" + this.password;
        final byte[] data = credential.getBytes(StandardCharsets.UTF_8);
        return Base64Compat.encodeToString(data).trim();
    }
}
