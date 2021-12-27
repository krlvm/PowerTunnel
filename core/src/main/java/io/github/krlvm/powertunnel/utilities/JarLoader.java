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

package io.github.krlvm.powertunnel.utilities;

import io.github.krlvm.powertunnel.callbacks.InputStreamConsumer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarLoader implements Closeable {

    private final JarFile jar;

    public JarLoader(File file) throws IOException {
        this.jar = new JarFile(file);
    }

    public void open(String entryName, InputStreamConsumer consumer) throws IOException {
        open(entryName, consumer, false);
    }

    public void open(String entryName, InputStreamConsumer consumer, boolean acceptNull) throws IOException {
        final JarEntry entry = this.jar.getJarEntry(entryName);
        if (entry == null) {
            if(!acceptNull) return;
            consumer.accept(null);
            return;
        }
        try(InputStream in = jar.getInputStream(entry)) {
            consumer.accept(in);
        }
    }

    public void close() throws IOException {
        this.jar.close();
    }


    public static void open(File file, String entry, InputStreamConsumer consumer) throws IOException {
        open(file, entry, consumer, false);
    }
    public static void open(File file, String entry, InputStreamConsumer consumer, boolean acceptNull) throws IOException {
        try(JarLoader loader = new JarLoader(file)) {
            loader.open(entry, consumer, acceptNull);
        }
    }
}
