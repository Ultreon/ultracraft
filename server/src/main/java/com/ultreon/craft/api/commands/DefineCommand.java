package com.ultreon.craft.api.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies a subcommand.
 * See {@link #value()} for details on the format.
 *
 * @see #value()
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DefineCommand {
    /**
     * The specification of the command overload.
     * <p>
     * Any {@code {...}} in the following docs are placeholders for values that will be filled.
     * <ul>
     *  <li>Use {@code <{...}>} to specify an argument type.</li>
     *  <li>You can use {@code <{...}:{...}>} to specify an argument type and a name.</li>
     *  <li>Any other characters will be used as literal text.</li>
     * </ul>
     * <p>
     * For example:
     * <pre>
     *  all &lt;entity&gt; in current world
     *  all &lt;entity&gt; in world &lt;world&gt;
     * </pre>
     * The above can be used as follows:
     * <pre>
     *  /kill all ultracraft:player in current world
     *  /kill all ultracraft:player in world ultracraft:overworld
     * </pre>
     *
     * <p>
     * If this is not specified, the overload specification will just be empty.
     *
     * @return The overload specification
     */
    String value() default "";

    /**
     * This is a description of the subcommand.
     * Used in the help command or the --help argument.
     *
     * @return The description
     */
    String comment() default "No info available...";
}
