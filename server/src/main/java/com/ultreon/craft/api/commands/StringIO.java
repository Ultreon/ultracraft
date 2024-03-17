package com.ultreon.craft.api.commands;

import java.io.EOFException;

/**
 * This class represents a simple String input/output stream.
 * Based ont the idea of Python's StringIO.
 */
public class StringIO {
    private final String string;
    private int offset = 0;

    /**
     * Constructs a StringIO object with the given string.
     *
     * @param string The input string
     */
    public StringIO(String string) {
        this.string = string;
    }

    /**
     * Reads a single character from the input stream.
     *
     * @return The next character
     * @throws EOFException if the end of the stream is reached
     */
    public char readChar() throws EOFException {
        if (this.offset >= this.string.length()) {
            throw new EOFException();
        }
        char c = this.string.charAt(this.offset);
        this.advance();
        return c;
    }

    /**
     * Reads an array of characters from the input stream.
     *
     * @param len The number of characters to read
     * @return An array of characters
     * @throws EOFException if the end of the stream is reached
     */
    public char[] readChars(int len) throws EOFException {
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = this.readChar();
        }
        return chars;
    }

    /**
     * Reads a string of specified length from the input stream.
     *
     * @param len The length of the string to read
     * @return The next string of specified length
     * @throws EOFException if the end of the stream is reached
     */
    public String readString(int len) throws EOFException {
        return new String(this.readChars(len));
    }

    /**
     * Checks if the end of the stream is reached.
     *
     * @return true if the end of the stream is reached, false otherwise
     */
    public boolean isEOF() {
        return this.offset >= this.string.length();
    }

    /**
     * Checks if the next position is at the end of the stream.
     *
     * @return true if the next position is at the end of the stream, false otherwise
     */
    public boolean isNextEOF() {
        return this.offset + 1 >= this.string.length();
    }

    private void advance() {
        this.offset++;
    }

    /**
     * Gets the current offset position in the stream.
     *
     * @return The current offset position
     */
    public int getOffset() {
        return this.offset;
    }
}