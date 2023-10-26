package com.ultreon.craft.util;

public class HexTable {
    public static void dumpHexTable(byte[] data) {
        int cols = 16; // Number of columns in the hex table.
        for (int i = 0; i < data.length; i++) {
            if (i % cols == 0) {
                System.out.printf("%04X: ", i);
            }
            System.out.printf("%02X ", data[i]);
            if (i % cols == (cols - 1) || i == data.length - 1) {
                System.out.println();
            }
        }
    }
}
