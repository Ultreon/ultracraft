package com.ultreon.craft.client;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultreon.craft.CrashHandler;
import com.ultreon.craft.ModInit;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.craft.client.audio.ClientSound;
import com.ultreon.craft.client.config.GameSettings;
import com.ultreon.craft.client.events.ClientLifecycleEvents;
import com.ultreon.craft.client.events.ScreenEvents;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.font.FontRegistry;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screen.ModIconOverrides;
import com.ultreon.craft.client.gui.screens.*;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.Fonts;
import com.ultreon.craft.client.input.DesktopInput;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.PlayerInput;
import com.ultreon.craft.client.item.ItemRenderer;
import com.ultreon.craft.client.model.*;
import com.ultreon.craft.client.network.ClientConnections;
import com.ultreon.craft.client.network.LoginClientPacketHandlerImpl;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.registry.LanguageRegistry;
import com.ultreon.craft.client.resources.ResourceFileHandle;
import com.ultreon.craft.client.rpc.Activity;
import com.ultreon.craft.client.rpc.RpcHandler;
import com.ultreon.craft.client.shader.Shaders;
import com.ultreon.craft.client.sound.ClientSoundRegistry;
import com.ultreon.craft.client.text.LanguageData;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.client.util.DeferredDisposable;
import com.ultreon.craft.client.util.GG;
import com.ultreon.craft.client.util.Resizer;
import com.ultreon.craft.client.world.ClientWorld;
import com.ultreon.craft.client.world.FaceProperties;
import com.ultreon.craft.client.world.WorldRenderer;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.item.tool.ToolItem;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.c2s.C2SLoginPacket;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.server.ServerConstants;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.util.*;
import com.ultreon.craft.world.BlockPos;
import com.ultreon.craft.world.SoundEvent;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.WorldStorage;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3d;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.datetime.v0.Duration;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
import com.ultreon.libs.resources.v0.ResourceManager;
import com.ultreon.libs.translations.v1.Language;
import com.ultreon.libs.translations.v1.LanguageManager;
import io.netty.channel.ChannelFuture;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;

@SuppressWarnings("UnusedReturnValue")
public class UltracraftClient extends PollingExecutorService implements DeferredDisposable {
    public static final String NAMESPACE = ServerConstants.NAMESPACE;
    public static final Logger LOGGER = LoggerFactory.getLogger("UltracraftClient");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    public static final int[] SIZES = new int[]{16, 24, 32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};
    private static final int CULL_FACE = GL_FRONT;
    public static final float FROM_ZOOM = 2.0f;
    public static final float TO_ZOOM = 1.3f;
    private static final float DURATION = 6000f;
    private static ArgParser arguments;
    public Connection connection;
    public ServerData serverData;
    public List<Vec3d> remotePlayers = new CopyOnWriteArrayList<>();
    private Duration bootTime;
    private String allUnicode;
    private GarbageCollector garbageCollector;
    private GameEnvironment gameEnv;
    private final Sound logoRevealSound;
    private final Texture ultreonBgTex;
    private final Texture ultreonLogoTex;
    private final Texture libGDXLogoTex;
    private final Resizer resizer;
    private boolean showUltreonSplash;
    private boolean showLibGDXSplash = true;
    private long ultreonSplashTime;
    private long libGDXSplashTime;
    public FileHandle configDir;

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";
    @UnknownNullability
    private static WorldStorage worldStorage;
    public boolean forceUnicode = false;
    public ItemRenderer itemRenderer;
    public Notifications notifications = new Notifications(this);
    @SuppressWarnings("FieldMayBeFinal")
    private boolean booted;
    public Font font;
    @UnknownNullability
    public BitmapFont unifont;
    public GameInput input;
    @Nullable
    public ClientWorld world;
    @Nullable
    public WorldRenderer worldRenderer;
    @UnknownNullability
    @SuppressWarnings("GDXJavaStaticResource")
    private static UltracraftClient instance;
    @Nullable
    public ClientPlayer player;
    @NotNull
    private final SpriteBatch spriteBatch;
    public final ModelBatch modelBatch;
    public final GameCamera camera;
    public final PlayerInput playerInput = new PlayerInput(this);
    private boolean isDevMode;
    @Nullable
    public Screen screen;
    public GameSettings settings;
    public final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final ResourceManager resourceManager;
    private final float guiScale = this.calculateGuiScale();

    public Hud hud;
    public HitResult hitResult;
    private Vec3i breaking;
    @Nullable
    private Block breakingBlock;
    public boolean showDebugHud = false;

    // Public Flags
    public boolean renderWorld = false;

    // Startup time
    public static final long BOOT_TIMESTAMP = System.currentTimeMillis();

    // Texture Atlases
    @UnknownNullability
    public TextureAtlas blocksTextureAtlas;
    @UnknownNullability
    public TextureAtlas itemTextureAtlas;
    private BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Nullable
    private Integer deferredWidth;
    @Nullable
    private Integer deferredHeight;
    private Texture windowTex;
    private DebugGui debugRenderer;
    private boolean closingWorld;
    private int oldSelected;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private boolean loading = true;
    private final Thread mainThread;
    public HitResult cursor;
    private LoadingOverlay loadingOverlay;
    private final String[] argv;
    private final ClientSoundRegistry soundRegistry = new ClientSoundRegistry();
    public IntegratedServer integratedServer;
    private final User user;
    private int currentTps;
    private double tickTime = 0d;
    private double gameFrameTime = 0d;
    private int ticksPassed = 0;

    double time = System.currentTimeMillis();
    private Activity activity = null;
    private Activity oldActivity = null;
    private Vec2i oldMode;
    private boolean isInThirdPerson;


