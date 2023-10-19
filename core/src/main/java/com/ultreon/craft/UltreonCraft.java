package com.ultreon.craft;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.config.GameSettings;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.LifecycleEvents;
import com.ultreon.craft.events.ScreenEvents;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.font.Font;
import com.ultreon.craft.init.Fonts;
import com.ultreon.craft.init.Sounds;
import com.ultreon.craft.input.*;
import com.ultreon.craft.platform.PlatformType;
import com.ultreon.craft.registry.LanguageRegistry;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.DebugRenderer;
import com.ultreon.craft.render.Hud;
import com.ultreon.craft.render.ItemRenderer;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.Notifications;
import com.ultreon.craft.render.gui.screens.*;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.BakedModelRegistry;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;
import com.ultreon.craft.render.world.WorldRenderer;
import com.ultreon.craft.resources.ResourceFileHandle;
import com.ultreon.craft.text.LanguageData;
import com.ultreon.craft.util.GG;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.Ray;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.datetime.v0.Duration;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
import com.ultreon.libs.resources.v0.ResourceManager;
import com.ultreon.libs.translations.v1.Language;
import com.ultreon.libs.translations.v1.LanguageManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;

public class UltreonCraft extends PollingExecutorService implements DeferredDisposable {
    public static final String NAMESPACE = "craft";
    public static final Logger LOGGER = GamePlatform.instance.getLogger("UltreonCraft");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    private static final int CULL_FACE = GL20.GL_FRONT;
    private final Duration bootTime;
    private final String allUnicode;
    private final GarbageCollector garbageCollector;
    private final GameEnvironment gameEnv;
    public FileHandle configDir;

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";
    @UnknownNullability
    private static SavedWorld savedWorld;
    public boolean forceUnicode = false;
    public ItemRenderer itemRenderer;
    public Notifications notifications = new Notifications(this);
    @SuppressWarnings("FieldMayBeFinal")
    private boolean booted;
    public static final int TPS = 20;
    public Font font;
    @UnknownNullability
    public BitmapFont unifont;
    public GameInput input;
    @Nullable public World world;
    @Nullable public WorldRenderer worldRenderer;
    @UnknownNullability
    @SuppressWarnings("GDXJavaStaticResource")
    private static UltreonCraft instance;
    @Nullable public Player player;
    private final SpriteBatch spriteBatch;
    public final ModelBatch modelBatch;
    public GameCamera camera;
    private final Environment env;
    private float timeUntilNextTick;
    public final PlayerInput playerInput = new PlayerInput(this);
    private final boolean isDevMode;
    @Nullable
    public Screen currentScreen;
    public GameSettings settings;
    ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final ResourceManager resourceManager;
    private final float guiScale = this.calculateGuiScale();

    public Hud hud;
    private int chunkRefresh;
    public boolean showDebugHud = true;

    // Public Flags
    public boolean renderWorld = false;

    // Startup time
    public static final long BOOT_TIMESTAMP = System.currentTimeMillis();

    // Texture Atlases
    @UnknownNullability
    public TextureAtlas blocksTextureAtlas;
    private final BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Nullable
    private Integer deferredWidth;
    @Nullable
    private Integer deferredHeight;
    private final Texture windowTex;
    private final DebugRenderer debugRenderer;
    private boolean closingWorld;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private boolean loading;
    private final Thread renderingThread;
    public HitResult cursor;
    private FrameBuffer worldFbo;

