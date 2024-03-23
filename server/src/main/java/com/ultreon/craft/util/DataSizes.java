package com.ultreon.craft.util;

public class DataSizes {
    public static String format(long bytes) {
        if (bytes < 0) throw new IllegalArgumentException("Invalid bytes: " + bytes);
        else if (bytes < 1024) return bytes + " B";

        int index = (int) (Math.floor(Math.log(bytes) / Math.log(1024)));
        double value = bytes / Math.pow(1024, index);

        Unit unit = Unit.values()[index];
        if (unit == Unit.B) return String.format("%d %s", (int) value, unit);
        return String.format("%.2f %s", value, unit);
    }

    public enum Unit {
        B(1),
        KiB(B.scale * 1024),
        MiB(KiB.scale * 1024),
        GiB(MiB.scale * 1024),
        TiB(GiB.scale * 1024),
        PiB(TiB.scale * 1024),
        EiB(PiB.scale * 1024);

        private final long scale;

        Unit(long bytes) {
            this.scale = bytes;
        }
    }

    public static long convert(long bytes, Unit from, Unit to) {
        if (from == to) return bytes;
        if (from == Unit.B) return bytes / to.scale;
        if (to == Unit.B) return bytes * from.scale;

        return bytes * from.scale / to.scale;
    }
}
