package com.ultreon.craft.client.text;

import com.ultreon.craft.world.rng.JavaRandomSource;
import com.ultreon.craft.world.rng.RandomSource;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Integer.max;

/**
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @version 5.1
 */
public class WordGenerator {
    private final int minSize;
    private final int maxSize;
    private final boolean isNamed;

    private static final String[] A = {
            "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "r", "s", "t", "v", "w", "x", "y", "z",
            "br", "bl", "cl", "cr", "dr", "fl", "fr", "gl", "gn", "gr", "kl", "kn", "kr", "ks", "mr",
            "ng", "nk", "ph", "pr", "ps", "rl", "rt", "sch", "sc", "sh", "sl", "sn", "sr", "st", "sw", "th",
            "tr", "vl", "vr", "wr"
    };
    private static final String[] B = {
            "a", "e", "i", "o", "u", "ee", "oo", "eo", "ou", "ue"
    };
    private static final String[] C = {
            "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "r", "s", "t", "v", "w", "x", "y", "z"
    };
    private static final String[] CA = {
            "br", "bl", "cr", "dr", "fr", "gr", "gl", "gn", "kr", "kl", "kn", "pr", "pl", "sr", "sl", "sn", "s"
    };
    private static final String[] CB = {
            "abs", "sch", "scr"
    };
    private static final String[] D = {
            "a", "e", "i", "o", "u", "qu"
    };
    private static final String[] DA = {
            "de", "re", "be", "ge", "qui"
    };
    private static final String[] DB = {
            "que", "quo"
    };
    private static final String[] E = {
            "b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "r", "s", "t", "v", "w", "x", "z"
    };
    private static final String[] Y = {
            "rld", "ld", "d", "ne", "re", "sh", "xyl", "zyl", "x", "th", "ph"
    };
    private static final String[] YA = {
            "d", "r", "s", "m", "th", "ph"
    };
    private static final String[] YB = {
            "ld", "rd", "rs", "ck"
    };
    private static final String[] YC = {
            "rld", "mt", "rt", "st", "pt"
    };
    private static final String[] YD = {
            "xyl", "zyl", "byl", "que"
    };
    private static final String[] ZA = {
            "e", "ed", "er", "en", "em", "im", "in", "an", "ian", "iam", "ing"
    };
    private static final String[] ZB = {
            "ink", "ick", "ock", "ong", "eng", "us", "urt", "erd", "ert", "erl", "orl", "url", "end", "ind"
    };
    private static final String[] ZC = {
            "urd", "urt", "ird", "ord", "irt"
    };
    private static final String[] ZD = {
            "yl", "ix", "ux", "ox", "ex", "yx", "um", "il"
    };
    private static final String[] ZE = {
            "ium"
    };
    private final RandomSource random;

    public WordGenerator(Config config) {
        this.minSize = config.minSize;
        this.maxSize = config.maxSize;
        this.isNamed = config.isNamed;
        long seed = config.seed;
        this.random = new JavaRandomSource(seed);
    }

    enum State {
        A, B, C, D, E1, E2, Y, Z
    }

    public String generate() {
        final var random = this.random();
        final var min = max(this.minSize, 2);
        final var max = this.maxSize + 1;
        final var len = this.random().nextInt(min, max);

        final var named = new AtomicBoolean(this.isNamed);
        var ref = new StateHolder(random);
        final var sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            this.switchState(ref, i, len, sb, named, random);
        }
        return sb.toString();
    }

    private void switchState(StateHolder ref, int i, int len, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        switch (ref.state) {
            case A -> this.switchToA(ref, i, len, sb, named, random);
            case B -> this.switchToB(ref, i, len, sb, named, random);
            case C -> this.switchToC(ref, sb, named, random);
            case D -> this.switchToD(ref, i, len, sb, named, random);
            case E1 -> this.switchToE1(ref, sb, named, random);
            case E2 -> this.switchToE2(ref, sb, named, random);
            default -> throw new IllegalStateException("Unexpected state: " + ref.state);
        }
    }

    private void switchToE2(StateHolder ref, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        ref.state = State.B;
        this.appendRandom(sb, named, random, WordGenerator.E);
    }

    private void switchToE1(StateHolder ref, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        ref.state = State.E2;
        this.appendRandom(sb, named, random, WordGenerator.E);
    }

    private void switchToD(StateHolder ref, int i, int len, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        if (i < len - 3) {
            if (random.nextBoolean()) ref.state = State.A;
            else ref.state = State.E1;
        } else {
            ref.state = State.A;
        }
        this.appendRandom(sb, named, random, WordGenerator.D, WordGenerator.DA, WordGenerator.DB);
    }

    private void switchToC(StateHolder ref, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        ref.state = State.B;
        this.appendRandom(sb, named, random, WordGenerator.C, WordGenerator.CA, WordGenerator.CB);
    }

    private void switchToB(StateHolder ref, int i, int len, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        if (i < len - 3) {
            if (random.nextBoolean()) {
                ref.state = State.A;
            } else {
                ref.state = State.E1;
            }
        } else {
            ref.state = State.A;
        }
        if (i == len - 1) {
            this.appendRandom(sb, named, random, WordGenerator.ZA, WordGenerator.ZB, WordGenerator.ZC, WordGenerator.ZD, WordGenerator.ZE);
            return;
        }
        this.appendRandom(sb, named, random, WordGenerator.B);
    }

    private void switchToA(StateHolder ref, int i, int len, StringBuilder sb, AtomicBoolean named, RandomSource random) {
        ref.state = State.B;
        if (i == len - 1) {
            this.appendRandom(sb, named, random, WordGenerator.Y, WordGenerator.YA, WordGenerator.YB, WordGenerator.YC, WordGenerator.YD);
            return;
        }
        this.appendRandom(sb, named, random, WordGenerator.A);
    }

    private RandomSource random() {
        return this.random;
    }

    private void appendRandom(StringBuilder sb, AtomicBoolean named, RandomSource random, String[]... lists) {
        @Nullable String[] list = null;
        for (var l : lists) {
            if (random.nextBoolean()) {
                list = l;
                break;
            }
        }
        if (list == null) {
            list = lists[0];
        }
        var choose = this.choose(random, list);
        if (named.get()) {
            named.set(false);
            final var c = choose.charAt(0);
            final var substring = choose.substring(1);
            choose = ("" + c).toUpperCase(Locale.ROOT) + substring;
        }
        sb.append(choose);
    }


    protected <T> T choose(RandomSource rand, T[] list) {
        return list[rand.nextInt(list.length)];
    }

    public static class Config {
        private static final RandomSource RANDOM = new JavaRandomSource();
        private int minSize = 2;
        private int maxSize = 5;
        private boolean isNamed = false;
        private long seed = Config.RANDOM.nextLong();

        public Config minSize(int minSize) {
            this.minSize = minSize;
            return this;
        }

        public Config maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Config named() {
            this.isNamed = true;
            return this;
        }

        public Config seed(long seed) {
            this.seed = seed;
            return this;
        }
    }

    private static class StateHolder {
        State state;

        public StateHolder(RandomSource random) {
            this.state = random.chance(1) ? State.C : State.D;
        }
    }
}
