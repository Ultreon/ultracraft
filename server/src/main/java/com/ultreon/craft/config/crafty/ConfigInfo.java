package com.ultreon.craft.config.crafty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigInfo {
    String fileName();
}
