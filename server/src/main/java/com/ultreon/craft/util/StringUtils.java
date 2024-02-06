package com.ultreon.craft.util;

import it.unimi.dsi.fastutil.chars.CharPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class StringUtils {

    public static String stripIndents(String trim) {
        int length = trim.length();
        if (length == 0) {
            return "";
        }
        char lastChar = trim.charAt(length - 1);
        boolean optOut = lastChar == '\n' || lastChar == '\r';
        List<String> lines = splitLines(trim);
        final int outdent = optOut ? 0 : outdent(lines);
        return lines.stream()
                .map(line -> {
                    int firstNonWhitespace = indexOfNonWhitespace(line);
                    int lastNonWhitespace = lastIndexOfNonWhitespace(line);
                    int incidentalWhitespace = Math.min(outdent, firstNonWhitespace);
                    return firstNonWhitespace > lastNonWhitespace
                            ? "" : line.substring(incidentalWhitespace, lastNonWhitespace);
                })
                .collect(Collectors.joining("\n", "", optOut ? "\n" : ""));
    }

    private static List<String> splitLines(String trim) {
        return splitBy(trim, "\r\n", "\r", "\n");
    }

    public static List<String> splitBy(String trim, String... delimiters) {
        List<String> lines = new ArrayList<>();
        int start = 0;
        while (start < trim.length()) {
            int end = indexOfAny(trim, start, delimiters);
            if (end == -1) {
                lines.add(trim.substring(start));
                break;
            }
            lines.add(trim.substring(start, end));
            start = end + 1;
        }
        return lines;
    }

    public static int indexOfAny(String line, int start, String... delimiters) {
        for (String delimiter : delimiters) {
            int index = line.indexOf(delimiter, start);
            if (index != -1) return index;
        }
        return -1;
    }

    private static int outdent(List<String> lines) {
        // Note: outdent is guaranteed to be zero or positive number.
        // If there isn't a non-blank line, then the last must be blank
        int outdent = Integer.MAX_VALUE;
        for (String line : lines) {
            int leadingWhitespace = indexOfNonWhitespace(line);
            if (leadingWhitespace != line.length()) {
                outdent = Integer.min(outdent, leadingWhitespace);
            }
        }
        String lastLine = lines.get(lines.size() - 1);
        if (lastLine.isBlank()) {
            outdent = Integer.min(outdent, lastLine.length());
        }
        return outdent;
    }

    public static int indexOfNonWhitespace(String line) {
        return indexOfFirst(line, ch -> !Character.isWhitespace(ch));
    }

    public static int lastIndexOfNonWhitespace(String line) {
        return indexOfLast(line, ch -> !Character.isWhitespace(ch));
    }

    public static int indexOfFirst(String line, CharPredicate predicate) {
        for (int i = 0; i < line.length(); i++) {
            if (!predicate.test(line.charAt(i))) return i;
        }
        return line.length();
    }

    public static int indexOfLast(String line, CharPredicate predicate) {
        for (int i = line.length() - 1; i >= 0; i--) {
            if (!predicate.test(line.charAt(i))) return i + 1;
        }
        return 0;
    }

    public static Stream<String> lines(String message) {
        return splitBy(message, "\r\n", "\r", "\n").stream();
    }
}
