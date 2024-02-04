package com.ultreon.craft.text;

import com.ultreon.craft.api.commands.CommandSender;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TextKey {
    String get(@Nullable CommandSender sender);
}
