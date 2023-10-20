package com.ultreon.craft.premain.win32;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Kernel32 extends Library {
    public Kernel32 INSTANCE = Native.load("Kernel32", Kernel32.class);

    /** BOOL SetCurrentDirectory( LPCTSTR lpPathName ); */
    int SetCurrentDirectoryW(char[] pathName);
}