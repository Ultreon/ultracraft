package com.ultreon.craft.api.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {
    String value() default "";
    String comment() default "No info available...";
}