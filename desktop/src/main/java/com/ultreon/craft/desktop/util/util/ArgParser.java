package com.ultreon.craft.desktop.util.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArgParser {
    private final List<String> args;
    private final Set<String> flags;
    private final Map<String, String> keywordArgs;
    private final List<String> argv;

    public ArgParser(String... argv) {
        this.argv = List.of(argv);

        var args =new ArrayList<String>();
        var flags = new HashSet<String>();
        var keywordArgs = new HashMap<String, String>();

        for (String s : argv) {
            if (s.startsWith("--")) {
                var name = s.substring(2);
                var split = name.split("=", 2);
                if (split.length == 1) {
                    flags.add(name);
                } else {
                    var key = split[0];
                    var value = split[1];
                    keywordArgs.put(key, value);
                }
            } else if (s.startsWith("-")) {
                var name = s.substring(1);
                if (name.length() != 1) {
                    args.add(name);
                } else {
                    flags.add(name);
                }
            }
        }
        this.args = Collections.unmodifiableList(args);
        this.flags = Collections.unmodifiableSet(flags);
        this.keywordArgs = Collections.unmodifiableMap(keywordArgs);
    }

    public List<String> getArgs() {
        return this.args;
    }

    public Set<String> getFlags() {
        return this.flags;
    }

    public Map<String, String> getKeywordArgs() {
        return this.keywordArgs;
    }

    public List<String> getArgv() {
        return this.argv;
    }
}
