package com.ultreon.craft.client.gui.screens;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Bounds;
import com.ultreon.craft.client.gui.GuiBuilder;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.hud.ChatOverlay;
import com.ultreon.craft.client.gui.widget.ChatTextBox;
import com.ultreon.craft.network.packets.c2s.C2SChatPacket;
import com.ultreon.craft.network.packets.c2s.C2SCommandPacket;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatScreen extends Screen {
    private static final Marker MARKER = MarkerFactory.getMarker("ChatScreen");
    private final String input;
    private ChatTextBox entry;
    private static final List<TextObject> MESSAGES = new CopyOnWriteArrayList<>();
    private static final LongList MESSAGE_TIMESTAMPS = LongLists.synchronize(new LongArrayList());

    public ChatScreen(String input) {
        super("");
        this.input = input;
    }

    public ChatScreen() {
        this("");
    }

    public static void addMessage(TextObject message) {
        ChatScreen.MESSAGES.add(0, message);
        ChatScreen.MESSAGE_TIMESTAMPS.add(0, System.currentTimeMillis());

        UltracraftClient.LOGGER.info(ChatScreen.MARKER, "Received message: " + message.getText());

        if (ChatScreen.MESSAGES.size() > 100) {
            ChatScreen.MESSAGES.remove(ChatScreen.getMessages().size() - 1);
            ChatScreen.MESSAGE_TIMESTAMPS.removeLong(ChatScreen.getMessages().size());
        }
    }

    public static List<TextObject> getMessages() {
        return Collections.unmodifiableList(ChatScreen.MESSAGES);
    }

    public static LongList getMessageTimestamps() {
        return LongLists.unmodifiable(ChatScreen.MESSAGE_TIMESTAMPS);
    }

    @Override
    public void build(GuiBuilder builder) {
        this.entry = (ChatTextBox) builder.add(new ChatTextBox(this).bounds(() -> new Bounds(-1, this.getHeight() - 21, this.getWidth() + 2, 21)));
    }

    public ChatTextBox getEntry() {
        return (ChatTextBox) this.entry;
    }

    @Override
    public void renderWidget(@NotNull Renderer renderer, int mouseX, int mouseY, @IntRange(from = 0) float deltaTime) {
        ChatOverlay.renderChatOverlay(this.font, renderer, true);

        super.renderWidget(renderer, mouseX, mouseY, deltaTime);
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

    public void onTabComplete(String[] options) {
        this.entry.onTabComplete(options);
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
