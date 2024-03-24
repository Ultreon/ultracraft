package com.ultreon.craft.client;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public class Metadata {
    private static Metadata instance;

    @SerializedName("version")
    public String version;

    public static Metadata get() {
        return Metadata.instance;
    }

    static Metadata load() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(Metadata.class.getResourceAsStream("/metadata.json")))) {
            Metadata.instance = UltracraftClient.GSON.fromJson(reader, Metadata.class);
            return Metadata.instance;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