    public UltreonCraft(String[] args) throws Throwable {
        UltreonCraft.LOGGER.info("Booting game!");

        this.loading = true;

        UltreonCraft.instance = this;
        this.renderingThread = Thread.currentThread();

        Identifier.setDefaultNamespace(UltreonCraft.NAMESPACE);
        GamePlatform.instance.preInitImGui();

        var argList = Arrays.asList(args);
        this.isDevMode = argList.contains("--dev") && GamePlatform.instance.isDevelopmentEnvironment();

        if (GamePlatform.instance.isDevelopmentEnvironment())
            this.gameEnv = GameEnvironment.DEVELOPMENT;
        else if (Objects.equals(System.getProperty("ultracraft.environment", "normal"), "packaged"))
            this.gameEnv = GameEnvironment.PACKAGED;
        else
            this.gameEnv = GameEnvironment.NORMAL;

        if (this.isDevMode)
            UltreonCraft.LOGGER.info("Developer mode is enabled");

        Thread.setDefaultUncaughtExceptionHandler(UltreonCraft::uncaughtException);

        UltreonCraft.LOGGER.info("Data directory is at: " + GamePlatform.data(".").file().getCanonicalFile().getAbsolutePath());

        Gdx.app.setApplicationLogger(new LibGDXLogger());

        this.configDir = UltreonCraft.createDir("config/");
        this.garbageCollector = new GarbageCollector();

        UltreonCraft.createDir("screenshots/");
        UltreonCraft.createDir("game-crashes/");
        UltreonCraft.createDir("logs/");

        GamePlatform.instance.setupMods();

        this.settings = new GameSettings();
        this.settings.reload();
        this.settings.reloadLanguage();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        this.resourceManager = new ResourceManager("assets");
        UltreonCraft.LOGGER.info("Importing resources");
        this.resourceManager.importDeferredPackage(this.getClass());
        GamePlatform.instance.importModResources(this.resourceManager);

        UltreonCraft.LOGGER.info("Initializing game");
        this.textureManager = new TextureManager(this.resourceManager);
        this.spriteBatch = new SpriteBatch();

        var resource = this.resourceManager.getResource(UltreonCraft.id("texts/unicode.txt"));
        if (resource == null) throw new FileNotFoundException("Unicode resource not found!");
        this.allUnicode = new String(resource.loadOrGet(), StandardCharsets.UTF_16);

        UltreonCraft.LOGGER.info("Generating bitmap fonts");
        this.unifont = new BitmapFont(Gdx.files.internal("assets/craft/font/unifont/unifont.fnt"), true);

        var generator = new FreeTypeFontGenerator(new ResourceFileHandle(UltreonCraft.id("font/dogica/dogicapixel.ttf")));
        var fontParameter = new FreeTypeFontParameter();
        fontParameter.size = 8;
        fontParameter.characters = this.allUnicode;
        fontParameter.minFilter = Texture.TextureFilter.Nearest;
        fontParameter.magFilter = Texture.TextureFilter.Nearest;
        fontParameter.flip = true;
        fontParameter.mono = true;

        this.font = new Font(generator.generateFont(fontParameter));

        //**********************//
        // Setting up rendering //
        //**********************//
        UltreonCraft.LOGGER.info("Initializing rendering stuffs");
        var config = new DepthShader.Config();
        config.defaultCullFace = GL20.GL_FRONT;
        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
//        this.batch.getRenderContext().setCullFace(UltreonCraft.CULL_FACE);
        this.camera = new GameCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.near = 0.01f;
        this.camera.far = 2;
        this.input = this.createInput();
        Gdx.input.setInputProcessor(this.input);

        var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        var white = new TextureRegion(new Texture(pixmap));

        this.shapes = new ShapeDrawer(this.spriteBatch, white);

        UltreonCraft.LOGGER.info("Setting up world environment");
        this.env = new Environment();
        this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
        this.env.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.env.add(new DirectionalLight().set(.8f, .8f, .8f, .8f, 0, -.6f));
        this.env.add(new DirectionalLight().set(.8f, .8f, .8f, -.8f, 0, .6f));
        this.env.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, 0, -1, 0));
        this.env.add(new DirectionalLight().set(0.17f, .17f, .17f, 0, 1, 0));

        this.worldFbo = new FrameBuffer(Pixmap.Format.RGBA8888, this.getWidth(), this.getHeight(), true);

        UltreonCraft.LOGGER.info("Setting up HUD");
        this.hud = new Hud(this);

        UltreonCraft.LOGGER.info("Setting up Debug Renderer");
        this.debugRenderer = new DebugRenderer(this);

        //**************************//
        // Registering game content //
        //**************************//
        UltreonCraft.LOGGER.info("Loading languages");
        this.loadLanguages();

        UltreonCraft.LOGGER.info("Registering stuff");
        Registries.init();

        Blocks.nopInit();
        NoiseSettingsInit.nopInit();
        Entities.nopInit();
        Fonts.nopInit();
        Sounds.nopInit();

        for (var registry : Registry.getRegistries()) {
            RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
        }

        Registry.freeze();

        UltreonCraft.LOGGER.info("Registering models");
        this.registerModels();

        //********************************************//
        // Post-initialize game content               //
        // Such as model baking and texture stitching //
        //********************************************//
        UltreonCraft.LOGGER.info("Stitching textures");
        this.blocksTextureAtlas = BlockModelRegistry.stitch(this.textureManager);

        this.itemRenderer = new ItemRenderer(this, this.env);

        UltreonCraft.LOGGER.info("Initializing sounds");
        for (var sound : Registries.SOUNDS.values()) {
            sound.register();
        }

        UltreonCraft.LOGGER.info("Baking models");
        this.bakedBlockModels = BlockModelRegistry.bake(this.blocksTextureAtlas);

        if (this.deferredWidth != null && this.deferredHeight != null) {
            this.camera.viewportWidth = this.deferredWidth;
            this.camera.viewportHeight = this.deferredHeight;
            this.camera.update();
        }

        this.windowTex = this.textureManager.getTexture(UltreonCraft.id("textures/gui/window.png"));

        LifecycleEvents.GAME_LOADED.factory().onGameLoaded(this);

        this.loading = false;

        //*************//
        // Final stuff //
        //*************//
        UltreonCraft.LOGGER.info("Opening title screen");
        this.showScreen(new TitleScreen());

        UltreonCraft.savedWorld = new SavedWorld(GamePlatform.data("world"));

        GamePlatform.instance.setupImGui();

        this.booted = true;

        this.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - UltreonCraft.BOOT_TIMESTAMP);
        UltreonCraft.LOGGER.info("Game booted in " + this.bootTime + "ms");
    }

    @CanIgnoreReturnValue
    public static <T> T invokeAndWait(Callable<@NotNull T> func) {
        return UltreonCraft.instance.submit(func).join();
    }

    public static void invokeAndWait(Runnable func) {
        UltreonCraft.instance.submit(func).join();
    }

    @CanIgnoreReturnValue
    public static @NotNull CompletableFuture<Void> invoke(Runnable func) {
        return UltreonCraft.instance.submit(func);
    }

    @CanIgnoreReturnValue
    public static <T> @NotNull CompletableFuture<T> invoke(Callable<T> func) {
        return UltreonCraft.instance.submit(func);
    }

    public static FileHandle resource(Identifier id) {
        return new ResourceFileHandle(id);
    }

    private static void uncaughtException(Thread t, Throwable e) {
        UltreonCraft.LOGGER.error("Exception in thread \"" + t.getName() + "\":", e);
    }

    public static boolean isOnRenderingThread() {
        return Thread.currentThread().getId() == UltreonCraft.instance.renderingThread.getId();
    }

    @Override
    public <T extends Disposable> T deferDispose(T disposable) {
        UltreonCraft.instance.disposables.add(disposable);
        return disposable;
    }

    public Duration getBootTime() {
        return this.bootTime;
    }

    public void delayCrash(CrashLog crashLog) {
        Gdx.app.postRunnable(() -> {
            var finalCrash = new CrashLog("An error occurred", crashLog, new RuntimeException("Delayed crash"));
            UltreonCraft.crash(finalCrash);
        });
    }

    public static UltreonCraft get() {
        return UltreonCraft.instance;
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
            languages = UltreonCraft.GSON.fromJson(reader, LanguageData.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load languages register", e);
        }

        for (var language : languages) {
            this.registerLanguage(UltreonCraft.id(language));
        }

        LanguageRegistry.doRegistration(this::registerLanguage);
    }

    private void registerLanguage(Identifier id) {
        var s = id.path().split("_", 2);
        var locale = s.length == 1 ? new Locale(s[0]) : new Locale(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, this.resourceManager);
    }

    private GameInput createInput() {
        return GamePlatform.instance.isMobile() ? new MobileInput(this, this.camera) : new DesktopInput(this, this.camera);
    }

    private void registerModels() {
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, CubeModel.of(UltreonCraft.id("blocks/grass_top"), UltreonCraft.id("blocks/dirt"), UltreonCraft.id("blocks/grass_side")));
        BlockModelRegistry.registerDefault(Blocks.DIRT);
        BlockModelRegistry.registerDefault(Blocks.SAND);
        BlockModelRegistry.registerDefault(Blocks.WATER);
        BlockModelRegistry.registerDefault(Blocks.STONE);
    }

    private static FileHandle createDir(String dirName) {
        var directory = GamePlatform.data(dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            directory.delete();
            directory.mkdirs();
        }
        return directory;
    }

    public void pause() {
        if (this.currentScreen == null && this.world != null) {
            this.showScreen(new PauseScreen());
        }
    }

    public void resume() {
        if (this.currentScreen instanceof PauseScreen && this.world != null) {
            this.showScreen(null);
        }
    }

    @CanIgnoreReturnValue
    public boolean showScreen(@Nullable Screen open) {
        var cur = this.currentScreen;
        if (open == null && this.world == null) {
            open = new TitleScreen();
        }

        if (open == null) {
            if (cur == null) return false;

            var result = ScreenEvents.CLOSE.factory().onCloseScreen(this.currentScreen);
            if (result.isCanceled()) return false;

            UltreonCraft.LOGGER.debug("Closing screen: " + this.currentScreen.getClass());

            cur.hide();
            this.currentScreen = null;
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

            cur.hide();
            if (open != null) {
                UltreonCraft.LOGGER.debug("Changing screen to: " + open.getClass());
            } else {
                UltreonCraft.LOGGER.debug("Closing screen: " + cur.getClass());
            }
        } else {
            if (open != null) {
                Gdx.input.setCursorCatched(false);
                UltreonCraft.LOGGER.debug("Opening screen: " + open.getClass());
            } else {
                return false;
            }
        }

        this.currentScreen = open;
        if (this.currentScreen != null) {
            this.currentScreen.show();
        }

        return true;
    }

    public void render() {
        if (!this.booted) {
            return;
        }

        try {
            final var tickTime = 1f / UltreonCraft.TPS;

            this.pollAll();

            var deltaTime = Gdx.graphics.getDeltaTime();
            this.timeUntilNextTick -= deltaTime;
            if (this.timeUntilNextTick < 0) {
                this.timeUntilNextTick = tickTime + this.timeUntilNextTick;

                this.tick();
            }

            this.input.update();

            if (Gdx.graphics.getFrameId() == 2) {
                GamePlatform.instance.firstRender();
                Gdx.graphics.setTitle("UltraCraft v" + Metadata.INSTANCE.version);
            }

            var world = this.world;
            var worldRenderer = this.worldRenderer;

            Texture worldTexture = null;
            if (this.player != null) {
                if (this.currentScreen == null && !GamePlatform.instance.isShowingImGui()) {
                    this.player.rotate(-Gdx.input.getDeltaX(), -Gdx.input.getDeltaY());
                }

                this.camera.update(this.player);
                this.camera.far = (this.settings.renderDistance.get() - 1) * World.CHUNK_SIZE;
//                this.camera.far = 10000;

                var rotation = this.player != null ? this.player.getRotation() : new Vector2();
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();

                if (this.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {
//                    this.worldFbo.begin();

                    ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
                    Gdx.gl20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//                    this.shader.bind();

                    this.modelBatch.begin(this.camera);
                    this.modelBatch.getRenderContext().setCullFace(UltreonCraft.CULL_FACE);
                    this.modelBatch.getRenderContext().setDepthTest(GL_DEPTH_FUNC);
                    this.modelBatch.render(worldRenderer, this.env);
                    this.modelBatch.end();

//                    Gdx.gl20.glUseProgram(GL_NONE);
//                    Gdx.gl20.glFlush();

//                    this.worldFbo.end(0, 0, this.getWidth(), this.getHeight());
//                    worldTexture = this.worldFbo.getColorBufferTexture();
                }
            }

//            ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
            this.spriteBatch.begin();
//            if (worldTexture != null) {
//                this.spriteBatch.draw(worldTexture, 0, 0, this.getWidth(), this.getHeight());
//                this.spriteBatch.flush();
//                Gdx.gl20.glFlush();
//            }

            var screen = this.currentScreen;
            var renderer = new Renderer(this.shapes);

            renderer.pushMatrix();
            renderer.translate(this.getDrawOffset().x, this.getDrawOffset().y);
            renderer.scale(this.guiScale, this.guiScale);
            this.renderGame(renderer, screen, world, deltaTime);
            renderer.popMatrix();

            if (GamePlatform.instance.getPlatformType() == PlatformType.DESKTOP && this.isCustomBorderShown()) {
                renderer.pushMatrix();
                renderer.scale(2, 2);
                this.renderWindow(renderer, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
                renderer.popMatrix();
            }

            GamePlatform.instance.renderImGui(this);

            this.spriteBatch.end();
        } catch (Throwable t) {
            UltreonCraft.crash(t);
        }

        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
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
        UltreonCraft.LOGGER.error("Game crash triggered:", throwable);
        try {
            var crashLog = new CrashLog("An unexpected error occurred", throwable);
            UltreonCraft.crash(crashLog);
        } catch (Throwable t) {
            UltreonCraft.LOGGER.error(UltreonCraft.FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    public static void crash(CrashLog crashLog) {
        try {
            UltreonCraft.instance.fillGameInfo(crashLog);
            var crash = crashLog.createCrash();
            UltreonCraft.crash(crash);
        } catch (Throwable t) {
            UltreonCraft.LOGGER.error(UltreonCraft.FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void fillGameInfo(CrashLog crashLog) {
        if (this.world != null) {
            this.world.fillCrashInfo(crashLog);
        }

        var game = new CrashCategory("Game Details");
        game.add("Time until crash", Duration.ofMilliseconds(System.currentTimeMillis() - UltreonCraft.BOOT_TIMESTAMP).toSimpleString()); // Could be the game only crashes after a long time.
        game.add("Game booted", this.booted); // Could be that the game isn't booted yet.
        game.add("LibGDX Platform", GamePlatform.instance.getGdxPlatform().getDisplayName());
        game.add("Can Access Data", GamePlatform.instance.canAccessData());
        game.add("Supports Mods", GamePlatform.instance.isModsSupported());
        game.add("Supports Quit", GamePlatform.instance.supportsQuit());
        crashLog.addCategory(game);
    }

    private static void crash(ApplicationCrash crash) {
        try {
            crash.printCrash();

            var crashLog = crash.getCrashLog();
            GamePlatform.instance.handleCrash(crashLog);
            if (GamePlatform.instance.isDesktop()) Gdx.app.exit();
        } catch (Throwable t) {
            UltreonCraft.LOGGER.error(UltreonCraft.FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    public void tick() {
        var world = this.world;
        if (world != null) {
            WorldEvents.PRE_TICK.factory().onPreTick(world);
            world.tick();
            WorldEvents.POST_TICK.factory().onPostTick(world);

            if (this.player != null) {
                this.cursor = world.rayCast(new Ray(this.player.getPosition().add(0, this.player.getEyeHeight(), 0), this.player.getLookVector()));
            }
        }


        var player = this.player;
        if (player != null) {
            this.camera.update(player);

            if (world != null && this.chunkRefresh-- == 0) {
                this.chunkRefresh = 20;
                world.updateChunksForPlayerAsync(player);
            }
        }
        this.input.update();
    }

    public CompletableFuture<Void> respawnAsync() {
        assert this.world != null;
        if (this.player != null && this.world.getEntity(this.player.getId()) == this.player) {
            this.world.despawn(this.player);
        }

        var spawnPoint = this.world.getSpawnPoint();

        return this.world.updateChunksForPlayerAsync(spawnPoint.x, spawnPoint.z).thenAccept(unused -> {
            var spawnPointY = this.world.getSpawnPoint().y;

            this.player = Entities.PLAYER.create(this.world);
            this.player.setHealth(this.player.getMaxHeath());
            this.player.setPosition(spawnPoint.x + 0.5f, spawnPointY, spawnPoint.z + 0.5f);
            this.world.spawn(this.player);
        });
    }

    public void respawn() {
        assert this.world != null;
        if (this.player != null && this.world.getEntity(this.player.getId()) == this.player) {
            this.world.despawn(this.player);
        }

        var spawnPoint = this.world.getSpawnPoint();

        this.world.updateChunksForPlayer(spawnPoint.x, spawnPoint.z);

        var spawnPointY = this.world.getSpawnPoint().y;

        this.player = Entities.PLAYER.create(this.world);
        UltreonCraft.LOGGER.debug("Player created, setting health now.");
        this.player.setHealth(this.player.getMaxHeath());
        UltreonCraft.LOGGER.debug("Health set, setting position now.");
        this.player.setPosition(spawnPoint.x + 0.5f, spawnPointY, spawnPoint.z + 0.5f);
        UltreonCraft.LOGGER.debug("Position set, spawning in world now..");
        this.world.spawn(this.player);
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

        var cur = this.currentScreen;
        if (cur != null) {
            cur.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));
        }
    }

    public void dispose() {
        try {
            while (!this.futures.isEmpty()) {
                this.futures.removeIf(CompletableFuture::isDone);
            }

            this.scheduler.shutdownNow();
            this.garbageCollector.shutdown();

            if (this.world != null) this.world.dispose();

            if (this.blocksTextureAtlas != null) this.blocksTextureAtlas.dispose();

            GamePlatform.instance.dispose();

            this.modelBatch.dispose();
            this.spriteBatch.dispose();
            if (this.unifont != null) this.unifont.dispose();

            for (var font : Registries.FONTS.values()) {
                font.dispose();
            }

            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();

            LifecycleEvents.GAME_DISPOSED.factory().onGameDisposed();
        } catch (Throwable t) {
            UltreonCraft.crash(t);
        }
    }

    public boolean isDevMode() {
        return this.isDevMode;
    }

    public boolean isShowingImGui() {
        return GamePlatform.instance.isShowingImGui();
    }

    public void setShowingImGui(boolean value) {
        GamePlatform.instance.setShowingImGui(value);
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
        this.showScreen(new WorldLoadScreen(UltreonCraft.getSavedWorld()));
    }

    public static SavedWorld getSavedWorld() {
        return UltreonCraft.savedWorld;
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
        this.exitWorldAndThen(() -> {
            this.showScreen(new TitleScreen());
        });
    }

    public synchronized void exitWorldAndThen(Runnable runnable) {
        this.closingWorld = true;
        final var world = this.world;
        if (world == null) return;
        this.showScreen(new MessageScreen(Language.translate("Saving world...")));
        var worldRenderer = this.worldRenderer;
        if (worldRenderer != null)
            worldRenderer.dispose();

        CompletableFuture.runAsync(() -> {
            try {
                world.dispose();
                this.worldRenderer = null;
                this.world = null;
                System.gc();
                UltreonCraft.invokeAndWait(runnable);
                this.closingWorld = false;
            } catch (Exception e) {
                UltreonCraft.crash(e);
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
                UltreonCraft.LOGGER.warn("Error occurred in task:", e);
            }
        });
    }

    @Deprecated(forRemoval = true)
    public void runLater(Task task) {
        Gdx.app.postRunnable(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltreonCraft.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        });
    }

    public ScheduledFuture<?> schedule(Task task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltreonCraft.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Task task, long time, TimeUnit unit) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltreonCraft.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        }, time, unit);
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public void playSound(SoundEvent event) {
        event.getSound().play();
    }

    public boolean closeRequested() {
        var eventResult = LifecycleEvents.WINDOW_CLOSED.factory().onWindowClose();
        if (!eventResult.isCanceled()) {
            if (this.world != null) {
                this.exitWorldAndThen(() -> Gdx.app.exit());
                return false;
            }
        }
        return !eventResult.isCanceled();
    }

    public void filesDropped(String[] files) {
        var currentScreen = this.currentScreen;
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

    private float calculateGuiScale() {
        return switch (GamePlatform.instance.getPlatformType()) {
            case MOBILE -> 4.0F;
            case DESKTOP, WEB -> 2.0F;
            default -> throw new IllegalArgumentException();
        };
    }

    public boolean isPlaying() {
        return this.world != null && this.currentScreen == null;
    }

    public static FileHandle getConfigDir() {
        return UltreonCraft.instance.configDir;
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
        if (UltreonCraft.instance == null) return GameEnvironment.UNKNOWN;
        return UltreonCraft.instance.gameEnv;
    }

    private static class LibGDXLogger implements ApplicationLogger {
        private final Logger LOGGER = GamePlatform.instance.getLogger("LibGDX");

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
