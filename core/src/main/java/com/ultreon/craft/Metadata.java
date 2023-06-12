package com.ultreon.craft;

import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;

public class Metadata {
    public static final Metadata INSTANCE;

    static {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(Metadata.class.getResourceAsStream("/metadata.json")))) {
            INSTANCE = UltreonCraft.GSON.fromJson(reader, Metadata.class);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @SerializedName("version")
    public String version;
}
