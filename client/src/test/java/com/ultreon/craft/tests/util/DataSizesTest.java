package com.ultreon.craft.tests.util;

import com.ultreon.craft.util.DataSizes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DataSizesTest {
    @Test
    @DisplayName("Test Conversion")
    public void testConversion() {
        Assertions.assertEquals(1, DataSizes.convert(1024, DataSizes.Unit.KiB, DataSizes.Unit.MiB));
        Assertions.assertEquals(1024, DataSizes.convert(1, DataSizes.Unit.MiB, DataSizes.Unit.KiB));

        Assertions.assertEquals(1048576, DataSizes.convert(1024, DataSizes.Unit.MiB, DataSizes.Unit.KiB));

        Assertions.assertEquals(1, DataSizes.convert(1024, DataSizes.Unit.B, DataSizes.Unit.KiB));
        Assertions.assertEquals(1024, DataSizes.convert(1, DataSizes.Unit.KiB, DataSizes.Unit.B));

        // Large Sizes
        Assertions.assertEquals(1073741824, DataSizes.convert(1024, DataSizes.Unit.MiB, DataSizes.Unit.B));
    }

    @Test
    @DisplayName("Test Format")
    public void testFormat() {
        Assertions.assertEquals("1 B", DataSizes.format(1));
        Assertions.assertEquals("1.00 KiB", DataSizes.format(1024));
        Assertions.assertEquals("1.00 MiB", DataSizes.format(1024 * 1024));
        Assertions.assertEquals("1.00 GiB", DataSizes.format(1024 * 1024 * 1024));
        Assertions.assertEquals("1.00 TiB", DataSizes.format(1024 * 1024 * 1024 * 1024L));
        Assertions.assertEquals("1.00 PiB", DataSizes.format(1024 * 1024 * 1024 * 1024L * 1024L));
        Assertions.assertEquals("1.00 EiB", DataSizes.format(1024 * 1024 * 1024 * 1024L * 1024L * 1024L));

        Assertions.assertEquals("512 B", DataSizes.format(512));
        Assertions.assertEquals("512.00 KiB", DataSizes.format(512 * 1024));
        Assertions.assertEquals("512.00 MiB", DataSizes.format(512 * 1024 * 1024));
        Assertions.assertEquals("512.00 GiB", DataSizes.format(512 * 1024 * 1024 * 1024L));
        Assertions.assertEquals("512.00 TiB", DataSizes.format(512 * 1024 * 1024 * 1024L * 1024L));
        Assertions.assertEquals("512.00 PiB", DataSizes.format(512 * 1024 * 1024 * 1024L * 1024L * 1024L));

        Assertions.assertEquals("256 B", DataSizes.format(256));
        Assertions.assertEquals("1.50 KiB", DataSizes.format(512 + 1024));
        Assertions.assertEquals("1.50 MiB", DataSizes.format((512 + 1024) * 1024));
        Assertions.assertEquals("1.50 GiB", DataSizes.format((512 + 1024) * 1024 * 1024));
        Assertions.assertEquals("1.50 TiB", DataSizes.format((512 + 1024) * 1024 * 1024 * 1024L));
        Assertions.assertEquals("1.50 PiB", DataSizes.format((512 + 1024) * 1024 * 1024 * 1024L * 1024L));
        Assertions.assertEquals("1.50 EiB", DataSizes.format((512 + 1024) * 1024 * 1024 * 1024L * 1024L * 1024L));

        Assertions.assertThrows(IllegalArgumentException.class, () -> DataSizes.format(-1));
    }
}
