package com.ultreon.craft;

import javax.annotation.Nullable;
import javax.annotation.meta.TypeQualifierDefault;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Nullable
@TypeQualifierDefault(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldsAreNullableByDefault {
}
