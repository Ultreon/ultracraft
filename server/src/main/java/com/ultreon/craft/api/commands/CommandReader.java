package com.ultreon.craft.api.commands;

import com.ultreon.craft.api.commands.selector.Selector;
import com.ultreon.craft.util.ElementID;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Pattern;

import static com.ultreon.craft.api.commands.CommandParseException.*;

public class CommandReader {
    private static final ThreadLocal<CommandReader> instance = new ThreadLocal<>();
    private static final char EOA = 0;
    private static final char SEP = ' ';
    private final CommandSender sender0;
    private final String cmd;
    private final String[] args;
    private String arg;
    private int cur = 0;
    private int off = 0;
    private int totalOff = 0;
    private final CharList hex = new CharArrayList("0123456789abcdefABCDEF".toCharArray());

    public CommandReader(CommandSender sender0, String cmd, String[] args) {
        this.sender0 = sender0;
        this.cmd = cmd;
        this.args = args;
        this.arg = args[this.cur];
        CommandReader.instance.set(this);
    }

    public static CommandReader get() {
        return CommandReader.instance.get();
    }

    public boolean isAtStartOfArg() {
        return this.off == 0;
    }

    public boolean isAtLastCharInArg() {
        return this.off == this.arg.length() - 1;
    }

    public boolean isAtEndOfArg() {
        return this.off == this.arg.length();
    }

    public boolean isAtStartEndOfArg() {
        return this.off == 0 && this.arg.isEmpty();
    }

    public boolean isAtHardEndOfArg() {
        return this.off == this.arg.length() + 1;
    }

    public boolean isAtLastCmd() {
        return this.cur + 1 == this.args.length;
    }

    public boolean isAtEndOfCmd() {
        return this.isAtHardEndOfArg() && this.isAtLastCmd();
    }

    public String readString() throws CommandParseException {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        return this.readArg();
    }

    public Selector readSelector() throws CommandParseException {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        return new Selector(this.readArg());
    }

