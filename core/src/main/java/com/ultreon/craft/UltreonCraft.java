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
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
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
import com.ultreon.craft.item.BlockItem;
import com.ultreon.craft.item.Item;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.item.tool.ToolItem;
import com.ultreon.craft.platform.PlatformType;
import com.ultreon.craft.registry.LanguageRegistry;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.*;
import com.ultreon.craft.render.gui.LoadingOverlay;
import com.ultreon.craft.render.gui.Notifications;
import com.ultreon.craft.render.gui.screens.*;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.BakedModelRegistry;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;
import com.ultreon.craft.render.texture.atlas.TextureStitcher;
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
import com.ultreon.libs.commons.v0.Mth;
import com.ultreon.libs.commons.v0.vector.Vec2f;
import com.ultreon.libs.commons.v0.vector.Vec3i;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.datetime.v0.Duration;
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
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.math.MathUtils.ceil;

public class UltreonCraft extends PollingExecutorService implements DeferredDisposable {
    public static final String NAMESPACE = "craft";
    public static final Logger LOGGER = GamePlatform.instance.getLogger("UltreonCraft");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    private static final int CULL_FACE = GL20.GL_FRONT;
    public static final float FROM_ZOOM = 2.0f;
    public static final float TO_ZOOM = 1.3f;
    private static final float DURATION = 6000f;
    private static final Thread MAIN_THREAD = Thread.currentThread();
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
    @NotNull
    private final SpriteBatch spriteBatch;
    public final ModelBatch modelBatch;
    public final GameCamera camera;
    private Environment env;
    private float timeUntilNextTick;
    public final PlayerInput playerInput = new PlayerInput(this);
    private boolean isDevMode;
    @Nullable
    public Screen currentScreen;
    public GameSettings settings;
    final ShapeDrawer shapes;
    private final TextureManager textureManager;
    private final ResourceManager resourceManager;
    private final float guiScale = this.calculateGuiScale();

    public Hud hud;
    private int chunkRefresh;
    @Nullable
    public HitResult hitResult;
    @Nullable
	private Vec3i breaking;
    @Nullable
	private Block breakingBlock;
	public boolean showDebugHud = true;

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
    private DebugRenderer debugRenderer;
    private boolean closingWorld;
    private int oldSelected;
    private final List<Disposable> disposables = new CopyOnWriteArrayList<>();
    private boolean loading;
    private final Thread renderingThread;
    public HitResult cursor;
    private LoadingOverlay loadingOverlay;
    private Object argv;

    public UltreonCraft(String[] argv) throws Throwable {
        UltreonCraft.LOGGER.info("Booting game!");
        UltreonCraft.instance = this;

        this.renderingThread =  Thread.currentThread();

        this.loading = true;

        GamePlatform.instance.preInitImGui();

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
        config.defaultCullFace = GL20.GL_FRONT;
        this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
        this.modelBatch.getRenderContext().setCullFace(UltreonCraft.CULL_FACE);

        this.ultreonBgTex = new Texture("assets/craft/textures/gui/loading_overlay_bg.png");
        this.ultreonLogoTex = new Texture("assets/craft/logo.png");
        this.libGDXLogoTex = new Texture("assets/craft/libgdx_logo.png");
        this.logoRevealSound = Gdx.audio.newSound(Gdx.files.internal("assets/craft/sounds/logo_reveal.mp3"));

        this.resizer = new Resizer(this.ultreonLogoTex.getWidth(), this.ultreonLogoTex.getHeight());
    }

    public static boolean isOnMainThread() {
        return Thread.currentThread() == UltreonCraft.MAIN_THREAD;
    }

