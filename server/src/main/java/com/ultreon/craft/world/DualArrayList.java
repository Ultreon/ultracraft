package com.ultreon.craft.world;

import com.google.common.collect.Streams;
import com.ultreon.libs.commons.v0.tuple.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class DualArrayList<F, S> implements Iterable<Pair<F, S>> {
    private F[] firstArr;
    private S[] secondArr;

    private final Object lock = new Object();

    public DualArrayList(F[] firstArr, S[] secondArr) {
        this.firstArr = firstArr;
        this.secondArr = secondArr;
    }

    @SuppressWarnings("unchecked")
    public DualArrayList(Class<F> t1Class, Class<S> t2Class) {
        this.firstArr = (F[]) Array.newInstance(t1Class, 0);
        this.secondArr = (S[]) Array.newInstance(t2Class, 0);
    }

    public Pair<F, S> get(int index) {
        return new Pair<>(firstArr[index], secondArr[index]);
    }

    public int size() {
        return firstArr.length;
    }

    public void add(F f, S s) {
        synchronized (lock) {
            this.firstArr = ArrayUtils.add(this.firstArr, f);
            this.secondArr = ArrayUtils.add(this.secondArr, s);
        }
    }

    public Pair<F, S> remove(int index) {
        synchronized (lock) {
            F f = this.firstArr[index];
            S s = this.secondArr[index];
            this.firstArr = ArrayUtils.remove(this.firstArr, index);
            this.secondArr = ArrayUtils.remove(this.secondArr, index);
            return new Pair<>(f, s);
        }
    }

    public void clear() {
        synchronized (lock) {
            this.firstArr = Arrays.copyOf(this.firstArr, 0);
            this.secondArr = Arrays.copyOf(this.secondArr, 0);
        }
    }

    public Collection<F> getFirst() {
        return new ReadOnlyList<>(firstArr);
    }

    public Collection<S> getSecond() {
        return new ReadOnlyList<>(secondArr);
    }

    public void set(int index, F f, S s) {
        synchronized (lock) {
            this.firstArr[index] = f;
            this.secondArr[index] = s;
        }
    }

    public void setFirst(int index, F f) {
        synchronized (lock) {
            this.firstArr[index] = f;
        }
    }

    public void setSecond(int index, S s) {
        synchronized (lock) {
            this.secondArr[index] = s;
        }
    }

    public F[] getFirstArray() {
        return Arrays.copyOf(firstArr, firstArr.length);
    }

    public S[] getSecondArray() {
        return Arrays.copyOf(secondArr, secondArr.length);
    }

    public boolean isEmpty() {
        return firstArr.length == 0;
    }

    public boolean containsFirst(F o) {
        for (F t1 : firstArr) {
            if (t1.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsSecond(S o) {
        for (S t2 : secondArr) {
            if (t2.equals(o)) {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Pair<F, S> pair) {
        return containsFirst(pair.getFirst()) && containsSecond(pair.getSecond());
    }

    public int indexOfFirst(F o) {
        for (int i = 0; i < firstArr.length; i++) {
            if (firstArr[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfSecond(S o) {
        for (int i = 0; i < secondArr.length; i++) {
            if (secondArr[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOf(Pair<F, S> pair) {
        int index = indexOfFirst(pair.getFirst());
        if (index == -1) return -1;
        return indexOfSecond(pair.getSecond()) == index ? index : -1;
    }

    public int lastIndexOfFirst(F o) {
        for (int i = firstArr.length - 1; i >= 0; i--) {
            if (firstArr[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOfSecond(S o) {
        for (int i = secondArr.length - 1; i >= 0; i--) {
            if (secondArr[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(Pair<F, S> pair) {
        int index = lastIndexOfFirst(pair.getFirst());
        if (index == -1) return -1;
        return lastIndexOfSecond(pair.getSecond()) == index ? index : -1;
    }

    public DualArrayList<F, S> subList(int fromIndex, int toIndex) {
        return new DualArrayList<>(ArrayUtils.subarray(firstArr, fromIndex, toIndex),
                ArrayUtils.subarray(secondArr, fromIndex, toIndex));
    }

    public List<Pair<F, S>> asList() {
        List<Pair<F, S>> list = new ArrayList<>();
        for (int i = 0; i < firstArr.length; i++) {
            list.add(new Pair<>(firstArr[i], secondArr[i]));
        }
        return list;
    }

    public List<Pair<F, S>> asListReversed() {
        List<Pair<F, S>> list = new ArrayList<>();
        for (int i = firstArr.length - 1; i >= 0; i--) {
            list.add(new Pair<>(firstArr[i], secondArr[i]));
        }
        return list;
    }

    @ApiStatus.Experimental
    @SuppressWarnings("UnstableApiUsage")
    public Stream<Pair<F, S>> stream() {
        Stream<F> stream = Arrays.stream(getFirstArray());
        Stream<S> stream2 = Arrays.stream(getSecondArray());

        return Streams.zip(stream, stream2, Pair::new);
    }

    @Override
    public @NotNull Iterator<Pair<F, S>> iterator() {
        var t1 = getFirstArray();
        var t2 = getSecondArray();

        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < t1.length;
            }

            @Override
            public Pair<F, S> next() {
                return new Pair<>(t1[index++], t2[index++]);
            }
        };
    }

    public static <T1, T2> DualArrayList<T1, T2> of(T1[] t1, T2[] t2) {
        return new DualArrayList<>(t1, t2);
    }

    public static <T1, T2> DualArrayList<T1, T2> copyOf(DualArrayList<T1, T2> dualArrayList) {
        return new DualArrayList<>(dualArrayList.getFirstArray(), dualArrayList.getSecondArray());
    }

    @ApiStatus.Experimental
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T1, T2> DualArrayList<T1, T2> empty() {
        return (DualArrayList) new DualArrayList<>(Object[].class, Object[].class);
    }

    public S removeFirst(F info) {
        int index = indexOfFirst(info);
        if (index == -1) return null;
        S removed = getSecondArray()[index];
        remove(index);
        return removed;
    }

    public F removeSecond(S info) {
        int index = indexOfSecond(info);
        if (index == -1) return null;
        F removed = getFirstArray()[index];
        remove(index);
        return removed;
    }

    public S getSecond(F info) {
        return get(indexOfFirst(info)).getSecond();
    }

    public F getFirst(S info) {
        return get(indexOfSecond(info)).getFirst();
    }

    private record ReadOnlyList<T>(T[] array) implements Collection<T> {
        @Override
        public int size() {
            return array.length;
        }

        @Override
        public boolean isEmpty() {
            return array.length == 0;
        }

        @Override
        public boolean contains(Object o) {
            for (T t1 : array) {
                if (t1.equals(o)) {
                    return true;
                }
            }
            return false;
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return Arrays.asList(array).iterator();
        }

        @Override
        public Object @NotNull [] toArray() {
            return Arrays.copyOf(array, array.length);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <A> A @NotNull [] toArray(@NotNull A @NotNull [] a) {
            return (A[]) Arrays.copyOf(array, array.length, a.getClass());
        }

        @Override
        public boolean add(T t1) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends T> c) {
            return false;
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }
    }
}
