package com.ultreon.craft.premain.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Windows API wrapper for the Kernel32 library.
 * Used for setting the current working directory.
 */
@SuppressWarnings("UnusedReturnValue")
public interface Kernel32 extends Library {
    /**
     * The Kernel32 instance.
     */
    Kernel32 INSTANCE = Native.load("Kernel32", Kernel32.class);

    /**
     * Sets the current working directory to the specified path.
     * <p>
     * Original Win32 API signature:
     * <pre>BOOL SetCurrentDirectory( LPCTSTR lpPathName );</pre>
     */
    int SetCurrentDirectoryW(char[] pathName);
}