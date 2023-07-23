package com.ultreon.craft;

import com.badlogic.gdx.ApplicationLogger;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.GridPoint2;
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
import com.ultreon.craft.render.*;
import com.ultreon.craft.render.gui.LoadingOverlay;
import com.ultreon.craft.render.gui.screens.*;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.BakedModelRegistry;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;
import com.ultreon.craft.text.LanguageData;
import com.ultreon.craft.util.GG;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.events.v1.EventResult;
import com.ultreon.libs.events.v1.ValueEventResult;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
import com.ultreon.libs.resources.v0.Resource;
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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.GL_DEPTH_FUNC;
import static com.badlogic.gdx.math.MathUtils.ceil;

public class UltreonCraft implements DeferredDisposable {
    public static final String NAMESPACE = "craft";
    public static final Logger LOGGER = GamePlatform.instance.getLogger("UltreonCraft");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    private static final int CULL_FACE = GL20.GL_FRONT;
    private static final float FROM_ZOOM = 2.0f;
    private static final float TO_ZOOM = 1.3f;
    private static final float DURATION = 6000f;
    private static final Thread MAIN_THREAD = Thread.currentThread();
    private final Sound logoRevealSound;
    private final Texture ultreonBgTex;
    private final Texture ultreonLogoTex;
    private final Texture libGDXLogoTex;
    private final Resizer resizer;
    private boolean showUltreonSplash;
    private boolean showLibGDXSplash = true;
    private long ultreonSplashTime;
    private long libGDXSplashTime;
    private Instant bootTime;
    private String allUnicode;
    public FileHandle configDir;

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";
    @UnknownNullability
    private static SavedWorld savedWorld;
    public boolean forceUnicode = false;
    public ItemRenderer itemRenderer;
    @SuppressWarnings("FieldMayBeFinal")
    private boolean booted;
    public static final int TPS = 20;
    public Font font;
    public BitmapFont unifont;
    public GameInput input;
    @Nullable public World world;
    @Nullable public WorldRenderer worldRenderer;
    @UnknownNullability
    private static UltreonCraft instance;
    @Nullable public Player player;
    @NotNull
    private final SpriteBatch spriteBatch;
    @NotNull
    private final ModelBatch batch;
    GameCamera camera;
    private Environment env;
    private float timeUntilNextTick;
    public final PlayerInput playerInput = new PlayerInput(this);
    private boolean isDevMode;
    @Nullable
    public Screen currentScreen;
    public GameSettings settings;
    ShapeDrawer shapes;
    private TextureManager textureManager;
    private ResourceManager resourceManager;
    private final float guiScale = this.calculateGuiScale();

    private final List<Task<?>> tasks = new CopyOnWriteArrayList<>();
    public Hud hud;
    private int chunkRefresh;
    public boolean showDebugHud = true;

    // Public Flags
    public boolean renderWorld = false;

    // Startup time
    public static final long BOOT_TIMESTAMP = System.currentTimeMillis();

    // Texture Atlases
    public TextureAtlas blocksTextureAtlas;
    private BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private final List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @Nullable
    private Integer deferredWidth;
    @Nullable
    private Integer deferredHeight;
    private Texture windowTex;
    private DebugRenderer debugRenderer;
    private boolean closingWorld;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private final String[] argv;
    private LoadingOverlay loadingOverlay;

    public UltreonCraft(String[] argv) throws Throwable {
        this.argv = argv;

        UltreonCraft.instance = this;

        LOGGER.info("Booting game!");

        this.camera = new GameCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.near = 0.01f;
        this.camera.far = 1000;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1F, 1F, 1F, 1F);
        pixmap.drawPixel(0, 0);
        TextureRegion white = new TextureRegion(new Texture(pixmap));

        this.spriteBatch = new SpriteBatch();
        this.shapes = new ShapeDrawer(this.spriteBatch, white);

        DefaultShader.Config config = new DefaultShader.Config();
        config.defaultCullFace = GL20.GL_FRONT;
        this.batch = new ModelBatch(new DefaultShaderProvider(config));

