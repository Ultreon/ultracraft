package com.ultreon.bubbles.gradle;

import com.google.gson.JsonArray;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.repositories.UrlArtifactRepository;
import org.gradle.api.internal.artifacts.dependencies.AbstractModuleDependency;
import org.gradle.api.tasks.Internal;
import org.gradle.jvm.tasks.Jar;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Retrieve URLs for the dependencies that will be downloaded by the game.
*/
public class RetrieveUrlsTask extends BaseTask {
    private final JsonArray urls = new JsonArray();
    private final List<com.ultreon.bubbles.gradle.Dependency> gameDeps = new ArrayList<>();

    public RetrieveUrlsTask() {
        super("retrieveUrls", "bubbles");

        this.doFirst(this::execute);
    }

    private void execute(Task task) {
        var project = task.getProject();
        var dependencies = project.getConfigurations().getByName("api").getDependencies();
        for (var dependency : dependencies) {
            if (dependency instanceof AbstractModuleDependency) {
                var dep = (AbstractModuleDependency) dependency;
                for (var repository : project.getRepositories()) {
                    if (repository instanceof UrlArtifactRepository) {
                        var urlRepo = (UrlArtifactRepository) repository;
                        var url = urlRepo.getUrl();
                        this.retrieveDependencyInfo(dependency, url, dep);
                        this.retrieveArtifactsInfo(dependency, url, dep);
                    }
                }
            }
            if (dependency instanceof ProjectDependency) {
                var dep = (ProjectDependency) dependency;
                var jarTask = dep.getDependencyProject().getTasks().getByName("jar");
                if (!(jarTask instanceof Jar)) {
                    throw new IllegalArgumentException("Gradle task 'jar' isn't an instance of org.gradle.jvm.tasks.Jar");
                }
                var jar = (Jar) jarTask;

                var name = jar.getArchiveBaseName().get();
                var group = project.getGroup().toString();
                var version = project.getVersion().toString();

                var nullableUrl = project.property("bubbles.project_dependency_url");
                if (nullableUrl == null) continue;
                var url = nullableUrl.toString();

                var jarUrl = String.format("%s%s/%s/%s/%s-%s-%s.%s",
                        url, group.replaceAll("\\.", "/"),
                        name, version,
                        name, version,
                        jar.getArchiveClassifier().get(), jar.getArchiveExtension());

                var platform = Platform.get(dep.getTargetConfiguration());
                if (platform == null) {
                    continue;
                }
                try {
                    var jarFile = new URL(jarUrl);
                    try (var in = jarFile.openStream()) {
                        if (in != null) {
                            this.getLogger().info(String.format("%s:%s:%s:%s:%s",
                                    dependency.getGroup(), dependency.getName(), dependency.getVersion(),
                                    jar.getArchiveClassifier(), jar.getArchiveExtension()));
                            this.urls.add(jarFile.toString());
                            this.gameDeps.add(new com.ultreon.bubbles.gradle.Dependency(
                                    Objects.requireNonNull(dependency.getGroup(), "Dependency should have group."),
                                    dependency.getName(),
                                    Objects.requireNonNull(dependency.getVersion(), "Dependency should have version."),
                                    platform,
                                    jar.getArchiveClassifier().get(),
                                    jar.getArchiveExtension().get(),
                                    url));
                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void retrieveArtifactsInfo(Dependency dependency, URI url, AbstractModuleDependency dep) {
        for (var artifact : dep.getArtifacts()) {
            var platform = Platform.get(dep.getTargetConfiguration());
            if (platform == null) {
                return;
            }
            if (dependency.getGroup() != null) {
                var jarUrl = String.format("%s%s/%s/%s/%s-%s-%s.%s",
                        url, dependency.getGroup().replaceAll("\\.", "/"),
                        dependency.getName(), dependency.getVersion(),
                        dependency.getName(), dependency.getVersion(),
                        artifact.getClassifier(), artifact.getExtension());

                try {
                    var jarFile = new URL(jarUrl);
                    try (var in = jarFile.openStream()) {
                        if (in != null) {
                            this.getLogger().info(String.format("%s:%s:%s:%s:%s",
                                    dependency.getGroup(), dependency.getName(), dependency.getVersion(),
                                    artifact.getClassifier(), artifact.getExtension()));
                            this.urls.add(jarFile.toString());
                            this.gameDeps.add(new com.ultreon.bubbles.gradle.Dependency(
                                    Objects.requireNonNull(dependency.getGroup(), "Dependency should have group."),
                                    dependency.getName(),
                                    Objects.requireNonNull(dependency.getVersion(), "Dependency should have version."),
                                    platform,
                                    artifact.getClassifier(),
                                    artifact.getExtension(),
                                    url.toString()));
                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    private void retrieveDependencyInfo(Dependency dependency, URI url, AbstractModuleDependency dep) {
        var artifacts = dep.getArtifacts();
        var platform = Platform.get(dep.getTargetConfiguration());
        if (platform == null) {
            return;
        }
        if (artifacts.isEmpty()) {
            if (dependency.getGroup() != null) {
                var jarUrl = String.format("%s%s/%s/%s/%s-%s.jar",
                        url, dependency.getGroup().replaceAll("\\.", "/"),
                        dependency.getName(), dependency.getVersion(),
                        dependency.getName(), dependency.getVersion());

                try {
                    var jarFile = new URL(jarUrl);
                    try (var in = jarFile.openStream()) {
                        if (in != null) {
                            this.getLogger().info(String.format("%s:%s:%s", dependency.getGroup(), dependency.getName(), dependency.getVersion()));
                            this.urls.add(jarFile.toString());
                            this.gameDeps.add(new com.ultreon.bubbles.gradle.Dependency(
                                    Objects.requireNonNull(dependency.getGroup(), "Dependency should have group."),
                                    dependency.getName(),
                                    Objects.requireNonNull(dependency.getVersion(), "Dependency should have version."),
                                    platform,
                                    url.toString()));
                        }
                    }
                } catch (Exception ignored) {

                }
            }
        }
    }

    @Internal
    public List<com.ultreon.bubbles.gradle.Dependency> getGameDeps() {
        return this.gameDeps;
    }

    @Internal
    public JsonArray getUrls() {
        return this.urls;
    }
}
