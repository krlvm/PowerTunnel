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

package io.github.krlvm.powertunnel.desktop.system;

import java.io.Console;
import java.util.Scanner;

public interface ConsoleReader {

    String readLine();

    class JavaConsoleWrapper implements ConsoleReader {
        private final Console console;
        JavaConsoleWrapper(Console console) {
            this.console = console;
        }
        @Override
        public String readLine() {
            return console.readLine("> ");
        }
    }
    class BufferedConsoleWrapper implements ConsoleReader {
        private final Scanner scanner = new Scanner(System.in);
        @Override
        public String readLine() {
            return scanner.nextLine();
        }
    }

    static ConsoleReader get() {
        final Console console = System.console();
        return console != null ? new JavaConsoleWrapper(console) : new BufferedConsoleWrapper();
    }
}
