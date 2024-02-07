package com.ultreon.craft.text;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.chars.CharList;

import java.util.*;

public class FormattedText {
    private final Map<Integer, TextFormatElement> indexedElements = Maps.newHashMap();
    private List<TextFormatElement> elements = new ArrayList<>();
    private int length;

    public FormattedText(List<TextFormatElement> elements) {
        this.elements = elements;
        for (TextFormatElement element : elements) {
            this.indexedElements.put(length, element);
            this.length += element.length();
        }
    }

    private FormattedText(TextStyle style, CharList text) {
        TextFormatElement element = new TextFormatElement(style, new String(text.toCharArray()), 0);
        this.elements.add(element);
        this.indexedElements.put(0, element);
        this.length = text.size();
    }

    private FormattedText(TextStyle style, String text) {
        TextFormatElement element = new TextFormatElement(style, text, 0);
        this.elements.add(element);
        this.indexedElements.put(0, element);
        this.length = text.length();
    }

    private FormattedText(String text) {
        this(new TextStyle(), text);
    }

    private FormattedText() {

    }

    public static FormattedText from(String text) {
        return new FormattedText(text);
    }

    public TextFormatElement getNextElement(int index) {
        for (int i = index + 1; i < length; i++) {
            TextFormatElement element = indexedElements.get(i);
            if (element != null) return element;
        }
        return null;
    }

    public TextFormatElement getPreviousElement(int index) {
        for (int i = index - 1; i >= 0; i--) {
            TextFormatElement element = indexedElements.get(i);
            if (element != null) return element;
        }
        return null;
    }

    public static FormattedText from(MutableText text) {
        List<TextFormatElement> elements = new ArrayList<>();
        int x = 0;
        for (TextObject object : text) {
            String string = object.createString();
            elements.add(new TextFormatElement(object.getStyle(), string, x));
            x += string.length();
        }
        return new FormattedText(elements);
    }

    public static FormattedText from(TextObject text) {
        if (text instanceof MutableText) return from((MutableText) text);
        else return new FormattedText(text.getStyle(), text.toString());
    }

    public List<TextFormatElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public int indexOf(String text) {
        int total = 0;

        for (TextFormatElement element : this.elements) {
            int index = element.indexOf(text);
            if (index != -1) {
                return total + index;
            }
            total += element.length();
        }

        return -1;
    }

    public Iterable<TextFormatElement> getElementsFrom(int index) {
        return () -> new ElementRangeView(index, length);
    }

    public Iterable<TextFormatElement> getElementsWithin(int from, int to) {
        return () -> new ElementRangeView(from, to);
    }

    public Iterable<TextFormatElement> getElementsFromTo(int from, int to) {
        return () -> new ElementRangeView(from, to);
    }

    public int indexOf(String text, int start) {
        int total = 0;

        for (TextFormatElement element : this.elements) {
            if (total + element.length() < start) {
                total += element.length();
                continue;
            }

            int index = element.indexOf(text);
            if (index != -1 && index >= start) {
                return total + index;
            }
            total += element.length();
        }

        return -1;
    }

    public int indexOf(String text, int start, int end) {
        int total = 0;

        for (TextFormatElement element : this.elements) {
            if (total + element.length() < start) {
                total += element.length();
                continue;
            }

            if (total + element.length() > end) {
                return -1;
            }

            int index = element.indexOf(text);
            if (index != -1 && index >= start && index <= end) {
                return total + index;
            }
            total += element.length();
        }

        return -1;
    }

    public int lastIndexOf(String text) {
        int total = length - 1;

        for (int i = elements.size() - 1; i >= 0; i--) {
            TextFormatElement element = elements.get(i);
            int index = element.indexOf(text);
            if (index != -1) {
                return total - index;
            }
            total -= element.length();
        }

        return -1;
    }

    public int lastIndexOf(String text, int end) {
        int total = length - 1;

        for (int i = elements.size() - 1; i >= 0; i--) {
            TextFormatElement element = elements.get(i);
            if (total - element.length() < end) {
                total -= element.length();
                continue;
            }

            int index = element.indexOf(text);
            if (index != -1 && index <= end) {
                return total - index;
            }
            total -= element.length();
        }

        return -1;
    }

    public int lastIndexOf(String text, int start, int end) {
        int total = length - 1;

        for (int i = elements.size() - 1; i >= 0; i--) {
            TextFormatElement element = elements.get(i);
            if (total - element.length() < start) {
                total -= element.length();
                continue;
            }

            if (total - element.length() > end) {
                return -1;
            }

            int index = element.indexOf(text);
            if (index != -1 && index >= start && index <= end) {
                return total - index;
            }
            total -= element.length();
        }

        return -1;
    }

    public int length() {
        return length;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (TextFormatElement element : this.elements) {
            builder.append(element.text);
        }
        return builder.toString();
    }

    public String getText() {
        return this.toString();
    }

    public FormattedText copy() {
        return new FormattedText(this.elements.stream().map(TextFormatElement::copy).toList());
    }

    public int indexOf(TextFormatElement element) {
        return element.index;
    }

    public record TextFormatElement(TextStyle style, String text, int index) {

        public int length() {
            return this.text.length();
        }

        public TextFormatElement[] split(int index) {
            return new TextFormatElement[]{
                    new TextFormatElement(this.style, this.text.substring(0, index), this.index),
                    new TextFormatElement(this.style, this.text.substring(index), this.index + index)
            };
        }

        public int indexOf(String text) {
            return this.text.indexOf(text);
        }

        public String toString() {
            return this.text;
        }

        public TextFormatElement copy() {
            return new TextFormatElement(this.style.copy(), this.text, this.index);
        }
    }

    private class ElementRangeView implements Iterator<TextFormatElement> {
        TextFormatElement next;
        int to;

        public ElementRangeView(int from, int to) {
            this.next = getNextElement(from);
            this.to = Math.min(to, length);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public TextFormatElement next() {
            TextFormatElement current = this.next;

            if (current == null)
                throw new NoSuchElementException("End of iterator");

            this.next = getNextElement(this.next.length());

            if (this.next.index > to)
                this.next = null;

            return current;
        }
    }
}
