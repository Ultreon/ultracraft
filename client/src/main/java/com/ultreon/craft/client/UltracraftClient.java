package com.ultreon.craft.client;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.ScreenUtils;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.RestrictedApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultreon.craft.*;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.atlas.TextureAtlas;
import com.ultreon.craft.client.atlas.TextureStitcher;
import com.ultreon.craft.client.audio.ClientSound;
import com.ultreon.craft.client.config.GameSettings;
import com.ultreon.craft.client.config.UltracraftClientConfig;
import com.ultreon.craft.client.events.ClientLifecycleEvents;
import com.ultreon.craft.client.events.ScreenEvents;
import com.ultreon.craft.client.events.WindowEvents;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.debug.*;
import com.ultreon.craft.client.gui.hud.GamepadHud;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.*;
import com.ultreon.craft.client.gui.screens.container.InventoryScreen;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.Fonts;
import com.ultreon.craft.client.init.Overlays;
import com.ultreon.craft.client.init.ShaderPrograms;
import com.ultreon.craft.client.init.Shaders;
import com.ultreon.craft.client.input.DesktopInput;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.PlayerInput;
import com.ultreon.craft.client.input.gamepad.GamepadContext;
import com.ultreon.craft.client.input.gamepad.GamepadInput;
import com.ultreon.craft.client.input.gamepad.InputType;
import com.ultreon.craft.client.input.gamepad.VirtualKeyboard;
import com.ultreon.craft.client.input.keyboard.KeyboardLayout;
import com.ultreon.craft.client.input.keyboard.KeyboardLayouts;
import com.ultreon.craft.client.item.ItemRenderer;
import com.ultreon.craft.client.model.JsonModelLoader;
import com.ultreon.craft.client.model.block.*;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.renderer.DroppedItemRenderer;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.client.model.entity.renderer.PlayerRenderer;
import com.ultreon.craft.client.multiplayer.MultiplayerData;
import com.ultreon.craft.client.network.ClientConnection;
import com.ultreon.craft.client.network.LoginClientPacketHandlerImpl;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.player.LocalPlayer;
import com.ultreon.craft.client.registry.*;
import com.ultreon.craft.client.render.RenderType;
import com.ultreon.craft.client.render.pipeline.CollectNode;
import com.ultreon.craft.client.render.pipeline.MainRenderNode;
import com.ultreon.craft.client.render.pipeline.RenderPipeline;
import com.ultreon.craft.client.render.pipeline.WorldDiffuseNode;
import com.ultreon.craft.client.render.shader.GameShaderProvider;
import com.ultreon.craft.client.resources.ResourceLoader;
import com.ultreon.craft.client.resources.ResourceNotFoundException;
import com.ultreon.craft.client.rpc.GameActivity;
import com.ultreon.craft.client.rpc.RpcHandler;
import com.ultreon.craft.client.sound.ClientSoundRegistry;
import com.ultreon.craft.client.text.Language;
import com.ultreon.craft.client.text.LanguageData;
import com.ultreon.craft.client.text.LanguageManager;
import com.ultreon.craft.client.texture.TextureManager;
import com.ultreon.craft.client.util.DeferredDisposable;
import com.ultreon.craft.client.util.GG;
import com.ultreon.craft.client.util.Resizer;
import com.ultreon.craft.client.world.*;
import com.ultreon.craft.config.UcConfiguration;
import com.ultreon.craft.crash.ApplicationCrash;
import com.ultreon.craft.crash.CrashCategory;
import com.ultreon.craft.crash.CrashLog;
import com.ultreon.craft.debug.DebugFlags;
import com.ultreon.craft.debug.Debugger;
import com.ultreon.craft.debug.inspect.DefaultInspections;
import com.ultreon.craft.debug.inspect.InspectionNode;
import com.ultreon.craft.debug.inspect.InspectionRoot;
import com.ultreon.craft.debug.profiler.Profiler;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.ConfigEvents;
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.ItemStack;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.item.tool.ToolItem;
import com.ultreon.craft.menu.MenuTypes;
import com.ultreon.craft.network.Connection;
import com.ultreon.craft.network.api.PacketDestination;
import com.ultreon.craft.network.packets.c2s.C2SLoginPacket;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.registry.event.RegistryEvents;
import com.ultreon.craft.resources.ResourceManager;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.sound.event.SoundEvents;
import com.ultreon.craft.text.LanguageBootstrap;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.*;
import com.ultreon.craft.world.*;
import com.ultreon.craft.world.gen.biome.Biomes;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec2i;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.datetime.v0.Duration;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;

@SuppressWarnings("UnusedReturnValue")
public class UltracraftClient extends PollingExecutorService implements DeferredDisposable {
    @SuppressWarnings("removal")
    @Deprecated(since = "0.1.0", forRemoval = true)
    public static final String NAMESPACE = UltracraftServer.NAMESPACE;
    public static final Logger LOGGER = LoggerFactory.getLogger("UltracraftClient");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    public static final int[] SIZES = new int[]{16, 24, 32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};
    public static final float FROM_ZOOM = 2.0f;
    public static final float TO_ZOOM = 1.3f;
    private static final float DURATION = 6000f;
    private static final int MINIMUM_WIDTH = 800;
    private static final int MINIMUM_HEIGHT = 600;
    private static ArgParser arguments;
    private static boolean crashing;
    private final Cursor normalCursor;
    private final Cursor clickCursor;
    private final RenderPipeline pipeline;
    private final boolean devWorld;
    public final Renderer renderer;
    public Connection connection;
    public ClientConnection clientConn;
    public ServerData serverData;
    public ExecutorService chunkLoadingExecutor = Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 3, 1));
    public static Profiler PROFILER = new Profiler();
    public final InspectionRoot<UltracraftClient> inspection;
    public UcConfiguration<UltracraftClientConfig> config;
    public boolean hideHud = false;
    @MonotonicNonNull
    public GamepadInput gamepadInput;
    public VirtualKeyboard virtualKeyboard;
    public int useItemCooldown = 0;

    private Duration bootTime;
    private GarbageCollector garbageCollector;
    private GameEnvironment gameEnv;
    private final Sound logoRevealSound;
    private final Texture ultreonBgTex;
    private final Texture ultreonLogoTex;
    private final Texture libGDXLogoTex;
    private final Resizer resizer;
    private boolean showUltreonSplash = false;
    private boolean showLibGDXSplash = !FabricLoader.getInstance().isDevelopmentEnvironment();
    private long ultreonSplashTime;
    private long libGDXSplashTime;
    public FileHandle configDir;
    public Metadata metadata = Metadata.load();

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";
    public boolean forceUnicode = false;
    public ItemRenderer itemRenderer;
    public NotifyManager notifications = new NotifyManager(this);
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
    public LocalPlayer player;
    @NotNull
    public final SpriteBatch spriteBatch;
    public final ModelBatch modelBatch;
    public final GameCamera camera;
    public final PlayerInput playerInput = new PlayerInput(this);
    private boolean isDevMode;
    @Nullable
    public Screen screen;
    @Deprecated
    public GameSettings settings = new GameSettings();
    public final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final ResourceManager resourceManager;
    private float guiScale = this.calcMaxGuiScale();

    public Hud hud;
    public GamepadHud gamepadHud;
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
    public TextureAtlas emmisiveTextureAtlas;
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
    public DebugGui debugGui;
    private boolean closingWorld;
    private int oldSelected;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final List<Shutdownable> shutdownables = new CopyOnWriteArrayList<>();
    private final List<AutoCloseable> closeables = new CopyOnWriteArrayList<>();
    private boolean loading = true;
    private final Thread mainThread;
    public HitResult cursor;
    private LoadingOverlay loadingOverlay;
    private final String[] argv;
    private final ClientSoundRegistry soundRegistry = new ClientSoundRegistry();
    public IntegratedServer integratedServer;
    private final User user;
    private int currentTps;
    private float tickTime = 0f;
    public float partialTick = 0f;
    public float frameTime = 0f;
    private int ticksPassed = 0;

    double time = System.currentTimeMillis();
    private GameActivity activity = null;
    private GameActivity oldActivity = null;
    private Vec2i oldMode;
    private boolean isInThirdPerson;
    private boolean triggerScreenshot;
    private boolean captureScreenshot;
    public int screenshotScale = 4;
    private final GameRenderer gameRenderer;
    private FrameBuffer fbo;
    private int width;
    private int height;
    private MultiplayerData multiplayerData;
    ManualCrashOverlay crashOverlay;
    private boolean wasClicking;
    private final Queue<Runnable> serverTickQueue = new ArrayDeque<>();
    private boolean startDevLoading = true;
    private final G3dModelLoader modelLoader;
    private final Environment defaultEnv = new Environment();
    private boolean autoScale;
    private boolean disposed;
    private final GameWindow window;

    @ApiStatus.Experimental
    private static Callback<CrashLog> crashHook;
    private final List<CrashLog> crashes = new CopyOnWriteArrayList<>();
    private long screenshotFlashTime;
    private ClientSound screenshotSound;
    private final Color tmpColor = new Color();
    private InputType inputType = InputType.KEYBOARD_AND_MOUSE;
    private int inputCooldown;
    private KeyboardLayout keyboardLayout;

    UltracraftClient(String[] argv) {
        super(UltracraftClient.PROFILER);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (this.integratedServer != null) {
                    this.integratedServer.shutdown();
                }
            } catch (Exception e) {
                UltracraftClient.LOGGER.error("Failed to shutdown server", e);
            }

            UltracraftClient.LOGGER.info("Shutting down game!");
            Gdx.app.postRunnable(Gdx.app::exit);

            UltracraftClient.instance = null;
        }));

        UltracraftClient.LOGGER.info("Booting game!");
        UltracraftClient.instance = this;

        this.unifont = deferDispose(new BitmapFont(Gdx.files.internal("assets/ultracraft/font/unifont/unifont.fnt"), true));
        this.font = new Font(new BitmapFont(Gdx.files.internal("assets/ultracraft/font/dogica/dogicapixel.fnt"), true));

        LanguageBootstrap.bootstrap.set(Language::translate);

        ShaderProgram.pedantic = false;

        this.window = new GameWindow();

