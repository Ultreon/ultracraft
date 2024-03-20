package com.ultreon.craft.api.commands;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(Perms.class)
public @interface Perm {
    String value();
}

