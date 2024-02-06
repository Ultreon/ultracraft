package com.ultreon.craft.client.gui.debug;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.ultreon.craft.client.ClientRegistries;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;

public class DebugGui {
    private static final int OFFSET = 10;
    final UltracraftClient client;
    private int leftY;
    private int rightY;
    private int page;

    public DebugGui(UltracraftClient client) {
        this.client = client;
    }

    public void render(Renderer renderer) {
        this.leftY = DebugGui.OFFSET;
        this.rightY = DebugGui.OFFSET;

        if (!this.client.showDebugHud || this.client.world == null) return;

        this.renderPage(renderer);
    }

    private void renderPage(Renderer renderer) {
        DebugPage page = this.getPage();
        page.render(new DebugRenderContext() {
            @Override
            public DebugRenderContext left() {
                DebugGui.this.left();
                return this;
            }

            @Override
            public DebugRenderContext left(String text) {
                DebugGui.this.left(renderer, text);
                return this;
            }

            @Override
            public DebugRenderContext left(String key, Object value) {
                DebugGui.this.left(renderer, key, value);
                return this;
            }

            @Override
            public DebugRenderContext right() {
                DebugGui.this.right();
                return this;
            }

            @Override
            public DebugRenderContext right(String text) {
                DebugGui.this.right(renderer, text);
                return this;
            }

            @Override
            public DebugRenderContext right(String key, Object value) {
                DebugGui.this.right(renderer, key, value);
                return this;
            }

            @Override
            public DebugRenderContext entryLine(int idx, String name, long nanos) {
                DebugGui.this.entryLine(renderer, idx, name, nanos);
                return this;
            }

            @Override
            public DebugRenderContext entryLine(String name, String value) {
                DebugGui.this.entryLine(renderer, name, value);
                return this;
            }

            @Override
            public DebugRenderContext entryLine(int idx, String name) {
                DebugGui.this.entryLine(renderer, idx, name);
                return this;
            }

            @Override
            public DebugRenderContext entryLine(TextObject text) {
                DebugGui.this.entryLine(renderer, text);
                return this;
            }

            @Override
            public DebugRenderContext entryLine() {
                DebugGui.this.entryLine(renderer);
                return this;
            }

            @Override
            public UltracraftClient client() {
                return DebugGui.this.client;
            }
        });
    }

    public void nextPage() {
        var page = this.page + 1;
        if (!this.client.showDebugHud) {
            page = 0;
            this.client.showDebugHud = true;
        }
        if (page >= ClientRegistries.DEBUG_PAGE.values().size()) {
            this.client.showDebugHud = false;
        }

        this.page = page;
    }

    public void prevPage() {
        var page = this.page - 1;
        if (!this.client.showDebugHud) {
            page = ClientRegistries.DEBUG_PAGE.values().size() - 1;
            this.client.showDebugHud = true;
        }
        if (page < 0) {
            this.client.showDebugHud = false;
        }

        this.page = page;
    }

    private DebugPage getPage() {
        if (ClientRegistries.DEBUG_PAGE.entries().isEmpty()) {
            return DebugPage.EMPTY;
        }
        return ClientRegistries.DEBUG_PAGE.byId(this.page);
    }

    @CanIgnoreReturnValue
    public DebugGui left(Renderer renderer, String name, Object value) {
        MutableText textObject = TextObject.literal(name).append(": ").append(TextObject.literal(String.valueOf(value)).setColor(Color.LIGHT_GRAY));
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, width + 5, 11, Color.BLACK.withAlpha(128));
        renderer.textLeft(textObject, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui left(Renderer renderer, String text) {
        MutableText textObject = TextObject.literal(text).setBold(true).setUnderlined(true).setColor(Color.GOLD);
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, width + 5, 11, Color.BLACK.withAlpha(128));
        renderer.textLeft(textObject, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui left() {
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui right(Renderer renderer, String name, Object value) {
        MutableText textObject = TextObject.literal(name).append(": ").append(TextObject.literal(String.valueOf(value)).setColor(Color.LIGHT_GRAY));
        int width = renderer.getFont().width(textObject);
        int screenWidth = this.client.getScaledWidth();
        renderer.fill(screenWidth - DebugGui.OFFSET - 3 - width, this.rightY - 1, width + 5, 11, Color.BLACK.withAlpha(128));
        renderer.textRight(textObject, screenWidth - DebugGui.OFFSET, this.rightY);
        this.rightY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui right(Renderer renderer, String text) {
        MutableText textObject = TextObject.literal(text).setBold(true).setUnderlined(true).setColor(Color.GOLD);
        int width = renderer.getFont().width(textObject);
        renderer.fill(DebugGui.OFFSET - 2, this.rightY - 1, width + 5, 11, Color.BLACK.withAlpha(128));
        renderer.textRight(textObject, DebugGui.OFFSET, this.rightY);
        this.rightY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui right() {
        this.rightY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui entryLine(Renderer renderer, int idx, String name, long nanos) {
        MutableText lText = TextObject.literal("[" + idx + "] ").setColor(Color.GOLD).append(TextObject.literal(name).setColor(Color.WHITE));
        MutableText rText;
        if (nanos < 10000.0)
            rText = TextObject.literal("< 0.01").setColor(Color.LIGHT_GRAY)
                    .append(TextObject.literal(" ms").setColor(Color.rgb(0xa0a0a0)));
        else
            rText = TextObject.literal(String.format("%.2f", nanos / 1000000.0)).setColor(Color.LIGHT_GRAY)
                    .append(TextObject.literal(" ms").setColor(Color.rgb(0xa0a0a0)));
        int lWidth = renderer.getFont().width(lText);
        int rWidth = renderer.getFont().width(rText);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, Color.BLACK.withAlpha(128));
        renderer.textLeft(lText, DebugGui.OFFSET, this.leftY);
        renderer.textRight(rText, DebugGui.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    DebugGui entryLine(Renderer renderer, int idx, String name) {
        MutableText text = TextObject.literal("[" + idx + "] ").setColor(Color.GOLD).append(TextObject.literal(name).setColor(Color.WHITE));
        int width = renderer.getFont().width(text);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, Color.BLACK.withAlpha(128));
        renderer.textLeft(text, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    DebugGui entryLine(Renderer renderer, TextObject text) {
        int width = renderer.getFont().width(text);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(width + 2, 304), 11, Color.BLACK.withAlpha(128));
        renderer.textLeft(text, DebugGui.OFFSET, this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui entryLine(Renderer renderer, String name, String value) {
        MutableText lText = TextObject.literal(name).setColor(Color.WHITE);
        MutableText rText = TextObject.literal(value).setColor(Color.LIGHT_GRAY);
        int lWidth = renderer.getFont().width(lText);
        int rWidth = renderer.getFont().width(rText);
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, Math.max(lWidth + rWidth + 18, 304), 11, Color.BLACK.withAlpha(128));
        renderer.textLeft(lText, DebugGui.OFFSET, this.leftY);
        renderer.textRight(rText, DebugGui.OFFSET + Math.max(lWidth + rWidth + 16, 300), this.leftY);
        this.leftY += 11;
        return this;
    }

    @CanIgnoreReturnValue
    public DebugGui entryLine(Renderer renderer) {
        renderer.fill(DebugGui.OFFSET - 2, this.leftY - 1, 304, 11, Color.BLACK.withAlpha(128));
        this.leftY += 11;
        return this;
    }

    public void updateProfiler() {
        if (this.getPage() instanceof ProfilerDebugPage page) {
            var profiler = this.client.profiler;
            page.profile = profiler.collect();
        }
    }
}
