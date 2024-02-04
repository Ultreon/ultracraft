package com.ultreon.craft.util;

import com.ultreon.craft.api.commands.*;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.libs.commons.v0.exceptions.SyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.error.YAMLException;

import javax.xml.crypto.NoSuchMechanismException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NoSuchFileException;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.util.*;

public class ExceptionMap {
    public static String getErrorCode(@NotNull Throwable t) {
        if (t instanceof NoSuchMechanismException) return "JX-0001";
        else if (t instanceof YAMLException) return "YML-0001";
        else if (t instanceof SyntaxException) return "SRV-0001";
        else if (t instanceof IllegalCommandException) return "SRV-0004";
        else if (t instanceof UnsupportedOperationException) return "J-0001";
        else if (t instanceof IllegalStateException) return "J-0002";
        else if (t instanceof IllegalFormatCodePointException) return "J-0023";
        else if (t instanceof IllegalFormatConversionException) return "J-0025";
        else if (t instanceof IllegalFormatFlagsException) return "J-0026";
        else if (t instanceof IllegalFormatPrecisionException) return "J-0027";
        else if (t instanceof IllegalFormatWidthException) return "J-0028";
        else if (t instanceof IllegalFormatException) return "J-0029";
        else if (t instanceof IllegalArgumentException) return "J-0003";
        else if (t instanceof IllegalAccessException) return "J-0004";
        else if (t instanceof IllegalAccessError) return "J-0004";
        else if (t instanceof VirtualMachineError) return "J-0005";
        else if (t instanceof ClassCastException) return "J-0006";
        else if (t instanceof ClassNotFoundException) return "J-0007";
        else if (t instanceof ClassFormatError) return "J-0008";
        else if (t instanceof FileNotFoundException) return "J-0009";
        else if (t instanceof EOFException) return "J-0009";
        else if (t instanceof AccessDeniedException) return "J-0010";
        else if (t instanceof FileAlreadyExistsException) return "J-0011";
        else if (t instanceof NullPointerException) return "J-0013";
        else if (t instanceof NoSuchFieldException) return "J-0014";
        else if (t instanceof NoSuchMethodException) return "J-0015";
        else if (t instanceof NoSuchFieldError) return "J-0016";
        else if (t instanceof NoSuchMethodError) return "J-0017";
        else if (t instanceof NoSuchElementException) return "J-0018";
        else if (t instanceof NoSuchFileException) return "J-0019";
        else if (t instanceof NoSuchObjectException) return "J-0020";
        else if (t instanceof NoClassDefFoundError) return "J-0021";
        else if (t instanceof AccessException) return "J-0022";
        else if (t instanceof ArrayIndexOutOfBoundsException) return "J-0030";
        else if (t instanceof StringIndexOutOfBoundsException) return "J-0031";
        else if (t instanceof IndexOutOfBoundsException) return "J-0032";
        else if (t instanceof InterruptedException) return "J-0033";
        else if (t instanceof Error) return "J-9996";
        else if (t instanceof RuntimeException) return "J-9997";
        else if (t instanceof Exception) return "J-9998";
        else return "J-9999";
    }

    public static void sendFatal(CommandSender s, Throwable t) {
        ExceptionMap.sendFatal(s, t, false);
    }

