package org.gradle.sample.transform.javamodules;

import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.artifacts.transform.TransformParameters;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

/**
 * An artifact transform that applies additional information to Jars without module information.
 * The transformation fails the build if a Jar does not contain information and no extra information
 * was defined for it. This way we make sure that all Jars are turned into modules.
 */
abstract public class ExtraModuleInfoTransform implements TransformAction<ExtraModuleInfoTransform.Parameter> {

    public static class Parameter implements TransformParameters, Serializable {
        private Map<String, ModuleInfo> moduleInfo = Collections.emptyMap();
        private Map<String, String> automaticModules = Collections.emptyMap();

        @Input
        public Map<String, ModuleInfo> getModuleInfo() {
            return this.moduleInfo;
        }

        @Input
        public Map<String, String> getAutomaticModules() {
            return this.automaticModules;
        }

        public void setModuleInfo(Map<String, ModuleInfo> moduleInfo) {
            this.moduleInfo = moduleInfo;
        }

        public void setAutomaticModules(Map<String, String> automaticModules) {
            this.automaticModules = automaticModules;
        }
    }

    @InputArtifact
    protected abstract Provider<FileSystemLocation> getInputArtifact();

    @Override
    public void transform(@NotNull TransformOutputs outputs) {
        var moduleInfo = this.getParameters().moduleInfo;
        var automaticModules = this.getParameters().automaticModules;
        var originalJar = this.getInputArtifact().get().getAsFile();
        var originalJarName = originalJar.getName();

        if (this.isModule(originalJar)) {
            outputs.file(originalJar);
        } else if (moduleInfo.containsKey(originalJarName)) {
            ExtraModuleInfoTransform.addModuleDescriptor(originalJar, this.getModuleJar(outputs, originalJar), moduleInfo.get(originalJarName));
        } else if (this.isAutoModule(originalJar)) {
            outputs.file(originalJar);
        } else if (automaticModules.containsKey(originalJarName)) {
            ExtraModuleInfoTransform.addAutomaticModuleName(originalJar, this.getModuleJar(outputs, originalJar), automaticModules.get(originalJarName));
        } else {
            throw new RuntimeException("Not a module and no mapping defined: " + originalJarName);
        }
    }

    private boolean isModule(File jar) {
        var moduleInfoClassMrjarPath = Pattern.compile("META-INF/versions/\\d+/module-info.class");
        try (var inputStream =  new JarInputStream(new FileInputStream(jar))) {
            var isMultiReleaseJar = this.containsMultiReleaseJarEntry(inputStream);
            var next = inputStream.getNextEntry();
            while (next != null) {
                if ("module-info.class".equals(next.getName())) {
                    return true;
                }
                if (isMultiReleaseJar && moduleInfoClassMrjarPath.matcher(next.getName()).matches()) {
                    return true;
                }
                next = inputStream.getNextEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean containsMultiReleaseJarEntry(JarInputStream jarStream) {
        var manifest = jarStream.getManifest();
        return manifest != null && Boolean.parseBoolean(manifest.getMainAttributes().getValue("Multi-Release"));
    }

    private boolean isAutoModule(File jar) {
        try (var inputStream = new JarInputStream(new FileInputStream(jar))) {
            return inputStream.getManifest().getMainAttributes().getValue("Automatic-Module-Name") != null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getModuleJar(TransformOutputs outputs, File originalJar) {
        return outputs.file(originalJar.getName().substring(0, originalJar.getName().lastIndexOf('.')) + "-module.jar");
    }

    private static void addAutomaticModuleName(File originalJar, File moduleJar, String moduleName) {
        try (var inputStream = new JarInputStream(new FileInputStream(originalJar))) {
            var manifest = inputStream.getManifest();
            manifest.getMainAttributes().put(new Attributes.Name("Automatic-Module-Name"), moduleName);
            try (var outputStream = new JarOutputStream(new FileOutputStream(moduleJar), inputStream.getManifest())) {
                ExtraModuleInfoTransform.copyEntries(inputStream, outputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addModuleDescriptor(File originalJar, File moduleJar, ModuleInfo moduleInfo) {
        try (var inputStream = new JarInputStream(new FileInputStream(originalJar))) {
            try (var outputStream = new JarOutputStream(new FileOutputStream(moduleJar), inputStream.getManifest())) {
                ExtraModuleInfoTransform.copyEntries(inputStream, outputStream);
                outputStream.putNextEntry(new JarEntry("module-info.class"));
                outputStream.write(ExtraModuleInfoTransform.addModuleInfo(moduleInfo));
                outputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyEntries(JarInputStream inputStream, JarOutputStream outputStream) throws IOException {
        var jarEntry = inputStream.getNextJarEntry();
        while (jarEntry != null) {
            outputStream.putNextEntry(jarEntry);
            outputStream.write(ExtraModuleInfoTransform.readAllBytes(inputStream));
            outputStream.closeEntry();
            jarEntry = inputStream.getNextJarEntry();
        }
    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        var baos = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }
    private static byte[] addModuleInfo(ModuleInfo moduleInfo) {
        var classWriter = new ClassWriter(0);
        classWriter.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
        var moduleVisitor = classWriter.visitModule(moduleInfo.getModuleName(), Opcodes.ACC_OPEN, moduleInfo.getModuleVersion());
        for (var packageName : moduleInfo.getExports()) {
            moduleVisitor.visitExport(packageName.replace('.', '/'), 0);
        }
        moduleVisitor.visitRequire("java.base", 0, null);
        for (var requireName : moduleInfo.getRequires()) {
            moduleVisitor.visitRequire(requireName, 0, null);
        }
        for (var requireName : moduleInfo.getRequiresTransitive()) {
            moduleVisitor.visitRequire(requireName, Opcodes.ACC_TRANSITIVE, null);
        }
        moduleVisitor.visitEnd();
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
}
