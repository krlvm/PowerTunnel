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

package io.github.krlvm.powertunnel.managers;

import io.github.krlvm.powertunnel.sdk.proxy.ProxyCredentials;
import org.littleshoot.proxy.ProxyAuthenticator;

public class ProxyAuthenticationManager implements ProxyAuthenticator {

    private final String username;
    private final String password;

    public ProxyAuthenticationManager(ProxyCredentials credentials) {
        this(credentials.getUsername(), credentials.getPassword());
    }

    public ProxyAuthenticationManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean authenticate(String userName, String password) {
        return userName.equals(this.username) && password.equals(this.password);
    }

    @Override
    public String getRealm() {
        return null;
    }
}