    public static void sendFatal(CommandSender s, Throwable t, boolean printStackTrace) {
        if (printStackTrace)
            UltracraftServer.LOGGER.error("An error occurred: ", t);

        if (t instanceof NoSuchMechanismException) s.sendMessage("[JX-0001] No such mechanism: " + t.getMessage());
        else if (t instanceof YAMLException) s.sendMessage("[YML-0001] Generic YAML error: " + t.getMessage());
        else if (t instanceof CommandExecuteException) s.sendMessage("[B-0002] Illegal or invalid command: " + t.getMessage());
        else if (t instanceof SpecSyntaxException) s.sendMessage("[SRV-0001] Syntax error: " + t.getMessage());
        else if (t instanceof OverloadConflictException) s.sendMessage("[SRV-0004] Overload conflicts for commands: " + StringUtils.join(((OverloadConflictException) t).getAliases(), ", ") + ": " + t.getMessage());
        else if (t instanceof UnsupportedOperationException) s.sendMessage("[J-0001] Unsupported operation: " + t.getMessage());
        else if (t instanceof IllegalStateException) s.sendMessage("[J-0002] Illegal state: " + t.getMessage());
        else if (t instanceof IllegalFormatCodePointException) s.sendMessage("[J-0023] Illegal format code point: " + t.getMessage());
        else if (t instanceof IllegalFormatConversionException) s.sendMessage("[J-0025] Illegal format conversion: " + t.getMessage());
        else if (t instanceof IllegalFormatFlagsException) s.sendMessage("[J-0026] Illegal format flags: " + t.getMessage());
        else if (t instanceof IllegalFormatPrecisionException) s.sendMessage("[J-0027] Illegal format precision: " + t.getMessage());
        else if (t instanceof IllegalFormatWidthException) s.sendMessage("[J-0028] Illegal format width: " + t.getMessage());
        else if (t instanceof IllegalFormatException) s.sendMessage("[J-0029] Illegal format: " + t.getMessage());
        else if (t instanceof IllegalArgumentException) s.sendMessage("[J-0003] Illegal argument: " + t.getMessage());
        else if (t instanceof IllegalAccessException) s.sendMessage("[J-0004] Illegal access: " + t.getMessage());
        else if (t instanceof IllegalAccessError) s.sendMessage("[J-0004] Illegal access error: " + t.getMessage());
        else if (t instanceof VirtualMachineError) s.sendMessage("[J-0005] Virtual machine error: " + t.getMessage());
        else if (t instanceof ClassCastException) s.sendMessage("[J-0006] Class cast failure: " + t.getMessage());
        else if (t instanceof ClassNotFoundException) s.sendMessage("[J-0007] Class was not found: " + t.getMessage());
        else if (t instanceof ClassFormatError) s.sendMessage("[J-0008] Invalid class format: " + t.getMessage());
        else if (t instanceof FileNotFoundException) s.sendMessage("[J-0009] File not found: " + t.getMessage());
        else if (t instanceof EOFException) s.sendMessage("[J-0009] Unexpected EOF: " + t.getMessage());
        else if (t instanceof AccessDeniedException) s.sendMessage("[J-0010] Access was internally denied: " + t.getMessage());
        else if (t instanceof FileAlreadyExistsException) s.sendMessage("[J-0011] File already exists: " + t.getMessage());
        else if (t instanceof NullPointerException) s.sendMessage("[J-0013] Unexpected null pointer: " + t.getMessage());
        else if (t instanceof NoSuchFieldException) s.sendMessage("[J-0014] No such field: " + t.getMessage());
        else if (t instanceof NoSuchMethodException) s.sendMessage("[J-0015] No such method: " + t.getMessage());
        else if (t instanceof NoSuchFieldError) s.sendMessage("[J-0016] No such field error: " + t.getMessage());
        else if (t instanceof NoSuchMethodError) s.sendMessage("[J-0017] No such method error: " + t.getMessage());
        else if (t instanceof NoSuchElementException) s.sendMessage("[J-0018] No such element: " + t.getMessage());
        else if (t instanceof NoSuchFileException) s.sendMessage("[J-0019] No such file: " + t.getMessage());
        else if (t instanceof NoSuchObjectException) s.sendMessage("[J-0020] No such object: " + t.getMessage());
        else if (t instanceof NoClassDefFoundError) s.sendMessage("[J-0021] No class definition found: " + t.getMessage());
        else if (t instanceof AccessException) s.sendMessage("[J-0022] Access: " + t.getMessage());
        else if (t instanceof ArrayIndexOutOfBoundsException) s.sendMessage("[J-0030] Array index out of bounds: " + t.getMessage());
        else if (t instanceof StringIndexOutOfBoundsException) s.sendMessage("[J-0031] String index out of bounds: " + t.getMessage());
        else if (t instanceof IndexOutOfBoundsException) s.sendMessage("[J-0032] Index out of bounds: " + t.getMessage());
        else if (t instanceof InterruptedException) s.sendMessage("[J-0033] Thread was interrupted: " + t.getMessage());
        else if (t instanceof Error) s.sendMessage("[J-9996] An unknown compile error occurred: " + t.getMessage());
        else if (t instanceof RuntimeException) s.sendMessage("[J-9997] An unknown runtime error occurred: " + t.getMessage());
        else if (t instanceof Exception) s.sendMessage("[J-9998] An unknown generic error occurred.: " + t.getMessage());
        else s.sendMessage("[J-9999] An unknown error occurred.");
    }

}
