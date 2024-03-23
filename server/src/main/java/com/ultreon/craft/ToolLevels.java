package com.ultreon.craft;

import com.ultreon.craft.block.ToolLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToolLevels {
    private static final List<ToolLevel> levels = new ArrayList<>();

    public static ToolLevel register(ToolLevel toolLevel) {
        levels.add(toolLevel);
        return toolLevel;
    }

    public static ToolLevel registerAfter(ToolLevel toolLevel, int level) {
        if (level < 1 || level > levels.size()) {
            throw new IllegalArgumentException("Invalid tool level: " + level);
        }
        levels.add(level, toolLevel);
        return toolLevel;
    }

    public static ToolLevel registerBefore(ToolLevel toolLevel, int level) {
        if (level < 1 || level > levels.size()) {
            throw new IllegalArgumentException("Invalid tool level: " + level);
        }
        levels.add(level - 1, toolLevel);
        return toolLevel;
    }

    public static ToolLevel registerFirst(ToolLevel toolLevel) {
        levels.addFirst(toolLevel);
        return toolLevel;
    }

    public static ToolLevel registerLast(ToolLevel toolLevel) {
        levels.addLast(toolLevel);
        return toolLevel;
    }

    public static List<ToolLevel> levels() {
        return Collections.unmodifiableList(levels);
    }

    public static ToolLevel level(int level) {
        if (level < 1 || level > levels.size()) {
            throw new IllegalArgumentException("Invalid tool level: " + level);
        }
        return levels.get(level - 1);
    }

    public static int level(ToolLevel toolLevel) {
        if (!levels.contains(toolLevel)) {
            throw new IllegalArgumentException("Invalid tool requirement: " + toolLevel);
        }
        return levels.indexOf(toolLevel) + 1;
    }

    public static int maxLevel() {
        return levels.size();
    }
}
