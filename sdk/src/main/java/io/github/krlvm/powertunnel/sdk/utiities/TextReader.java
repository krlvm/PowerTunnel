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

package io.github.krlvm.powertunnel.sdk.utiities;

import java.io.*;

public class TextReader {

    public static String read(File file) throws IOException {
        return read(new FileReader(file));
    }

    public static String read(InputStream in) throws IOException {
        return read(new InputStreamReader(in));
    }

    public static String read(Reader r) throws IOException {
        final StringBuilder builder = new StringBuilder();
        try(BufferedReader reader = new BufferedReader(r)) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        if(builder.length() > 2) {
            return builder.substring(0, builder.lastIndexOf("\n"));
        } else {
            return builder.toString();
        }
    }
}
