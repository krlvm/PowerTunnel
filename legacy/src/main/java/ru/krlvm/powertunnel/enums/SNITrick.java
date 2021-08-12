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

package ru.krlvm.powertunnel.enums;

public enum SNITrick {

    ERASE,
    SPOIL,
    FAKE;

    public static SNITrick fromID(int id) {
        switch (id) {
            case 1: {
                return SPOIL;
            }
            case 2: {
                return ERASE;
            }
            case 3: {
                return FAKE;
            }
            default: {
                return null;
            }
        }
    }

    public static final String SUPPORT_REFERENCE = "https://github.com/krlvm/PowerTunnel/wiki/SNI-Tricks";
}