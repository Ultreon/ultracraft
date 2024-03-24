package com.ultreon.craft.api.commands;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;

import java.util.Collection;
import java.util.Iterator;

public class IndexedCommandSpecValues implements Iterable<IndexedCommandSpecValues.Entry> {
    private final Int2ReferenceArrayMap<CommandSpecValues> mapping = new Int2ReferenceArrayMap<>();

    public void set(int index, CommandSpecValues values) {
        this.mapping.put(index, values);
    }

    public CommandSpecValues get(int index) {
        return this.mapping.get(index);
    }

    public boolean has(int index) {
        return this.mapping.containsKey(index) && this.mapping.get(index) != null;
    }

    public boolean isBlank() {
        for (CommandSpecValues values : this.mapping.values()) {
            if (values != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof IndexedCommandSpecValues)) return false;
        return this.mapping.equals(((IndexedCommandSpecValues) other).mapping);
    }

    @Override
    public int hashCode() {
        return this.mapping.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        Iterator<Int2ReferenceMap.Entry<CommandSpecValues>> i = this.mapping.int2ReferenceEntrySet().iterator();
        int n = this.mapping.size();
        boolean first = true;
        s.append("{");
        while (n-- != 0) {
            if (first) {
                first = false;
            } else {
                s.append(", ");
            }
            Int2ReferenceMap.Entry<CommandSpecValues> e = i.next();
            s.append(e.getIntKey());
            s.append("=>");
            s.append(e.getValue());
        }
        s.append("}");
        return s.toString();
    }

    @Override
    public Iterator<Entry> iterator() {
        return new Iterator<>() {
            private final Iterator<Int2ReferenceMap.Entry<CommandSpecValues>> entries =
                    IndexedCommandSpecValues.this.mapping.int2ReferenceEntrySet().iterator();

            @Override
            public boolean hasNext() {
                return this.entries.hasNext();
            }

            @Override
            public Entry next() {
                return new Entry(this.entries.next());
            }
        };
    }

    public Collection<CommandSpecValues> values() {
        return this.mapping.values();
    }

    public class Entry {
        private final Int2ReferenceMap.Entry<CommandSpecValues> wrapped;
        public Entry(Int2ReferenceMap.Entry<CommandSpecValues> wrapped) {
            this.wrapped = wrapped;
        }

        public void set(CommandSpecValues values) {
            this.wrapped.setValue(values);
        }

        public int index() {
            return this.wrapped.getIntKey();
        }

        public CommandSpecValues values() {
            return this.wrapped.getValue();
        }
    }
}