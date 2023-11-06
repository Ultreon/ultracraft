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
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.google.common.base.Preconditions;
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
import com.ultreon.craft.client.events.WindowEvents;
import com.ultreon.craft.client.font.Font;
import com.ultreon.craft.client.font.FontRegistry;
import com.ultreon.craft.client.gui.Renderer;
import com.ultreon.craft.client.gui.*;
import com.ultreon.craft.client.gui.screens.Screen;
import com.ultreon.craft.client.gui.screens.*;
import com.ultreon.craft.client.imgui.ImGuiOverlay;
import com.ultreon.craft.client.init.Fonts;
import com.ultreon.craft.client.input.DesktopInput;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.input.GameInput;
import com.ultreon.craft.client.input.PlayerInput;
import com.ultreon.craft.client.item.ItemRenderer;
import com.ultreon.craft.client.model.*;
import com.ultreon.craft.client.multiplayer.MultiplayerData;
import com.ultreon.craft.client.network.ClientConnection;
import com.ultreon.craft.client.network.LoginClientPacketHandlerImpl;
import com.ultreon.craft.client.player.ClientPlayer;
import com.ultreon.craft.client.player.LocalPlayer;
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
import com.ultreon.craft.client.world.*;
import com.ultreon.craft.debug.DefaultInspections;
import com.ultreon.craft.debug.Profiler;
import com.ultreon.craft.debug.inspect.InspectionNode;
import com.ultreon.craft.debug.inspect.InspectionRoot;
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
import com.ultreon.craft.server.CommonConstants;
import com.ultreon.craft.server.UltracraftServer;
import com.ultreon.craft.sound.event.SoundEvents;
import com.ultreon.craft.text.TextObject;
import com.ultreon.craft.util.*;
import com.ultreon.craft.world.*;
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
import com.ultreon.libs.translations.v1.LanguageManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModOrigin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

import static com.badlogic.gdx.graphics.GL20.GL_CULL_FACE;
import static com.badlogic.gdx.graphics.GL20.GL_FRONT;
import static com.badlogic.gdx.math.MathUtils.ceil;

@SuppressWarnings("UnusedReturnValue")
public class UltracraftClient extends PollingExecutorService implements DeferredDisposable {
    public static final String NAMESPACE = CommonConstants.NAMESPACE;
    public static final Logger LOGGER = LoggerFactory.getLogger("UltracraftClient");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    public static final int[] SIZES = new int[]{16, 24, 32, 40, 48, 64, 72, 80, 96, 108, 128, 160, 192, 256, 1024};
    static final int CULL_FACE = GL_FRONT;
    public static final float FROM_ZOOM = 2.0f;
    public static final float TO_ZOOM = 1.3f;
    private static final float DURATION = 6000f;
    private static ArgParser arguments;
    private final Cursor normalCursor;
    private final Cursor clickCursor;
    public Connection connection;
    public ServerData serverData;
    @Deprecated
    public List<Vec3d> remotePlayers = new CopyOnWriteArrayList<>();
    public ExecutorService chunkLoadingExecutor = Executors.newFixedThreadPool(4);
    public static Profiler PROFILER = new Profiler();
    public final InspectionRoot<UltracraftClient> inspection = new InspectionRoot<>(this);
    private Duration bootTime;
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
    public LocalPlayer player;
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
    private float guiScale = this.calculateGuiScale();

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
    DebugGui debugRenderer;
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