        this.ultreonBgTex = new Texture("assets/craft/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/craft/logo.png");
        this.libGDXLogoTex = new Texture("assets/craft/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/craft/sounds/logo_reveal.mp3"));

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());
    }

    public static boolean isOnMainThread() {
        return Thread.currentThread() == MAIN_THREAD;
    }

    private void load() throws Throwable {
        Identifier.setDefaultNamespace(NAMESPACE);
        GamePlatform.instance.preInitImGui();

        List<String> argList = Arrays.asList(this.argv);
        this.isDevMode = argList.contains("--dev");

        if (this.isDevMode) {
            LOGGER.debug("Developer mode is enabled");
        }

        Thread.setDefaultUncaughtExceptionHandler(UltreonCraft::uncaughtException);

        LOGGER.info("Data directory is at: " + GamePlatform.data(".").file().getAbsolutePath());

        this.loadingOverlay.setProgress(0.075F);

        Gdx.app.setApplicationLogger(new LibGDXLogger());

        this.configDir = createDir("config/");

        createDir("screenshots/");
        createDir("game-crashes/");
        createDir("logs/");

        GamePlatform.instance.setupMods();

        this.settings = new GameSettings();
        this.settings.reload();
        this.settings.reloadLanguage();

        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        this.loadingOverlay.setProgress(0.15F);

        LOGGER.info("Initializing game");
        this.textureManager = new TextureManager();

        this.resourceManager = new ResourceManager("assets");
        LOGGER.info("Importing resources");
        this.resourceManager.importDeferredPackage(this.getClass());
        GamePlatform.instance.importModResources(this.resourceManager);

        this.loadingOverlay.setProgress(0.35F);

        Resource resource = this.resourceManager.getResource(id("texts/unicode.txt"));
        if (resource == null) throw new FileNotFoundException("Unicode resource not found!");
        this.allUnicode = new String(resource.loadOrGet(), StandardCharsets.UTF_16);

        LOGGER.info("Generating bitmap fonts");
        this.unifont = this.runLater(new Task<>(id("main/unifont"), () -> new BitmapFont(Gdx.files.internal("assets/craft/font/unifont/unifont.fnt")))).join();

        this.font = new Font(this.runLater(new Task<>(id("main/game_font"), () -> new BitmapFont(Gdx.files.internal("assets/craft/font/dogica/dogicapixel.fnt")))).join());

        this.loadingOverlay.setProgress(0.7F);

        //**********************//
        // Setting up rendering //
        //**********************//
        LOGGER.info("Initializing rendering stuffs");
        this.input = this.createInput();
        Gdx.input.setInputProcessor(this.input);

        LOGGER.info("Setting up world environment");
        this.env = new Environment();
        this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
        this.env.add(new DirectionalLight().set(.8f, .8f, .8f, .8f, 0, -.6f));
        this.env.add(new DirectionalLight().set(.8f, .8f, .8f, -.8f, 0, .6f));
        this.env.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, 0, -1, 0));
        this.env.add(new DirectionalLight().set(0.17f, .17f, .17f, 0, 1, 0));

        LOGGER.info("Setting up HUD");
        this.hud = this.getAndWait(id("main/load/create_hud"), () -> new Hud(this));

        LOGGER.info("Setting up Debug Renderer");
        this.debugRenderer = new DebugRenderer(this);

        this.loadingOverlay.setProgress(0.83F);

        //**************************//
        // Registering game content //
        //**************************//
        LOGGER.info("Loading languages");
        this.loadLanguages();

        LOGGER.info("Registering stuff");
        Registries.init();

        Blocks.nopInit();
        NoiseSettingsInit.nopInit();
        Entities.nopInit();
        Fonts.nopInit();
        Sounds.nopInit();

        for (Registry<?> registry : Registry.getRegistries()) {
            RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
        }
        Registry.freeze();

        LOGGER.info("Registering models");
        this.registerModels();

        this.loadingOverlay.setProgress(0.95F);

        //********************************************//
        // Post-initialize game content               //
        // Such as model baking and texture stitching //
        //********************************************//
        LOGGER.info("Stitching textures");
        this.blocksTextureAtlas = BlockModelRegistry.stitch(this.textureManager);

        this.loadingOverlay.setProgress(0.98F);

        this.itemRenderer = this.runLater(new Task<>(id("main/item_renderer"), () -> new ItemRenderer(this, this.env))).join();

        LOGGER.info("Initializing sounds");
        for (SoundEvent sound : Registries.SOUNDS.values()) {
            sound.register();
        }

        LOGGER.info("Baking models");
        this.bakedBlockModels = BlockModelRegistry.bake(this.blocksTextureAtlas);

        if (this.deferredWidth != null && this.deferredHeight != null) {
            this.camera.viewportWidth = this.deferredWidth;
            this.camera.viewportHeight = this.deferredHeight;
            this.camera.update();
        }

        this.windowTex = this.textureManager.getTexture(id("textures/gui/window.png"));

        this.loadingOverlay.setProgress(0.99F);

        LifecycleEvents.GAME_LOADED.factory().onGameLoaded(this);

        //*************//
        // Final stuff //
        //*************//
        LOGGER.info("Opening title screen");

        savedWorld = new SavedWorld(GamePlatform.data("world"));

        GamePlatform.instance.setupImGui();

        this.booted = true;

        this.loadingOverlay.setProgress(1.0F);

        this.bootTime = Instant.ofEpochMilli(System.currentTimeMillis() - BOOT_TIMESTAMP);
        LOGGER.info("Game booted in " + this.bootTime + "ms");

        this.runLater(new Task<>(id("main/show_title_screen"), () -> this.showScreen(new TitleScreen())));
        this.loadingOverlay = null;
    }

    private static void uncaughtException(Thread t, Throwable e) {
        LOGGER.error("Exception in thread \"" + t.getName() + "\":", e);
    }

    @Override
    public void deferDispose(Disposable disposable) {
        instance.disposables.add(disposable);
    }

    public Instant getBootTime() {
        return this.bootTime;
    }

    public void delayCrash(CrashLog crashLog) {
        Gdx.app.postRunnable(() -> {
            CrashLog finalCrash = new CrashLog("An error occurred", crashLog, new RuntimeException("Delayed crash"));
            crash(finalCrash);
        });
    }

    public static UltreonCraft get() {
        return instance;
    }

    public static Identifier id(String path) {
        return new Identifier(path);
    }

    public static GG ggBro() {
        return new GG();
    }

    private void loadLanguages() {
        FileHandle internal = Gdx.files.internal("assets/craft/languages.json");
        List<String> languages;
        try (Reader reader = internal.reader()) {
            languages = GSON.fromJson(reader, LanguageData.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load languages register", e);
        }

        for (String language : languages) {
            this.registerLanguage(id(language));
        }

        LanguageRegistry.doRegistration(this::registerLanguage);
    }

    private void registerLanguage(Identifier id) {
        String[] s = id.path().split("_", 2);
        Locale locale = s.length == 1 ? new Locale(s[0]) : new Locale(s[0], s[1]);
        LanguageManager.INSTANCE.register(locale, id);
        LanguageManager.INSTANCE.load(locale, id, this.resourceManager);
    }

    private GameInput createInput() {
        return GamePlatform.instance.isMobile() ? new MobileInput(this, this.camera) : new DesktopInput(this, this.camera);
    }

    private void registerModels() {
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, CubeModel.of(id("blocks/grass_top"), id("blocks/dirt"), id("blocks/grass_side")));
        BlockModelRegistry.registerDefault(Blocks.DIRT);
        BlockModelRegistry.registerDefault(Blocks.SAND);
        BlockModelRegistry.registerDefault(Blocks.WATER);
        BlockModelRegistry.registerDefault(Blocks.STONE);
    }

    private static FileHandle createDir(String dirName) {
        FileHandle directory = GamePlatform.data(dirName);
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
        Screen cur = this.currentScreen;
        if (open == null && this.world == null) {
            open = new TitleScreen();
        }

        if (open == null) {
            if (cur == null) return false;

            EventResult result = ScreenEvents.CLOSE.factory().onCloseScreen(this.currentScreen);
            if (result.isCanceled()) return false;

            LOGGER.debug("Closing screen: " + this.currentScreen.getClass());

            cur.hide();
            this.currentScreen = null;
            Gdx.input.setCursorCatched(true);

            return true;
        }
        ValueEventResult<Screen> openResult = ScreenEvents.OPEN.factory().onOpenScreen(open);
        if (openResult.isCanceled()) {
            return false;
        }

        if (openResult.isInterrupted()) {
            open = openResult.getValue();
        }

        if (cur != null) {
            EventResult closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(cur);
            if (closeResult.isCanceled()) return false;

            cur.hide();
            if (open != null) {
                LOGGER.debug("Changing screen to: " + open.getClass());
            } else {
                LOGGER.debug("Closing screen: " + cur.getClass());
            }
        } else {
            if (open != null) {
                Gdx.input.setCursorCatched(false);
                LOGGER.debug("Opening screen: " + open.getClass());
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

    //from https://www.java2s.com
    public static double interpolate(double a, double b, double d) {
        return a + (b - a) * d;
    }

    public void render() {
        try {
            if (Gdx.graphics.getFrameId() == 2) {
                GamePlatform.instance.firstRender();
                Gdx.graphics.setTitle("UltraCraft v" + Metadata.INSTANCE.version);
            }

            final float tickTime = 1f / TPS;

            float deltaTime = Gdx.graphics.getDeltaTime();
            this.timeUntilNextTick -= deltaTime;
            if (this.timeUntilNextTick < 0) {
                this.timeUntilNextTick = tickTime + this.timeUntilNextTick;

                this.tick();
            }

            this.tasks.forEach(task -> {
                try {
                    task.run();
                } catch (Throwable e) {
                    LOGGER.warn("Error occurred in task " + task.id() + ":", e);
                }
                this.tasks.remove(task);
            });

            if (this.showLibGDXSplash) {
                if (this.libGDXSplashTime == 0L) {
                    this.libGDXSplashTime = System.currentTimeMillis();
                }

                ScreenUtils.clear(1, 1, 1, 1, true);
                Renderer renderer = new Renderer(this.shapes);

                this.spriteBatch.begin();
                int size = Math.min(this.getWidth(), this.getHeight()) / 2;
                renderer.texture(this.libGDXLogoTex, (float) this.getWidth() / 2 - (float) size / 2, (float) this.getHeight() / 2 - (float) size / 2, size, size);
                this.spriteBatch.end();

                if (System.currentTimeMillis() - this.libGDXSplashTime > DURATION) {
                    this.showLibGDXSplash = false;
                    this.showUltreonSplash = true;
                }
                return;
            }

            if (this.showUltreonSplash) {
                if (this.ultreonSplashTime == 0L) {
                    this.ultreonSplashTime = System.currentTimeMillis();

                    this.logoRevealSound.play();
                }

                ScreenUtils.clear(0, 0, 0, 1, true);
                Renderer renderer = new Renderer(this.shapes);

                final long timeDiff = System.currentTimeMillis() - this.ultreonSplashTime;
                float zoom = (float) interpolate(FROM_ZOOM, TO_ZOOM, Mth.clamp(timeDiff / DURATION, 0f, 1f));
                Vec2f thumbnail = this.resizer.thumbnail(this.getWidth() * zoom, this.getHeight() * zoom);

                float drawWidth = thumbnail.x;
                float drawHeight = thumbnail.y;

                float drawX = (this.getWidth() - drawWidth) / 2;
                float drawY = (this.getHeight() - drawHeight) / 2;

                this.spriteBatch.begin();
                renderer.texture(this.ultreonBgTex, 0, 0, this.getWidth(), this.getHeight(), 0, 0, 1024, 1024, 1024, 1024);
                renderer.texture(this.ultreonLogoTex, (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, 1920, 1080, 1920, 1080);
                this.spriteBatch.end();

                if (System.currentTimeMillis() - this.ultreonSplashTime > DURATION) {
                    this.showUltreonSplash = false;

                    this.loadingOverlay = new LoadingOverlay(this.ultreonBgTex);

                    CompletableFuture.runAsync(() -> {
                        try {
                            this.load();
                        } catch (Throwable t) {
                            crash(t);
                        }
                    });
                }

                return;
            }

            if (this.input != null) {
                this.input.update();
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

            World world = this.world;
            WorldRenderer worldRenderer = this.worldRenderer;

            if (this.renderWorld && world != null && worldRenderer != null) {
                ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
                this.batch.begin(this.camera);
                this.batch.getRenderContext().setCullFace(CULL_FACE);
                this.batch.getRenderContext().setDepthTest(GL_DEPTH_FUNC);
                this.batch.render(worldRenderer, this.env);
                this.batch.end();
            }

            this.spriteBatch.begin();

            Screen screen = this.currentScreen;
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
            crash(t);
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
            screen.render(renderer, (int) ((Gdx.input.getX() - this.getDrawOffset().x) / this.getGuiScale()), (int) ((this.getHeight() - Gdx.input.getY() + this.getDrawOffset().y) / this.getGuiScale()), deltaTime);
        }
    }

    public static void crash(Throwable throwable) {
        throwable.printStackTrace();
        try {
            CrashLog crashLog = new CrashLog("An error occurred", throwable);
            crash(crashLog);
        } catch (Throwable t) {
            LOGGER.error(FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    public static void crash(CrashLog crashLog) {
        try {
            UltreonCraft.instance.fillGameInfo(crashLog);
            ApplicationCrash crash = crashLog.createCrash();
            crash(crash);
        } catch (Throwable t) {
            LOGGER.error(FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    private void fillGameInfo(CrashLog crashLog) {
        if (this.world != null) {
            this.world.fillCrashInfo(crashLog);
        }

        CrashCategory game = new CrashCategory("Game Details");
        game.add("Time until crash", Duration.ofMillis(System.currentTimeMillis() - BOOT_TIMESTAMP).toString()); // Could be the game only crashes after a long time.
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

            CrashLog crashLog = crash.getCrashLog();
            GamePlatform.instance.handleCrash(crashLog);
            if (GamePlatform.instance.isDesktop()) Gdx.app.exit();
        } catch (Throwable t) {
            LOGGER.error(FATAL_ERROR_MSG, t);
            Gdx.app.exit();
        }
    }

    public void tick() {
        World world = this.world;
        if (world != null) {
            WorldEvents.PRE_TICK.factory().onPreTick(world);
            world.tick();
            WorldEvents.POST_TICK.factory().onPostTick(world);
        }

        Player player = this.player;
        if (player != null) {
            this.camera.update(player);

            if (world != null && this.chunkRefresh-- == 0) {
                this.chunkRefresh = 20;
                world.updateChunksForPlayerAsync(player);
            }
        }
    }

    public CompletableFuture<Void> respawnAsync() {
        assert this.world != null;
        if (this.player != null && this.world.getEntity(this.player.getId()) == this.player) {
            this.world.despawn(this.player);
        }

        Vec3i spawnPoint = this.world.getSpawnPoint();

        return this.world.updateChunksForPlayerAsync(spawnPoint.x, spawnPoint.z).thenAccept(unused -> {
            this.player = Entities.PLAYER.create(this.world);
            this.player.setHealth(this.player.getMaxHeath());
            this.player.setPosition(spawnPoint.x + 0.5f, spawnPoint.y, spawnPoint.z + 0.5f);
            this.world.spawn(this.player);
        });
    }

    public void respawn() {
        assert this.world != null;
        if (this.player != null && this.world.getEntity(this.player.getId()) == this.player) {
            this.world.despawn(this.player);
        }

        Vec3i spawnPoint = this.world.getSpawnPoint();

        this.world.updateChunksForPlayer(spawnPoint.x, spawnPoint.z);
        this.player = Entities.PLAYER.create(this.world);
        LOGGER.debug("Player created, setting health now.");
        this.player.setHealth(this.player.getMaxHeath());
        LOGGER.debug("Health set, setting position now.");
        this.player.setPosition(spawnPoint.x + 0.5f, spawnPoint.y, spawnPoint.z + 0.5f);
        LOGGER.debug("Position set, spawning in world now..");
        this.world.spawn(this.player);
    }

    public void resize(int width, int height) {
        this.spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        this.deferredWidth = width;
        this.deferredHeight = height;

        if (this.camera != null) {
            this.camera.viewportWidth = width;
            this.camera.viewportHeight = height;
            this.camera.update();
        }

        Screen cur = this.currentScreen;
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

            if (this.world != null) {
                this.world.dispose();
            }

            this.blocksTextureAtlas.dispose();

            GamePlatform.instance.dispose();

            this.batch.dispose();
            this.spriteBatch.dispose();
            this.unifont.dispose();

            for (Font font : Registries.FONTS.values()) {
                font.dispose();
            }

            this.disposables.forEach(Disposable::dispose);
            this.disposables.clear();

            LifecycleEvents.GAME_DISPOSED.factory().onGameDisposed();
        } catch (Throwable t) {
            t.printStackTrace();
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
        this.showScreen(new WorldLoadScreen(getSavedWorld()));
    }

    public static SavedWorld getSavedWorld() {
        return savedWorld;
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
        final World world = this.world;
        if (world == null) return;
        this.showScreen(new MessageScreen(Language.translate("Saving world...")));
        this.worldRenderer = null;
        this.world = null;
        CompletableFuture.runAsync(() -> {
            world.dispose();
            System.gc();
            this.runLater(new Task<Void>(id("post_world_exit"), runnable));
            this.closingWorld = false;
        });
    }

    public boolean isClosingWorld() {
        return this.closingWorld;
    }

    /**
     * @deprecated use {@link #runLater(Task)} instead.
     */
    @Deprecated
    public void runLater(Runnable task) {
        Gdx.app.postRunnable(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Error occurred in task:", e);
            }
        });
    }

    public <T> CompletableFuture<T> runLater(Task<T> task) {
        CompletableFuture<T> future = task.future = new CompletableFuture<>();
        this.tasks.add(task);
        return future;
    }

    public void runAndWait(Identifier id, Runnable task) {
        this.runLater(new Task<>(id, task));
    }

    public <T> T getAndWait(Identifier id, Supplier<T> task) {
        return this.runLater(new Task<>(id, task)).join();
    }

    public ScheduledFuture<?> schedule(Task<?> task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Task<?> task, long time, TimeUnit unit) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Error occurred in task " + task.id() + ":", e);
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
        EventResult eventResult = LifecycleEvents.WINDOW_CLOSED.factory().onWindowClose();
        if (!eventResult.isCanceled()) {
            if (this.world != null) {
                this.exitWorldAndThen(() -> Gdx.app.exit());
                return false;
            }
        }
        return !eventResult.isCanceled();
    }

    public void filesDropped(String[] files) {
        Screen currentScreen = this.currentScreen;
        List<FileHandle> handles = Arrays.stream(files).map(FileHandle::new).collect(Collectors.toList());

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
        switch (GamePlatform.instance.getPlatformType()) {
            case MOBILE:
                return 4.0F;
            case DESKTOP:
            case WEB:
                return 2.0F;
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean isPlaying() {
        return this.world != null && this.currentScreen == null;
    }

    public static FileHandle getConfigDir() {
        return instance.configDir;
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

    public float getBreakProgress() {
        return 1.0F;
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
