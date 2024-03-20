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
        return switch (t) {
            case NoSuchMechanismException noSuchMechanismException -> "JX-0001";
            case YAMLException yamlException -> "YML-0001";
            case SyntaxException syntaxException -> "SRV-0001";
            case IllegalCommandException illegalCommandException -> "SRV-0004";
            case UnsupportedOperationException unsupportedOperationException -> "J-0001";
            case IllegalStateException illegalStateException -> "J-0002";
            case IllegalFormatCodePointException illegalFormatCodePointException -> "J-0023";
            case IllegalFormatConversionException illegalFormatConversionException -> "J-0025";
            case IllegalFormatFlagsException illegalFormatFlagsException -> "J-0026";
            case IllegalFormatPrecisionException illegalFormatPrecisionException -> "J-0027";
            case IllegalFormatWidthException illegalFormatWidthException -> "J-0028";
            case IllegalFormatException illegalFormatException -> "J-0029";
            case IllegalArgumentException illegalArgumentException -> "J-0003";
            case IllegalAccessException illegalAccessException -> "J-0004";
            case IllegalAccessError illegalAccessError -> "J-0004";
            case VirtualMachineError virtualMachineError -> "J-0005";
            case ClassCastException classCastException -> "J-0006";
            case ClassNotFoundException classNotFoundException -> "J-0007";
            case ClassFormatError classFormatError -> "J-0008";
            case FileNotFoundException fileNotFoundException -> "J-0009";
            case EOFException eofException -> "J-0009";
            case AccessDeniedException accessDeniedException -> "J-0010";
            case FileAlreadyExistsException fileAlreadyExistsException -> "J-0011";
            case NullPointerException nullPointerException -> "J-0013";
            case NoSuchFieldException noSuchFieldException -> "J-0014";
            case NoSuchMethodException noSuchMethodException -> "J-0015";
            case NoSuchFieldError noSuchFieldError -> "J-0016";
            case NoSuchMethodError noSuchMethodError -> "J-0017";
            case NoSuchElementException noSuchElementException -> "J-0018";
            case NoSuchFileException noSuchFileException -> "J-0019";
            case NoSuchObjectException noSuchObjectException -> "J-0020";
            case NoClassDefFoundError noClassDefFoundError -> "J-0021";
            case AccessException accessException -> "J-0022";
            case ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException -> "J-0030";
            case StringIndexOutOfBoundsException stringIndexOutOfBoundsException -> "J-0031";
            case IndexOutOfBoundsException indexOutOfBoundsException -> "J-0032";
            case InterruptedException interruptedException -> "J-0033";
            case Error error -> "J-9996";
            case RuntimeException runtimeException -> "J-9997";
            case Exception exception -> "J-9998";
            default -> "J-9999";
        };
    }

    public static void sendFatal(CommandSender s, Throwable t) {
        ExceptionMap.sendFatal(s, t, false);
    }

    public static void sendFatal(CommandSender s, Throwable thrown, boolean printStackTrace) {
        if (printStackTrace)
            UltracraftServer.LOGGER.error("An error occurred: ", thrown);

        switch (thrown) {
            case NoSuchMechanismException noSuchMechanismException ->
                    s.sendMessage("[JX-0001] No such mechanism: " + thrown.getMessage());
            case YAMLException yamlException ->
                    s.sendMessage("[YML-0001] Generic YAML error: " + thrown.getMessage());
            case CommandExecuteException commandExecuteException ->
                    s.sendMessage("[B-0002] Illegal or invalid command: " + thrown.getMessage());
            case SpecSyntaxException specSyntaxException ->
                    s.sendMessage("[SRV-0001] Syntax error: " + thrown.getMessage());
            case OverloadConflictException overloadConflictException ->
                    s.sendMessage("[SRV-0004] Overload conflicts for commands: " + StringUtils.join(overloadConflictException.getAliases(), ", ") + ": " + thrown.getMessage());
            case UnsupportedOperationException unsupportedOperationException ->
                    s.sendMessage("[J-0001] Unsupported operation: " + thrown.getMessage());
            case IllegalStateException illegalStateException ->
                    s.sendMessage("[J-0002] Illegal state: " + thrown.getMessage());
            case IllegalFormatCodePointException illegalFormatCodePointException ->
                    s.sendMessage("[J-0023] Illegal format code point: " + thrown.getMessage());
            case IllegalFormatConversionException illegalFormatConversionException ->
                    s.sendMessage("[J-0025] Illegal format conversion: " + thrown.getMessage());
            case IllegalFormatFlagsException illegalFormatFlagsException ->
                    s.sendMessage("[J-0026] Illegal format flags: " + thrown.getMessage());
            case IllegalFormatPrecisionException illegalFormatPrecisionException ->
                    s.sendMessage("[J-0027] Illegal format precision: " + thrown.getMessage());
            case IllegalFormatWidthException illegalFormatWidthException ->
                    s.sendMessage("[J-0028] Illegal format width: " + thrown.getMessage());
            case IllegalFormatException illegalFormatException ->
                    s.sendMessage("[J-0029] Illegal format: " + thrown.getMessage());
            case IllegalArgumentException illegalArgumentException ->
                    s.sendMessage("[J-0003] Illegal argument: " + thrown.getMessage());
            case IllegalAccessException illegalAccessException ->
                    s.sendMessage("[J-0004] Illegal access: " + thrown.getMessage());
            case IllegalAccessError illegalAccessError ->
                    s.sendMessage("[J-0004] Illegal access error: " + thrown.getMessage());
            case VirtualMachineError virtualMachineError ->
                    s.sendMessage("[J-0005] Virtual machine error: " + thrown.getMessage());
            case ClassCastException classCastException ->
                    s.sendMessage("[J-0006] Class cast failure: " + thrown.getMessage());
            case ClassNotFoundException classNotFoundException ->
                    s.sendMessage("[J-0007] Class was not found: " + thrown.getMessage());
            case ClassFormatError classFormatError -> s.sendMessage("[J-0008] Invalid class format: " + thrown.getMessage());
            case FileNotFoundException fileNotFoundException ->
                    s.sendMessage("[J-0009] File not found: " + thrown.getMessage());
            case EOFException eofException ->
                    s.sendMessage("[J-0009] Unexpected EOF: " + thrown.getMessage());
            case AccessDeniedException accessDeniedException ->
                    s.sendMessage("[J-0010] Access was internally denied: " + thrown.getMessage());
            case FileAlreadyExistsException fileAlreadyExistsException ->
                    s.sendMessage("[J-0011] File already exists: " + thrown.getMessage());
            case NullPointerException nullPointerException ->
                    s.sendMessage("[J-0013] Unexpected null pointer: " + thrown.getMessage());
            case NoSuchFieldException noSuchFieldException ->
                    s.sendMessage("[J-0014] No such field: " + thrown.getMessage());
            case NoSuchMethodException noSuchMethodException ->
                    s.sendMessage("[J-0015] No such method: " + thrown.getMessage());
            case NoSuchFieldError noSuchFieldError ->
                    s.sendMessage("[J-0016] No such field error: " + thrown.getMessage());
            case NoSuchMethodError noSuchMethodError ->
                    s.sendMessage("[J-0017] No such method error: " + thrown.getMessage());
            case NoSuchElementException noSuchElementException ->
                    s.sendMessage("[J-0018] No such element: " + thrown.getMessage());
            case NoSuchFileException noSuchFileException ->
                    s.sendMessage("[J-0019] No such file: " + thrown.getMessage());
            case NoSuchObjectException noSuchObjectException ->
                    s.sendMessage("[J-0020] No such object: " + thrown.getMessage());
            case NoClassDefFoundError noClassDefFoundError ->
                    s.sendMessage("[J-0021] No class definition found: " + thrown.getMessage());
            case AccessException accessException ->
                    s.sendMessage("[J-0022] Access: " + thrown.getMessage());
            case ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException ->
                    s.sendMessage("[J-0030] Array index out of bounds: " + thrown.getMessage());
            case StringIndexOutOfBoundsException stringIndexOutOfBoundsException ->
                    s.sendMessage("[J-0031] String index out of bounds: " + thrown.getMessage());
            case IndexOutOfBoundsException indexOutOfBoundsException ->
                    s.sendMessage("[J-0032] Index out of bounds: " + thrown.getMessage());
            case InterruptedException interruptedException ->
                    s.sendMessage("[J-0033] Thread was interrupted: " + thrown.getMessage());
            case Error error ->
                    s.sendMessage("[J-9996] An unknown compile error occurred: " + thrown.getMessage());
            case RuntimeException runtimeException ->
                    s.sendMessage("[J-9997] An unknown runtime error occurred: " + thrown.getMessage());
            case Exception exception ->
                    s.sendMessage("[J-9998] An unknown generic error occurred.: " + thrown.getMessage());
            case null, default ->
                    s.sendMessage("[J-9999] An unknown error occurred.");
        }
    }
}
