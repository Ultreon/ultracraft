package org.gradle.sample.transform.javamodules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Data class to hold the information that should be added as module-info.class to an existing Jar file.
 */
public class ModuleInfo implements Serializable {
    private final String moduleName;
    private final String moduleVersion;
    private final List<String> exports = new ArrayList<>();
    private final List<String> requires = new ArrayList<>();
    private final List<String> requiresTransitive = new ArrayList<>();

    ModuleInfo(String moduleName, String moduleVersion) {
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
    }

    public void exports(String exports) {
        this.exports.add(exports);
    }

    public void requires(String requires) {
        this.requires.add(requires);
    }

    public void requiresTransitive(String requiresTransitive) {
        this.requiresTransitive.add(requiresTransitive);
    }

    public String getModuleName() {
        return this.moduleName;
    }

    protected String getModuleVersion() {
        return this.moduleVersion;
    }

    protected List<String> getExports() {
        return this.exports;
    }

    protected List<String> getRequires() {
        return this.requires;
    }

    protected List<String> getRequiresTransitive() {
        return this.requiresTransitive;
    }
}
