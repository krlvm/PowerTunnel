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

package ru.krlvm.powertunnel.system;

import ru.krlvm.powertunnel.frames.LogFrame;
import ru.krlvm.powertunnel.utilities.Utility;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This class is used to mirror system outputs
 * to the log and back to the terminal (console)
 */
public class MirroredOutputStream extends FilterOutputStream {

    private final PrintStream SYSTEM_OUTPUT;

    public MirroredOutputStream(OutputStream out, PrintStream systemOutput) {
        super(out);
        SYSTEM_OUTPUT = systemOutput;
    }

    @Override
    public void write(byte[] b) throws IOException {
        String s = new String(b);
        SYSTEM_OUTPUT.write(b);
        writeToLog(s);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        String s = new String(b, off, len);
        SYSTEM_OUTPUT.write(b, off, len);
        writeToLog(s);
    }

    private void writeToLog(String s) {
        if(Utility.LOGGER != null && !s.startsWith(MIRROR_TAG)) {
            Utility.LOGGER.info(MIRROR_TAG + s);
        }
        LogFrame.print(s);
    }

    private static final String MIRROR_TAG = "[Mirrored] ";
}
