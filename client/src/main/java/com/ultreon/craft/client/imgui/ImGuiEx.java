package com.ultreon.craft.client.imgui;

import com.ultreon.libs.functions.v0.consumer.BooleanConsumer;
import com.ultreon.libs.functions.v0.consumer.ByteConsumer;
import com.ultreon.libs.functions.v0.consumer.DoubleConsumer;
import com.ultreon.libs.functions.v0.consumer.FloatConsumer;
import com.ultreon.libs.functions.v0.consumer.ShortConsumer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;
import java.util.function.Supplier;

import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImLong;
import imgui.type.ImShort;
import imgui.type.ImString;

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

    public static void editString(String label, String id, String value, Consumer<String> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImString i = new ImString(value);
        if (ImGui.inputText("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editByte(String label, String id, byte value, ByteConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImShort i = new ImShort(value);
        if (ImGui.inputScalar("##" + id, ImGuiDataType.U8, i)) {
            setter.accept((byte) i.get());
        }
    }

    public static void editShort(String label, String id, short value, ShortConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImShort i = new ImShort(value);
        if (ImGui.inputScalar("##" + id, ImGuiDataType.S16, i)) {
            setter.accept(i.get());
        }
    }

    public static void editInt(String label, String id, int value, IntConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImInt i = new ImInt(value);
        if (ImGui.inputInt("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editLong(String label, String id, long value, LongConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImLong i = new ImLong(value);
        if (ImGui.inputScalar("##" + id, ImGuiDataType.S64, i)) {
            setter.accept(i.get());
        }
    }

    public static void editFloat(String label, String id, float value, FloatConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImFloat i = new ImFloat(value);
        if (ImGui.inputFloat("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editDouble(String label, String id, double value, DoubleConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImDouble i = new ImDouble(value);
        if (ImGui.inputDouble("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editBool(String label, String id, boolean value, BooleanConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImBoolean i = new ImBoolean(value);
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
        int[] v = new int[]{value};
        if (ImGui.sliderInt("##" + id, v, min, max)) {
            onChange.accept(v[0]);
        }
    }
}