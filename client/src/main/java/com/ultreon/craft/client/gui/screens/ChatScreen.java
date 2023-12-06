package com.ultreon.craft.client.gui.screens;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.widget.ChatTextEntry;
import com.ultreon.craft.client.gui.widget.TextEntry;
import com.ultreon.craft.client.gui.widget.Widget;
import com.ultreon.craft.network.packets.c2s.C2SChatPacket;
import com.ultreon.craft.network.packets.c2s.C2SCommandPacket;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatScreen extends Screen {
    private static final Marker MARKER = MarkerFactory.getMarker("ChatScreen");
    private final String input;
    private TextEntry entry;
    private static final List<TextObject> MESSAGES = new ArrayList<>();

    public ChatScreen(String input) {
        super("");
        this.input = input;
    }

    public ChatScreen() {
        this("");
    }

    public static void addMessage(TextObject message) {
        ChatScreen.MESSAGES.add(0, message);

        UltracraftClient.LOGGER.info(ChatScreen.MARKER, "Received message: " + message.getText());

        if (ChatScreen.MESSAGES.size() > 100)
            ChatScreen.MESSAGES.remove(ChatScreen.MESSAGES.size() - 1);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.entry = (TextEntry) builder.add(new ChatTextEntry(this).bounds(() -> new Bounds(-1, this.getHeight() - 21, this.getWidth() + 2, 21)));
    }

    public Widget getEntry() {
        return this.entry;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        super.renderWidget(renderer, mouseX, mouseY, deltaTime);

        int y = this.getHeight() - 40;
        for (TextObject text : this.MESSAGES) {
            renderer.textLeft(text, 10, y);
            y -= this.font.lineHeight + 2;
        }
    }

    public void send() {
        var input = this.entry.getValue();
        if (input.startsWith("/")) {
            this.client.connection.send(new C2SCommandPacket(input.substring(1)));
            this.close();
            return;
        }

        this.client.connection.send(new C2SChatPacket(input));
        this.close();
    }

    private static class AWTColorTypeAdapter extends TypeAdapter<java.awt.Color> {
        @Override
        public void write(JsonWriter out, java.awt.Color value) throws IOException {
            out.value(value.getRGB());
        }

        @Override
        public java.awt.Color read(JsonReader in) throws IOException {
            return new java.awt.Color(in.nextInt(), true);
        }
    }

    private static class UltracraftColorTypeAdapter extends TypeAdapter<Color> {
        @Override
        public void write(JsonWriter out, Color value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public Color read(JsonReader in) throws IOException {
            return Color.hex(in.nextString());
        }
    }
}
