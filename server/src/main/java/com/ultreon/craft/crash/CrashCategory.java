package com.ultreon.craft.crash;

import com.ultreon.craft.CommonConstants;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

/**
 * @author <a href="https://github.com/XyperCodee">XyperCode</a>
 */
public class CrashCategory {
    protected final List<AbstractMap.SimpleEntry<String, String>> entries = new ArrayList<>();
    protected final String details;
    protected Throwable throwable;

    public CrashCategory(String details) {
        this(details, null);
    }

    public CrashCategory(String details, Throwable throwable) {
        if (throwable instanceof ApplicationCrash crash) {
            CrashLog crashLog = crash.getCrashLog();
            this.details = this.detectThrowable(crashLog.details, crashLog.throwable);
            this.entries.addAll(crashLog.entries);
            return;
        }

        this.details = this.detectThrowable(details, throwable);
    }

    private String detectThrowable(String details, Throwable throwable) {
        var current = throwable;
        if (current != null) {
            do {
                if (current instanceof ApplicationCrash crash) {
                    CrashLog crashLog = crash.getCrashLog();
                    this.entries.addAll(crashLog.entries);
                    return this.detectThrowable(crashLog.details, crashLog.throwable);
                }

                if (!(current instanceof InvocationTargetException) && !(current instanceof CompletionException)) {
                    break;
                }

                current = current.getCause();
            } while (current.getCause() != null);
        }

        if (current instanceof ApplicationCrash crash) {
            return this.detectThrowable(crash.getCrashLog().details, crash.getCrashLog().throwable);
        } else {
            this.throwable = current;
        }
        return details;
    }

    public void add(String key, @Nullable Object value) {
        if (key.contains(":")) {
            throw new IllegalArgumentException("Key cannot contain a colon");
        }

        if (key.length() > 32) {
            throw new IllegalArgumentException("Key cannot be longer than 32 characters.");
        }

        this.entries.add(new AbstractMap.SimpleEntry<>(key, value != null ? value.toString() : "null@0"));
    }

    public String getDetails() {
        return this.details;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.details).append(": \r\n");

        if (!this.entries.isEmpty()) {
            ArrayList<AbstractMap.SimpleEntry<String, String>> simpleEntries = new ArrayList<>(this.entries);
            for (int i = 0; i < simpleEntries.size() - 1; i++) {
                AbstractMap.SimpleEntry<String, String> entry = simpleEntries.get(i);
                sb.append("   ");
                sb.append(entry.getKey());
                sb.append(": ");
                sb.append(entry.getValue());
                sb.append(System.lineSeparator());
            }

            AbstractMap.SimpleEntry<String, String> entry = simpleEntries.getLast();
            sb.append("   ");
            sb.append(entry.getKey());
            sb.append(": ");
            sb.append(entry.getValue());
            sb.append(System.lineSeparator());
        }

        if (this.throwable != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);

            this.throwable.printStackTrace(printWriter);
            printWriter.flush();

            StringBuffer outputBuf = stringWriter.getBuffer();
            try {
                stringWriter.close();
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to close StringWriter", e);
            }

            String output = outputBuf.toString();
            List<String> lines = output.lines().toList();
            String finalResult = "   " + String.join(System.lineSeparator() + "   ", lines);

            sb.append(finalResult);
        }

        sb.append(System.lineSeparator());

        return sb.toString();
    }
}
