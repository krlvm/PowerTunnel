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

package ru.krlvm.powertunnel.frames.journal;

import ru.krlvm.powertunnel.PowerTunnel;

public class BlacklistFrame extends UserListFrame {

    public BlacklistFrame() {
        super("Blacklist");
    }

    @Override
    protected void userActed(String address) {
        PowerTunnel.removeFromUserBlacklist(address);
    }

    @Override
    protected String[] getElements() {
        return PowerTunnel.getUserBlacklist().toArray(new String[0]);
    }
}