//        RpcHandler.start();

        this.inspection = deferDispose(new InspectionRoot<>(this));

        this.resourceManager = new ResourceManager("assets");
        this.textureManager = new TextureManager(this.resourceManager);

        this.resourceManager.importDeferredPackage(this.getClass());

        ResourceLoader.init(this);

        this.config = new UcConfiguration<>("ultracraft-client", EnvType.CLIENT, new UltracraftClientConfig());
        this.config.event.listen(this::onReloadConfig);

        this.registerAutoFillers();

        this.argv = argv;

        this.devWorld = UltracraftClient.arguments.getFlags().contains("devWorld");

        this.user = new User("Player" + MathUtils.random(100, 999));

        this.mainThread = Thread.currentThread();

        ImGuiOverlay.preInitImGui();

        this.modelLoader = new G3dModelLoader(new JsonReader());

        this.camera = new GameCamera(67, this.getWidth(), this.getHeight());
        this.camera.near = 0.1f;
        this.camera.far = 2;

        this.pipeline = deferDispose(new RenderPipeline(new MainRenderNode(), this.camera)
                .node(new CollectNode())
                .node(new WorldDiffuseNode())
                .node(new MainRenderNode()));

        // White pixel for the shape drawer.
        Pixmap pixmap = deferDispose(new Pixmap(1, 1, Pixmap.Format.RGBA8888));
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        TextureRegion white = new TextureRegion(new Texture(pixmap));

        this.spriteBatch = deferDispose(new SpriteBatch());
        this.shapes = new ShapeDrawer(this.spriteBatch, white);
        this.renderer = new Renderer(this.shapes);

        DepthShader.Config shaderConfig = new DepthShader.Config();
        shaderConfig.defaultCullFace = GL_BACK;
        shaderConfig.defaultDepthFunc = GL_DEPTH_FUNC;
        this.modelBatch = deferDispose(new ModelBatch(new GameShaderProvider(shaderConfig)));

        this.gameRenderer = new GameRenderer(this, this.modelBatch, this.pipeline);

        // Textures
        this.ultreonBgTex = new Texture("assets/ultracraft/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/ultracraft/logo.png");
        this.libGDXLogoTex = new Texture("assets/ultracraft/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/ultracraft/sounds/logo_reveal.mp3"));

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());

        this.normalCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/ultracraft/textures/cursors/normal.png")), 0, 0);
        this.clickCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/ultracraft/textures/cursors/click.png")), 0, 0);

        LanguageManager.setCurrentLanguage(Locale.of("en", "us"));

        Gdx.graphics.setCursor(this.normalCursor);
        if (DebugFlags.INSPECTION_ENABLED.enabled()) {
            InspectionNode<Application> libGdxNode = this.inspection.createNode("libGdx", value -> Gdx.app);
            InspectionNode<Graphics> graphicsNode = libGdxNode.createNode("graphics", Application::getGraphics);
            graphicsNode.create("backBufferScale", Graphics::getBackBufferScale);
            graphicsNode.create("backBufferWidth", Graphics::getBackBufferWidth);
            graphicsNode.create("backBufferHeight", Graphics::getBackBufferHeight);
            graphicsNode.create("width", Graphics::getWidth);
            graphicsNode.create("height", Graphics::getHeight);
            graphicsNode.createNode("bufferFormat", Graphics::getBufferFormat);
            graphicsNode.create("density", Graphics::getDensity);
            graphicsNode.create("deltaTime", Graphics::getDeltaTime);
            graphicsNode.createNode("displayMode", Graphics::getDisplayMode);
            graphicsNode.createNode("primaryMonitor", Graphics::getPrimaryMonitor);
            graphicsNode.create("glVersion", Graphics::getGLVersion);
            graphicsNode.create("framesPerSecond", Graphics::getFramesPerSecond);
            graphicsNode.create("frameId", Graphics::getFrameId);
            graphicsNode.create("type", Graphics::getType);

            libGdxNode.create("version", Application::getVersion);
            libGdxNode.create("javaHeap", Application::getJavaHeap);
        }
    }

    /**
     * Game crash hook, which will be called when a crash occurs.
     * <p>
     * <p style="font-size: 16px"><b>ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING</b></p>
     * <p>WHEN THIS IS NON-NULL CRASHES WILL BE CAPTURED AND WILL STOP THE GAME FROM HANDLING THEM.</p>
     * <p>So, make sure to actually handle the crash when using this.</p>
     */
    @RestrictedApi(
            explanation = "Only use this if you know what you are doing",
            link = "https://github.com/Ultreon/ultracraft/wiki/Crash-Hooks#important",
            allowlistAnnotations = OptIn.class
    )
    public static void setCrashHook(Callback<CrashLog> crashHook) {
        UltracraftClient.crashHook = crashHook;
    }

    private void onReloadConfig() {
        UltracraftClientConfig config = this.config.get();
        boolean changed = false;
        if (config.video == null) {
            config.video = new UltracraftClientConfig.VideoConfig();
            changed = true;
        }
        if (config.privacy == null) {
            config.privacy = new UltracraftClientConfig.PrivacyConfig();
            changed = true;
        }
        if (config.accessibility == null) {
            config.accessibility = new UltracraftClientConfig.AccessibilityConfig();
            changed = true;
        }
        if (config.language == null) {
            config.language = UltracraftClient.id("en_us");
            changed = true;
        }
        if (config.personalisation == null) {
            config.personalisation = new UltracraftClientConfig.PersonalisationConfig();
            changed = true;
        }
        if (config.crafting == null) {
            config.crafting = new UltracraftClientConfig.CraftingConfig();
            changed = true;
        }
        if (changed) {
            this.config.set(new UltracraftClientConfig());
            this.config.save();
        }

        if (config.video.fullscreen) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());

        String[] split = config.language.path().split("_");
        if (split.length == 2) {
            LanguageManager.setCurrentLanguage(Locale.of(split[0], split[1]));
        } else {
            UltracraftClient.LOGGER.error("Invalid language: {}", config.language);
            LanguageManager.setCurrentLanguage(Locale.of("en", "us"));
            config.language = UltracraftClient.id("en_us");
            this.config.save();
        }

        if (config.video.guiScale != 0) {
            this.setAutomaticScale(false);
            this.setGuiScale(config.video.guiScale);
        } else {
            this.setAutomaticScale(true);
        }

        if (config.privacy.hideRpc) {
            RpcHandler.disable();
        } else {
            RpcHandler.enable();
            if (this.activity != null) {
                this.setActivity(this.activity);
                RpcHandler.setActivity(this.activity);
            }
        }

        if (!config.accessibility.vibration) {
            GameInput.cancelVibration();
        }

        UltracraftClient.invoke(() -> {
            boolean enableVsync = config.video.enableVsync;
            Gdx.graphics.setVSync(enableVsync);

            int fpsLimit = config.video.fpsLimit;
            if (fpsLimit >= 240) UltracraftClient.setFpsLimit(Integer.MAX_VALUE);
            else UltracraftClient.setFpsLimit(fpsLimit < 10 ? 60 : fpsLimit);
        });
    }

    public static void setFpsLimit(int limit) {
        Gdx.graphics.setForegroundFPS(limit);
    }

    private void registerAutoFillers() {
        InspectionRoot.registerAutoFill(ClientChunk.class, node -> {
            node.createNode("mesh", value -> value.solidMesh);
            node.createNode("transparentMesh", value -> value.transparentMesh);
            node.createNode("dirty", value -> value.dirty);
            node.createNode("renderOffset", value -> value.renderOffset);
        });

        InspectionRoot.registerAutoFill(ClientWorld.class, node -> node.create("renderDistance", ClientWorld::getRenderDistance));
        InspectionRoot.registerAutoFill(Graphics.DisplayMode.class, node -> {
            node.create("width", n -> n.width);
            node.create("height", n -> n.height);
            node.create("hertz", n -> n.refreshRate);
            node.create("bitsPerPixel", n -> n.bitsPerPixel);
        });

        InspectionRoot.registerAutoFill(Graphics.Monitor.class, node -> {
            node.create("name", n -> n.name);
            node.create("x", n -> n.virtualX);
            node.create("y", n -> n.virtualY);
        });

        InspectionRoot.registerAutoFill(Graphics.BufferFormat.class, node -> {
            node.create("red", n -> n.r);
            node.create("green", n -> n.g);
            node.create("blue", n -> n.b);
            node.create("alpha", n -> n.a);
            node.create("depth", n -> n.depth);
            node.create("stencil", n -> n.stencil);
            node.create("samples", n -> n.samples);
            node.create("coverageSamples", n -> n.coverageSampling);
        });

        InspectionRoot.registerFormatter(boolean.class, element -> Boolean.toString(element));
        InspectionRoot.registerFormatter(void.class, element -> "void");
        InspectionRoot.registerFormatter(byte.class, element -> Byte.toString(element));
        InspectionRoot.registerFormatter(short.class, element -> Short.toString(element));
        InspectionRoot.registerFormatter(int.class, element -> Integer.toString(element));
        InspectionRoot.registerFormatter(long.class, element -> Long.toString(element));
        InspectionRoot.registerFormatter(float.class, element -> Float.toString(element));
        InspectionRoot.registerFormatter(double.class, element -> Double.toString(element));
        InspectionRoot.registerFormatter(char.class, element -> Character.toString(element));
        InspectionRoot.registerFormatter(String.class, element -> "\"" + element + "\"");
        InspectionRoot.registerFormatter(IntegratedServer.class, element -> "integratedServer");
        InspectionRoot.registerFormatter(ClientWorld.class, element -> "clientWorld");
        InspectionRoot.registerFormatter(ClientPlayer.class, element -> "clientPlayer[" + element.getUuid() + "]");
        InspectionRoot.registerFormatter(ClientChunk.class, element -> "clientChunk[" + element.getPos() + "]");
        InspectionRoot.registerFormatter(ChunkMesh.class, element -> "chunkMesh[" + element.meshPart.mesh.getVertexSize() + "]");
        InspectionRoot.registerFormatter(Mesh.class, element -> "mesh[" + element.getVertexSize() + "]");
        InspectionRoot.registerFormatter(MeshPart.class, element -> "meshPart[" + element.mesh.getVertexSize() + "]");
        InspectionRoot.registerFormatter(TextureRegion.class, element -> "textureRegion[" + element.getRegionWidth() + "x" + element.getRegionHeight() + "]");
        InspectionRoot.registerFormatter(Texture.class, element -> "texture[" + element.getWidth() + "x" + element.getHeight() + "]");
        InspectionRoot.registerFormatter(Vector2.class, element -> "vector2(" + element.x + ", " + element.y + ")");
        InspectionRoot.registerFormatter(Vector3.class, element -> "vector3(" + element.x + ", " + element.y + ", " + element.z + ")");
        InspectionRoot.registerFormatter(Quaternion.class, element -> "quaternion(" + element.x + ", " + element.y + ", " + element.z + ", " + element.w + ")");
        InspectionRoot.registerFormatter(Color.class, element -> "rgba(" + element.r + ", " + element.g + ", " + element.b + ", " + element.a + ")");
        InspectionRoot.registerFormatter(Circle.class, element -> "circle(" + element.x + ", " + element.y + ", rad=" + element.radius + ")");
        InspectionRoot.registerFormatter(Rectangle.class, element -> "rectangle(" + element.x + ", " + element.y + " + " + element.width + "x" + element.height + ")");
        InspectionRoot.registerFormatter(Ellipse.class, element -> "ellipse(" + element.x + ", " + element.y + " + " + element.width + "x" + element.height + ")");
        InspectionRoot.registerFormatter(GridPoint2.class, element -> "gridPoint2(" + element.x + ", " + element.y + ")");
        InspectionRoot.registerFormatter(GridPoint3.class, element -> "gridPoint3(" + element.x + ", " + element.y + ", " + element.z + ")");
        InspectionRoot.registerFormatter(GLVersion.class, glVersion -> glVersion.getType() + " " + glVersion.getMajorVersion() + "." + glVersion.getMinorVersion() + "." + glVersion.getReleaseVersion());

        DefaultInspections.register();
    }

    /**
     * Gets a file handle in the game directory.
     *
     * @param path the path in the game directory.
     * @return the file handle.
     * @see #getGameDir()
     */
    public static FileHandle data(String path) {
        return Gdx.files.absolute(UltracraftClient.getGameDir().toAbsolutePath().toString()).child(path);
    }

    /**
     * Makes a screenshot of the game.
     */
    public void screenshot() {
        this.triggerScreenshot = true;
    }

    /**
     * Launches the game.
     * <p style="color:red;"><b>Note: This method should not be called.</b></p>
     *
     * @param argv the arguments to pass to the game
     */
    @ApiStatus.Internal
    public static void main(String[] argv) {
        try {
            UltracraftClient.arguments = new ArgParser(argv);
            CrashHandler.addHandler(crashLog -> {
                try {
                    DesktopInput.setCursorCaught(false);
                    Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
                    graphics.getWindow().setVisible(false);
                } catch (Exception e) {
                    UltracraftClient.LOGGER.error("Failed to hide cursor", e);
                }
            });

            UltracraftClient.launch(argv);
        } catch (Exception | OutOfMemoryError e) {
            CrashHandler.handleCrash(new CrashLog("Launch failed", e).createCrash().getCrashLog());
        }
    }

    /**
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param argv the arguments to pass to the game
     */
    @SuppressWarnings("unused")
    private static void launch(String[] argv) {
        UltracraftClient.logDebug();

        FlatMacLightLaf.setup();

        // Before initializing LibGDX or creating a window:
        try (var ignored = GLFW.glfwSetErrorCallback((error, description) -> UltracraftClient.LOGGER.error("GLFW Error: {}", description))) {
            try {
                new Lwjgl3Application(new GameLibGDXWrapper(argv), UltracraftClient.createConfig());
            } catch (ApplicationCrash e) {
                CrashLog crashLog = e.getCrashLog();
                UltracraftClient.crash(crashLog);
            } catch (Exception e) {
                UltracraftClient.crash(e);
            }
        }
    }

    @NotNull
    private static Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setIdleFPS(10);
        config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 0);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 4, 1);
        config.setInitialVisible(false);
        config.setTitle("Ultracraft");
        config.setWindowIcon(UltracraftClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new WindowAdapter());
        return config;
    }

    private static void logDebug() {
        if (UltracraftClient.isPackaged()) UltracraftClient.LOGGER.warn("Running in the JPackage environment.");
        UltracraftClient.LOGGER.debug("Java Version: {}", System.getProperty("java.version"));
        UltracraftClient.LOGGER.debug("Java Vendor: {}", System.getProperty("java.vendor"));
        UltracraftClient.LOGGER.debug("Operating System: {} {} ({})", new Object[]{System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch")});
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
     * @return true if in the JPackage environment, false otherwise.
     */
    public static boolean isPackaged() {
        return UltracraftClient.arguments.getFlags().contains("packaged");
    }

    /**
     * Gets the game directory.
     *
     * @return the game directory.
     */
    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void load() {
        var argList = Arrays.asList(this.argv);
        this.isDevMode = argList.contains("--dev") && FabricLoader.getInstance().isDevelopmentEnvironment();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) this.gameEnv = GameEnvironment.DEVELOPMENT;
        else if (Objects.equals(System.getProperty("ultracraft.environment", "normal"), "packaged"))
            this.gameEnv = GameEnvironment.PACKAGED;
        else this.gameEnv = GameEnvironment.NORMAL;

        if (this.isDevMode) UltracraftClient.LOGGER.info("Developer mode is enabled");

        Thread.setDefaultUncaughtExceptionHandler(UltracraftClient::uncaughtException);

        this.loadingOverlay.setProgress(0.075F);

        Gdx.app.setApplicationLogger(new LibGDXLogger());

        this.configDir = UltracraftClient.createDir("config/");
        this.garbageCollector = new GarbageCollector();

        UltracraftClient.createDir("screenshots/");
        UltracraftClient.createDir("game-crashes/");
        UltracraftClient.createDir("logs/");

        this.setupMods();

        int scale = this.config.get().video.guiScale;
        if (scale == 0) {
            this.setAutomaticScale(true);
        }
        this.setGuiScale(scale);

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        this.loadingOverlay.setProgress(0.15F);

        UltracraftClient.LOGGER.info("Importing resources");
        this.importModResources(this.resourceManager);

        this.loadingOverlay.setProgress(0.35F);

        UltracraftClient.LOGGER.info("Generating bitmap fonts");
        var resource = this.resourceManager.getResource(UltracraftClient.id("texts/unicode.txt"));
        if (resource == null) {
            throw new ApplicationCrash(new CrashLog("Where are my symbols", new ResourceNotFoundException(UltracraftClient.id("texts/unicode.txt"))));
        }

        this.crashOverlay = new ManualCrashOverlay(this);

        this.loadingOverlay.setProgress(0.7F);

        //----------------------
        // Setting up rendering
        //----------------------
        UltracraftClient.LOGGER.info("Initializing rendering stuffs");
        UltracraftClient.invokeAndWait(() -> {
            this.input = deferDispose(this.createInput());
        });
        Gdx.input.setInputProcessor(this.input);

        UltracraftClient.LOGGER.info("Setting up HUD");
        this.hud = UltracraftClient.invokeAndWait(() -> new Hud(this));

        UltracraftClient.LOGGER.info("Setting up Debug Renderer");
        this.debugGui = new DebugGui(this);

        this.loadingOverlay.setProgress(0.83F);

        //--------------------------
        // Registering game content
        //--------------------------
        UltracraftClient.LOGGER.info("Loading languages");
        this.loadLanguages();

        UltracraftClient.LOGGER.info("Registering stuff");

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            Registries.nopInit();
            RegistryEvents.REGISTRY_CREATION.factory().onRegistryCreation();
        });

        LoadingContext.withinContext(new LoadingContext(CommonConstants.NAMESPACE), () -> {
            CommonRegistries.registerGameStuff();

            // Client registry
            Fonts.nopInit();
            Overlays.nopInit();
            UltracraftClient.invokeAndWait(() -> {
                Shaders.nopInit();
                ShaderPrograms.nopInit();
            });

            this.registerDebugPages();

            Biomes.nopInit();
        });

        for (var mod : FabricLoader.getInstance().getAllMods()) {
            final String id = mod.getMetadata().getId();
            LoadingContext.withinContext(new LoadingContext(id), () -> {
                for (Registry<?> registry : Registry.getRegistries()) {
                    RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(id, registry);
                }
            });
        }

        Registry.freeze();

        for (Biome biome : Registries.BIOME.values()) {
            biome.buildLayers();
        }

        UltracraftClient.LOGGER.info("Registering models");
        this.registerMenuScreens();
        this.registerRendering();

        this.loadingOverlay.setProgress(0.95F);

        //*
        //* Post-initialize game content
        //* Such as model baking and texture stitching
        //*
        UltracraftClient.LOGGER.info("Stitching textures");
        this.stitchTextures();

        this.loadingOverlay.setProgress(0.98F);

        JsonModelLoader jsonModelLoader = new JsonModelLoader(this.resourceManager);

        BlockModelRegistry.load(jsonModelLoader);
        UltracraftClient.LOGGER.info("Initializing sounds");
        this.soundRegistry.registerSounds();

        UltracraftClient.LOGGER.info("Baking models");
        BlockModelRegistry.bakeJsonModels(this);
        this.bakedBlockModels = BlockModelRegistry.bake(this.blocksTextureAtlas);

        this.itemRenderer = UltracraftClient.invokeAndWait(() -> new ItemRenderer(this));
        this.itemRenderer.registerModels(jsonModelLoader);
        this.itemRenderer.loadModels(this);

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

        ImGuiOverlay.setupImGui();

        this.booted = true;

        this.loadingOverlay.setProgress(1.0F);

        this.gamepadInput = deferDispose(new GamepadInput(this));
        this.gamepadHud = new GamepadHud();
        this.keyboardLayout = KeyboardLayouts.QWERTY;
        this.gamepadInput.setLayout(this.keyboardLayout);
        this.virtualKeyboard = new VirtualKeyboard();

        GamepadContext.freeze();

        this.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - UltracraftClient.BOOT_TIMESTAMP);
        UltracraftClient.LOGGER.info("Game booted in {}.", this.bootTime.toSimpleString());

        UltracraftClient.invokeAndWait(new Task<>(UltracraftClient.id("main/show_title_screen"), () -> {
            if (this.devWorld) {
                this.startDevWorld();
                return;
            }
        }));
        this.loadingOverlay = null;
    }

    public void setAutomaticScale(boolean b) {
        this.autoScale = b;
        this.guiScale = this.calcMaxGuiScale();
        this.resize(this.width, this.height);
    }

    private void registerDebugPages() {
        ClientRegistries.DEBUG_PAGE.register(UltracraftClient.id("simple"), new SimpleDebugPage());
        ClientRegistries.DEBUG_PAGE.register(UltracraftClient.id("generic"), new GenericDebugPage());
        ClientRegistries.DEBUG_PAGE.register(UltracraftClient.id("profiler"), new ProfilerDebugPage());
        ClientRegistries.DEBUG_PAGE.register(UltracraftClient.id("inspector"), new InspectorDebugPage());
    }

    private void startDevWorld() {
        WorldStorage storage = new WorldStorage("worlds/dev");
        try {
            if (Gdx.files.local("worlds/dev").exists())
                storage.delete();

            storage.createWorld();
        } catch (IOException e) {
            throw new GdxRuntimeException(e);
        }

        this.startWorld(storage);
    }

    private void registerMenuScreens() {
        MenuRegistry.registerScreen(MenuTypes.INVENTORY, InventoryScreen::new);
    }

    private void importModResources(ResourceManager resourceManager) {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            if (mod.getOrigin().getKind() != ModOrigin.Kind.PATH) continue;
            for (Path rootPath : mod.getRootPaths()) {
                // Try to import a resource package for the given mod path.
                try {
                    resourceManager.importPackage(rootPath);
                } catch (IOException e) {
                    UltracraftClient.LOGGER.warn("Importing resources failed for path: " + rootPath.toFile(), e);
                }
            }
        }
    }

    private void setupMods() {
        // Set mod icon overrides.
        ModIconOverrides.set("ultracraft", UltracraftClient.id("icon.png"));
        ModIconOverrides.set("gdx", new ElementID("gdx", "icon.png"));

        // Invoke entry points.
        FabricLoader.getInstance().invokeEntrypoints(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
        FabricLoader.getInstance().invokeEntrypoints(ClientModInit.ENTRYPOINT_KEY, ClientModInit.class, ClientModInit::onInitializeClient);

        ConfigEvents.LOAD.factory().onConfigLoad(EnvType.CLIENT);
    }

    /**
     * Executes the specified {@link Callable} function on the client thread and waits until it completes.
     * This method is designed to be invoked from a different thread to ensure that the function is executed on the
     * client thread.
     *
     * @param func the Callable task to be executed
     * @param <T>  the type of result returned by the Callable task
     * @return the result returned by the Callable task
     */
    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(@NotNull Callable<T> func) {
        return UltracraftClient.instance.submit(func).join();
    }

    /**
     * Executes the specified {@link Runnable} function on the client thread and waits until it completes.
     * This method is designed to be invoked from a different thread to ensure that the function is executed on the
     * client thread.
     *
     * @param func the {@link Runnable} function to be executed on the UltracraftClient thread
     */
    public static void invokeAndWait(Runnable func) {
        UltracraftClient.instance.submit(func).join();
    }

    /**
     * Invokes the given runnable asynchronously and returns a {@link CompletableFuture} that completes with Void.
     *
     * @param func the runnable to be invoked
     * @return a CompletableFuture that completes with Void once the runnable has been invoked
     */
    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        return UltracraftClient.instance.submit(func);
    }

    /**
     * Invokes the given callable function asynchronously and returns a {@link CompletableFuture}.
     *
     * @param func the callable function to be invoked
     * @param <T>  the type parameter of the callable function's return value
     * @return a CompletableFuture representing the pending result of the callable function
     */
    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
        return UltracraftClient.instance.submit(func);
    }

    /**
     * Returns a new instance of FileHandle for the specified resource identifier.
     *
     * @param id The identifier of the resource.
     * @return A new instance of FileHandle for the specified resource.
     */
    @NewInstance
    public static @NotNull FileHandle resource(ElementID id) {
        return Gdx.files.internal("assets/" + id.namespace() + "/" + id.path());
    }

    private static void uncaughtException(Thread t, Throwable e) {
        UltracraftClient.LOGGER.error("Exception in thread \"" + t.getName() + "\":", e);
    }

    /**
     * Checks whether the current thread is the main thread.
     *
     * @return true if the current thread is the main thread, false otherwise.
     */
    public static boolean isOnMainThread() {
        return Thread.currentThread().threadId() == UltracraftClient.instance.mainThread.threadId();
    }

    /**
     * Gets an Ultracraft identifier from a path, but as a string.
     *
     * @param path the path.
     * @return the identifier as string.
     * @see #id(String)
     */
    public static String strId(String path) {
        return UltracraftClient.id(path).toString();
    }

    /**
     * Delays disposing of a disposable.
     * This method disposes the given disposable when the game is shutdown.
     *
     * @param disposable the disposable to delay disposal.
     * @param <T>        the type of the disposable.
     * @return the same disposable.
     */
    @Override
    public <T extends Disposable> T deferDispose(T disposable) {
        if (disposable == null) return null;
        if (UltracraftClient.instance.disposables.contains(disposable)) return disposable;

        if (UltracraftClient.instance.disposed) {
            UltracraftClient.LOGGER.warn("UltracraftClient already disposed, immediately disposing {}", disposable.getClass().getName());
            disposable.dispose();
            return disposable;
        }

        UltracraftClient.instance.disposables.add(disposable);
        return disposable;
    }

    /**
     * Delays shutting down of a shutdownable.
     * This method shuts down the given shutdownable when the game is shutdown
     *
     * @param shutdownable the shutdownable to delay shutdown.
     * @param <T>          the type of the shutdownable.
     * @return the same shutdownable
     */
    public <T extends Shutdownable> T deferShutdown(T shutdownable) {
        UltracraftClient.instance.shutdownables.add(shutdownable);
        return shutdownable;
    }

    /**
     * Delays closing of a "closeable".
     * This method closes the given closeable when the game is shutdown.
     *
     * @param closeable the closeable to delay closing.
     * @param <T>       the type of the closeable.
     * @return the same closeable.
     */
    public <T extends AutoCloseable> T deferClose(T closeable) {
        UltracraftClient.instance.closeables.add(closeable);
        return closeable;
    }

    /**
     * Gets the boot time of the game.
     *
     * @return the boot time
     */
    public Duration getBootTime() {
        return this.bootTime;
    }

    /**
     * Delays crashing the game.
     *
     * @param crashLog the crash log.
     */
    public void delayCrash(CrashLog crashLog) {
        final var finalCrash = new CrashLog("An error occurred", crashLog, new RuntimeException("Delayed crash"));
        Gdx.app.postRunnable(() -> UltracraftClient.crash(finalCrash));
    }

    /**
     * Self-explanatory, gets the Ultracraft client instance.
     *
     * @return the Ultracraft client instance.
     */
    public static UltracraftClient get() {
        return UltracraftClient.instance;
    }

    /**
     * Gets the Ultracraft identifier for the given path.
     *
     * @param path the path to the resource.
     * @return the identifier for the given path.
     */
    public static ElementID id(String path) {
        return new ElementID(CommonConstants.NAMESPACE, path);
    }

    /**
     * GG bro!
     */
    public static GG ggBro() {
        return new GG();
    }

    private void loadLanguages() {
        var internal = Gdx.files.internal("assets/ultracraft/languages.json");
        List<String> languages;
        try (var reader = internal.reader()) {
            languages = UltracraftClient.GSON.fromJson(reader, LanguageData.class);
        } catch (IOException e) {
            throw new ApplicationCrash(new CrashLog("Language!", e));
        }

        for (var language : languages) {
            this.registerLanguage(UltracraftClient.id(language));
        }

        LanguageRegistry.doRegistration(this::registerLanguage);
    }

    private void registerLanguage(ElementID id) {
        var s = id.path().split("_", 2);
        var locale = s.length == 1 ? Locale.of(s[0]) : Locale.of(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, this.resourceManager);
    }

    private void stitchTextures() {
        this.blocksTextureAtlas = deferDispose(BlockModelRegistry.stitch(this.textureManager));

        TextureStitcher itemTextures = new TextureStitcher(UltracraftClient.id("item"));
        for (Map.Entry<ElementID, Item> e : Registries.ITEM.entries()) {
            if (e.getValue() == Items.AIR || e.getValue() instanceof BlockItem) continue;

            ElementID texId = e.getKey().mapPath(path -> "textures/items/" + path + ".png");
            Texture tex = this.textureManager.getTexture(texId);
            itemTextures.add(texId, tex);
        }
        this.itemTextureAtlas = itemTextures.stitch();
    }

    private void registerRendering() {
        this.registerBlockModels();
        this.registerBlockEntityModels();
        this.registerEntityModels();
        this.registerEntityRenderers();
        this.registerBlockRenderers();
        this.registerBlockRenderTypes();

        for (var e : Registries.ENTITY_TYPE.entries()) {
            EntityType<?> type = e.getValue();
            EntityRenderer<?> renderer = RendererRegistry.get(type);
            EntityModel<?> entityModel = ModelRegistry.get(type);

            FileHandle handle = UltracraftClient.resource(e.getKey().mapPath(path -> "models/entity/" + path + ".g3dj"));
            if (handle.exists()) {
                Model model = UltracraftClient.invokeAndWait(() -> this.modelLoader.loadModel(handle, fileName -> {
                    String filePath = fileName.substring(("assets/" + e.getKey().namespace() + "/models/entity/").length());
                    return new Texture(UltracraftClient.resource(e.getKey().mapPath(path -> "textures/entity/" + filePath)));
                }));
                if (model == null)
                    throw new RuntimeException("Failed to load entity model: " + e.getKey().mapPath(path -> "models/entity/" + path + ".g3dj"));
                model.materials.forEach(modelModel -> {
                    modelModel.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
                    modelModel.set(FloatAttribute.createAlphaTest(0.5f));
                });
                ModelRegistry.registerFinished(type, model);
            } else {
                if (entityModel == null) {
                    LOGGER.warn("Model not found for entity {}", type.getId());
                    continue;
                }

                if (renderer == null) {
                    LOGGER.warn("Renderer not found for entity {}", type.getId());
                    continue;
                }

                ModelRegistry.registerFinished(type, entityModel.finish(renderer.getTextures()));
            }
        }

        RendererRegistry.load();
    }

    private void registerBlockEntityModels() {
        ClientLifecycleEvents.REGISTER_BLOCK_ENTITY_MODELS.factory().onRegister();

        BlockEntityModelRegistry.load(this);
    }

    private void registerBlockRenderTypes() {
        BlockRenderTypeRegistry.register(Blocks.WATER, RenderType.WATER);

        ClientLifecycleEvents.REGISTER_BLOCK_RENDER_TYPES.factory().onRegister();
    }

    private void registerBlockRenderers() {
        ClientLifecycleEvents.REGISTER_BLOCK_RENDERERS.factory().onRegister();
    }

    private void registerBlockModels() {
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, CubeModel.of(UltracraftClient.id("blocks/grass_top"), UltracraftClient.id("blocks/dirt"), UltracraftClient.id("blocks/grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.LOG, CubeModel.of(UltracraftClient.id("blocks/log"), UltracraftClient.id("blocks/log"), UltracraftClient.id("blocks/log_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.CRAFTING_BENCH, CubeModel.of(UltracraftClient.id("blocks/crafting_bench_top"), UltracraftClient.id("blocks/crafting_bench_bottom"), UltracraftClient.id("blocks/crafting_bench_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));

        ClientLifecycleEvents.REGISTER_BLOCK_MODELS.factory().onRegister();

        BlockModelRegistry.registerDefault(Blocks.VOIDGUARD);
        BlockModelRegistry.registerDefault(Blocks.ERROR);
        BlockModelRegistry.registerDefault(Blocks.DIRT);
        BlockModelRegistry.registerDefault(Blocks.SAND);
        BlockModelRegistry.registerDefault(Blocks.SANDSTONE);
        BlockModelRegistry.registerDefault(Blocks.GRAVEL);
        BlockModelRegistry.registerDefault(Blocks.WATER);
        BlockModelRegistry.registerDefault(Blocks.STONE);
        BlockModelRegistry.registerDefault(Blocks.LEAVES);
        BlockModelRegistry.registerDefault(Blocks.PLANKS);
        BlockModelRegistry.registerDefault(Blocks.COBBLESTONE);
        BlockModelRegistry.registerDefault(Blocks.TALL_GRASS);
    }

    private void registerEntityModels() {
        ClientLifecycleEvents.REGISTER_MODELS.factory().onRegister();
    }

    private void registerEntityRenderers() {
        RendererRegistry.register(EntityTypes.PLAYER, PlayerRenderer::new);
        RendererRegistry.register(EntityTypes.DROPPED_ITEM, DroppedItemRenderer::new);

        ClientLifecycleEvents.REGISTER_ENTITY_RENDERERS.factory().onRegister();
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

    /**
     * Pauses the game by showing the pause screen.
     * If the current screen is not null and the world is not null, it will show the pause screen.
     */
    public void pause() {
        if (this.screen == null && this.world != null) {
            this.showScreen(new PauseScreen());
        }
    }

    /**
     * Resumes the game by hiding the pause screen.
     * If the current screen is a PauseScreen and the world is not null,
     * the screen is set to null to resume the game.
     */
    public void resume() {
        if (this.screen instanceof PauseScreen && this.world != null) {
            this.showScreen(null);
        }
    }

    /**
     * Shows the given screen.
     *
     * @param next the screen to open, or null to close the current screen
     * @return true if the screen was opened, false if opening was canceled.
     */
    @CanIgnoreReturnValue
    public boolean showScreen(@Nullable Screen next) {
        if (!isOnMainThread()) {
            @Nullable Screen finalNext = next;
            return invokeAndWait(() -> this.showScreen(finalNext));
        }

        var cur = this.screen;
        if (next == null && this.world == null)
            next = new TitleScreen();

        if (next == null)
            return cur == null || this.closeScreen(cur);

        // Call open event.
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(next);
        if (openResult.isCanceled())
            return false;

        if (openResult.isInterrupted())
            next = openResult.getValue();

        if (cur != null && this.closeScreen(next, cur))
            return false; // Close was canceled
        if (next == null)
            return false; // The next screen is null, canceling.

        this.screen = next;
        this.screen.init(this.getScaledWidth(), this.getScaledHeight());
        DesktopInput.setCursorCaught(false);

        return true;
    }

    private boolean closeScreen(@Nullable Screen next, Screen cur) {
        var closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(cur);
        if (closeResult.isCanceled()) return true;

        if (!cur.onClose(next)) return true;
        cur.onClosed();
        return false;
    }

    private boolean closeScreen(Screen cur) {
        if (this.closeScreen(null, cur)) return false;
        this.screen = null;
        DesktopInput.setCursorCaught(true);

        return true;
    }

    /**
     * Interpolates a value between two values.
     * From <a href="https://www.java2s.com">Java2s</a>
     */
    public static double interpolate(double a, double b, double d) {
        return a + (b - a) * d;
    }

    /**
     * Renders the game.
     * <p>NOTE: This method should not be called.
     * This is invoked by libGDX.</p>
     */
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        try {
            UltracraftClient.PROFILER.update();

            if (this.debugGui != null && this.showDebugHud) {
                this.debugGui.updateProfiler();
            }

            UltracraftClient.PROFILER.section("render", () -> this.doRender(deltaTime));
            this.renderer.actuallyEnd();
        } catch (OutOfMemoryError e) {
            System.gc(); // try to free up some memory before handling out of memory.
            try {
                if (this.integratedServer != null) {
                    this.integratedServer.shutdownNow();
                    this.integratedServer = null;
                }

                if (this.worldRenderer != null) {
                    this.worldRenderer.dispose();
                }
                System.gc();

                this.showScreen(new OutOfMemoryScreen());
            } catch (OutOfMemoryError | Exception t) {
                UltracraftClient.crash(t);
            }
        } catch (Exception t) {
            UltracraftClient.crash(t);
        }

        Gdx.gl.glDisable(GL_CULL_FACE);
    }

    private void doRender(float deltaTime) {
        this.width = Gdx.graphics.getWidth();
        this.height = Gdx.graphics.getHeight();

        if (this.width == 0 || this.height == 0) return;
        if (this.triggerScreenshot) this.prepareScreenshot();

        UltracraftClient.PROFILER.section("renderGame", () -> this.renderGame(renderer, deltaTime));

        if (this.captureScreenshot) this.handleScreenshot();

        if (this.screenshotFlashTime > System.currentTimeMillis() - 200) {
            this.renderer.begin();
            this.shapes.filledRectangle(0, 0, this.width, this.height, this.tmpColor.set(1, 1, 1, 1 - (System.currentTimeMillis() - this.screenshotFlashTime) / 200f));
            this.renderer.end();
        }

        if (this.isCustomBorderShown()) this.drawCustomBorder(renderer);

        ImGuiOverlay.renderImGui(this);
    }

    private void prepareScreenshot() {
        this.screenshotScale = 1;

        if (this.config.get().enable4xScreenshot && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
            this.screenshotScale = 4;
        }

        this.width = this.width * this.screenshotScale;
        this.height = this.height * this.screenshotScale;

        this.captureScreenshot = true;
        this.triggerScreenshot = false;

        this.fbo = new FrameBuffer(Pixmap.Format.RGB888, this.width, this.height, true);
        this.fbo.begin();

        ScreenUtils.clear(0, 0, 0, 1, true);
    }

    private void drawCustomBorder(Renderer renderer) {
        this.renderer.begin();
        renderer.pushMatrix();
        renderer.scale(2, 2);
        this.renderWindow(renderer, this.getWidth() / 2, this.getHeight() / 2);
        renderer.popMatrix();
        this.renderer.end();
    }

    private void handleScreenshot() {
        this.captureScreenshot = false;

        this.saveScreenshot();
        this.fbo.end();
        this.fbo.dispose();

        this.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private boolean renderGame(Renderer renderer, float deltaTime) {
        if (Gdx.graphics.getFrameId() == 2) {
            UltracraftClient.firstRender();
            Gdx.graphics.setTitle("Ultracraft %s".formatted(UltracraftClient.getGameVersion()));
        }

        this.updateActivity();

        this.poll();

        UltracraftClient.PROFILER.section("client-tick", this::tryClientTick);

        boolean renderSplash = this.showUltreonSplash || this.showLibGDXSplash;
        if (this.showLibGDXSplash) {
            this.renderLibGDXSplash(renderer);
        }

        if (this.showUltreonSplash) {
            this.renderUltreonSplash(renderer);
        }
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && this.startDevLoading) {
            this.startLoading();
        }
        if (renderSplash) {
            return true;
        }

        final LoadingOverlay loading = this.loadingOverlay;
        if (loading != null) {
            this.renderLoadingOverlay(renderer, deltaTime, loading);
        }

        if (loading != null) {
            return true;
        }

        this.renderMain(renderer, deltaTime);
        return false;
    }

    private void renderMain(Renderer renderer, float deltaTime) {
        Player player = this.player;
        if (player == null) {
            this.hitResult = null;
        } else {
            UltracraftClient.PROFILER.section("playerRayCast", () -> this.hitResult = player.rayCast());
        }
        GameInput input = this.input;
        if (input != null) {
            UltracraftClient.PROFILER.section("input", input::update);
        }

        Screen screen = this.screen;
        if (screen != null && DesktopInput.isPressingAnyButton() && !this.wasClicking) {
            Gdx.graphics.setCursor(this.clickCursor);
            this.wasClicking = true;
        } else if (screen != null && !DesktopInput.isPressingAnyButton() && this.wasClicking) {
            Gdx.graphics.setCursor(this.normalCursor);
            this.wasClicking = false;
        }

        this.gameRenderer.render(renderer, deltaTime);
    }

    private void renderLoadingOverlay(Renderer renderer, float deltaTime, LoadingOverlay loading) {
        UltracraftClient.PROFILER.section("loading", () -> {
            renderer.begin();
            renderer.pushMatrix();
            renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
            renderer.scale(this.getGuiScale(), this.getGuiScale());
            loading.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
            renderer.popMatrix();
        });
    }

    private void startLoading() {
        this.startDevLoading = false;

        UltracraftClient.setCrashHook(this.crashes::add);

        this.loadingOverlay = new LoadingOverlay(this.ultreonBgTex);

        CompletableFuture.runAsync(this::load).exceptionally(throwable -> {
            // Clear the crash handling
            UltracraftClient.crashHook = null;

            this.crashes.add(new CrashLog("Failed to load", throwable));

            // Show the crash screen
            UltracraftClient.invoke(() -> {
                this.screen = new CrashScreen(this.crashes);
                this.screen.init(this.getScaledWidth(), this.getScaledHeight());
                this.loadingOverlay = null;
            }).exceptionally(throwable1 -> {
                crash(throwable1);
                return null;
            });
            return null;
        }).thenRun(() -> {
            // Clear the crash handling
            UltracraftClient.crashHook = null;

            UltracraftClient.invoke(() -> {
                this.screen = this.crashes.isEmpty() ? new DevScreen() : new CrashScreen(this.crashes);
                this.screen.init(this.getScaledWidth(), this.getScaledHeight());
                this.loadingOverlay = null;
            }).exceptionally(throwable -> {
                crash(throwable);
                return null;
            });
        });
    }

    private void renderLibGDXSplash(Renderer renderer) {
        UltracraftClient.PROFILER.section("libGdxSplash", () -> {
            if (this.libGDXSplashTime == 0L) {
                this.libGDXSplashTime = System.currentTimeMillis();
            }

            ScreenUtils.clear(1, 1, 1, 1, true);

            this.renderer.begin();
            int size = Math.min(this.getWidth(), this.getHeight()) / 2;
            renderer.blit(this.libGDXLogoTex, (float) this.getWidth() / 2 - (float) size / 2, (float) this.getHeight() / 2 - (float) size / 2, size, size);
            this.renderer.end();

            if (System.currentTimeMillis() - this.libGDXSplashTime > 4000f) {
                this.showLibGDXSplash = false;
                this.showUltreonSplash = true;
            }
        });
    }

    private void renderUltreonSplash(Renderer renderer) {
        UltracraftClient.PROFILER.section("ultreonSplash", () -> {
            if (this.ultreonSplashTime == 0L) {
                this.ultreonSplashTime = System.currentTimeMillis();

                this.logoRevealSound.play(0.5f);
            }

            ScreenUtils.clear(0, 0, 0, 1, true);

            final long timeDiff = System.currentTimeMillis() - this.ultreonSplashTime;
            float zoom = (float) UltracraftClient.interpolate(UltracraftClient.FROM_ZOOM, UltracraftClient.TO_ZOOM, Mth.clamp(timeDiff / UltracraftClient.DURATION, 0f, 1f));
            Vec2f thumbnail = this.resizer.thumbnail(this.getWidth() * zoom, this.getHeight() * zoom);

            float drawWidth = thumbnail.x;
            float drawHeight = thumbnail.y;

            float drawX = (this.getWidth() - drawWidth) / 2;
            float drawY = (this.getHeight() - drawHeight) / 2;

            this.renderer.begin();
            renderer.blit(this.ultreonBgTex, 0, 0, this.getWidth(), this.getHeight(), 0, 0, 1024, 1024, 1024, 1024);
            renderer.blit(this.ultreonLogoTex, (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, 1920, 1080, 1920, 1080);
            this.renderer.end();

            if (System.currentTimeMillis() - this.ultreonSplashTime > UltracraftClient.DURATION) {
                this.startLoading();
            }
        });
    }

    /**
     * Retrieves the game version of the UltraCraft mod.
     *
     * @return The game version as a {@code String}.
     */
    public static String getGameVersion() {
        return FabricLoader.getInstance().getModContainer("ultracraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    private void tryClientTick() {
        var canTick = false;

        double time2 = System.currentTimeMillis();
        var passed = time2 - this.time;
        this.frameTime += (float) passed;
        this.tickTime += (float) passed;

        this.time = time2;

        float tickCap = 1000f / UltracraftServer.TPS;
        while (this.frameTime >= tickCap) {
            this.frameTime -= tickCap;
            this.partialTick = this.frameTime / tickCap;

            canTick = true;
        }

        if (canTick) {
            this.ticksPassed++;
            try {
                this.clientTick();
            } catch (ApplicationCrash e) {
                UltracraftClient.crash(e.getCrashLog());
            } catch (Exception t) {
                var crashLog = new CrashLog("Game being ticked.", t);
                UltracraftClient.crash(crashLog);
            }
        }

        if (this.tickTime >= 1000.0d) {
            this.currentTps = this.ticksPassed;
            this.ticksPassed = 0;
            this.tickTime = 0;
        }
    }

    private void updateActivity() {
        if (this.activity != this.oldActivity) {
            this.oldActivity = this.activity;
            if (this.activity == null) {
                Gdx.graphics.setTitle("Ultracraft " + UltracraftClient.getGameVersion());
            } else {
                var name = this.activity.getDisplayName();
                Gdx.graphics.setTitle("Ultracraft " + UltracraftClient.getGameVersion() + " - " + name);
            }

            RpcHandler.setActivity(this.activity);
        }
    }

    private void saveScreenshot() {
        if (this.spriteBatch.isDrawing()) this.spriteBatch.flush();
        if (this.modelBatch.getCamera() != null) this.modelBatch.flush();

        Screenshot grabbed = Screenshot.grab(this.width, this.height);
        FileHandle save = grabbed.save("screenshots/%s.png".formatted(DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss").format(LocalDateTime.now())));

        this.screenshotFlashTime = System.currentTimeMillis();

        this.playSound(SoundEvents.SCREENSHOT, 0.5f);

        this.notifications.add("Screenshot taken.", save.name(), "screenshots");
    }

    private static void firstRender() {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
        Lwjgl3Window window = graphics.getWindow();
        window.setVisible(true);
    }

    private void renderWindow(Renderer renderer, int width, int height) {
        renderer.draw9PatchTexture(this.windowTex, 0, 0, width, height, 0, 0, 18, 22, 256, 256);
    }

    public static void crash(Throwable throwable) {
        UltracraftClient.LOGGER.error("Game crash triggered:", throwable);
        var crashLog = new CrashLog("An unexpected error occurred", throwable);
        UltracraftClient.crash(crashLog);
    }

    public static void crash(CrashLog crashLog) {
        try {
            Callback<CrashLog> handler = UltracraftClient.crashHook;
            if (handler != null) {
                try {
                    handler.call(crashLog);
                    return;
                } catch (Exception e) {
                    UltracraftClient.LOGGER.error("Crash hook failed", e);
                    System.exit(3);
                }
            }

            UltracraftClient.instance.fillGameInfo(crashLog);
            var crash = crashLog.createCrash();
            UltracraftClient.crash(crash);
        } catch (Exception | OutOfMemoryError t) {
            UltracraftClient.LOGGER.error(UltracraftClient.FATAL_ERROR_MSG, t);
            System.exit(2);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @ApiStatus.Internal
    public void fillGameInfo(CrashLog crashLog) {
        if (this.world != null) {
            this.world.fillCrashInfo(crashLog);
        }

        var client = new CrashCategory("Game Details");
        client.add("Time until crash", Duration.ofMilliseconds(System.currentTimeMillis() - UltracraftClient.BOOT_TIMESTAMP).toSimpleString()); // Could be the game only crashes after a long time.
        client.add("Game booted", this.booted); // Could be that the game isn't booted yet.
        crashLog.addCategory(client);
    }

    private static void crash(ApplicationCrash crash) {
        if (crashing) {
            LOGGER.error("Double crash detected, ignoring.");
            return;
        }
        crashing = true;
        try {
            var crashLog = crash.getCrashLog();
            CrashHandler.handleCrash(crashLog);
            Runtime.getRuntime().exit(1);
        } catch (Exception | OutOfMemoryError t) {
            UltracraftClient.LOGGER.error(UltracraftClient.FATAL_ERROR_MSG, t);
            Runtime.getRuntime().exit(2);
        }
    }

    private static void cleanUp(@Nullable Disposable disposable) {
        if (disposable == null) return;

        try {
            disposable.dispose();
        } catch (Exception throwable) {
            Debugger.log("Failed to dispose " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable Shutdownable disposable) {
        if (disposable == null) return;

        try {
            disposable.shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception throwable) {
            Debugger.log("Failed to shut down " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable ExecutorService disposable) {
        if (disposable == null) return;

        try {
            disposable.shutdownNow();
        } catch (Exception throwable) {
            Debugger.log("Failed to shut down " + disposable.getClass().getName(), throwable);
        }
    }

    private static void cleanUp(@Nullable AutoCloseable disposable) {
        if (disposable == null) return;

        try {
            disposable.close();
        } catch (Exception throwable) {
            Debugger.log("Failed to close " + disposable.getClass().getName(), throwable);
        }
    }

    public void clientTick() {
        if (this.player != null && this.world != null) {
            this.cursor = this.world.rayCast(new Ray(this.player.getPosition().add(0, this.player.getEyeHeight(), 0), this.player.getLookVector()));
        }

        Connection connection = this.connection;
        if (connection != null) {
            connection.tick();
            this.clientConn.tick(connection);
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

        if (this.inputCooldown-- < 0) {
            this.inputCooldown = 0;
        }

        if (this.gamepadInput != null) {
            this.gamepadInput.update();

            if (this.screen != null) this.gamepadInput.updateScreen(this.screen);
            else this.gamepadInput.update();
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
            if (item instanceof ToolItem toolItem && this.breakingBlock.getEffectiveTool() == ((ToolItem) item).getToolType()) {
                efficiency = toolItem.getEfficiency();
            }

            if (world.continueBreaking(breaking, 1.0F / (Math.max(this.breakingBlock.getHardness() * UltracraftServer.TPS / efficiency, 0) + 1), this.player) != BreakResult.CONTINUE) {
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
        LocalPlayer player = this.player;

        if (this.world == null) return;
        if (this.breaking == null) return;
        if (player == null) return;

        this.world.stopBreaking(new BlockPos(this.breaking), player);
        Block block = hitResult.getBlock();

        if (block == null || block.isAir()) {
            this.breaking = null;
            this.breakingBlock = null;
        } else {
            this.breaking = hitResult.getPos();
            this.breakingBlock = block;
            this.world.startBreaking(new BlockPos(hitResult.getPos()), player);
        }
    }

    public void resize(int width, int height) {
        if (!UltracraftClient.isOnMainThread()) {
            UltracraftClient.invokeAndWait(() -> this.resize(width, height));
            return;
        }

        this.spriteBatch.getProjectionMatrix().setToOrtho(0, width, height, 0, 0, 1000000);
        this.deferredWidth = width;
        this.deferredHeight = height;

        if (this.autoScale) {
            this.guiScale = this.calcMaxGuiScale();
        }

        if (this.camera != null) {
            this.camera.viewportWidth = width;
            this.camera.viewportHeight = height;
            this.camera.update();
        }

        if (this.itemRenderer != null) {
            this.itemRenderer.resize(width, height);
        }

        this.gameRenderer.resize(width, height);

        var cur = this.screen;
        if (cur != null) {
            cur.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));
        }
    }

    @Override
    @SuppressWarnings("ConstantValue")
    public void dispose() {
        if (!UltracraftClient.isOnMainThread()) {
            throw new IllegalThreadError("Should only dispose on LibGDX main thread");
        }

        synchronized (this) {
            this.disposed = true;

            try {
                while (!this.futures.isEmpty()) {
                    this.futures.removeIf(CompletableFuture::isDone);
                }

                GameInput.cancelVibration();

                UltracraftServer.getWatchManager().stop();

                UltracraftClient.cleanUp((ExecutorService) this.integratedServer);

                if (this.scheduler != null) this.scheduler.shutdownNow();
                if (this.chunkLoadingExecutor != null) this.chunkLoadingExecutor.shutdownNow();

                UltracraftClient.cleanUp(this.garbageCollector);
                UltracraftClient.cleanUp(this.world);
                UltracraftClient.cleanUp(this.worldRenderer);
                UltracraftClient.cleanUp(this.profiler);
                GamepadInput gamepadInput = this.gamepadInput;
                if (gamepadInput != null) {
                    gamepadInput.dispose();
                }

                ImGuiOverlay.dispose();

                this.disposables.forEach(UltracraftClient::cleanUp);
                this.shutdownables.forEach(UltracraftClient::cleanUp);
                this.closeables.forEach(UltracraftClient::cleanUp);

                ClientLifecycleEvents.GAME_DISPOSED.factory().onGameDisposed();
            } catch (Exception t) {
                UltracraftClient.crash(t);
                Runtime.getRuntime().halt(2);
            }
        }
    }

    public boolean isDevMode() {
        return this.isDevMode;
    }

    public boolean isShowingImGui() {
        return ImGuiOverlay.isShown();
    }

    public void setShowingImGui(boolean value) {
        ImGuiOverlay.setShowingImGui(value);
    }

    public int getWidth() {
        return this.width - this.getDrawOffset().x * 2;
    }

    public int getHeight() {
        return this.height - this.getDrawOffset().y * 2;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public void startWorld(WorldStorage storage) {
        this.showScreen(new WorldLoadScreen(storage));
    }

    public void startWorld(Path path) {
        this.showScreen(new WorldLoadScreen(new WorldStorage(path)));
    }

    public float getGuiScale() {
        return this.guiScale;
    }

    public int getScaledWidth() {
        return ceil(Gdx.graphics.getWidth() / this.getGuiScale());
    }

    public int getScaledHeight() {
        return ceil(Gdx.graphics.getHeight() / this.getGuiScale());
    }

    public void exitWorldToTitle() {
        this.exitWorldAndThen(() -> this.showScreen(new TitleScreen()));
    }

    public void exitWorldAndThen(Runnable runnable) {
        this.closingWorld = true;
        this.renderWorld = false;

        final @Nullable WorldRenderer worldRenderer = this.worldRenderer;
        this.showScreen(new MessageScreen(TextObject.translation("ultracraft.screen.message.saving_world"))); // "Saving world..."
        UltracraftClient.cleanUp(worldRenderer);

        CompletableFuture.runAsync(() -> {
            this.player = null;

            if (this.connection != null) {
                try {
                    var close = this.connection.close();
                    if (close != null) close.sync();
                    var future = this.connection.closeGroup();
                    if (future != null) future.sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    UltracraftClient.crash(e);
                    return;
                }
            }

            if (this.integratedServer != null) {
                this.integratedServer.shutdown();
            }

            this.serverTickQueue.clear();

            try {
                GameInput.cancelVibration();

                UltracraftClient.cleanUp(this.world);
                this.worldRenderer = null;
                this.world = null;
                UltracraftClient.invoke(runnable);
            } catch (Exception e) {
                UltracraftClient.crash(e);
            }
        });
    }

    public boolean isClosingWorld() {
        return this.closingWorld;
    }

    public ScheduledFuture<Void> schedule(Task<?> task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltracraftClient.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
            return null;
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<Void> schedule(Task<?> task, long time, TimeUnit unit) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltracraftClient.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
            return null;
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
        if (!eventResult.isCanceled() && (this.world != null)) {
            this.exitWorldAndThen(() -> {
                try {
                    var close = this.connection.close();
                    if (close != null) close.sync();
                    var future = this.connection.closeGroup();
                    if (future != null) future.sync();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    UltracraftClient.LOGGER.warn("Error occurred while closing connection:", e);
                }
                Gdx.app.postRunnable(Gdx.app::exit);
            });
            return false;
        }
        return !eventResult.isCanceled();
    }

    public void filesDropped(String[] files) {
        var currentScreen = this.screen;
        var handles = Arrays.stream(files).map(FileHandle::new).toList();

        if (currentScreen != null) {
            currentScreen.filesDropped(handles);
        }
    }

    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

    public @NotNull BakedCubeModel getBakedBlockModel(Block block) {
        return Objects.requireNonNull(this.bakedBlockModels.bakedModels().getOrDefault(block, BakedCubeModel.DEFAULT));
    }

    public void resetBreaking() {
        HitResult hitResult = this.hitResult;
        Player player = this.player;
        if (hitResult == null || this.world == null || this.breaking == null) return;
        if (player == null) return;
        this.world.stopBreaking(new BlockPos(hitResult.getPos()), player);
        this.world.startBreaking(new BlockPos(hitResult.getPos()), player);
        this.breaking = hitResult.getPos();
        this.breakingBlock = hitResult.getBlock();
    }

    public void startBreaking() {
        HitResult hitResult = this.hitResult;
        LocalPlayer player = this.player;
        if (hitResult == null || this.world == null) return;
        if (this.world.getBreakProgress(new BlockPos(hitResult.getPos())) >= 0.0F) return;
        if (player == null) return;
        this.world.startBreaking(new BlockPos(hitResult.getPos()), player);
        this.breaking = hitResult.getPos();
        this.breakingBlock = hitResult.getBlock();
    }

    public void stopBreaking() {
        HitResult hitResult = this.hitResult;
        LocalPlayer player = this.player;
        if (hitResult == null || this.world == null) return;
        if (player == null) return;

        this.world.stopBreaking(new BlockPos(hitResult.getPos()), player);
        this.breaking = null;
        this.breakingBlock = null;
    }

    public float getBreakProgress() {
        Vec3i breaking = this.breaking;
        World world = this.world;
        if (breaking == null || world == null) return -1;
        return world.getBreakProgress(new BlockPos(breaking));
    }

    private int calcMaxGuiScale() {
        var windowWidth = Gdx.graphics.getWidth();
        var windowHeight = Gdx.graphics.getHeight();

        if (windowWidth / UltracraftClient.MINIMUM_WIDTH < windowHeight / UltracraftClient.MINIMUM_HEIGHT) {
            return windowWidth / UltracraftClient.MINIMUM_WIDTH;
        }

        if (windowHeight / UltracraftClient.MINIMUM_HEIGHT < windowWidth / UltracraftClient.MINIMUM_WIDTH) {
            return windowHeight / UltracraftClient.MINIMUM_HEIGHT;
        }

        return Math.min(windowWidth / UltracraftClient.MINIMUM_WIDTH, windowHeight / UltracraftClient.MINIMUM_HEIGHT);
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

    @ApiStatus.Experimental
    public boolean isCustomBorderShown() {
        return false;
    }

    public boolean isLoading() {
        return this.loading;
    }

    public static GameEnvironment getGameEnv() {
        if (UltracraftClient.instance == null) return GameEnvironment.UNKNOWN;
        return UltracraftClient.instance.gameEnv;
    }

    public IntegratedServer getSingleplayerServer() {
        return this.integratedServer;
    }

    public boolean isSinglePlayer() {
        return this.integratedServer != null && !this.integratedServer.isOpenToLan();
    }

    public void playSound(@NotNull SoundEvent soundEvent, float volume) {
        Preconditions.checkNotNull(soundEvent);
        Preconditions.checkArgument(volume >= 0.0F && volume <= 1.0F, "Volume must be between 0.0F and 1.0F");

        Sound sound = this.soundRegistry.getSound(soundEvent.getId());
        if (sound == null) {
            UltracraftClient.LOGGER.warn("Unknown sound event: {}", soundEvent.getId());
            return;
        }
        sound.play(volume);
    }

    public void startIntegratedServer() {
        this.integratedServer.start();

        SocketAddress localServer = this.integratedServer.getConnections().startMemoryServer();
        this.connection = ClientConnection.connectToLocalServer(localServer);
        this.clientConn = new ClientConnection(localServer);

        // Initialize (memory) connection.
        this.multiplayerData = new MultiplayerData(this);
        this.connection.initiate(localServer.toString(), 0, new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    public void connectToServer(String host, int port) {
        this.connection = new Connection(PacketDestination.SERVER);
        ClientConnection.connectTo(new InetSocketAddress(host, port), this.connection).syncUninterruptibly();

        // Initialize remote connection.
        this.multiplayerData = new MultiplayerData(this);
        this.connection.initiate(host, port, new LoginClientPacketHandlerImpl(this.connection), new C2SLoginPacket(this.user.name()));
    }

    public int getCurrentTps() {
        return this.currentTps;
    }

    public void setActivity(GameActivity activity) {
        this.activity = activity;
    }

    public void setFullScreen(boolean fullScreen) {
        if (Gdx.graphics.isFullscreen() != fullScreen) {
            if (fullScreen) {
                this.oldMode = new Vec2i(this.getWidth(), this.getHeight());
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

    public void setGuiScale(float guiScale) {
        if (autoScale) {
            this.guiScale = this.calcMaxGuiScale();
            return;
        }
        this.guiScale = guiScale;
        this.resize(this.width, this.height);
    }

    public MultiplayerData getMultiplayerData() {
        return this.multiplayerData;
    }

    public boolean isRenderingWorld() {
        return this.world != null && this.worldRenderer != null && this.renderWorld;
    }

    @Override
    public String toString() {
        return "UltracraftClient";
    }

    public void runInTick(Runnable func) {
        this.serverTickQueue.add(func);
    }

    @ApiStatus.Internal
    public void pollServerTick() {
        Runnable task;
        while ((task = this.serverTickQueue.poll()) != null) {
            task.run();
        }
    }

    public User getUser() {
        return this.user;
    }

    public Environment getEnvironment() {
        if (this.worldRenderer != null) {
            return this.worldRenderer.getEnvironment();
        }
        return this.defaultEnv;
    }

    public RenderPipeline getPipeline() {
        return pipeline;
    }

    public GameWindow getWindow() {
        return window;
    }

    public void setInputType(InputType inputType) {
        if (this.inputCooldown > 0) {
            return;
        }

        this.inputType = inputType;
        this.inputCooldown = 10;
    }

    public InputType getInputType() {
        return this.inputType;
    }

    public void forceSetInputType(InputType inputType, int cooldown) {
        this.inputType = inputType;
        this.inputCooldown = cooldown;
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

    private static class WindowAdapter extends Lwjgl3WindowAdapter {
        private Lwjgl3Window window;

        @Override
        public void created(Lwjgl3Window window) {
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.NOTIFICATION, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.LOW, false);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.MEDIUM, true);
            Lwjgl3Application.setGLDebugMessageControl(Lwjgl3Application.GLDebugMessageSeverity.HIGH, true);

            WindowEvents.WINDOW_CREATED.factory().onWindowCreated(window);
            this.window = window;
        }

        @Override
        public void focusLost() {
            UltracraftClient.get().pause();

            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(this.window, false);
        }

        @Override
        public void focusGained() {
            WindowEvents.WINDOW_FOCUS_CHANGED.factory().onWindowFocusChanged(this.window, true);
        }

        @Override
        public boolean closeRequested() {
            return UltracraftClient.get().tryShutdown();
        }

        @Override
        public void filesDropped(String[] files) {
            UltracraftClient.get().filesDropped(files);

            WindowEvents.WINDOW_FILES_DROPPED.factory().onWindowFilesDropped(this.window, files);
        }

    }
}
