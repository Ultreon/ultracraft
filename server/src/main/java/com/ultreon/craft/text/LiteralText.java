package com.ultreon.craft.text;

import com.ultreon.data.types.ListType;
import com.ultreon.data.types.MapType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LiteralText extends MutableText {
    private final @NotNull String text;

    LiteralText(@NotNull String text) {
        this.text = text;
    }

    public static LiteralText deserialize(MapType data) {
        String text = data.getString("text");
        LiteralText literal = new LiteralText(text);

        literal.style = TextStyle.deserialize(data.getMap("Style"));

        ListType<MapType> extrasData = data.getList("Extras");
        for (MapType extraData : extrasData.getValue()) {
            literal.extras.add(TextObject.deserialize(extraData));
        }

        return literal;
    }

    @Override
    public @NotNull String createString() {
        return this.text;
    }

    @Override
    public MapType serialize() {
        MapType data = new MapType();
        data.putString("type", "literal");
        data.putString("text", this.text);

        data.put("Style", this.style.serialize());

        ListType<MapType> extrasData = new ListType<>();
        for (TextObject extra : this.extras) {
            extrasData.add(extra.serialize());
        }
        data.put("Extras", extrasData);

        return data;
    }

    @Override
    public LiteralText style(Consumer<TextStyle> consumer) {
        return (LiteralText) super.style(consumer);
    }

    @Override
    public LiteralText copy() {
        var copy = this.extras.stream().map(TextObject::copy).collect(Collectors.toList());
        var literalText = new LiteralText(this.text);
        literalText.extras.addAll(copy);
        return literalText;
    }
}
