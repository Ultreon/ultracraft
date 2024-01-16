package com.ultreon.gameprovider.craft;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.GameProviderHelper;
import net.fabricmc.loader.impl.game.LibClassifier;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.ExceptionUtil;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import org.jetbrains.annotations.NotNull;

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

    private final GameTransformer transformer = new GameTransformer();
    private EnvType env;
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

    public UltracraftGameprovider() {
        InputStream stream = this.getClass().getResourceAsStream("/versions.properties");
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.versions = properties;
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
        return List.of(
                new BuiltinMod(List.of(this.libGdxJar), new BuiltinModMetadata.Builder("gdx", this.versions.getProperty("gdx"))
                        .setName("LibGDX")
                        .setDescription("A game framework used by Ultracraft (and various other games).")
                        .addLicense("Apache-2.0")
                        .addAuthor("libGDX", Map.of("homepage", "http://www.libgdx.com/", "patreon", "https://patreon.com/libgdx", "github", "https://github.com/libgdx", "sources", "https://github.com/libgdx/libgdx"))
                        .addAuthor("Mario Zechner", Map.of("github", "https://github.com/badlogic", "email", "badlogicgames@gmail.com"))
                        .addAuthor("Nathan Sweet", Map.of("github", "https://github.com/NathanSweet", "email", "nathan.sweet@gmail.com"))
                        .addIcon(200, "assets/gdx/icon.png")
                        .build()),
                new BuiltinMod(this.gameJars, new BuiltinModMetadata.Builder("ultracraft", this.versions.getProperty("ultreoncraft"))
                        .addLicense("Ultreon-Api-1.1")
                        .addAuthor("Ultreon Team", Map.of("github", "https://github.com/badlogic", "email", "badlogicgames@gmail.com"))
                        .addContributor("XyperCode", Map.of("github", "https://github.com/XyperCode"))
                        .addContributor("Creatomat Gaming", Map.of("github", "https://github.com/Creatomat"))
                        .addIcon(128, "assets/craft/icon.png")
                        .setEnvironment(ModEnvironment.UNIVERSAL)
                        .setContact(new ContactInformationImpl(Map.of("sources", "https://github.con/Ultreon/ultreon-craft", "email", "contact.ultreon@gmail.com", "discord", "https://discord.gg/sQsU7sE2Sx")))
                        .setDescription("It's the game you are now playing.")
                        .setName("Ultracraft")
                        .build())
        );
    }

    @Override
    public String getEntrypoint() {
        return this.entrypoint;
    }

    @SuppressWarnings("NewApi")
    @Override
    public Path getLaunchDirectory() {
        if (!Objects.equals(System.getProperty("ultracraft.environment", "normal"), "packaged"))
            return Path.of(".");

        return UltracraftGameprovider.getDataDir();
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
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        this.env = launcher.getEnvironmentType();
        this.arguments = new Arguments();
        this.arguments.parse(args);

        try {
            var classifier = new LibClassifier<>(GameLibrary.class, this.env, this);
            var clientLib = GameLibrary.ULTRACRAFT_CLIENT;
            var serverLib = GameLibrary.ULTRACRAFT_SERVER;
            var clientJar = GameProviderHelper.getCommonGameJar();
            var commonGameJarDeclared = clientJar != null;

            if (commonGameJarDeclared) {
                classifier.process(clientJar);
            }

            classifier.process(launcher.getClassPath());

            clientJar = classifier.getOrigin(GameLibrary.ULTRACRAFT_CLIENT);
            var serverJar = classifier.getOrigin(GameLibrary.ULTRACRAFT_SERVER);
            this.libGdxJar = classifier.getOrigin(GameLibrary.LIBGDX);

            if (commonGameJarDeclared && clientJar == null) {
                Log.warn(LogCategory.GAME_PROVIDER, "The declared common game jar didn't contain any of the expected classes!");
            }

            if (clientJar != null) {
                this.gameJars.add(clientJar);
            }

            if (serverJar != null) {
                this.gameJars.add(serverJar);
            }

            if (this.libGdxJar != null) {
                this.gameJars.add(this.libGdxJar);
            }

            this.entrypoint = classifier.getClassName(clientLib);
            if (this.entrypoint == null) {
                this.entrypoint = classifier.getClassName(serverLib);
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
        var share = FabricLoaderImpl.INSTANCE.getObjectShare();
        share.put("fabric-loader:inputGameJar", this.gameJars.get(0)); // deprecated
        share.put("fabric-loader:inputGameJars", this.gameJars);

        return true;
    }

    @Override
    public void initialize(FabricLauncher launcher) {
        launcher.setValidParentClassPath(this.validParentClassPath);

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

    private void setupLogHandler(FabricLauncher launcher, boolean useTargetCl) {
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

            Log.init((LogHandler) logHandlerCls.getConstructor().newInstance());
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
        return false;
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {
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
    public boolean canOpenErrorGui() {
        if (this.arguments == null || this.env == env.CLIENT)
            return !OS.isMobile();

        return false;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }
}
