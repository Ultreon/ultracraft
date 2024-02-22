package com.ultreon.craft.client.font;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.ultreon.craft.text.FormattedText;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class WordWrapper {
    private final BitmapFont bitmapFont;
    private final Font font;

    public WordWrapper(BitmapFont bitmapFont, Font font) {
        this.bitmapFont = bitmapFont;
        this.font = font;
    }

    public List<FormattedText> wrap(FormattedText text, int width) {
        List<FormattedText> lines = new ArrayList<>();
        var ctx = new SplitContext();

        for (FormattedText.TextFormatElement element : text.getElements()) {
            float elementWidth = font.width(element);

            pollSplit(text, width, ctx, lines);
            split(text, width, element, ctx, elementWidth, lines);
        }
        pollSplit(text, width, ctx, lines);

        FormattedText formattedText = new FormattedText(ctx.currentLine);
        lines.add(formattedText);

        return lines;
    }

    private void pollSplit(FormattedText text, int width, SplitContext ctx, List<FormattedText> lines) {
        FormattedText.TextFormatElement polled;
        while ((polled = ctx.queue.poll()) != null) {
            split(text, width, polled, ctx, font.width(polled), lines);
        }
    }

    private void split(FormattedText text, int width, FormattedText.TextFormatElement element, SplitContext ctx, float elementWidth, List<FormattedText> lines) {
        if (ctx.x + elementWidth <= width) {
            ctx.x += (int) elementWidth;
            ctx.currentLine.add(element);
            return;
        }

        String raw = element.text();
        for (int elemIdx = 0; elemIdx < raw.length(); elemIdx++) {
            ctx.idx = element.index() + elemIdx;
            if (checkSplit(text, width, element, ctx, lines, raw, elemIdx)) {
                return;
            }
        }

        ctx.currentLine.add(element);
    }

    private boolean checkSplit(FormattedText text, int width, FormattedText.TextFormatElement element, SplitContext ctx, List<FormattedText> lines, String raw, int elemIdx) {
        char c = raw.charAt(elemIdx);
        BitmapFont.BitmapFontData data = this.bitmapFont.getData();
        BitmapFont.Glyph glyph = data.getGlyph(c);

        if (glyph != null) ctx.x += glyph.xadvance + (element.style().isBold() ? 1 : 0);
        if (c == ' ') {
            ctx.x += (int) data.spaceXadvance;
            ctx.wrapElem = element;
            ctx.wrapElemIdx = ctx.idx + 1;
            var splitResult = element.split(ctx.idx + 1 - element.index());
            ctx.queue.addLast(splitResult[1]);
            ctx.currentLine.add(splitResult[0]);
            return false;
        }
        int idx = element.index();
        if (ctx.x <= width) {
            return false;
        }

        int wrapIdx = ctx.wrapElemIdx - element.index();
        FormattedText.TextFormatElement[] splitResult;
        if (wrapIdx == -1) wrapIdx = elemIdx == 0 ? 0 : elemIdx - 1;

        if (ctx.wrapElemIdx == ctx.lastWrapIdx) return false;

        if (wrapIdx == 0) return false;

        if (wrapIdx >= element.length()) {
            return false;
        }
        if (wrapIdx < 0) {
            return false;
        }

        splitResult = element.split(wrapIdx);
        ctx.queue.addLast(splitResult[1]);
        ctx.currentLine.add(splitResult[0]);
        FormattedText formattedText = new FormattedText(ctx.currentLine);
        ctx.currentLine = new ArrayList<>();

        lines.add(formattedText);
        ctx.x = 0;

        ctx.lastWrapIdx = wrapIdx;
        return true;
    }

    private static class SplitContext {
        int idx;
        int wrapElemIdx = -1;
        int lastWrapIdx = -1;
        int x = 0;
        FormattedText.TextFormatElement wrapElem = null;
        FormattedText.TextFormatElement lastElem = null;
        List<FormattedText.TextFormatElement> currentLine = new ArrayList<>();
        Deque<FormattedText.TextFormatElement> queue = new ArrayDeque<>();

        public int lastElemIdx() {
            if (lastElem != null) {
                return lastElem.index();
            }
            return -1;
        }
    }
}
