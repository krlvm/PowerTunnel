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
