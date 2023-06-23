package com.ultreon.craft;

import static com.badlogic.gdx.math.MathUtils.ceil;

import com.badlogic.gdx.ApplicationAdapter;
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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ultreon.craft.audio.SoundEvent;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.ScreenEvents;
import com.ultreon.craft.events.WindowCloseEvent;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.init.Fonts;
import com.ultreon.craft.init.Sounds;
import com.ultreon.craft.input.DesktopInput;
import com.ultreon.craft.input.GameCamera;
import com.ultreon.craft.input.GameInput;
import com.ultreon.craft.input.MobileInput;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.options.GameSettings;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Hud;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.gui.screens.PauseScreen;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.screens.TitleScreen;
import com.ultreon.craft.render.gui.screens.WorldLoadScreen;
import com.ultreon.craft.render.model.BakedCubeModel;
import com.ultreon.craft.render.model.BakedModelRegistry;
import com.ultreon.craft.render.model.CubeModel;
import com.ultreon.craft.render.texture.atlas.TextureAtlas;
import com.ultreon.craft.util.GG;
import com.ultreon.craft.world.SavedWorld;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.crash.v0.ApplicationCrash;
import com.ultreon.libs.crash.v0.CrashCategory;
import com.ultreon.libs.crash.v0.CrashLog;
import com.ultreon.libs.events.v1.EventResult;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
import com.ultreon.libs.resources.v0.ResourceManager;
import com.ultreon.libs.translations.v0.LanguageManager;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import space.earlygrey.shapedrawer.ShapeDrawer;

public class UltreonCraft extends ApplicationAdapter {
    public static final String NAMESPACE = "craft";
    public static final Logger LOGGER = GamePlatform.instance.getLogger("UltreonCraft");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    public static final FileHandle CONFIG_DIR = Gdx.files.external("config/");

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";

    private static SavedWorld savedWorld;
    private boolean booted = false;
    public static final int TPS = 20;
    public BitmapFont font;
    public BitmapFont largeFont;
    public BitmapFont xlFont;
    public GameInput input;
    @Nullable public World world;
    private static UltreonCraft instance;
    @Nullable public Player player;
    public int renderDistance = 8;
    private SpriteBatch spriteBatch;
    private ModelBatch batch;
    GameCamera camera;
    private Environment env;
    private float timeUntilNextTick;
    public final PlayerInput playerInput = new PlayerInput(this);
    private final boolean isDevMode;
    public Screen currentScreen;
    public GameSettings settings;
    ShapeDrawer shapes;
    private TextureRegion white;
    private TextureManager textureManager;
    private ResourceManager resourceManager;
    @Deprecated
    private Texture tilesTex;
    private float guiScale = calculateGuiScale();

    private final List<Runnable> tasks = new CopyOnWriteArrayList<>();
    public Hud hud;
    private int chunkRefresh;
    public boolean showDebugHud = true;

    // Public Flags
    public boolean renderWorld = false;
    public boolean advancedShadows = true;

    // Startup time
    public static final long BOOT_TIMESTAMP = System.currentTimeMillis();

    // Texture Atlases
    public TextureAtlas blocksTextureAtlas;
    private BakedModelRegistry bakedBlockModels;

    // Advanced Shadows
    private DirectionalShadowLight shadowLight;
    private ModelBatch shadowBatch;
    private List<CompletableFuture<?>> futures = new CopyOnWriteArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Integer deferredWidth;
    private Integer deferredHeight;

