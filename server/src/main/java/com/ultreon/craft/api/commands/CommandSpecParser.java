package com.ultreon.craft.api.commands;

import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.ultreon.craft.server.util.Utils.reprChar;

/**
 * Command overload specification parser.
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
class CommandSpecParser {
    private final int line = 0;
    private @Nullable StringIO io = null;
    private boolean optional = false;
    private final Boolean commandPrefix;

    public CommandSpecParser() {
        this(true);
    }

    public CommandSpecParser(boolean commandPrefix) {
        this.commandPrefix = commandPrefix;
    }

    /**
     * Parses a command spec string into an object.<br></br>
     * <br></br>
     * Example Spec:
     * <p>
     * <pre>/kill &lt;entity&gt;</pre>
     * <pre>/kill all &lt;entity&gt; in current world</pre>
     * <pre>/kill all &lt;entity&gt; in world &lt;world&gt;</pre>
     *
     *
     * @throws SpecSyntaxException if the syntax of the command specification is invalid.
     * @param spec the command specification to parse into an object.
     * @return the result of the parse.
     */
    public CommandSpec parse(String spec) {
        this.io = new StringIO(spec);
        if (Objects.requireNonNull(this.io).isEOF()) {
            throw new SpecSyntaxException(this.line, Objects.requireNonNull(this.io).getOffset() + 1, "Empty command.");
        }
        if (this.commandPrefix) {
            this.checkChar('/');
        }
        final var commandName = this.readCommandName();
        if (Objects.requireNonNull(this.io).isEOF()) {
            return new CommandSpec(commandName, new ArrayList<>());
        }
        final List<CommandParameter> parameters = new ArrayList<>();
        while (!Objects.requireNonNull(this.io).isEOF()) {
            final var parameter = this.readArgument();
            parameters.add(parameter);
        }
        return new CommandSpec(commandName, parameters);
    }

    private CommandParameter readArgument() {
        final char first;
        try {
            char cur;
            do {
                cur = Objects.requireNonNull(this.io).readChar();
            } while (cur == ' ');
            first = cur;
        } catch (EOFException e) {
            throw new SpecSyntaxException(
                    this.line,
                    Objects.requireNonNull(this.io).getOffset(),
                    "Expected '[' or an argument type, got an EOF."
            );
        }
        final CharList lettersOuter = CharArrayList.wrap("abcdefghijklmnopqrstuvwxyz0123456789".toCharArray());
        final CharList lettersInner = CharArrayList.wrap("abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray());
        int startOffset;
        if (first == '[') {
            if (this.optional) {
                throw new SpecSyntaxException(
                        this.line,
                        Objects.requireNonNull(this.io).getOffset(),
                        "Expected an argument type, got " + reprChar(first) + "."
                );
            }
            this.optional = true;
            return this.readArgument();
        } else if (first == '<') {
            startOffset = -1;
            @Nullable CharArrayList tagChars = null;
            final CharArrayList typeChars;
            final CharArrayList commentIdChars;
            try {
                final char second;
                try {
                    second = this.io.readChar();
                } catch (EOFException e) {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Expected an argument type, got an EOF."
                    );
                }
                tagChars = CharArrayList.of(second);
                commentIdChars = CharArrayList.of();
                typeChars = CharArrayList.of();
                if (lettersOuter.contains(second)) {
                    char cur;
                    startOffset = Objects.requireNonNull(this.io).getOffset();
                    boolean isComment = false;
                    int commentTypeDepth = 0;
                    while (true) {
                        cur = Objects.requireNonNull(this.io).readChar();
                        if (cur == '>') {
                            break;
                        }
                        if (cur == ':') {
                            isComment = true;
                            continue;
                        }
                        if (!lettersInner.contains(cur)) {
                            if (cur == ']') {
                                throw new SpecSyntaxException(
                                        this.line,
                                        Objects.requireNonNull(this.io).getOffset(),
                                        "Cannot end an [optional context] without a start."
                                );
                            }
                            if (isComment) {
                                if (cur != '{' && cur != '}' && (cur != ' ' || commentTypeDepth <= 0)) {
                                    throw new SpecSyntaxException(
                                            this.line,
                                            Objects.requireNonNull(this.io).getOffset(),
                                            "Illegal character for comment: " + cur
                                    );
                                }
                                if (cur == '{') {
                                    commentTypeDepth++;
                                }
                                if (cur == '}') {
                                    if (commentTypeDepth <= 0) {
                                        throw new SpecSyntaxException(
                                                this.line,
                                                Objects.requireNonNull(this.io).getOffset(),
                                                "Cannot end an comment messageType without a start."
                                        );
                                    } else {
                                        commentTypeDepth--;
                                    }
                                }
                                if (commentTypeDepth >= 1) {
                                    typeChars.add(cur);
                                }
                            } else {
                                throw new SpecSyntaxException(
                                        this.line,
                                        Objects.requireNonNull(this.io).getOffset(),
                                        "Illegal character for argument type: " + cur
                                );
                            }
                        }
                        if (isComment) {
                            commentIdChars.add(cur);
                        } else {
                            tagChars.add(cur);
                        }
                    }
                    if (commentTypeDepth > 0) {
                        throw new SpecSyntaxException(
                                this.line,
                                Objects.requireNonNull(this.io).getOffset(),
                                "Expected '}', got " + reprChar(cur) + "."
                        );
                    }
                    if (tagChars.getChar(tagChars.size() - 1) == '>') {
                        tagChars.removeChar(tagChars.size() - 1);
                    }
                    if (!lettersOuter.contains(tagChars.getChar(tagChars.size() - 1))) {
                        throw new SpecSyntaxException(
                                this.line,
                                Objects.requireNonNull(this.io).getOffset(),
                                "Illegal character for argument type: " + cur
                        );
                    }
                    final var tag = new String(tagChars.toCharArray());
                    if (!CommandData.validateArgId(tag)) {
                        throw new SpecSyntaxException(
                                this.line,
                                startOffset + 1,
                                "Unknown argument type: " + tag
                        );
                    }
                    final CommandParser<?> parser = CommandData.getParser(tag);
                    final Class<?> type = CommandData.getType(tag);
                    final CommandTabCompleter completer = CommandData.getTabCompleter(tag);
                    if (this.optional) {
                        if (Objects.requireNonNull(this.io).readChar() == ']') {
                            this.optional = false;
                        }
                    }
                    final var comment = new String(commentIdChars.toCharArray());
                    return CommandParameter.ofArgType(parser, completer, type, tag, this.optional, comment);
                } else {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Illegal character for argument type: " + second
                    );
                }
            } catch (EOFException e) {
                if (startOffset == -1) {
                    throw new InternalError("Start offset is not set yet, Java probably teleported to another part of the code.");
                }
                if (this.optional) {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Expected ']', got an EOF."
                    );
                }
                final var tag = new String(Objects.requireNonNull(tagChars).toCharArray());
                if (!ArgumentType.getIdArgumentMap().containsKey(tag)) {
                    throw new SpecSyntaxException(
                            this.line,
                            startOffset + 1,
                            "Unknown argument type: " + tag
                    );
                }
                final CommandParser<?> parser = Objects.requireNonNull(CommandData.getParser(tag));
                final Class<?> type = Objects.requireNonNull(CommandData.getType(tag));
                CommandTabCompleter completer = CommandData.getTabCompleter(tag);
                if (completer == null) completer = ($1, $2, ctx, $3) -> {
                    ctx.readString();
                    return List.of();
                };
                return CommandParameter.ofArgType(parser, completer, type, tag, this.optional, null);
            }
        } else if (first == '(') {
            startOffset = -1;
            final List<CharList> argTypeIdCharsList = new ArrayList<>();
            try {
                final char second;
                try {
                    second = Objects.requireNonNull(this.io).readChar();
                } catch (EOFException e) {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Expected an argument type, got an EOF."
                    );
                }
                CharList argTypeIdChars = CharArrayList.of(second);
                if (lettersOuter.contains(second)) {
                    char cur;
                    startOffset = Objects.requireNonNull(this.io).getOffset();
                    while ((cur = Objects.requireNonNull(this.io).readChar()) !=')'){
                        if (!lettersInner.contains(cur)) {
                            if (cur == '|') {
                                argTypeIdCharsList.add(argTypeIdChars);
                                argTypeIdChars = new CharArrayList();
                                continue;
                            }
                            if (cur == ']') {
                                throw new SpecSyntaxException(
                                        this.line,
                                        Objects.requireNonNull(this.io).getOffset(),
                                        "Cannot end an [optional context] without a start."
                                );
                            }
                            throw new SpecSyntaxException(
                                    this.line,
                                    Objects.requireNonNull(this.io).getOffset(),
                                    "Illegal character for text: " + cur
                            );
                        }
                        argTypeIdChars.add(cur);
                    }
                    argTypeIdCharsList.add(argTypeIdChars);
                    final var characters = argTypeIdCharsList.get(argTypeIdCharsList.size() - 1);
                    if (characters.getChar(characters.size() - 1) == ')') {
                        characters.removeChar(characters.size() - 1);
                    }
                    if (!lettersOuter.contains(characters.getChar(argTypeIdCharsList.size() - 1))) {
                        throw new SpecSyntaxException(
                                this.line,
                                Objects.requireNonNull(this.io).getOffset(),
                                "Illegal character for argument type: " + cur
                        );
                    }
                    if (this.optional) {
                        if (Objects.requireNonNull(this.io).readChar() == ']') {
                            this.optional = false;
                        }
                    }
                    final List<String> strings = CommandSpecParser.collectTextArgList(argTypeIdCharsList);
                    return CommandParameter.ofText(this.optional, strings.toArray(new String[0]));
                } else {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Illegal character for argument type: " + second
                    );
                }
            } catch (EOFException e) {
                if (startOffset == -1) {
                    throw new InternalError("Start offset is not set yet, Java probably teleported to another part of the code.");
                }
                if (this.optional) {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Expected ']', got an EOF."
                    );
                }
                final List<String> strings = CommandSpecParser.collectTextArgList(argTypeIdCharsList);
                return CommandParameter.ofText(this.optional, strings.toArray(new String[0]));
            }
        } else if (lettersOuter.contains(first)) {
            final CharList argTypeIdChars = CharArrayList.of(first);
            startOffset = -1;
            try {
                char cur;
                startOffset = Objects.requireNonNull(this.io).getOffset();
                while ((cur = Objects.requireNonNull(this.io).readChar()) !=' '){
                    if (!lettersInner.contains(cur)) {
                        if (cur == ']') {
                            throw new SpecSyntaxException(
                                    this.line,
                                    Objects.requireNonNull(this.io).getOffset(),
                                    "Cannot end an [optional context] without a start."
                            );
                        }
                        throw new SpecSyntaxException(
                                this.line,
                                Objects.requireNonNull(this.io).getOffset(),
                                "Illegal character for text: " + cur
                        );
                    }
                    argTypeIdChars.add(cur);
                }
                if (!lettersOuter.contains(argTypeIdChars.getChar(argTypeIdChars.size() - 1))) {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Illegal character for text: " + cur
                    );
                }
                if (this.optional) {
                    if (Objects.requireNonNull(this.io).readChar() == ']') {
                        this.optional = false;
                    }
                }
                final var text = new String(argTypeIdChars.toCharArray());
                return CommandParameter.ofText(this.optional, text);
            } catch (EOFException e) {
                if (startOffset == -1) {
                    throw new InternalError("Start offset is not set yet, Java probably teleported to another part of the code.");
                }
                if (this.optional) {
                    throw new SpecSyntaxException(
                            this.line,
                            Objects.requireNonNull(this.io).getOffset(),
                            "Expected ']', got an EOF."
                    );
                }
                final var text = new String(argTypeIdChars.toCharArray());
                return CommandParameter.ofText(false, text);
            }
        } else if (this.optional) {
            throw new SpecSyntaxException(
                    this.line,
                    Objects.requireNonNull(this.io).getOffset(),
                    "Expected an argument type, got " + reprChar(first) + "."
            );
        } else {
            throw new SpecSyntaxException(
                    this.line,
                    Objects.requireNonNull(this.io).getOffset(),
                    "Expected '[' or an argument type, got " + reprChar(first) + "."
            );
        }
    }

    @NotNull
    private static List<String> collectTextArgList(List<CharList> argTypeIdCharsList) {
        final List<String> strings = new ArrayList<>();
        for (var argTypeIdChars1 : argTypeIdCharsList) {
            final var chars =argTypeIdChars1.toCharArray();
            final var s = new String(chars);
            strings.add(s);
        }
        return strings;
    }

    private String readCommandName() {
        final char first;
        try {
            first = Objects.requireNonNull(this.io).readChar();
        } catch (EOFException e) {
            throw new SpecSyntaxException(this.line, Objects.requireNonNull(this.io).getOffset(), "Expected an command name, got an EOF.");
        }
        final CharList letters = CharArrayList.wrap("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-_/".toCharArray());
        CharList commandNameChars = CharArrayList.of(first);
        try {
            char cur;
            while ((cur = Objects.requireNonNull(this.io).readChar()) != ' ') {
                if (!letters.contains(cur)) {
                    throw new SpecSyntaxException(this.line, Objects.requireNonNull(this.io).getOffset(), "Illegal character in command name: %s".formatted(reprChar(cur)));
                } else {
                    commandNameChars.add(cur);
                }
            }
            return new String(commandNameChars.toCharArray());
        } catch (EOFException e) {
            return new String(commandNameChars.toCharArray());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void checkChar(char expected) {
        try {
            this.checkChar(Objects.requireNonNull(this.io).readChar(), expected);
        } catch (EOFException e) {
            throw new SpecSyntaxException(
                this.line,
                Objects.requireNonNull(this.io).getOffset() + 1,
                "Expected '/', got an EOF."
            );
        }
    }

    private void checkChar(char got, char expected) {
        if (got != expected) {
            throw new SpecSyntaxException(
                this.line,
                Objects.requireNonNull(this.io).getOffset() + 1,
                "Expected '" + reprChar(expected) + ", got " + reprChar(got)
            );
        }
    }
}