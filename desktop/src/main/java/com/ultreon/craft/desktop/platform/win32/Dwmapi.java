package com.ultreon.craft.desktop.platform.win32;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.system.Pointer;

public interface Dwmapi extends StdCallLibrary {
    Dwmapi INSTANCE = Native.load(Dwmapi.class);

    // ...

    WinNT.HRESULT DwmExtendFrameIntoClientArea(WinDef.HWND hWnd, MARGINS pMarInset);

    // ...

    int DwmIsCompositionEnabled(IntByReference pfEnabled);

    // ...

    int DwmSetIconicThumbnail(WinDef.HWND hwnd, WinDef.HBITMAP hbmp, WinDef.DWORD dwSITFlags);

    // ...

    int DwmSetWindowAttribute(WinDef.HWND hwnd, WinDef.DWORD dwAttribute, Pointer pvAttribute, WinDef.DWORD cbAttribute);

    // ...


    @Structure.FieldOrder({"cxLeftWidth", "cxRightWidth", "cyTopHeight", "cyBottomHeight"})
    public static class MARGINS extends Structure implements Structure.ByReference {

        public MARGINS(int cxLeftWidth, int cxRightWidth, int cyTopHeight, int cyBottomHeight) {
            this.cxLeftWidth = cxLeftWidth;
            this.cxRightWidth = cxRightWidth;
            this.cyTopHeight = cyTopHeight;
            this.cyBottomHeight = cyBottomHeight;
        }

        public int cxLeftWidth = 0;
        public int cxRightWidth = 0;
        public int cyTopHeight = 0;
        public int cyBottomHeight = 0;
    }
}