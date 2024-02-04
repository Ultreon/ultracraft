package com.ultreon.craft.api.commands;

import org.apache.commons.lang3.StringUtils;
import java.util.*;

public record CommandSpec(String commandName, List<CommandParameter> arguments) {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(this.commandName).append(" ");
        boolean optional = false;
        for (CommandParameter parameter : this.arguments) {
            if (!optional && parameter.isOptional()) {
                sb.append("[ ");
                optional = true;
            }
            if (optional && !parameter.isOptional()) {
                sb.append("] ");
                optional = false;
            }
            parameter.ifArgType(p -> {
                sb.append("<");
                sb.append(p.getTag());
                if (parameter.getComment() != null && !parameter.getComment().isEmpty() && !parameter.getComment().equals(p.getTag())) {
                    sb.append(":").append(parameter.getComment());
                }
                sb.append(">");
            }).ifText(obj -> {
                if (obj.length == 1) {
                    sb.append(obj[0]);
                } else {
                    sb.append("(");
                    sb.append(StringUtils.join(obj, "|"));
                    sb.append(")");
                }
            });
            sb.append(" ");
        }
        String s = sb.toString().replaceAll(" \\[ ", " [").replaceAll(" ] ", "] ");
        return s.substring(0, s.length() - 1);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        CommandSpec that = (CommandSpec) other;
        return this.commandName.equals(that.commandName) && this.arguments.equals(that.arguments);
    }

}