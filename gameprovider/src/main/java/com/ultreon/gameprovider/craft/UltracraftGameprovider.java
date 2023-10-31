package com.ultreon.gameprovider.craft;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModDependency;
import org.quiltmc.loader.api.ModDependencyIdentifier;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.api.VersionRange;
import org.quiltmc.loader.api.plugin.ModMetadataExt;
import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.game.GameProviderHelper;
import org.quiltmc.loader.impl.game.LibClassifier;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.metadata.qmj.*;
import org.quiltmc.loader.impl.util.Arguments;
import org.quiltmc.loader.impl.util.ExceptionUtil;
import org.quiltmc.loader.impl.util.SystemProperties;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;
import org.quiltmc.loader.impl.util.log.LogHandler;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue", "unused"})
public class UltracraftGameprovider implements GameProvider {
    private static final String[] ALLOWED_EARLY_CLASS_PREFIXES = { "org.apache.logging.log4j.", "com.ultreon.gameprovider.craft.", "com.ultreon.premain." };
    private static final String MIN_JAVA_VERSION = String.valueOf(17);
    private static final String MAX_JAVA_VERSION = String.valueOf(20);

    private final GameTransformer transformer = new GameTransformer();
    private EnvType envType;
    private Arguments arguments;
    private final List<Path> gameJars = new ArrayList<>();
    private final List<Path> logJars = new ArrayList<>();
    private final List<Path> miscGameLibraries = new ArrayList<>();
    private Collection<Path> validParentClassPath = new ArrayList<>();
    private String entrypoint;
    private boolean log4jAvailable;
    private boolean slf4jAvailable;
    private Path libGdxJar;
    private final Properties versions;
    private final List<String> gamePackages = List.of(
            "com.ultreon.craft.",
            "com.ultreon.common.",
            "com.ultreon.corelibs.",
            "com.ultreon.premain.",
            "com.ultreon.gameprovider.craft.",
            "com.ultreon.gameprovider."
    );

    public UltracraftGameprovider() {
        InputStream stream = this.getClass().getResourceAsStream("/versions.properties");
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.versions = properties;

        System.setProperty("swing.systemlaf", FlatMacDarkLaf.class.getName());
        FlatMacDarkLaf.installLafInfo();
        FlatMacDarkLaf.setup();
    }

    @Override
    public String getGameId() {
        return "ultracraft";
    }

    @Override
    public String getGameName() {
        return "Ultracraft";
    }

    @Override
    public String getRawGameVersion() {
        return this.versions.getProperty("ultreoncraft");
    }

