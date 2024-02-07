package com.ultreon.craft.api.ubo;

import com.ultreon.craft.text.MutableText;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.Color;
import com.ultreon.data.types.IType;
import com.ultreon.data.types.MapType;
import com.ultreon.data.util.DataTypeVisitor;

import java.util.BitSet;
import java.util.List;

class UboFormatterVisitor implements DataTypeVisitor<TextObject> {
    private final int ident;

    public UboFormatterVisitor(int ident) {
        this.ident = ident;
    }

    @Override
    public TextObject visit(IType<?> type) {
        return switch (type.id()) {
            case Types.BYTE -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("b").setColor(Color.LIGHT_GRAY));
            case Types.SHORT -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("s").setColor(Color.LIGHT_GRAY));
            case Types.INT -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("i").setColor(Color.LIGHT_GRAY));
            case Types.LONG -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("l").setColor(Color.LIGHT_GRAY));
            case Types.FLOAT -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("f").setColor(Color.LIGHT_GRAY));
            case Types.DOUBLE -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("d").setColor(Color.LIGHT_GRAY));
            case Types.BIG_INT -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("I").setColor(Color.LIGHT_GRAY));
            case Types.BIG_DEC -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.CYAN).append(TextObject.literal("D").setColor(Color.LIGHT_GRAY));
            case Types.BOOLEAN -> TextObject.literal(String.valueOf(type.getValue())).setColor(Color.MAGENTA);
            case Types.STRING -> {
                String replace = String.valueOf(type.getValue())
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                        .replace("\\", "\\\\");
                yield TextObject.literal("\"" + replace + "\"").setColor(Color.GREEN);
            }
            case Types.CHAR -> {
                String replace = String.valueOf(type.getValue())
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                        .replace("\b", "\\b")
                        .replace("\f", "\\f")
                        .replace("\\", "\\\\");
                yield TextObject.literal("'" + replace + "'").setColor(Color.GREEN);
            }
            case Types.BIT_SET -> {
                MutableText result = TextObject.literal("x").setColor(Color.LIGHT_GRAY.brighter().brighter());
                for (int i = 0; i < ((BitSet) type.getValue()).length(); i++) {
                    result.append(TextObject.literal(String.valueOf(((BitSet) type.getValue()).get(i) ? 1 : 0)).setColor(Color.CYAN));
                }

                yield result;
            }
            case Types.UUID -> {
                String[] replace = String.valueOf(type.getValue()).split("-");

                MutableText result = TextObject.literal(replace[0]).setColor(Color.YELLOW);
                for (int i = 1; i < replace.length; i++) {
                    result.append(TextObject.literal("-").setColor(Color.WHITE));
                    result.append(TextObject.literal(replace[i]).setColor(Color.YELLOW));
                }

                yield MutableText.literal("<").setColor(Color.LIGHT_GRAY).append(result).append(TextObject.literal(">").setColor(Color.LIGHT_GRAY));
            }
            case Types.LIST -> {
                MutableText result = TextObject.literal("[").append(TextObject.literal("\n  ").setColor(Color.WHITE)).append(TextObject.literal("  ".repeat(ident))).setColor(Color.LIGHT_GRAY);
                for (int i = 0; i < ((List<?>) type.getValue()).size(); i++) {
                    result.append(UboFormatter.format((IType<?>) ((List<?>) type.getValue()).get(i), ident + 1));
                    if (i < ((List<?>) type.getValue()).size() - 1) {
                        result.append(TextObject.literal(",").append(TextObject.literal("\n  ").append(TextObject.literal("  ".repeat(ident)))).setColor(Color.WHITE));
                    }
                }
                result.append(TextObject.literal("\n").append(TextObject.literal("  ".repeat(ident))).append(TextObject.literal("]").setColor(Color.LIGHT_GRAY)));
                yield result;
            }
            case Types.MAP -> {
                MutableText result = TextObject.literal("{").append(TextObject.literal("\n  ").append(TextObject.literal("  ".repeat(ident))).setColor(Color.WHITE)).setColor(Color.LIGHT_GRAY);
                for (int i = 0; i < ((MapType)type).getValue().size(); i++) {
                    String s = ((String) ((MapType) type).getValue().keySet().toArray()[i])
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t")
                            .replace("\b", "\\b")
                            .replace("\f", "\\f")
                            .replace("\\", "\\\\");

                    result.append(TextObject.literal("\"" + s + "\"").setColor(Color.GRAY));
                    result.append(TextObject.literal(": ").setColor(Color.WHITE));
                    result.append(UboFormatter.format((IType<?>) ((MapType)type).getValue().values().toArray()[i], ident + 1));
                    if (i < ((MapType)type).getValue().size() - 1) {
                        result.append(TextObject.literal(",  ").append(TextObject.literal("\n  ").append(TextObject.literal("  ".repeat(ident))).setColor(Color.WHITE)).setColor(Color.WHITE));
                    }
                }
                result.append(TextObject.literal("\n").append(TextObject.literal("  ".repeat(ident))).append(TextObject.literal("}").setColor(Color.LIGHT_GRAY)));
                yield result;
            }
            case Types.BYTE_ARRAY -> {
                MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
                result.append(TextObject.literal("b").setColor(Color.YELLOW));
                result.append(TextObject.literal(";").setColor(Color.WHITE));
                for (int i = 0; i < ((byte[]) type.getValue()).length; i++) {
                    result.append(TextObject.literal(String.valueOf(((byte[]) type.getValue())[i])).setColor(Color.CYAN));
                    if (i < ((byte[]) type.getValue()).length - 1) {
                        result.append(TextObject.literal(", ").setColor(Color.WHITE));
                    }
                }

                result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

                yield result;
            }
            case Types.SHORT_ARRAY -> {
                MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
                result.append(TextObject.literal("s").setColor(Color.YELLOW));
                result.append(TextObject.literal(";").setColor(Color.WHITE));
                for (int i = 0; i < ((short[]) type.getValue()).length; i++) {
                    result.append(TextObject.literal(String.valueOf(((short[]) type.getValue())[i])).setColor(Color.CYAN));
                    if (i < ((short[]) type.getValue()).length - 1) {
                        result.append(TextObject.literal(", ").setColor(Color.WHITE));
                    }
                }

                result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

                yield result;
            }
            case Types.INT_ARRAY -> {
                MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
                result.append(TextObject.literal("i").setColor(Color.YELLOW));
                result.append(TextObject.literal(";").setColor(Color.WHITE));
                for (int i = 0; i < ((int[]) type.getValue()).length; i++) {
                    result.append(TextObject.literal(String.valueOf(((int[]) type.getValue())[i])).setColor(Color.CYAN));
                    if (i < ((int[]) type.getValue()).length - 1) {
                        result.append(TextObject.literal(", ").setColor(Color.WHITE));
                    }
                }

                result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

                yield result;
            }
            case Types.LONG_ARRAY -> {
                MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
                result.append(TextObject.literal("l").setColor(Color.YELLOW));
                result.append(TextObject.literal(";").setColor(Color.WHITE));
                for (int i = 0; i < ((long[]) type.getValue()).length; i++) {
                    result.append(TextObject.literal(String.valueOf(((long[]) type.getValue())[i])).setColor(Color.CYAN));
                    if (i < ((long[]) type.getValue()).length - 1) {
                        result.append(TextObject.literal(", ").setColor(Color.WHITE));
                    }
                }

                result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

                yield result;
            }
            case Types.FLOAT_ARRAY -> {
                MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
                result.append(TextObject.literal("f").setColor(Color.YELLOW));
                result.append(TextObject.literal(";").setColor(Color.WHITE));
                for (int i = 0; i < ((float[]) type.getValue()).length; i++) {
                    result.append(TextObject.literal(String.valueOf(((float[]) type.getValue())[i])).setColor(Color.CYAN));
                    if (i < ((float[]) type.getValue()).length - 1) {
                        result.append(TextObject.literal(", ").setColor(Color.WHITE));
                    }
                }

                result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

                yield result;
            }
            case Types.DOUBLE_ARRAY -> {
                MutableText result = TextObject.literal("(").setColor(Color.LIGHT_GRAY);
                result.append(TextObject.literal("d").setColor(Color.YELLOW));
                result.append(TextObject.literal(";").setColor(Color.WHITE));
                for (int i = 0; i < ((double[]) type.getValue()).length; i++) {
                    result.append(TextObject.literal(String.valueOf(((double[]) type.getValue())[i])).setColor(Color.CYAN));
                    if (i < ((double[]) type.getValue()).length - 1) {
                        result.append(TextObject.literal(", ").setColor(Color.WHITE));
                    }
                }

                result.append(TextObject.literal(")").setColor(Color.LIGHT_GRAY));

                yield result;
            }
            default -> TextObject.literal(String.valueOf(type.getValue()));
        };
    }
}