    public short readByte() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readString0();
        try {
            return Byte.parseByte(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public short readByteHex() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!this.hex.contains(cur)) {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        String read = sb.toString();
        try {
            return (short) Integer.parseInt(read, 16);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public short readShort() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readString0();
        try {
            return Short.parseShort(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public short readShortHex() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!this.hex.contains(cur)) {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        String read = sb.toString();
        try {
            return (short) Integer.parseInt(read, 16);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public int readInt() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readString0();
        try {
            return Integer.parseInt(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public int readIntHex() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!this.hex.contains(cur)) {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        String read = sb.toString();
        try {
            return Integer.parseInt(read, 16);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public long readLong() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readString0();
        try {
            return Long.parseLong(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    @NotNull
    private String readString0() throws NotAtStartOfArg, EndOfArgument, NotADigit, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!Character.isDigit(cur)) {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        return sb.toString();
    }

    public long readLongHex() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!this.hex.contains(cur)) {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        String read = sb.toString();
        try {
            return Long.parseLong(read, 16);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public float readFloat() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readDecimalNumber();
        try {
            return Float.parseFloat(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    @NotNull
    private String readDecimalNumber() throws NotAtStartOfArg, EndOfArgument, NotADigit, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!Character.isDigit(cur) && cur != '.') {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        return sb.toString();
    }

    public double readDouble() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readDecimalNumber();
        try {
            return Double.parseDouble(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public BigInteger readBigInteger() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readString0();
        try {
            return new BigInteger(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public BigInteger readBigIntegerHex() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        if (!this.isAtStartOfArg()) {
            throw new NotAtStartOfArg(this.totalOff);
        }
        char cur;
        StringBuilder sb = new StringBuilder();
        while ((cur = this.read()) != CommandReader.EOA) {
            if (!this.hex.contains(cur)) {
                throw new NotADigit(cur, this.totalOff);
            }
            sb.append(cur);
        }
        this.tryNextArg();
        String read = sb.toString();
        try {
            return new BigInteger(read, 16);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public BigDecimal readBigDecimal() throws NotAtStartOfArg, NotADigit, NotANumber, EndOfArgument, NotAtEndOfArg {
        String read = this.readDecimalNumber();
        try {
            return new BigDecimal(read);
        } catch (NumberFormatException e) {
            throw new NotANumber(read, this.totalOff);
        }
    }

    public String readMessage() throws CommandParseException {
        StringBuilder sb = new StringBuilder();
        if (!this.isAtStartOfArg()) {
            throw new CommandParseException("Argument is not complete", this.totalOff);
        }
        while (!this.isAtEndOfCmd()) {
            sb.append(this.readArg());
            if (!this.isAtEndOfCmd()) {
                sb.append(CommandReader.SEP);
            }
        }
        return sb.toString();
    }

    public String readMessageUntil(char until, boolean force) throws CommandParseException {
        StringBuilder sb = new StringBuilder();
        if (!this.isAtEndOfArg()) {
            throw new CommandParseException("Argument is not complete", this.totalOff);
        }
        while (!this.isAtEndOfCmd()) {
            char c = this.readAdvance();
            if (c == until) {
                return sb.toString();
            }
            sb.append(c);
        }
        if (force) {
            throw new CommandParseException("Expected text until '" + this.repr(until) + "', but couldn't find that end.",
                    this.totalOff
            );
        }
        return sb.toString();
    }

    private String repr(char until) {
        return until == '\'' ? "\\'" : String.valueOf(until);
    }

    public String readRemaining() throws EndOfArgument, NotAtEndOfArg {
        if (this.isAtEndOfArg()) {
            this.read();
            this.tryNextArg();
            return "";
        }

        StringBuilder sb = new StringBuilder();
        char i;
        while ((i = this.read()) != CommandReader.EOA) {
            sb.append(i);
        }
        this.tryNextArg();
        return sb.toString();
    }

    public String readUntil(char until) throws EndOfArgument {
        if (this.isAtStartEndOfArg()) {
            this.read();
            return "";
        }

        StringBuilder sb = new StringBuilder();
        char i;
        while ((i = this.read()) != CommandReader.EOA && i != until) {
            sb.append(i);
        }
        return sb.toString();
    }

    public void back() throws CommandParseException {
        if (this.off == 0) throw new CommandParseException("Can't go back while already at beginning.");
        this.off--;
        this.totalOff--;
    }

    public char readChar() throws EndOfArgument {
        return this.read();
    }

    private char requireInCommand(char c) throws CommandParseException {
        if (c == CommandReader.EOA) {
            throw new CommandParseException("Expected a character, but got EOA.", this.totalOff);
        }
        return c;
    }

    private char read() throws EndOfArgument {
        if (this.isAtHardEndOfArg()) throw new EndOfArgument(this.totalOff);
        if (this.isAtEndOfArg()) {
            this.off++;
            this.totalOff++;
            return CommandReader.EOA;
        }
        char c = this.arg.charAt(this.off++);
        this.totalOff++;
        return c;
    }

    private char readAdvance() throws NotAtEndOfArg, EndOfCommand {
        if (this.isAtEndOfArg()) {
            this.nextArg();
            return CommandReader.SEP;
        }
        char c = this.arg.charAt(this.off++);
        this.totalOff++;
        return c;
    }

    private void nextArg() throws NotAtEndOfArg, EndOfCommand {
        if (!this.isAtEndOfArg() && !this.isAtHardEndOfArg()) {
            throw new NotAtEndOfArg(this.totalOff);
        }
        if (this.isAtLastCmd()) {
            throw new EndOfCommand(this.totalOff);
        }
        this.arg = this.args[++this.cur];
        this.off = 0;
    }

    public boolean tryNextArg() throws NotAtEndOfArg {
        if (!this.isAtHardEndOfArg()) {
            throw new NotAtEndOfArg(this.totalOff);
        }
        if (this.isAtLastCmd()) {
            return false;
        }
        this.arg = this.args[++this.cur];
        this.off = 0;
        return true;
    }

    public String[] nextSplit(String regex) throws CommandParseException {
        return Pattern.compile(regex).split(this.readArg());
    }

    private String readArg() throws CommandParseException {
        if (!this.isAtStartOfArg()) throw new NotAtStartOfArg(this.totalOff);
        return this.readRemaining();
    }

    public ElementID readId() throws CommandParseException {
        String text = this.readString();
        ElementID key = ElementID.tryParse(text);
        if (key == null) {
            throw new CommandParseException("Invalid key: " + text);
        }
        return key;
    }

    public boolean readBoolean() throws CommandParseException {
        String text = this.readString();
        return switch (text) {
            case "true", "on", "enabled", "yes" -> true;
            case "false", "off", "disabled", "no" -> false;
            default -> throw new CommandParseException("Invalid boolean: " + text);
        };
    }

    public int getOffset() {
        return Math.min(this.totalOff, this.getCommandlineArgs().length());
    }

    public int getCurrent() {
        return this.cur;
    }

    public CommandSender getSender() {
        return this.sender0;
    }

    public String getCommand() {
        return this.cmd;
    }

    public String getCommandline() {
        return this.cmd + " " + String.join(" ", this.args);
    }

    public String getCommandlineArgs() {
        return String.join(" ", this.args);
    }

    public String getArgument() {
        return this.arg;
    }

    public String[] getArguments() {
        return this.args;
    }
}