package com.ultreon.craft.premain.posix;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Posix API wrapper for the C library.
 * Used for setting the current working directory.
 */
@SuppressWarnings({"UnusedReturnValue"})
public interface CLibrary extends Library {
    CLibrary INSTANCE = Native.load("c", CLibrary.class);

    /**
     * Sets the current working directory to the specified path.
     * <p>
     * Original Posix API signature:
     * <pre>int chdir(const char *path);</pre>
     */
    int chdir( String path );
}