    @Override
    public String getNormalizedGameVersion() {
        return this.versions.getProperty("ultreoncraft");
    }


    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        InternalModMetadata built = this.createUltracraftMetadata();
        return List.of(new BuiltinMod(this.gameJars, built), new BuiltinMod(Collections.singletonList(this.libGdxJar), this.createLibGDXMetadata()));
    }

    private InternalModMetadata createUltracraftMetadata() {
        V1ModMetadataBuilder metadata = new V1ModMetadataBuilder();
        metadata.id = "craft";
        metadata.group = "builtin";
        metadata.version = Version.of(this.getNormalizedGameVersion());
        metadata.name = "Ultracraft";
        metadata.contributors.add(new ModContributorImpl("XyperCode", List.of("Owner", "Head Development", "Development")));
        metadata.contributors.add(new ModContributorImpl("Creatomat Gaming", List.of("Closed Beta Tester")));
        metadata.licenses.add(ModLicenseImpl.fromIdentifierOrDefault("Ultreon-Api-1.1"));
        metadata.repositories.add("https://github.com/Ultreon/ultracraft");
        metadata.repositories.add("https://github.com/Ultreon/ultracraft");
        metadata.contactInformation.put("homepage","https://ultreon.github.io");
        metadata.contactInformation.put("discord","https://discord.gg/WePT9v2CmQ");
        metadata.contactInformation.put("sources","https://github.com/Ultreon/ultracraft");
        metadata.contactInformation.put("issues","https://github.com/Ultreon/ultracraft/issues");
        metadata.loadType = ModMetadataExt.ModLoadType.ALWAYS;
        metadata.description = "The Ultracraft game. This is the game you are now playing.";
        metadata.breaks.add(new ModDependency.Only() {
            @Override
            public boolean shouldIgnore() {
                return false;
            }

            @Override
            public boolean matches(Version version) {
                return true;
            }

            @Override
            public ModDependencyIdentifier id() {
                return new ModDependencyIdentifierImpl("minecraft");
            }

            @Override
            public VersionRange versionRange() {
                return VersionRange.ANY;
            }

            @Override
            public String reason() {
                return "Different game.";
            }

            @Override
            public @Nullable ModDependency unless() {
                return null;
            }

            @Override
            public boolean optional() {
                return false;
            }
        });
        metadata.breaks.add(new ModDependency.Only() {
            @Override
            public boolean shouldIgnore() {
                return false;
            }

            @Override
            public boolean matches(Version version) {
                return true;
            }

            @Override
            public ModDependencyIdentifier id() {
                return new ModDependencyIdentifierImpl("bubbleblaster");
            }

            @Override
            public VersionRange versionRange() {
                return VersionRange.ANY;
            }

            @Override
            public String reason() {
                return "Different game.";
            }

            @Override
            public @Nullable ModDependency unless() {
                return null;
            }

            @Override
            public boolean optional() {
                return false;
            }
        });

        Version minJava = Version.of(UltracraftGameprovider.MIN_JAVA_VERSION);
        Version maxJava = Version.of(UltracraftGameprovider.MAX_JAVA_VERSION);
        VersionRange range = VersionRange.ofInterval(minJava, true, null, true);

        metadata.depends.add(new ModDependency.Only() {
            @Override
            public boolean shouldIgnore() {
                return false;
            }

            @Override
            public VersionRange versionRange() {
                return range;
            }

            @Override
            public ModDependency unless() {
                return null;
            }

            @Override
            public String reason() {
                return "";
            }

            @Override
            public boolean optional() {
                return false;
            }

            @Override
            public ModDependencyIdentifier id() {
                return ModDependencyIdentifier.of("", "java");
            }
        });

        return metadata.build();
    }

    private InternalModMetadata createLibGDXMetadata() {
        V1ModMetadataBuilder metadata = new V1ModMetadataBuilder();
        metadata.id = "libgdx";
        metadata.group = "builtin";
        metadata.version = Version.of(this.versions.getProperty("libgdx"));
        metadata.name = "LibGDX";
        metadata.contributors.add(new ModContributorImpl("LibGDX Development Team", List.of("Team")));
        metadata.contactInformation.put("homepage", "https://libgdx.com");
        metadata.contactInformation.put("discord", "https://libgdx.com/community/discord/");
        metadata.contactInformation.put("reddit", "https://reddit.com/r/libgdx/");
        metadata.licenses.add(ModLicenseImpl.fromIdentifierOrDefault("Apache-2.0"));
        return metadata.build();
    }

    @Override
    public String getEntrypoint() {
        return this.entrypoint;
    }

    @Override
    public Path getLaunchDirectory() {
        if (!Objects.equals(System.getProperty("ultracraft.environment", "normal"), "packaged"))
            return Path.of(".");

        Path path = getDataDir();

        return path;
    }

    @NotNull
    public static Path getDataDir() {
        Path path;
        if (OS.isWindows())
            path = Paths.get(System.getenv("APPDATA"), "Ultracraft");
        else if (OS.isMac())
            path = Paths.get(System.getProperty("user.home"), "Library/Application Support/Ultracraft");
        else if (OS.isLinux())
            path = Paths.get(System.getProperty("user.home"), ".config/Ultracraft");
        else
            throw new FormattedException("Unsupported Platform", "Platform unsupported: " + System.getProperty("os.name"));

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(QuiltLauncher launcher, String[] args) {
        this.envType = launcher.getEnvironmentType();
        this.arguments = new Arguments();
        this.arguments.parse(args);

        try {
            System.setProperty("io.netty.tryReflectionSetAccessible", "false");

            var classifier = new LibClassifier<>(GameLibrary.class, this.envType, this);
            var clientLib = GameLibrary.ULTREONCRAFT_CLIENT;
            var serverLib = GameLibrary.ULTREONCRAFT_SERVER;
            var gameJar = GameProviderHelper.getCommonGameJar();
            var commonGameJarDeclared = gameJar != null;

            if (commonGameJarDeclared) {
                classifier.process(gameJar);
            }

            classifier.process(launcher.getClassPath());

            gameJar = classifier.getOrigin(GameLibrary.ULTREONCRAFT_CLIENT);
            var coreJar = classifier.getOrigin(GameLibrary.ULTREONCRAFT_SERVER);
            this.libGdxJar = classifier.getOrigin(GameLibrary.LIBGDX);

            if (commonGameJarDeclared && gameJar == null) {
                Log.warn(LogCategory.GAME_PROVIDER, "The declared common game jar didn't contain any of the expected classes!");
            }

            if (gameJar != null) {
                this.gameJars.add(gameJar);
            }

            if (coreJar != null) {
                this.gameJars.add(coreJar);
            }

            if (this.libGdxJar != null) {
                this.gameJars.add(this.libGdxJar);
            }

            this.entrypoint = classifier.getClassName(clientLib);
            if (this.entrypoint == null) this.entrypoint = classifier.getClassName(serverLib);
            if (this.entrypoint == null) {
                throw new IllegalArgumentException("Can't find game entrypoint.");
            }
            this.log4jAvailable = classifier.has(GameLibrary.LOG4J_API) && classifier.has(GameLibrary.LOG4J_CORE);
            this.slf4jAvailable = classifier.has(GameLibrary.SLF4J_API) && classifier.has(GameLibrary.SLF4J_CORE);
            var hasLogLib = this.log4jAvailable || this.slf4jAvailable;

            Log.configureBuiltin(hasLogLib, !hasLogLib);

            for (var lib : GameLibrary.LOGGING) {
                var path = classifier.getOrigin(lib);

                if (path != null) {
                    if (hasLogLib) {
                        this.logJars.add(path);
                    } else if (!this.gameJars.contains(path)) {
                        this.miscGameLibraries.add(path);
                    }
                }
            }

            this.miscGameLibraries.addAll(classifier.getUnmatchedOrigins());
            this.validParentClassPath = classifier.getSystemLibraries();
        } catch (IOException e) {
            throw ExceptionUtil.wrap(e);
        }

        // expose obfuscated jar locations for mods to more easily remap code from obfuscated to intermediary
        var share = QuiltLoaderImpl.INSTANCE.getObjectShare();
        share.put("fabric-loader:inputGameJar", this.gameJars.get(0)); // deprecated
        share.put("fabric-loader:inputGameJars", this.gameJars);

        return true;
    }

    @Override
    public boolean isGameClass(String name) {
        for (var pak : this.gamePackages) {
            if (name.startsWith(pak)) return true;
        }
        return false;
    }

    @Override
    public void initialize(QuiltLauncher launcher) {
        // Load the logger libraries on the platform CL when in a unit test
        if (!this.logJars.isEmpty() && !Boolean.getBoolean(SystemProperties.UNIT_TEST)) {
            for (var jar : this.logJars) {
                if (this.gameJars.contains(jar)) {
                    launcher.addToClassPath(jar, UltracraftGameprovider.ALLOWED_EARLY_CLASS_PREFIXES);
                } else {
                    launcher.addToClassPath(jar);
                }
            }
        }

        this.setupLogHandler(launcher, true);

        this.transformer.locateEntrypoints(launcher, new ArrayList<>());
    }

    private void setupLogHandler(QuiltLauncher launcher, boolean useTargetCl) {
        System.setProperty("log4j2.formatMsgNoLookups", "true"); // lookups are not used by mc and cause issues with older log4j2 versions

        try {
            final var logHandlerClsName = "com.ultreon.gameprovider.craft.UltracraftLogHandler";

            var prevCl = Thread.currentThread().getContextClassLoader();
            Class<?> logHandlerCls;

            if (useTargetCl) {
                Thread.currentThread().setContextClassLoader(launcher.getTargetClassLoader());
                logHandlerCls = launcher.loadIntoTarget(logHandlerClsName);
            } else {
                logHandlerCls = Class.forName(logHandlerClsName);
            }

            Log.init((LogHandler) logHandlerCls.getConstructor().newInstance(), true);
            Thread.currentThread().setContextClassLoader(prevCl);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return this.transformer;
    }

    @Override
    public boolean hasAwtSupport() {
        return true;
    }

    @Override
    public void unlockClassPath(QuiltLauncher launcher) {
        for (var gameJar : this.gameJars) {
            if (this.logJars.contains(gameJar)) {
                launcher.setAllowedPrefixes(gameJar);
            } else {
                launcher.addToClassPath(gameJar);
            }
        }

        for (var lib : this.miscGameLibraries) {
            launcher.addToClassPath(lib);
        }
    }

    public Path getGameJar() {
        return this.gameJars.get(0);
    }

    @Override
    public void launch(ClassLoader loader) {
        var targetClass = this.entrypoint;

        Path launchDirectory = this.getLaunchDirectory();
        String absolutePath = launchDirectory.toFile().getAbsolutePath();
        System.setProperty("user.dir", absolutePath);

        MethodHandle invoker;

        try {
            var c = loader.loadClass(targetClass);
            invoker = MethodHandles.lookup().findStatic(c, "main", MethodType.methodType(void.class, String[].class));
        } catch (NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            throw new FormattedException("Failed to start Ultracraft", e);
        }

        try {
            //noinspection ConfusingArgumentToVarargsMethod
            invoker.invokeExact(this.arguments.toArray());
        } catch (Throwable t) {
            throw new FormattedException("Ultracraft has crashed", t);
        }
    }

    @Override
    public Arguments getArguments() {
        return this.arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }
}
