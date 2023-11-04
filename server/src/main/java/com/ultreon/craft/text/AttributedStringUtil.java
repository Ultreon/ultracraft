package com.ultreon.craft.text;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;

class AttributedStringUtil {

    public static AttributedString concat(AttributedString first, AttributedString second, String separation) {
        var firstString = AttributedStringUtil.getString(first);
        var secondString = AttributedStringUtil.getString(second);
        var resultString = firstString + separation + secondString;
        var result = new AttributedString(resultString);
        AttributedStringUtil.addAttributes(result, first, second, separation.length());
        return result;
    }

    public static AttributedString concat(AttributedString first, AttributedString second) {
        return AttributedStringUtil.concat(first, second, "");
    }

    private static void addAttributes(AttributedString result, AttributedString first, AttributedString second, int separationOffset) {
        var resultIterator = result.getIterator();
        var firstIterator = first.getIterator();
        var secondIterator = second.getIterator();

        var resultCharacter = resultIterator.current();
        var truePosition = 0;
        int usePosition;

        while (resultCharacter != CharacterIterator.DONE) {
            usePosition = truePosition;
            var it = AttributedStringUtil.getIterator(firstIterator, secondIterator);
            if (it == null) {
                break;
            }
            if (it == secondIterator) {
                usePosition += separationOffset;
            }
            result.addAttributes(it.getAttributes(), usePosition, usePosition + 1);
            resultCharacter = resultIterator.next();
            it.next();
            truePosition++;
        }
    }

    private static AttributedCharacterIterator getIterator(AttributedCharacterIterator firstIterator, AttributedCharacterIterator secondIterator) {
        if (firstIterator.current() != CharacterIterator.DONE) {
            return firstIterator;
        }
        if (secondIterator.current() != CharacterIterator.DONE) {
            return secondIterator;
        }
        return null;

    }

    public static String getString(AttributedString attributedString) {
        var it = attributedString.getIterator();
        var stringBuilder = new StringBuilder();

        var ch = it.current();
        while (ch != CharacterIterator.DONE) {
            stringBuilder.append(ch);
            ch = it.next();
        }
        return stringBuilder.toString();
    }
}