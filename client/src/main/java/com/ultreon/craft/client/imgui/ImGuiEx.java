package com.ultreon.craft.client.imgui;

import com.ultreon.craft.client.util.Color;
import com.ultreon.libs.commons.v0.util.EnumUtils;
import com.ultreon.libs.functions.v0.consumer.DoubleConsumer;
import com.ultreon.libs.functions.v0.consumer.*;
import com.ultreon.libs.functions.v0.supplier.FloatSupplier;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.*;

public class ImGuiEx {
    public static void text(String label, Supplier<Object> value) {
        ImGui.text(label);
        ImGui.sameLine();
        Object o;
        try {
            o = value.get();
        } catch (Throwable t) {
            o = "~@# " + t.getClass().getName() + " #@~";
        }
        ImGui.text(String.valueOf(o));
    }

    public static void editString(String label, String id, Supplier<String> value, Consumer<String> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImString i = new ImString(value.get(), 256);
            if (ImGui.inputText("##" + id, i, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept(i.get());
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editByte(String label, String id, byte value, ByteConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImShort i = new ImShort(value);
            if (ImGui.inputScalar("##" + id, ImGuiDataType.U8, i)) {
                setter.accept((byte) i.get());
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editShort(String label, String id, short value, ShortConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImShort i = new ImShort(value);
            if (ImGui.inputScalar("##" + id, ImGuiDataType.S16, i)) {
                setter.accept(i.get());
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editInt(String label, String id, IntSupplier value, IntConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImInt i = new ImInt(value.getAsInt());
            if (ImGui.inputInt("##" + id, i)) {
                setter.accept(i.get());
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editLong(String label, String id, LongSupplier value, LongConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImLong i = new ImLong(value.getAsLong());
            if (ImGui.inputScalar("##" + id, ImGuiDataType.S64, i)) {
                setter.accept(i.get());
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editFloat(String label, String id, FloatSupplier value, FloatConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImFloat i = new ImFloat(value.getFloat());
            if (ImGui.inputFloat("##" + id, i)) {
                setter.accept(i.get());
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editDouble(String label, String id, DoubleSupplier value, DoubleConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImDouble i = new ImDouble(value.getAsDouble());
        if (ImGui.inputDouble("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editBool(String label, String id, BooleanSupplier value, BooleanConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImBoolean i = new ImBoolean(value.getAsBoolean());
        if (ImGui.checkbox("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void bool(String label, BooleanSupplier value) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImGui.checkbox("##", value.getAsBoolean());
        } catch (Throwable t) {
            ImGui.text("~@# " + t.getClass().getName() + " #@~");
        }
    }

    public static void slider(String label, String id, int value, int min, int max, IntConsumer onChange) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            int[] v = new int[]{value};
            if (ImGui.sliderInt("##" + id, v, min, max)) {
                onChange.accept(v[0]);
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void button(String label, String id, Runnable func) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            if (ImGui.button("##" + id, 120, 16)) {
                func.run();
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editColor3(String color, String s, Supplier<@NotNull Color> getter, Consumer<@NotNull Color> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, 1f};
            if (ImGui.colorEdit3("##" + s, floats)) {
                setter.accept(new Color(floats[0], floats[1], floats[2], 1f));
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static void editColor4(String color, String s, Supplier<Color> getter, Consumer<Color> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f};
            if (ImGui.colorEdit4("##" + s, floats)) {
                setter.accept(new Color(floats[0], floats[1], floats[2], floats[3]));
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }

    public static <T extends Enum<T>> void editEnum(String s, String s1, Supplier<T> getter, Consumer<T> setter) {
        ImGui.text(s);
        ImGui.sameLine();
        try {
            T e = getter.get();
            ImInt index = new ImInt(e.ordinal());
            if (ImGui.combo("##" + s1, index, Arrays.stream(e.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new))) {
                setter.accept(EnumUtils.byOrdinal(index.get(), e));
            }
        } catch (Throwable t) {
            ImGui.text(String.valueOf(t));
        }
    }
}