    private void load() throws Throwable {
        Identifier.setDefaultNamespace(UltreonCraft.NAMESPACE);

        var argList = Arrays.asList(this.argv);
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

        UltreonCraft.LOGGER.info("Data directory is at: " + GamePlatform.data(".").file().getAbsolutePath());

        this.loadingOverlay.setProgress(0.075F);

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

        this.loadingOverlay.setProgress(0.15F);

        UltreonCraft.LOGGER.info("Importing resources");
        this.resourceManager.importDeferredPackage(this.getClass());
        GamePlatform.instance.importModResources(this.resourceManager);

        this.loadingOverlay.setProgress(0.35F);

        UltreonCraft.LOGGER.info("Generating bitmap fonts");
        var resource = this.resourceManager.getResource(UltreonCraft.id("texts/unicode.txt"));
        if (resource == null) throw new FileNotFoundException("Unicode resource not found!");
        this.allUnicode = new String(resource.loadOrGet(), StandardCharsets.UTF_16);

        this.unifont = UltreonCraft.invokeAndWait(() -> new BitmapFont(Gdx.files.internal("assets/craft/font/unifont/unifont.fnt"), true));
        this.font = new Font(UltreonCraft.invokeAndWait(() -> new BitmapFont(Gdx.files.internal("assets/craft/font/dogica/dogicapixel.fnt"), true)));

        this.loadingOverlay.setProgress(0.7F);

        //**********************//
        // Setting up rendering //
        //**********************//
        UltreonCraft.LOGGER.info("Initializing rendering stuffs");
        UltreonCraft.invokeAndWait(() -> {
                    this.input = this.createInput();
        });
        Gdx.input.setInputProcessor(this.input);

        UltreonCraft.LOGGER.info("Setting up world environment");
        this.env = new Environment();
        this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
        this.env.set(new ColorAttribute(ColorAttribute.Fog, 0.6F, 0.7F, 1.0F, 1.0F));
        this.env.add(new DirectionalLight().set(.8f, .8f, .8f, .8f, 0, -.6f));
        this.env.add(new DirectionalLight().set(.8f, .8f, .8f, -.8f, 0, .6f));
        this.env.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, 0, -1, 0));
        this.env.add(new DirectionalLight().set(0.17f, .17f, .17f, 0, 1, 0));

        UltreonCraft.LOGGER.info("Setting up HUD");
        this.hud = this.getAndWait(UltreonCraft.id("main/load/create_hud"), () -> new Hud(this));

        UltreonCraft.LOGGER.info("Setting up Debug Renderer");
        this.debugRenderer = new DebugRenderer(this);

        this.loadingOverlay.setProgress(0.83F);

        //**************************//
        // Registering game content //
        //**************************//
        UltreonCraft.LOGGER.info("Loading languages");
        this.loadLanguages();

        UltreonCraft.LOGGER.info("Registering stuff");
        Registries.init();

        Blocks.nopInit();
        Items.nopInit();
        NoiseSettingsInit.nopInit();
        Entities.nopInit();
        Fonts.nopInit();
        Sounds.nopInit();
        UltreonCraft.invokeAndWait(Shaders::nopInit);

        for (var registry : Registry.getRegistries()) {
            RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
        }

        Registry.freeze();

        UltreonCraft.LOGGER.info("Registering models");
        this.registerModels();

        this.loadingOverlay.setProgress(0.95F);

        //********************************************//
        // Post-initialize game content               //
        // Such as model baking and texture stitching //
        //********************************************//
        UltreonCraft.LOGGER.info("Stitching textures");
        this.stitchTextures();

        this.loadingOverlay.setProgress(0.98F);

        this.itemRenderer = UltreonCraft.invokeAndWait(() -> new ItemRenderer(this, this.env));

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

        this.loadingOverlay.setProgress(0.99F);

        LifecycleEvents.GAME_LOADED.factory().onGameLoaded(this);

        this.loading = false;

        //*************//
        // Final stuff //
        //*************//
        UltreonCraft.LOGGER.info("Opening title screen");

        UltreonCraft.savedWorld = new SavedWorld(GamePlatform.data("world"));

        GamePlatform.instance.setupImGui();

        this.booted = true;

        this.loadingOverlay.setProgress(1.0F);

        this.bootTime = Duration.ofMilliseconds(System.currentTimeMillis() - UltreonCraft.BOOT_TIMESTAMP);
        UltreonCraft.LOGGER.info("Game booted in " + this.bootTime + "ms");

        this.runLater(new Task<>(UltreonCraft.id("main/show_title_screen"), () -> this.showScreen(new TitleScreen())));
        this.loadingOverlay = null;
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

    public static String strId(String path) {
        return UltreonCraft.id(path).toString();
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
		BlockModelRegistry.register(Blocks.GRASS_BLOCK, CubeModel.of(UltreonCraft.id("blocks/grass_top"), UltreonCraft.id("blocks/dirt"), UltreonCraft.id("blocks/grass_side")));
		BlockModelRegistry.registerDefault(Blocks.DIRT);
		BlockModelRegistry.registerDefault(Blocks.SAND);
		BlockModelRegistry.registerDefault(Blocks.WATER);
		BlockModelRegistry.registerDefault(Blocks.STONE);
		BlockModelRegistry.registerDefault(Blocks.COBBLESTONE);
	}

    private GameInput createInput() {
        return GamePlatform.instance.isMobile() ? new MobileInput(this, this.camera) : new DesktopInput(this, this.camera);
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

            final var tickTime = 1f / UltreonCraft.TPS;

            this.pollAll();

            var deltaTime = Gdx.graphics.getDeltaTime();
            this.timeUntilNextTick -= deltaTime;
            if (this.timeUntilNextTick < 0) {
                this.timeUntilNextTick = tickTime + this.timeUntilNextTick;

                this.tick();
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

                if (System.currentTimeMillis() - this.libGDXSplashTime > UltreonCraft.DURATION) {
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
                float zoom = (float) UltreonCraft.interpolate(UltreonCraft.FROM_ZOOM, UltreonCraft.TO_ZOOM, Mth.clamp(timeDiff / UltreonCraft.DURATION, 0f, 1f));
                Vec2f thumbnail = this.resizer.thumbnail(this.getWidth() * zoom, this.getHeight() * zoom);

                float drawWidth = thumbnail.x;
                float drawHeight = thumbnail.y;

                float drawX = (this.getWidth() - drawWidth) / 2;
                float drawY = (this.getHeight() - drawHeight) / 2;

                this.spriteBatch.begin();
                renderer.blit(this.ultreonBgTex, 0, 0, this.getWidth(), this.getHeight(), 0, 0, 1024, 1024, 1024, 1024);
                renderer.blit(this.ultreonLogoTex, (int) drawX, (int) drawY, (int) drawWidth, (int) drawHeight, 0, 0, 1920, 1080, 1920, 1080);
                this.spriteBatch.end();

                if (System.currentTimeMillis() - this.ultreonSplashTime > UltreonCraft.DURATION) {
                    this.showUltreonSplash = false;

                    this.loadingOverlay = new LoadingOverlay(this.ultreonBgTex);

                    CompletableFuture.runAsync(() -> {
                        try {
                            this.load();
                        } catch (Throwable t) {
                            UltreonCraft.crash(t);
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

            Texture worldTexture = null;
            if (this.player != null) {
                if (this.currentScreen == null && !GamePlatform.instance.isShowingImGui()) {
                    this.player.rotate(-Gdx.input.getDeltaX() / 2f, -Gdx.input.getDeltaY() / 2f);
                }

                this.camera.update(this.player);
                this.camera.far = (this.settings.renderDistance.get() - 1) * World.CHUNK_SIZE;

                var rotation = this.player != null ? this.player.getRotation() : new Vector2();
                var quaternion = new Quaternion();
                quaternion.setFromAxis(Vector3.Y, rotation.x);
                quaternion.mul(new Quaternion(Vector3.X, rotation.y));
                quaternion.conjugate();

                if (this.renderWorld && world != null && worldRenderer != null && !worldRenderer.isDisposed()) {
                    ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
                    Gdx.gl20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                    this.modelBatch.begin(this.camera);
                    this.modelBatch.getRenderContext().setCullFace(UltreonCraft.CULL_FACE);
                    this.modelBatch.getRenderContext().setDepthTest(GL_DEPTH_FUNC);
                    this.modelBatch.render(worldRenderer, this.env);
                    this.modelBatch.end();
                }
            }

            this.spriteBatch.begin();

            var screen = this.currentScreen;

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

        Vec3i breaking = this.breaking;
        if (this.world != null && breaking != null) {
			HitResult hitResult = this.hitResult;

            if (hitResult != null) {
                this.handleBlockBreaking(breaking, hitResult);
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
	}

    private void handleBlockBreaking(Vec3i breaking, HitResult hitResult) {
        World world = this.world;
        if (world == null) return;
        if (!hitResult.getPos().equals(breaking) || !hitResult.getBlock().equals(this.breakingBlock) || this.player == null) {
            this.resetBreaking(hitResult);
        } else {
            float efficiency = 1.0F;
            if (this.player.getSelectedItem() instanceof ToolItem &&
                    this.breakingBlock.getEffectiveTool() == ((ToolItem) this.player.getSelectedItem()).getToolType()) {
                ToolItem toolItem = (ToolItem) this.player.getSelectedItem();
                efficiency = toolItem.getEfficiency();
            }

            if (!world.continueBreaking(breaking, 1.0F / (Math.max(this.breakingBlock.getHardness() * UltreonCraft.TPS / efficiency, 0) + 1))) {
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
        this.world.stopBreaking(this.breaking);
		Block block = hitResult.getBlock();
		if (block == null || block.isAir()) {
			this.breaking = null;
			this.breakingBlock = null;
		} else {
			this.breaking = hitResult.getPos();
			this.breakingBlock = block;
			this.world.startBreaking(hitResult.getPos());
		}
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

        if (this.itemRenderer != null) {
            this.itemRenderer.resize(width, height);
        }

        var cur = this.currentScreen;
        if (cur != null) {
            cur.resize(ceil(width / this.getGuiScale()), ceil(height / this.getGuiScale()));
        }
    }

    @SuppressWarnings("ConstantValue")
    public void dispose() {
        try {
            while (!this.futures.isEmpty()) {
                this.futures.removeIf(CompletableFuture::isDone);
            }

            if (this.scheduler != null) this.scheduler.shutdownNow();
            if (this.garbageCollector != null) this.garbageCollector.shutdown();

            if (this.world != null) this.world.dispose();
            if (this.worldRenderer != null) this.worldRenderer.dispose();
            if (this.blocksTextureAtlas != null) this.blocksTextureAtlas.dispose();

            GamePlatform.instance.dispose();

            if (this.modelBatch != null) this.modelBatch.dispose();
            if (this.spriteBatch != null) this.spriteBatch.dispose();
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
        final @Nullable WorldRenderer worldRenderer = this.worldRenderer;
        if (world == null) return;
        this.showScreen(new MessageScreen(Language.translate("Saving world...")));
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

    public <T> T getAndWait(Identifier id, Supplier<T> task) {
        return UltreonCraft.invokeAndWait(task::get);
    }

    public ScheduledFuture<?> schedule(Task<?> task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                UltreonCraft.LOGGER.warn("Error occurred in task " + task.id() + ":", e);
            }
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Task<?> task, long time, TimeUnit unit) {
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

	public void resetBreaking() {
		HitResult hitResult = this.hitResult;
        if (hitResult == null || this.world == null || this.breaking == null) return;
        this.world.stopBreaking(hitResult.getPos());
        this.world.startBreaking(hitResult.getPos());
        this.breaking = hitResult.getPos();
		this.breakingBlock = hitResult.getBlock();
	}

	public void startBreaking() {
		HitResult hitResult = this.hitResult;
        if (hitResult == null || this.world == null) return;
        if (this.world.getBreakProgress(hitResult.getPos()) >= 0.0F) return;
        this.world.startBreaking(hitResult.getPos());
		this.breaking = hitResult.getPos();
		this.breakingBlock = hitResult.getBlock();
	}

	public void stopBreaking() {
		HitResult hitResult = this.hitResult;
		if (hitResult == null || this.world == null) return;
		this.world.stopBreaking(hitResult.getPos());
		this.breaking = null;
		this.breakingBlock = null;
	}

    public float getBreakProgress() {
        Vec3i breaking = this.breaking;
        World world = this.world;
        if (breaking == null || world == null) return -1;
        return world.getBreakProgress(breaking);
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
