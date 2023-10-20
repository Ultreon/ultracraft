package com.ultreon.craft.premain.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface CLibrary extends Library {
    CLibrary INSTANCE = Native.load("c", CLibrary.class);

    /** int chdir(const char *path); */
    int chdir( String path );
}