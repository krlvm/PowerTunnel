package ru.krlvm.powertunnel.system.windows;

import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions; 
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;
 
public interface Wininet extends StdCallLibrary {

    Wininet INSTANCE = (Wininet)Native.loadLibrary("Wininet", Wininet.class,
            W32APIOptions.DEFAULT_OPTIONS);

    int INTERNET_OPTION_SETTINGS_CHANGED = 39;
    int INTERNET_OPTION_REFRESH = 37;

    boolean InternetSetOptionW(int unused, int dwOption, Pointer lpBuffer, int dwBufferLength);
}