    public UltracraftClient(String[] argv) throws Throwable {
        UltracraftClient.LOGGER.info("Booting game!");
        UltracraftClient.instance = this;

        this.argv = argv;

        this.user = new User("Player" + MathUtils.random(100, 999));

        this.mainThread = Thread.currentThread();

        ImGuiOverlay.preInitImGui();

        this.resourceManager = new ResourceManager("assets");
        this.textureManager = new TextureManager(this.resourceManager);

        this.camera = new GameCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.near = 0.01f;
        this.camera.far = 2;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        TextureRegion white = new TextureRegion(new Texture(pixmap));

        this.spriteBatch = new SpriteBatch();
        this.shapes = new ShapeDrawer(this.spriteBatch, white);

        var config = new DepthShader.Config();
        config.defaultCullFace = GL_FRONT;
        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
        this.modelBatch.getRenderContext().setCullFace(UltracraftClient.CULL_FACE);

        this.ultreonBgTex = new Texture("assets/craft/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/craft/logo.png");
        this.libGDXLogoTex = new Texture("assets/craft/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/craft/sounds/logo_reveal.mp3"));

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());
    }

    public static FileHandle data(String path) {
        return Gdx.files.absolute(UltracraftClient.getGameDir().toAbsolutePath().toString()).child(path);
    }

    @ApiStatus.Internal
    public static void main(String[] argv) {
        try {
            UltracraftClient.arguments = new ArgParser(argv);

            try {
                CrashHandler.addHandler(crashLog -> {
                    try {
                        Gdx.input.setCursorCatched(false);
                        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
                        graphics.getWindow().setVisible(false);
                    } catch (Throwable ignored) {

                    }
                });

                UltracraftClient.launch(argv);
            } catch (Error e) {
                CrashHandler.handleCrash(new CrashLog("Launch failed", e).createCrash().getCrashLog());
            }
        } catch (Throwable t) {
            try {
                UltracraftClient.LOGGER.error("Launch failed!", t);
                UltracraftClient.crash(t);
            } catch (Throwable throwable) {
                try {
                    UltracraftClient.LOGGER.error("Fatal error occurred when trying to launch the game!", throwable);
                } catch (Throwable ignored) {
                    Runtime.getRuntime().halt(2);
                }
            }
        }
    }

    private static void launch(String[] argv) {
        UltracraftClient.logDebug();

        FlatMacLightLaf.setup();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> UltracraftClient.LOGGER.error("GLFW Error: " + description))) {
            try {
                new Lwjgl3Application(new GameLibGDXWrapper(argv), UltracraftClient.createConfig());
            } catch (Throwable t) {
                UltracraftClient.LOGGER.error("Failed to create LWJGL3 Application:", t);
                JOptionPane.showMessageDialog(null, t.getMessage() + "\n\nCheck the debug.log file for more info!", "Launch failed!", JOptionPane.ERROR_MESSAGE);
                Runtime.getRuntime().halt(1);
            }
        }
    }

    @NotNull
    private static Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(true);
        config.setForegroundFPS(120);
        config.setIdleFPS(10);
        config.setBackBufferConfig(8, 8, 8, 8, 8, 0, 0);
        config.setInitialVisible(false);
        config.setTitle("Ultracraft");
        config.setWindowIcon(UltracraftClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new Lwjgl3WindowAdapter() {
            @Override
            public void created(Lwjgl3Window window) {
                Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
                Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
                Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
                Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);
            }

            @Override
            public void focusLost() {
                UltracraftClient.get().pause();
            }

            @Override
            public boolean closeRequested() {
                return UltracraftClient.get().tryShutdown();
            }

            @Override
            public void filesDropped(String[] files) {
                UltracraftClient.get().filesDropped(files);
            }

        });
        return config;
    }

    private static void logDebug() {
        if (UltracraftClient.isPackaged()) UltracraftClient.LOGGER.warn("Running in the JPackage environment.");
        UltracraftClient.LOGGER.debug("Java Version: " + System.getProperty("java.version"));
        UltracraftClient.LOGGER.debug("Java Vendor: " + System.getProperty("java.vendor"));
        UltracraftClient.LOGGER.debug("Operating System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")");
    }

    private static String[] getIcons() {
        String[] icons = new String[UltracraftClient.SIZES.length];
        for (int i = 0, sizesLength = UltracraftClient.SIZES.length; i < sizesLength; i++) {
            var size = UltracraftClient.SIZES[i];
            icons[i] = "icons/icon_" + size + ".png";
        }

        return icons;
    }

    /**
     * Check whether the application is packaged using JPackage.
     *
     * @return true if in the JPackage environment.
     */
    public static boolean isPackaged() {
        return UltracraftClient.arguments.getFlags().contains("packaged");
    }

    public static Path getGameDir() {
        return QuiltLoader.getGameDir();
    }

    private void load() throws Throwable {
        Identifier.setDefaultNamespace(UltracraftClient.NAMESPACE);

        var argList = Arrays.asList(this.argv);
        this.isDevMode = argList.contains("--dev") && QuiltLoader.isDevelopmentEnvironment();

        if (QuiltLoader.isDevelopmentEnvironment())
            this.gameEnv = GameEnvironment.DEVELOPMENT;
        else if (Objects.equals(System.getProperty("ultracraft.environment", "normal"), "packaged"))
            this.gameEnv = GameEnvironment.PACKAGED;
        else
            this.gameEnv = GameEnvironment.NORMAL;

        if (this.isDevMode)
            UltracraftClient.LOGGER.info("Developer mode is enabled");

        Thread.setDefaultUncaughtExceptionHandler(UltracraftClient::uncaughtException);

        this.loadingOverlay.setProgress(0.075F);

        Gdx.app.setApplicationLogger(new LibGDXLogger());

        this.configDir = UltracraftClient.createDir("config/");
        this.garbageCollector = new GarbageCollector();

        UltracraftClient.createDir("screenshots/");
        UltracraftClient.createDir("game-crashes/");
        UltracraftClient.createDir("logs/");

        this.setupMods();

        this.settings = new GameSettings();
        this.settings.reload();
        this.settings.reloadLanguage();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        this.loadingOverlay.setProgress(0.15F);

        UltracraftClient.LOGGER.info("Importing resources");
        this.resourceManager.importDeferredPackage(this.getClass());
        this.importModResources(this.resourceManager);

        this.loadingOverlay.setProgress(0.35F);

        UltracraftClient.LOGGER.info("Generating bitmap fonts");
        var resource = this.resourceManager.getResource(UltracraftClient.id("texts/unicode.txt"));
        if (resource == null) throw new FileNotFoundException("Unicode resource not found!");
        this.allUnicode = new String(resource.loadOrGet(), StandardCharsets.UTF_16);

        this.unifont = UltracraftClient.invokeAndWait(() -> new BitmapFont(Gdx.files.internal("assets/craft/font/unifont/unifont.fnt"), true));
        this.font = new Font(UltracraftClient.invokeAndWait(() -> new BitmapFont(Gdx.files.internal("assets/craft/font/dogica/dogicapixel.fnt"), true)));

        this.loadingOverlay.setProgress(0.7F);

        //----------------------
        // Setting up rendering
        //----------------------
        UltracraftClient.LOGGER.info("Initializing rendering stuffs");
        UltracraftClient.invokeAndWait(() -> {
            this.input = this.createInput();
        });
        Gdx.input.setInputProcessor(this.input);

        UltracraftClient.LOGGER.info("Setting up HUD");
        this.hud = UltracraftClient.invokeAndWait(() -> new Hud(this));

        UltracraftClient.LOGGER.info("Setting up Debug Renderer");
        this.debugRenderer = new DebugGui(this);

        this.loadingOverlay.setProgress(0.83F);

        //--------------------------
        // Registering game content
        //--------------------------
        UltracraftClient.LOGGER.info("Loading languages");
        this.loadLanguages();

        UltracraftClient.LOGGER.info("Registering stuff");
        Registries.init();

        Blocks.nopInit();
        Items.nopInit();
        NoiseSettingsInit.nopInit();
        EntityTypes.nopInit();
        Fonts.nopInit();
        UltracraftClient.invokeAndWait(Shaders::nopInit);

        for (var registry : Registry.getRegistries()) {
            RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
        }

        Registry.freeze();

        UltracraftClient.LOGGER.info("Registering models");
        this.registerModels();

        this.loadingOverlay.setProgress(0.95F);

        //*
        //* Post-initialize game content
        //* Such as model baking and texture stitching
        //*
        UltracraftClient.LOGGER.info("Stitching textures");
        this.stitchTextures();

        this.loadingOverlay.setProgress(0.98F);

        this.itemRenderer = UltracraftClient.invokeAndWait(() -> new ItemRenderer(this));

        UltracraftClient.LOGGER.info("Initializing sounds");
        this.soundRegistry.registerSounds();

        UltracraftClient.LOGGER.info("Baking models");
        this.bakedBlockModels = BlockModelRegistry.bake(this.blocksTextureAtlas);

        if (this.deferredWidth != null && this.deferredHeight != null) {
            this.camera.viewportWidth = this.deferredWidth;
            this.camera.viewportHeight = this.deferredHeight;
            this.camera.update();
        }

        this.windowTex = this.textureManager.getTexture(UltracraftClient.id("textures/gui/window.png"));

        this.loadingOverlay.setProgress(0.99F);

        ClientLifecycleEvents.GAME_LOADED.factory().onGameLoaded(this);

        this.loading = false;

        //*************//
        // Final stuff //
        //*************//
        UltracraftClient.LOGGER.info("Opening title screen");

        UltracraftClient.worldStorage = new WorldStorage(UltracraftClient.data("world").path());

        ImGuiOverlay.setupImGui();

        this.booted = true;

        this.loadingOverlay.setProgress(1.0F);

        this.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - UltracraftClient.BOOT_TIMESTAMP);
        UltracraftClient.LOGGER.info("Game booted in " + this.bootTime + "ms");

        UltracraftClient.invoke(new Task<>(UltracraftClient.id("main/show_title_screen"), () -> this.showScreen(new TitleScreen())));
        this.loadingOverlay = null;
    }

    public void importModResources(ResourceManager resourceManager) {
        for (ModContainer mod : QuiltLoader.getAllMods()) {
            for (Path rootPath : mod.getSourcePaths().stream().reduce(new ArrayList<>(), (objects, paths) -> {
                objects.addAll(paths);
                return objects;
            })) {
                try {
                    resourceManager.importPackage(rootPath);
                } catch (IOException e) {
                    UltracraftClient.LOGGER.warn("Importing resources failed for path: " + rootPath.toFile(), e);
                }
            }
        }
    }

    private void setupMods() {
        ModIconOverrides.set("craft", UltracraftClient.id("icon.png"));
        ModIconOverrides.set("libgdx", new Identifier("libgdx", "icon.png"));

        // Invoke entry points.
        EntrypointUtil.invoke(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
        EntrypointUtil.invoke(ClientModInit.ENTRYPOINT_KEY, ClientModInit.class, ClientModInit::onInitializeClient);
    }

    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        return UltracraftClient.instance.submit(func).join();
    }

    public static void invokeAndWait(Runnable func) {
        UltracraftClient.instance.submit(func).join();
    }

    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        return UltracraftClient.instance.submit(func);
    }

    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
        return UltracraftClient.instance.submit(func);
    }

    public static FileHandle resource(Identifier id) {
        return new ResourceFileHandle(id);
    }

    private static void uncaughtException(Thread t, Throwable e) {
        UltracraftClient.LOGGER.error("Exception in thread \"" + t.getName() + "\":", e);
    }

    public static boolean isOnMainThread() {
        return Thread.currentThread().getId() == UltracraftClient.instance.mainThread.getId();
    }

    public static String strId(String path) {
        return UltracraftClient.id(path).toString();
    }

    @Override
    public <T extends Disposable> T deferDispose(T disposable) {
        UltracraftClient.instance.disposables.add(disposable);
        return disposable;
    }

    public Duration getBootTime() {
        return this.bootTime;
    }

    public void delayCrash(CrashLog crashLog) {
        Gdx.app.postRunnable(() -> {
            var finalCrash = new CrashLog("An error occurred", crashLog, new RuntimeException("Delayed crash"));
            UltracraftClient.crash(finalCrash);
        });
    }

    public static UltracraftClient get() {
        return UltracraftClient.instance;
    }

    public static Identifier id(String path) {
        return new Identifier(path);
    }

    public static GG ggBro() {
        return new GG();
    }

    private void loadLanguages() {
        var internal = Gdx.files.internal("assets/craft/languages.json");
        List<String> languages;
        try (var reader = internal.reader()) {
            languages = UltracraftClient.GSON.fromJson(reader, LanguageData.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load languages register", e);
        }

        for (var language : languages) {
            this.registerLanguage(UltracraftClient.id(language));
        }

        LanguageRegistry.doRegistration(this::registerLanguage);
    }

    private void registerLanguage(Identifier id) {
        var s = id.path().split("_", 2);
        var locale = s.length == 1 ? new Locale(s[0]) : new Locale(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, this.resourceManager);
    }

    private void stitchTextures() {
        this.blocksTextureAtlas = BlockModelRegistry.stitch(this.textureManager);

        TextureStitcher itemTextures = new TextureStitcher();
        for (Map.Entry<Identifier, Item> e : Registries.ITEMS.entries()) {
            if (e.getValue() == Items.AIR) continue;
            if (e.getValue() instanceof BlockItem) continue;

            Identifier texId = e.getKey().mapPath(path -> "textures/items/" + path + ".png");
            Texture tex = this.textureManager.getTexture(texId);
            itemTextures.add(texId, tex);
        }
        this.itemTextureAtlas = itemTextures.stitch();
    }

    private void registerModels() {
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, CubeModel.of(UltracraftClient.id("blocks/grass_top"), UltracraftClient.id("blocks/dirt"), UltracraftClient.id("blocks/grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.registerDefault(Blocks.ERROR);
        BlockModelRegistry.registerDefault(Blocks.DIRT);
        BlockModelRegistry.registerDefault(Blocks.SAND);
        BlockModelRegistry.registerDefault(Blocks.WATER);
        BlockModelRegistry.registerDefault(Blocks.STONE);
        BlockModelRegistry.registerDefault(Blocks.COBBLESTONE);
    }

    private GameInput createInput() {
        return new DesktopInput(this, this.camera);
    }

    private static FileHandle createDir(String dirName) {
        var directory = UltracraftClient.data(dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            directory.delete();
            directory.mkdirs();
        }
        return directory;
    }

    public void pause() {
        if (this.screen == null && this.world != null) {
            this.showScreen(new PauseScreen());
        }
    }

    public void resume() {
        if (this.screen instanceof PauseScreen && this.world != null) {
            this.showScreen(null);
        }
    }

    @CanIgnoreReturnValue
    public boolean showScreen(@Nullable Screen open) {
        var cur = this.screen;
        if (open == null && this.world == null) {
            open = new TitleScreen();
        }

        if (open == null) {
            if (cur == null) return false;

            var result = ScreenEvents.CLOSE.factory().onCloseScreen(this.screen);
            if (result.isCanceled()) return false;

            UltracraftClient.LOGGER.debug("Closing screen: " + this.screen.getClass());

            if (!cur.close(null)) return false;
            this.screen = null;
            Gdx.input.setCursorCatched(true);

            return true;
        }
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(open);
        if (openResult.isCanceled()) {
            return false;
        }

        if (openResult.isInterrupted()) {
            open = openResult.getValue();
        }

        if (cur != null) {
            var closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(cur);
            if (closeResult.isCanceled()) return false;

            if (!cur.close(open)) return false;
            if (open != null) {
                UltracraftClient.LOGGER.debug("Changing screen to: " + open.getClass());
            } else {
                UltracraftClient.LOGGER.debug("Closing screen: " + cur.getClass());
            }
        } else {
            if (open != null) {
                Gdx.input.setCursorCatched(false);
                UltracraftClient.LOGGER.debug("Opening screen: " + open.getClass());
            } else {
                return false;
            }
        }

        this.screen = open;
        if (this.screen != null) {
            this.screen.init();
        }

        return true;
    }

    //from https://www.java2s.com
    public static double interpolate(double a, double b, double d) {
        return a + (b - a) * d;
    }

    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        try {
            if (Gdx.graphics.getFrameId() == 2) {
                UltracraftClient.firstRender();
                Gdx.graphics.setTitle("Ultracraft " + QuiltLoader.getRawGameVersion());
            }

            if (this.activity != this.oldActivity) {
                this.oldActivity = this.activity;
                if (this.activity == null) {
                    Gdx.graphics.setTitle("Ultracraft " + QuiltLoader.getRawGameVersion());
                } else {
                    var name = this.activity.getDisplayName();
                    Gdx.graphics.setTitle("Ultracraft " + QuiltLoader.getRawGameVersion() + " - " + name);
                }

                RpcHandler.setActivity(this.activity);
            }

            this.pollAll();

            var canTick = false;

            double time2 = System.currentTimeMillis();
            var passed = time2 - this.time;
            this.gameFrameTime += passed;
            this.tickTime += passed;

            this.time = time2;

            double tickCap = 1000.0 / (double) UltracraftServer.TPS;
            while (this.gameFrameTime >= tickCap) {
                this.gameFrameTime -= tickCap;

                canTick = true;
            }

            if (canTick) {
                this.ticksPassed++;
                try {
                    this.clientTick();
                } catch (Throwable t) {
                    var crashLog = new CrashLog("Game being ticked.", t);
                    UltracraftClient.crash(crashLog.createCrash());
                }
            }

            if (this.tickTime >= 1000.0d) {
                this.currentTps = this.ticksPassed;
                this.ticksPassed = 0;
                this.tickTime = 0;
            }

            if (this.showLibGDXSplash) {
                if (this.libGDXSplashTime == 0L) {
                    this.libGDXSplashTime = System.currentTimeMillis();
                }

                ScreenUtils.clear(1, 1, 1, 1, true);
                Renderer renderer = new Renderer(this.shapes);

                this.spriteBatch.begin();
                int size = Math.min(this.getWidth(), this.getHeight()) / 2;
                renderer.blit(this.libGDXLogoTex, (float) this.getWidth() / 2 - (float) size / 2, (float) this.getHeight() / 2 - (float) size / 2, size, size);
                this.spriteBatch.end();

                if (System.currentTimeMillis() - this.libGDXSplashTime > 4000f) {
                    this.showLibGDXSplash = false;
                    this.showUltreonSplash = true;
                }
                return;
            }

            if (this.showUltreonSplash) {
                if (this.ultreonSplashTime == 0L) {
                    this.ultreonSplashTime = System.currentTimeMillis();

                    this.logoRevealSound.play(0.5f);
                }

                ScreenUtils.clear(0, 0, 0, 1, true);
                Renderer renderer = new Renderer(this.shapes);

                final long timeDiff = System.currentTimeMillis() - this.ultreonSplashTime;
                float zoom = (float) UltracraftClient.interpolate(UltracraftClient.FROM_ZOOM, UltracraftClient.TO_ZOOM, Mth.clamp(timeDiff / UltracraftClient.DURATION, 0f, 1f));
                Vec2f thumbnail = this.resizer.thumbnail(this.getWidth() * zoom, this.getHeight() * zoom);

                float drawWidth = thumbnail.x;
                float drawHeight = thumbnail.y;

                float drawX = (this.getWidth() - drawWidth) / 2;
                float drawY = (this.getHeight() - drawHeight) / 2;

                this.spriteBatch.begin();
                renderer.blit(this.ultreonBgTex, 0, 0, this.getWidth(), this.getHeight(), 0, 0, 1024, 1024, 1024, 1024);
                renderer.blit(this.ultreonLogoTex, (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, 1920, 1080, 1920, 1080);
                this.spriteBatch.end();

                if (System.currentTimeMillis() - this.ultreonSplashTime > UltracraftClient.DURATION) {
                    this.showUltreonSplash = false;

                    this.loadingOverlay = new LoadingOverlay(this.ultreonBgTex);

                    CompletableFuture.runAsync(() -> {
                        try {
                            this.load();
                        } catch (Throwable t) {
                            UltracraftClient.crash(t);
                        }
                    });
                }

                return;
            }

            Player player = this.player;
            this.hitResult = player == null ? null : player.rayCast();
            GameInput input = this.input;
            if (input != null) {
                input.update();
            }

            Renderer renderer = new Renderer(this.shapes);
            if (this.loadingOverlay != null) {
                this.spriteBatch.begin();
                renderer.pushMatrix();
                renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
                renderer.scale(this.guiScale, this.guiScale);
                this.loadingOverlay.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
                renderer.popMatrix();
                this.spriteBatch.end();
                return;
            }

            var world = this.world;
            var worldRenderer = this.worldRenderer;

            if (this.player != null) {
                if (this.screen == null && !ImGuiOverlay.isShowingImGui()) {
                    this.player.rotate(-Gdx.input.getDeltaX() / 2f, -Gdx.input.getDeltaY() / 2f);
                }

                this.camera.update(this.player);
//                this.camera.far = (this.settings.renderDistance.get() - 1) * World.CHUNK_SIZE;
                this.camera.far = 3000;

                var rotation = this.player != null ? this.player.getRotation() : new Vec2f();
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();
            }

            if (this.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {
                ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
                Gdx.gl20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                this.modelBatch.begin(this.camera);
                this.modelBatch.getRenderContext().setCullFace(UltracraftClient.CULL_FACE);
                this.modelBatch.getRenderContext().setDepthTest(GL_DEPTH_FUNC);
                this.modelBatch.render(worldRenderer, worldRenderer.getEnvironment());
                this.modelBatch.end();
            }

            this.spriteBatch.begin();

            var screen = this.screen;

            renderer.pushMatrix();
            renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
            renderer.scale(this.guiScale, this.guiScale);
            this.renderGame(renderer, screen, world, deltaTime);
            renderer.popMatrix();

            if (this.isCustomBorderShown()) {
                renderer.pushMatrix();
                renderer.scale(2, 2);
                this.renderWindow(renderer, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
                renderer.popMatrix();
            }

            ImGuiOverlay.renderImGui(this);

            this.spriteBatch.end();
        } catch (Throwable t) {
            UltracraftClient.crash(t);
        }

        Gdx.gl.glDisable(GL_CULL_FACE);
    }

    private static void firstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    private void renderWindow(Renderer renderer, int width, int height) {
        renderer.draw9PatchTexture(this.windowTex, 0, 0, width, height, 0, 0, 18, 22, 256, 256);
    }

    private void renderGame(Renderer renderer, @Nullable Screen screen, @Nullable World world, float deltaTime) {
        if (world != null) {
            if (this.showDebugHud) {
                this.debugRenderer.render(renderer);
            }

            this.hud.render(renderer, deltaTime);
        }

        if (screen != null) {
            screen.render(renderer, (int) ((Gdx.input.getX() - this.getDrawOffset().x) / this.getGuiScale()), (int) ((Gdx.input.getY() + this.getDrawOffset().y) / this.getGuiScale()), deltaTime);
        }
    }

    public static void crash(Throwable throwable) {
        UltracraftClient.LOGGER.error("Game crash triggered:", throwable);
        try {
            var crashLog = new CrashLog("An unexpected error occurred", throwable);
            UltracraftClient.crash(crashLog);
        } catch (Throwable t) {
            UltracraftClient.LOGGER.error(UltracraftClient.FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    public static void crash(CrashLog crashLog) {
        try {
            UltracraftClient.instance.fillGameInfo(crashLog);
            var crash = crashLog.createCrash();
            UltracraftClient.crash(crash);
        } catch (Throwable t) {
            UltracraftClient.LOGGER.error(UltracraftClient.FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void fillGameInfo(CrashLog crashLog) {
        if (this.world != null) {
            this.world.fillCrashInfo(crashLog);
        }

        var client = new CrashCategory("Game Details");
        client.add("Time until crash", Duration.ofMilliseconds(System.currentTimeMillis() - UltracraftClient.BOOT_TIMESTAMP).toSimpleString()); // Could be the game only crashes after a long time.
        client.add("Game booted", this.booted); // Could be that the game isn't booted yet.
        crashLog.addCategory(client);
    }

    private static void crash(ApplicationCrash crash) {
        Application app = Gdx.app;
        try {
            if (app != null) {
                app.postRunnable(() -> {
                    try {
                        UltracraftClient.cleanUp();
                    } catch (Throwable throwable) {
                        UltracraftClient.LOGGER.error("Failed to clean up the mess:", throwable);
                    }
                });
            }
            crash.printCrash();

            var crashLog = crash.getCrashLog();
            CrashHandler.handleCrash(crashLog);

            if (app != null) app.exit();
        } catch (Throwable t) {
            UltracraftClient.LOGGER.error(UltracraftClient.FATAL_ERROR_MSG, t);

            if (app != null) app.exit();
        }
    }

    @SuppressWarnings("removal")
    private static void cleanUp() {
        UltracraftClient client = UltracraftClient.instance;
        if (client != null) {
            UltracraftClient.cleanUp(client.world);
            UltracraftClient.cleanUp(client.worldRenderer);
            UltracraftClient.cleanUp(client.unifont);
            UltracraftClient.cleanUp(client.modelBatch);
            UltracraftClient.cleanUp(client.spriteBatch);
            UltracraftClient.cleanUp(client.windowTex);
            UltracraftClient.cleanUp(client.ultreonLogoTex);
            UltracraftClient.cleanUp(client.ultreonBgTex);
            UltracraftClient.cleanUp(TextureManager.DEFAULT_TEX);
            UltracraftClient.cleanUp(TextureManager.DEFAULT_TEXTURE);
        }
    }

    private static void cleanUp(@Nullable Disposable disposable) {
        if (disposable == null) return;

        try {
            disposable.dispose();
        } catch (Throwable throwable) {
            UltracraftClient.LOGGER.error("Failed to dispose " + disposable.getClass().getName(), throwable);
        }
    }

    public void clientTick() {
        if (this.player != null && this.world != null) {
            this.cursor = this.world.rayCast(new Ray(this.player.getPosition().add(0, this.player.getEyeHeight(), 0), this.player.getLookVector()));
        }

        if (this.player != null) this.player.tick();
        if (this.world != null) this.world.tick();

        BlockPos breaking = this.breaking != null ? new BlockPos(this.breaking) : null;
        if (this.world != null && breaking != null) {
            HitResult hitResult = this.hitResult;

            if (hitResult != null) {
                this.handleBlockBreaking(breaking, hitResult);
            }
        }

        var player = this.player;
        if (player != null) {
            this.camera.update(player);
        }
    }

    private void handleBlockBreaking(BlockPos breaking, HitResult hitResult) {
        World world = this.world;
        if (world == null) return;
        if (!hitResult.getPos().equals(breaking.vec()) || !hitResult.getBlock().equals(this.breakingBlock) || this.player == null) {
            this.resetBreaking(hitResult);
        } else {
            float efficiency = 1.0F;
            ItemStack stack = this.player.getSelectedItem();
            Item item = stack.getItem();
            if (item instanceof ToolItem toolItem &&
                    this.breakingBlock.getEffectiveTool() == ((ToolItem) item).getToolType()) {
                efficiency = toolItem.getEfficiency();
            }

            if (!world.continueBreaking(breaking, 1.0F / (Math.max(this.breakingBlock.getHardness() * UltracraftServer.TPS / efficiency, 0) + 1), this.player)) {
                this.stopBreaking();
            } else {
                if (this.oldSelected != this.player.selected) {
                    this.resetBreaking();
                }
                this.oldSelected = this.player.selected;
            }
        }
    }

    private void resetBreaking(HitResult hitResult) {
        if (this.world == null) return;
        if (this.breaking == null) return;
        this.world.stopBreaking(new BlockPos(this.breaking));
        Block block = hitResult.getBlock();
        if (block == null || block.isAir()) {
            this.breaking = null;
            this.breakingBlock = null;
        } else {
            this.breaking = hitResult.getPos();
            this.breakingBlock = block;
            this.world.startBreaking(new BlockPos(hitResult.getPos()));
        }
    }

    public void resize(int width, int height) {
        this.spriteBatch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1000000);
        this.deferredWidth = width;
        this.deferredHeight = height;

        if (this.camera != null) {
            this.camera.viewportWidth = width;
            this.camera.viewportHeight = height;
            this.camera.update();
        }

        if (this.itemRenderer != null) {
            this.itemRenderer.resize(width, height);
        }

        var cur = this.screen;
        if (cur != null) {
            cur.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));
        }
    }

    @SuppressWarnings("ConstantValue")
    public void dispose() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new IllegalThreadError("Should only dispose on LibGDX main thread");
        }
        try {
            while (!this.futures.isEmpty()) {
                this.futures.removeIf(CompletableFuture::isDone);
            }

            if (this.scheduler != null) this.scheduler.shutdownNow().clear();
            if (this.garbageCollector != null) this.garbageCollector.shutdown();

            GameInput.cancelVibration();

            if (this.world != null) this.world.dispose();
            if (this.worldRenderer != null) this.worldRenderer.dispose();
            if (this.blocksTextureAtlas != null) this.blocksTextureAtlas.dispose();

            if (!this.loading) {
                ImGuiOverlay.dispose();
            }

            if (this.modelBatch != null) this.modelBatch.dispose();
            if (this.spriteBatch != null) this.spriteBatch.dispose();
            if (this.unifont != null) this.unifont.dispose();

            for (var font : FontRegistry.getAll()) {
                font.dispose();
            }

            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();

            ClientLifecycleEvents.GAME_DISPOSED.factory().onGameDisposed();
        } catch (Throwable t) {
            UltracraftClient.crash(t);
        }
    }

    public boolean isDevMode() {
        return this.isDevMode;
    }

    public boolean isShowingImGui() {
        return ImGuiOverlay.isShowingImGui();
    }

    public void setShowingImGui(boolean value) {
        ImGuiOverlay.setShowingImGui(value);
    }

    public int getWidth() {
        return Gdx.graphics.getWidth() - this.getDrawOffset().x * 2;
    }

    public int getHeight() {
        return Gdx.graphics.getHeight() - this.getDrawOffset().y * 2;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public void startWorld() {
        this.showScreen(new WorldLoadScreen(UltracraftClient.getSavedWorld()));
    }

    public static WorldStorage getSavedWorld() {
        return UltracraftClient.worldStorage;
    }

    public float getGuiScale() {
        return this.guiScale;
    }

    public int getScaledWidth() {
        return ceil(this.getWidth() / this.getGuiScale());
    }

    public int getScaledHeight() {
        return ceil(this.getHeight() / this.getGuiScale());
    }

    public void exitWorldToTitle() {
        this.exitWorldAndThen(() -> this.showScreen(new TitleScreen()));
    }

    public synchronized void exitWorldAndThen(Runnable runnable) {
        this.closingWorld = true;
        final var world = this.world;
        final @Nullable WorldRenderer worldRenderer = this.worldRenderer;
        if (world == null) return;
        this.showScreen(new MessageScreen(Language.translate("Saving world...")));
        if (worldRenderer != null)
            worldRenderer.dispose();

        if (this.connection != null) {
            this.connection.disconnect("User self-disconnected");
        }

        if (this.integratedServer != null) {
            this.integratedServer.shutdown();
        }

        CompletableFuture.runAsync(() -> {
            try {
                GameInput.cancelVibration();

                world.dispose();
                this.worldRenderer = null;
                this.world = null;
                System.gc();
                UltracraftClient.invokeAndWait(runnable);
            } catch (Exception e) {
                UltracraftClient.crash(e);
            }
        });
    }

    public boolean isClosingWorld() {
        return this.closingWorld;
    }

    /**
     * @deprecated use {@link #submit(Runnable)} instead.
     */
    @Deprecated
    public void runLater(Runnable task) {
        Gdx.app.postRunnable(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltracraftClient.LOGGER.warn("Error occurred in task:", e);
            }
        });
    }

    public ScheduledFuture<?> schedule(Task<?> task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltracraftClient.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Task<?> task, long time, TimeUnit unit) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltracraftClient.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        }, time, unit);
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void playSound(ClientSound event) {
        event.getSound().play();
    }

    public boolean tryShutdown() {
        var eventResult = ClientLifecycleEvents.WINDOW_CLOSED.factory().onWindowClose();
        if (!eventResult.isCanceled()) {
            if (this.world != null) {
                this.exitWorldAndThen(() -> {
                    try {
                        this.connection.close();
                    } catch (Exception e) {
                        UltracraftClient.LOGGER.warn("Error occurred while closing connection:", e);
                    }
                    Gdx.app.postRunnable(Gdx.app::exit);
                });
                return false;
            }
        }
        return !eventResult.isCanceled();
    }

    public void filesDropped(String[] files) {
        var currentScreen = this.screen;
        var handles = Arrays.stream(files).map(FileHandle::new).collect(Collectors.toList());

        if (currentScreen != null) {
            currentScreen.filesDropped(handles);
        }
    }

    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

    public @Nullable BakedCubeModel getBakedBlockModel(Block block) {
        return this.bakedBlockModels.bakedModels().get(block);
    }

    public void resetBreaking() {
        HitResult hitResult = this.hitResult;
        if (hitResult == null || this.world == null || this.breaking == null) return;
        this.world.stopBreaking(new BlockPos(hitResult.getPos()));
        this.world.startBreaking(new BlockPos(hitResult.getPos()));
        this.breaking = hitResult.getPos();
        this.breakingBlock = hitResult.getBlock();
    }

    public void startBreaking() {
        HitResult hitResult = this.hitResult;
        if (hitResult == null || this.world == null) return;
        if (this.world.getBreakProgress(new BlockPos(hitResult.getPos())) >= 0.0F) return;
        this.world.startBreaking(new BlockPos(hitResult.getPos()));
        this.breaking = hitResult.getPos();
        this.breakingBlock = hitResult.getBlock();
    }

    public void stopBreaking() {
        HitResult hitResult = this.hitResult;
        if (hitResult == null || this.world == null) return;
        this.world.stopBreaking(new BlockPos(hitResult.getPos()));
        this.breaking = null;
        this.breakingBlock = null;
    }

    public float getBreakProgress() {
        Vec3i breaking = this.breaking;
        World world = this.world;
        if (breaking == null || world == null) return -1;
        return world.getBreakProgress(new BlockPos(breaking));
    }

    private float calculateGuiScale() {
        return 2.0F;
    }

    public boolean isPlaying() {
        return this.world != null && this.screen == null;
    }

    public static FileHandle getConfigDir() {
        return UltracraftClient.instance.configDir;
    }

    public GridPoint2 getDrawOffset() {
        return this.isCustomBorderShown() ? new GridPoint2(18 * 2, 22 * 2) : new GridPoint2();
    }

    public boolean isCustomBorderShown() {
//        return !Gdx.graphics.isFullscreen();
        return false;
    }

    public String getAllUnicode() {
        return this.allUnicode;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public static GameEnvironment getGameEnv() {
        if (UltracraftClient.instance == null) return GameEnvironment.UNKNOWN;
        return UltracraftClient.instance.gameEnv;
    }

    public String getGaveVersion() {
        return QuiltLoader.getNormalizedGameVersion();
    }

    public IntegratedServer getSingleplayerServer() {
        return this.integratedServer;
    }

    public boolean isSinglePlayer() {
        return this.integratedServer != null && !this.integratedServer.isOpenToLan();
    }

    public void playSound(@NotNull SoundEvent soundEvent, float volume) {
        Sound sound = this.soundRegistry.getSound(soundEvent.getId());
        if (sound == null) {
            UltracraftClient.LOGGER.warn("Unknown sound event: " + soundEvent.getId());
            return;
        }
        sound.play(volume);
    }

    public void startIntegratedServer() {
        this.integratedServer.start();

        SocketAddress localServer = this.integratedServer.getConnection().startMemoryServer();
        this.connection = ClientConnections.connectToLocalServer(localServer);
        this.connection.initiate(localServer.toString(), 0, new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    public void connectToServer(String host, int port) {
        this.connection = new Connection(PacketDestination.SERVER);
        ChannelFuture future = ClientConnections.connectTo(new InetSocketAddress(host, port), this.connection);
        future.syncUninterruptibly();
        this.connection.initiate(host, port, new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    public int getCurrentTps() {
        return this.currentTps;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setFullScreen(boolean fullScreen) {
        if (Gdx.graphics.isFullscreen()!= fullScreen) {
            if (fullScreen) {
                this.oldMode = new Vec2i(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            } else {
                Gdx.graphics.setWindowedMode(this.oldMode.x, this.oldMode.y);
            }
        }
    }

    public boolean isFullScreen() {
        return Gdx.graphics.isFullscreen();
    }

    /**
     * @return true if the player is in the third person, false otherwise.
     */
    public boolean isInThirdPerson() {
        return this.isInThirdPerson;
    }

    /**
     * @param thirdPerson true to set the player to be in the third person, false for first person.
     */
    public void setInThirdPerson(boolean thirdPerson) {
        this.isInThirdPerson = thirdPerson;
    }

    private static class LibGDXLogger implements ApplicationLogger {
        private final Logger LOGGER = LoggerFactory.getLogger("LibGDX");

        @Override
        public void log(String tag, String message) {
            this.LOGGER.info(MarkerFactory.getMarker(tag), message);
        }

        @Override
        public void log(String tag, String message, Throwable exception) {
            this.LOGGER.info(MarkerFactory.getMarker(tag), message, exception);
        }

        @Override
        public void error(String tag, String message) {
            this.LOGGER.error(MarkerFactory.getMarker(tag), message);
        }

        @Override
        public void error(String tag, String message, Throwable exception) {
            this.LOGGER.error(MarkerFactory.getMarker(tag), message, exception);
        }

        @Override
        public void debug(String tag, String message) {
            this.LOGGER.debug(MarkerFactory.getMarker(tag), message);
        }

        @Override
        public void debug(String tag, String message, Throwable exception) {
            this.LOGGER.debug(MarkerFactory.getMarker(tag), message, exception);
        }
    }
}