    public UltreonCraft(String[] args) {
        LOGGER.info("Booting game!");

        Identifier.setDefaultNamespace(NAMESPACE);
        GamePlatform.instance.preInitImGui();

        List<String> argList = List.of(args);
        isDevMode = argList.contains("--dev");

        if (isDevMode) {
            LOGGER.debug("Developer mode is enabled");
        }

        instance = this;

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            CrashLog crashLog = new CrashLog("Exception in thread", e);
            CrashCategory cat = new CrashCategory("Thread");
            crashLog.addCategory(cat);
            delayCrash(crashLog);
        });
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

    @Override
    public void create() {
        try {
            if (!CONFIG_DIR.isDirectory()) {
                CONFIG_DIR.delete();
                CONFIG_DIR.mkdirs();
            }

            this.settings = new GameSettings();

            Gdx.input.setCatchKey(Input.Keys.BACK, true);

            LOGGER.info("Initializing game");
            this.textureManager = new TextureManager();
            this.spriteBatch = new SpriteBatch();

            createDir("screenshots/");
            createDir("game-crashes/");
            createDir("logs/");

            this.resourceManager = new ResourceManager("assets");
            LOGGER.info("Importing resources");
            this.resourceManager.importDeferredPackage(this.getClass());

            LOGGER.info("Generating bitmap fonts");
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("assets/craft/font/dogica/dogicapixel.ttf"));
            FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
            fontParameter.size = 8;
            fontParameter.minFilter = Texture.TextureFilter.Nearest;
            fontParameter.magFilter = Texture.TextureFilter.Nearest;
            fontParameter.mono = true;
            this.font = generator.generateFont(fontParameter);
            FreeTypeFontParameter largeFontParameter = new FreeTypeFontParameter();
            largeFontParameter.size = 16;
            largeFontParameter.minFilter = Texture.TextureFilter.Nearest;
            largeFontParameter.magFilter = Texture.TextureFilter.Nearest;
            largeFontParameter.mono = true;
            this.largeFont = generator.generateFont(largeFontParameter);
            FreeTypeFontParameter xlFontParameter = new FreeTypeFontParameter();
            xlFontParameter.size = 24;
            xlFontParameter.minFilter = Texture.TextureFilter.Nearest;
            xlFontParameter.magFilter = Texture.TextureFilter.Nearest;
            xlFontParameter.mono = true;
            this.xlFont = generator.generateFont(xlFontParameter);

            LOGGER.info("Initializing rendering stuffs");
            DefaultShader.Config config = new DefaultShader.Config();
            config.defaultCullFace = GL20.GL_FRONT;
            this.batch = new ModelBatch(new DefaultShaderProvider(config));
            this.shadowBatch = new ModelBatch(new DefaultShaderProvider(config));
            this.camera = new GameCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            this.camera.near = 0.01f;
            this.camera.far = 1000;
            this.input = createInput();
            Gdx.input.setInputProcessor(this.input);

            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1F, 1F, 1F, 1F);
            pixmap.drawPixel(0, 0);
            this.white = new TextureRegion(new Texture(pixmap));

            this.shapes = new ShapeDrawer(this.spriteBatch, this.white);

            LOGGER.info("Setting up environment");
            this.env = new Environment();
            this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
            this.env.add(new DirectionalLight().set(.8f, .8f, .8f, .8f, 0, -.6f));
            this.env.add(new DirectionalLight().set(.8f, .8f, .8f, -.8f, 0, .6f));
            this.env.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, 0, -1, 0));
            this.env.add(new DirectionalLight().set(0.17f, .17f, .17f, 0, 1, 0));

            LOGGER.info("Loading languages");
            LanguageManager.INSTANCE.load(new Locale("en"), id("english"), resourceManager);
            LanguageManager.INSTANCE.load(new Locale("nl"), id("dutch"), resourceManager);
            LanguageManager.INSTANCE.load(new Locale("de"), id("german"), resourceManager);

            LOGGER.info("Registering stuff");
            Registries.nopInit();

            Blocks.nopInit();
            NoiseSettingsInit.nopInit();
            Entities.nopInit();
            Fonts.nopInit();
            Sounds.nopInit();

            for (var registry : Registry.getRegistries()) {
                RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
            }
            Registry.freeze();

            LOGGER.info("Registering models");
            registerModels();

            LOGGER.info("Stitching textures");
            this.blocksTextureAtlas = BlockModelRegistry.stitch(this.textureManager);

            LOGGER.info("Initializing sounds");
            for (SoundEvent sound : Registries.SOUNDS.values()) {
                if (sound == null) {
                    continue;
                }
                sound.register();
            }

            LOGGER.info("Baking models");
            this.bakedBlockModels = BlockModelRegistry.bake(this.blocksTextureAtlas);

            LOGGER.info("Setting up HUD");
            this.hud = new Hud(this);

            if (this.deferredWidth != null && this.deferredHeight != null) {
                this.camera.viewportWidth = this.deferredWidth;
                this.camera.viewportHeight = this.deferredHeight;
                this.camera.update();
            }

            LOGGER.info("Opening title screen");
            showScreen(new TitleScreen());

            savedWorld = new SavedWorld(Gdx.files.external("world"));

            GamePlatform.instance.setupImGui();
        } catch (Exception e) {
            crash(e);
            return;
        }

        booted = true;
        LOGGER.info("Game booted in " + (System.currentTimeMillis() - BOOT_TIMESTAMP) + "ms");
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

    private static void createDir(String dirName) {
        FileHandle directory = Gdx.files.local(dirName);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void pause() {
        super.pause();

        if (this.currentScreen == null && this.world != null) {
            this.showScreen(new PauseScreen());
        }
    }

    @Override
    public void resume() {
        super.resume();

        if (this.currentScreen instanceof PauseScreen && this.world != null) {
            this.showScreen(null);
        }
    }

    @CanIgnoreReturnValue
    public boolean showScreen(Screen open) {
        Screen cur = this.currentScreen;
        if (open == null && world == null) {
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
        var openResult = ScreenEvents.OPEN.factory().onOpenScreen(open);
        if (openResult.isCanceled()) {
            return false;
        }

        if (openResult.isInterrupted()) {
            open = openResult.getValue();
        }

        if (cur != null) {
            EventResult closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(this.currentScreen);
            if (closeResult.isCanceled()) return false;

            cur.hide();
            if (open != null) {
                LOGGER.debug("Changing screen to: " + open.getClass());
            } else {
                LOGGER.debug("Closing screen: " + this.currentScreen.getClass());
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

    @Override
    public void render() {
        if (!this.booted) {
            return;
        }

        try {
            final var tickTime = 1f / TPS;

            float deltaTime = Gdx.graphics.getDeltaTime();
            this.timeUntilNextTick -= deltaTime;
            if (this.timeUntilNextTick < 0) {
                this.timeUntilNextTick = tickTime + this.timeUntilNextTick;

                tick();
            }

            this.tasks.forEach(runnable -> {
                runnable.run();
                this.tasks.remove(runnable);
            });

            this.input.update();

            if (Gdx.graphics.getFrameId() == 2) {
                GamePlatform.instance.firstRender();
                Gdx.graphics.setTitle("UltraCraft v" + Metadata.INSTANCE.version);
            }

            ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
            World world = this.world;

            if (this.renderWorld && world != null) {
                this.batch.begin(this.camera);
                this.batch.render(world, this.env);
                this.batch.end();
            }

            this.spriteBatch.begin();

            Screen screen = this.currentScreen;
            Renderer renderer = new Renderer(this.shapes);
            this.spriteBatch.setTransformMatrix(this.spriteBatch.getTransformMatrix().scale(this.guiScale, this.guiScale, 1));
            renderGame(renderer, screen, world, deltaTime);
            this.spriteBatch.setTransformMatrix(this.spriteBatch.getTransformMatrix().scale(1F / this.guiScale, 1F / this.guiScale, 1));

            GamePlatform.instance.renderImGui(this);

            this.spriteBatch.end();
        } catch (Exception e) {
            crash(e);
        }
    }

    private void renderGame(Renderer renderer, Screen screen, World world, float deltaTime) {
        if (world != null) {
            if (this.showDebugHud) {
                this.font.setColor(Color.rgb(0xffffff).toGdx());
                this.font.draw(this.spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + world.getRenderedChunks() + "/"
                        + world.getTotalChunks(), 20, this.getScaledHeight() - 20);
                if (this.player != null) {
                    this.font.draw(this.spriteBatch, "xyz: " + this.player.blockPosition(), 20, this.getScaledHeight() - 30);
                    this.font.draw(this.spriteBatch, "chunk shown: " + (world.getChunkAt(this.player.blockPosition()) != null), 20, this.getScaledHeight() - 40);
                    this.font.draw(this.spriteBatch, "region opened: " + (world.getRegionAt(this.player.blockPosition()) != null), 20, this.getScaledHeight() - 50);
                    this.font.draw(this.spriteBatch, "block at: " + (Registries.BLOCK.getKey(world.get(this.player.blockPosition()))), 20, this.getScaledHeight() - 60);
                    if (GamePlatform.instance.isMobile()) {
                        Hud hud = this.hud;
                        if (hud != null) {
                            this.font.draw(this.spriteBatch, "joysttick: " + hud.getJoyStick(), 20, this.getScaledHeight() - 70);
                        }
                    }
                }
            }

            this.hud.render(renderer, deltaTime);
        }
        if (screen != null) {
            screen.render(renderer, (int) (Gdx.input.getX() / this.getGuiScale()), (int) (Gdx.input.getY() / this.getGuiScale()), deltaTime);
        }
    }

    public static void crash(Exception e) {
        e.printStackTrace();
        try {
            CrashLog crashLog = new CrashLog("An error occurred", e);
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
        game.add("Game booted", this.booted); // Could be the game isn't booted yet.
        crashLog.addCategory(game);
    }

    private static void crash(ApplicationCrash crash) {
        try {
            crash.printCrash();

            if (GamePlatform.instance.canAccessData()) {
                crash.getCrashLog().defaultSave();
            } else {
                CrashLog crashLog = crash.getCrashLog();
                Throwable throwable = crashLog.getThrowable();

                for (var category : crashLog.getCategories()) {
                    Throwable categoryThrowable = category.getThrowable();
                    throwable.addSuppressed(categoryThrowable);
                }

                throw throwable;
            }
            Gdx.app.exit();
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
        this.input.update();
    }

    public CompletableFuture<Void> respawnAsync() {
        assert this.world != null;
        if (this.player != null && this.world.getEntity(this.player.getId()) == this.player) {
            this.world.despawn(this.player);
        }

        GridPoint3 spawnPoint = this.world.getSpawnPoint();

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

        GridPoint3 spawnPoint = this.world.getSpawnPoint();

        this.world.updateChunksForPlayer(spawnPoint.x, spawnPoint.z);
        this.player = Entities.PLAYER.create(this.world);
        LOGGER.debug("Player created, setting health now.");
        this.player.setHealth(this.player.getMaxHeath());
        LOGGER.debug("Health set, setting position now.");
        this.player.setPosition(spawnPoint.x + 0.5f, spawnPoint.y, spawnPoint.z + 0.5f);
        LOGGER.debug("Position set, spawning in world now..");
        this.world.spawn(this.player);
    }

    @Override
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
            cur.resize(ceil(width / getGuiScale()), ceil(height / getGuiScale()));
        }
    }

    @Override
    public void dispose() {
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
        this.font.dispose();
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

    public BitmapFont getBitmapFont() {
        return font;
    }

    public int getWidth() {
        return Gdx.graphics.getWidth();
    }

    public int getHeight() {
        return Gdx.graphics.getHeight();
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    @Deprecated
    public Texture getTilesTex() {
        return tilesTex;
    }

    public void startWorld() {
        this.showScreen(new WorldLoadScreen(getSavedWorld()));
    }

    public static SavedWorld getSavedWorld() {
        return savedWorld;
    }

    public float getGuiScale() {
        return guiScale;
    }

    public int getScaledWidth() {
        return ceil(getWidth() / getGuiScale());
    }

    public int getScaledHeight() {
        return ceil(getHeight() / getGuiScale());
    }

    public void exitWorld() {
        world.dispose();
        world = null;
        System.gc();
        showScreen(new TitleScreen());
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

    public void runLater(Task task) {
        Gdx.app.postRunnable(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Error occurred in task {}:", task.id(), e);
            }
        });
    }

    public ScheduledFuture<?> schedule(Task task, long timeMillis) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Error occurred in task {}:", task.id(), e);
            }
        }, timeMillis, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(Task task, long time, TimeUnit unit) {
        return this.scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.warn("Error occurred in task {}:", task.id(), e);
            }
        }, time, unit);
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void playSound(SoundEvent event) {
        event.getSound().play();
    }

    public boolean closeRequested() {
        EventResult eventResult = WindowCloseEvent.EVENT.factory().onWindowClose();
        return !eventResult.isCanceled();
    }

    public void filesDropped(String[] files) {

    }

    public void addFuture(CompletableFuture<?> future) {
        this.futures.add(future);
    }

    public BakedCubeModel getBakedBlockModel(Block block) {
        return bakedBlockModels.bakedModels().get(block);
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

    private int calculateRenderDistance() {
        switch (GamePlatform.instance.getPlatformType()) {
            case DESKTOP:
                return 8;
            case MOBILE:
            case WEB:
                return 4;
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean isPlaying() {
        return this.world != null && this.currentScreen == null;
    }
}