    UltracraftClient(String[] argv) {
        super(UltracraftClient.PROFILER);
        UltracraftClient.LOGGER.info("Booting game!");
        UltracraftClient.instance = this;

        this.registerAutoFillers();

        this.argv = argv;

        this.user = new User("Player" + MathUtils.random(100, 999));

        this.mainThread = Thread.currentThread();

        ImGuiOverlay.preInitImGui();

        this.resourceManager = new ResourceManager("assets");
        this.textureManager = new TextureManager(this.resourceManager);

        this.camera = new GameCamera(67, this.getWidth(), this.getHeight());
        this.camera.near = 0.01f;
        this.camera.far = 2;

        // White pixel for the shape drawer.
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

        this.gameRenderer = new GameRenderer(this, this.modelBatch, this.spriteBatch);

        // Textures
        this.ultreonBgTex = new Texture("assets/ultracraft/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/ultracraft/logo.png");
        this.libGDXLogoTex = new Texture("assets/ultracraft/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/ultracraft/sounds/logo_reveal.mp3"));

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());

        this.normalCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/ultracraft/textures/cursors/normal.png")), 0, 0);
        this.clickCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("assets/ultracraft/textures/cursors/click.png")), 0, 0);

        LanguageManager.setCurrentLanguage(new Locale("en", "us"));

        Gdx.graphics.setCursor(this.normalCursor);

        if (CommonConstants.INSPECTION_ENABLED) {
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

    private void registerAutoFillers() {
        InspectionRoot.registerAutoFill(ClientChunk.class, node -> {
            node.createNode("mesh", value -> value.mesh);
            node.createNode("transparentMesh", value -> value.transparentMesh);
            node.createNode("dirty", value -> value.dirty);
            node.createNode("renderOffset", value -> value.renderOffset);
        });

        InspectionRoot.registerAutoFill(ClientWorld.class, node -> {
            node.create("renderDistance", ClientWorld::getRenderDistance);
        });
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
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     *
     * @param argv the arguments to pass to the game
     * @deprecated the method is replaced by {@link #launch(String[])}
     */
    @Deprecated
    @ApiStatus.Internal
    public static void main(String[] argv) {
        try {
            UltracraftClient.arguments = new ArgParser(argv);

            try {
                CrashHandler.addHandler(crashLog -> {
                    try {
                        DesktopInput.setCursorCaught(false);
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

    /**
     * <h2 style="color:red;"><b>Note: This method should not be called.</b></h2>
     * Launches the game.
     * This method gets invoked dynamically by the FabricMC game provider.
     *
     * @param argv the arguments to pass to the game
     */
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
        config.useVsync(false);
        config.setForegroundFPS(2000);
        config.setIdleFPS(10);
        config.setBackBufferConfig(8, 8, 8, 8, 8, 8, 0);
        config.setInitialVisible(false);
        config.setTitle("Ultracraft");
        config.setWindowIcon(UltracraftClient.getIcons());
        config.setWindowedMode(1280, 720);
        config.setWindowListener(new Lwjgl3WindowAdapter() {
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
    private void load() throws Throwable {
        Identifier.setDefaultNamespace(UltracraftClient.NAMESPACE);

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

        this.unifont = UltracraftClient.invokeAndWait(() -> new BitmapFont(Gdx.files.internal("assets/ultracraft/font/unifont/unifont.fnt"), true));
        this.font = new Font(UltracraftClient.invokeAndWait(() -> new BitmapFont(Gdx.files.internal("assets/ultracraft/font/dogica/dogicapixel.fnt"), true)));

        this.crashOverlay = new ManualCrashOverlay(this);

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
        SoundEvents.nopInit();
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
        UltracraftClient.worldStorage.createWorld();

        ImGuiOverlay.setupImGui();

        this.booted = true;

        this.loadingOverlay.setProgress(1.0F);

        this.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - UltracraftClient.BOOT_TIMESTAMP);
        UltracraftClient.LOGGER.info("Game booted in " + this.bootTime.toSimpleString() + ".");

        UltracraftClient.invoke(new Task<>(UltracraftClient.id("main/show_title_screen"), () -> this.showScreen(new TitleScreen())));
        this.loadingOverlay = null;
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
        ModIconOverrides.set("gdx", new Identifier("gdx", "icon.png"));

        // Invoke entry points.
        FabricLoader.getInstance().invokeEntrypoints(ModInit.ENTRYPOINT_KEY, ModInit.class, ModInit::onInitialize);
        FabricLoader.getInstance().invokeEntrypoints(ClientModInit.ENTRYPOINT_KEY, ClientModInit.class, ClientModInit::onInitializeClient);
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

    /**
     * Gets the resource file handle for the given identifier.
     *
     * @param id the identifier.
     * @return the resource file handle.
     */
    public static FileHandle resource(Identifier id) {
        return new ResourceFileHandle(id);
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
        return Thread.currentThread().getId() == UltracraftClient.instance.mainThread.getId();
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
        UltracraftClient.instance.disposables.add(disposable);
        return disposable;
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
        Gdx.app.postRunnable(() -> {
            var finalCrash = new CrashLog("An error occurred", crashLog, new RuntimeException("Delayed crash"));
            UltracraftClient.crash(finalCrash);
        });
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
    public static Identifier id(String path) {
        return new Identifier(path);
    }

    public static GG ggBro() {
        return new GG();
    }

    private void loadLanguages() {
        var internal = Gdx.files.internal("assets/ultracraft/languages.json");
        List<String> languages;
        try (var reader = internal.reader()) {
            languages = UltracraftClient.GSON.fromJson(reader, LanguageData.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load languages register", e);
        }

        System.out.println(internal.readString());
        for (var language : languages) {
            this.registerLanguage(UltracraftClient.id(language));
        }

        LanguageRegistry.doRegistration(this::registerLanguage);

        Locale locale = new Locale("lol", "us");
        System.out.println("locale.getDisplayLanguage() = " + locale.getDisplayLanguage());
        System.out.println("locale.getDisplayName() = " + locale.getDisplayName());
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

    /**
     * Shows the given screen.
     *
     * @param next the screen to open, or null to close the current screen
     * @return true if the screen was opened, false if opening was canceled.
     */
    @CanIgnoreReturnValue
    public boolean showScreen(@Nullable Screen next) {
        var cur = this.screen;
        if (next == null && this.world == null) {
            next = new TitleScreen();
        }

        if (next == null) {
            if (cur == null) return true;

            var result = ScreenEvents.CLOSE.factory().onCloseScreen(this.screen);
            if (result.isCanceled()) return false;

            if (!cur.onClose(null)) return false;
            cur.onClosed();
            this.screen = null;
            DesktopInput.setCursorCaught(true);

            return true;
        }

        // Call open event.
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(next);
        if (openResult.isCanceled()) {
            return false;
        }

        if (openResult.isInterrupted()) {
            next = openResult.getValue();
        }

        if (cur != null) {
            var closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(cur);
            if (closeResult.isCanceled()) return false;

            if (!cur.onClose(next)) return false;
            cur.onClosed();
        } else {
            if (next == null) return false;

            DesktopInput.setCursorCaught(false);
        }

        this.screen = next;
        if (this.screen != null) {
            this.screen.init(this.getScaledWidth(), this.getScaledHeight());
        }

        return true;
    }

    /**
     * Interpolates a value between two values.
     * From <a href="https://www.java2s.com">Java2s</a>
     */
    public static double interpolate(double a, double b, double d) {
        return a + (b - a) * d;
    }

    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        try {
            UltracraftClient.PROFILER.update();

            if (this.debugRenderer != null && this.showDebugHud) {
                this.debugRenderer.updateProfiler();
            }

            UltracraftClient.PROFILER.section("render", () -> {
                this.width = Gdx.graphics.getWidth();
                this.height = Gdx.graphics.getHeight();

                if (this.width == 0 || this.height == 0) return;
                if (this.triggerScreenshot) this.prepareScreenshot();

                Renderer renderer = new Renderer(this.shapes);

                UltracraftClient.PROFILER.section("renderGame", () -> this.renderGame(renderer, deltaTime));

                if (this.captureScreenshot) this.handleScreenshot();
                if (this.isCustomBorderShown()) this.drawCustomBorder(renderer);

                ImGuiOverlay.renderImGui(this);
            });
        } catch (Throwable t) {
            UltracraftClient.crash(t);
        }

        Gdx.gl.glDisable(GL_CULL_FACE);
    }

    private void prepareScreenshot() {
        this.screenshotScale = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ? 4 : 1;
        this.width = this.width * this.screenshotScale;
        this.height = this.height * this.screenshotScale;

        this.captureScreenshot = true;
        this.triggerScreenshot = false;

        this.fbo = new FrameBuffer(Pixmap.Format.RGB888, this.width, this.height, true);
        this.fbo.begin();

        ScreenUtils.clear(0, 0, 0, 1, true);
    }

    private void drawCustomBorder(Renderer renderer) {
        this.spriteBatch.begin();
        renderer.pushMatrix();
        renderer.scale(2, 2);
        this.renderWindow(renderer, this.getWidth() / 2, this.getHeight() / 2);
        renderer.popMatrix();
        this.spriteBatch.end();
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
            Gdx.graphics.setTitle("Ultracraft " + UltracraftClient.getGameVersion());
        }

        this.updateActivity();

        this.pollAll();

        UltracraftClient.PROFILER.section("client-tick", this::tryClientTick);

        boolean renderSplash = this.showUltreonSplash || this.showLibGDXSplash;
        if (this.showLibGDXSplash) {
            UltracraftClient.PROFILER.section("libGdxSplash", () -> {
                if (this.libGDXSplashTime == 0L) {
                    this.libGDXSplashTime = System.currentTimeMillis();
                }

                ScreenUtils.clear(1, 1, 1, 1, true);

                this.spriteBatch.begin();
                int size = Math.min(this.getWidth(), this.getHeight()) / 2;
                renderer.blit(this.libGDXLogoTex, (float) this.getWidth() / 2 - (float) size / 2, (float) this.getHeight() / 2 - (float) size / 2, size, size);
                this.spriteBatch.end();

                if (System.currentTimeMillis() - this.libGDXSplashTime > 4000f) {
                    this.showLibGDXSplash = false;
                    this.showUltreonSplash = true;
                }
            });
        }

        if (this.showUltreonSplash) {
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
            });
        }

        if (renderSplash) {
            return true;
        }

        final LoadingOverlay loading = this.loadingOverlay;
        if (loading != null) {
            UltracraftClient.PROFILER.section("loading", () -> {
                this.spriteBatch.begin();
                renderer.pushMatrix();
                renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
                renderer.scale(this.getGuiScale(), this.getGuiScale());
                loading.render(renderer, Integer.MAX_VALUE, Integer.MAX_VALUE, deltaTime);
                renderer.popMatrix();
                this.spriteBatch.end();
            });
        }

        if (loading != null) {
            return true;
        }

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
        return false;
    }

    public static String getGameVersion() {
        return FabricLoader.getInstance().getModContainer("ultracraft").orElseThrow().getMetadata().getVersion().getFriendlyString();
    }

    private void tryClientTick() {
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

        byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, this.width, this.height, false);
        Pixmap pixmap = new Pixmap(this.width, this.height, Pixmap.Format.RGBA8888);
        BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);

        PixmapIO.writePNG(UltracraftClient.data(String.format("screenshots/screenshot_%s.png", DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss").format(LocalDateTime.now()))), pixmap, Deflater.DEFAULT_COMPRESSION, true);
        pixmap.dispose();
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

        Connection connection = this.connection;
        if (connection != null) connection.tick();

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

            this.profiler.close();

            if (this.integratedServer != null && this.integratedServer.isRunning()) this.integratedServer.shutdown();

            if (this.scheduler != null) this.scheduler.shutdownNow().clear();
            if (this.garbageCollector != null) this.garbageCollector.shutdown();
            if (this.chunkLoadingExecutor != null) this.chunkLoadingExecutor.shutdownNow();

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

    public void startWorld() {
        this.showScreen(new WorldLoadScreen(UltracraftClient.getSavedWorld()));
    }

    public void startWorld(WorldStorage storage) {
        this.showScreen(new WorldLoadScreen(storage));
    }

    public void startWorld(Path path) {
        this.showScreen(new WorldLoadScreen(new WorldStorage(path)));
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
        this.showScreen(new MessageScreen(TextObject.translation("ultracraft.screen.message.saving_world"))); // "Saving world..."
        if (worldRenderer != null) worldRenderer.dispose();

        if (this.connection != null) {
            this.connection.disconnect("User self-disconnected");
            this.connection.close();
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
            UltracraftClient.LOGGER.warn("Unknown sound event: " + soundEvent.getId());
            return;
        }
        sound.play(volume);
    }

    public void startIntegratedServer() {
        this.integratedServer.start();

        SocketAddress localServer = this.integratedServer.getConnections().startMemoryServer();
        this.connection = ClientConnection.connectToLocalServer(localServer);

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

    public void setActivity(Activity activity) {
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
        this.guiScale = guiScale;
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
