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

package ru.krlvm.powertunnel.utilities;

/**
 * PowerTunnel Debugger
 *
 * @author krlvm
 */
public class Debugger {

    private static boolean DEBUG_ENABLED = false;

    /**
     * Enable or disable debug
     *
     * @param debug - true/false
     */
    public static void setDebug(boolean debug) {
        if(Debugger.DEBUG_ENABLED == debug) {
            return;
        }
        String state;
        if(!debug) {
            state = "disabled";
        } else {
            state = "enabled";
        }
        Utility.print("[Debug] Debug %s", state);
        Debugger.DEBUG_ENABLED = debug;
    }

    /**
     * Retrieve is debug enabled
     *
     * @return true if debug enabled or false if it isn't
     */
    public static boolean isDebug() {
        return DEBUG_ENABLED;
    }

    /**
     * Debug message
     *
     * @param message - message
     * @param args - arguments for formatting
     */
    public static void debug(String message, Object... args) {
        if(DEBUG_ENABLED) {
            Utility.print("[Debug] " + message, args);
        }
    }

    /**
     * Debug an exception - print stacktrace
     *
     * @param ex - exception
     */
    public static void debug(Exception ex) {
        if(DEBUG_ENABLED) {
            ex.printStackTrace();
        }
    }
}
