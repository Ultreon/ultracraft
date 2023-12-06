package com.ultreon.craft.api.commands;

import java.io.EOFException;

public class StringIO {
    private final String string;
    private int offset = 0;

    public StringIO(String string) {
        this.string = string;
    }

    public char readChar() throws EOFException {
        if (this.offset >= this.string.length()) {
            throw new EOFException();
        }
        char c = this.string.charAt(this.offset);
        this.advance();
        return c;
    }

    public char[] readChars(int len) throws EOFException {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = this.readChar();
        }
        return chars;
    }

    public String readString(int len) throws EOFException {
        return new String(this.readChars(len));
    }

    public boolean isEOF() {
        return this.offset >= this.string.length();
    }

    public boolean isNextEOF() {
        return this.offset + 1 >= this.string.length();
    }

    private void advance() {
        this.offset++;
    }

    public int getOffset() {
        return this.offset;
    }
}