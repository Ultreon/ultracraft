package com.ultreon.craft;

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
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.GridPoint3;
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
import com.ultreon.craft.events.ScreenEvents;
import com.ultreon.craft.events.WindowCloseEvent;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.font.Font;
import com.ultreon.craft.init.Fonts;
import com.ultreon.craft.init.Sounds;
import com.ultreon.craft.input.*;
import com.ultreon.craft.platform.PlatformType;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.DebugRenderer;
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
import com.ultreon.craft.resources.ResourceFileHandle;
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
import com.ultreon.libs.resources.v0.Resource;
import com.ultreon.libs.resources.v0.ResourceManager;
import com.ultreon.libs.translations.v1.LanguageManager;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

import static com.badlogic.gdx.math.MathUtils.ceil;

public class UltreonCraft extends ApplicationAdapter {
    public static final String NAMESPACE = "craft";
    public static final Logger LOGGER = GamePlatform.instance.getLogger("UltreonCraft");
    public static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setPrettyPrinting().create();
    private String allUnicode;
    public FileHandle configDir;

    private static final String FATAL_ERROR_MSG = "Fatal error occurred when handling crash:";

    private static SavedWorld savedWorld;
    public boolean forceUnicode = false;
    private boolean booted = false;
    public static final int TPS = 20;
    public Font font;
    public BitmapFont unifont;
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
    private Texture windowTex;
    private DebugRenderer debugRenderer;

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

//        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
//            CrashLog crashLog = new CrashLog("Exception in thread", e);
//            CrashCategory cat = new CrashCategory("Thread");
//            crashLog.addCategory(cat);
//            delayCrash(crashLog);
//        });
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
            this.configDir = Gdx.files.external("config/");
            if (!this.configDir.isDirectory()) {
                this.configDir.delete();
                this.configDir.mkdirs();
            }

            this.settings = new GameSettings();
            this.settings.reload();
            this.settings.reloadLanguage();

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

            Resource resource = this.resourceManager.getResource(id("texts/unicode.txt"));
            if (resource == null) throw new FileNotFoundException("Unicode resource not found!");
            this.allUnicode = new String(resource.loadOrGet(), StandardCharsets.UTF_16);

            LOGGER.info("Generating bitmap fonts");
            this.unifont = new BitmapFont(Gdx.files.internal("assets/craft/font/unifont/unifont.fnt"));

            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(new ResourceFileHandle(id("font/dogica/dogicapixel.ttf")));
            FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
            fontParameter.size = 8;
            fontParameter.characters = this.allUnicode;
            fontParameter.minFilter = Texture.TextureFilter.Nearest;
            fontParameter.magFilter = Texture.TextureFilter.Nearest;
            fontParameter.mono = true;

            this.font = new Font(generator.generateFont(fontParameter));

            //**********************//
            // Setting up rendering //
            //**********************//
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

            LOGGER.info("Setting up world environment");
            this.env = new Environment();
            this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1f));
            this.env.add(new DirectionalLight().set(.8f, .8f, .8f, .8f, 0, -.6f));
            this.env.add(new DirectionalLight().set(.8f, .8f, .8f, -.8f, 0, .6f));
            this.env.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, 0, -1, 0));
            this.env.add(new DirectionalLight().set(0.17f, .17f, .17f, 0, 1, 0));

            LOGGER.info("Setting up HUD");
            this.hud = new Hud(this);

            LOGGER.info("Setting up Debug Renderer");
            this.debugRenderer = new DebugRenderer(this);

            //**************************//
            // Registering game content //
            //**************************//
            LOGGER.info("Loading languages");
            this.registerLanguage(id("af_za"));
            this.registerLanguage(id("ar_ae"));
            this.registerLanguage(id("ar_ar"));
            this.registerLanguage(id("ar_bh"));
            this.registerLanguage(id("ar_dj"));
            this.registerLanguage(id("ar_dz"));
            this.registerLanguage(id("ar_eg"));
            this.registerLanguage(id("ar_eh"));
            this.registerLanguage(id("ar_er"));
            this.registerLanguage(id("ar_il"));
            this.registerLanguage(id("ar_iq"));
            this.registerLanguage(id("ar_iq"));
            this.registerLanguage(id("ar_jo"));
            this.registerLanguage(id("ar_km"));
            this.registerLanguage(id("ar_kw"));
            this.registerLanguage(id("ar_lb"));
            this.registerLanguage(id("ar_ly"));
            this.registerLanguage(id("ar_ma"));
            this.registerLanguage(id("ar_mr"));
            this.registerLanguage(id("ar_om"));
            this.registerLanguage(id("ar_ps"));
            this.registerLanguage(id("ar_qa"));
            this.registerLanguage(id("ar_sa"));
            this.registerLanguage(id("ar_sd"));
            this.registerLanguage(id("ar_so"));
            this.registerLanguage(id("ar_sy"));
            this.registerLanguage(id("ar_td"));
            this.registerLanguage(id("ar_tn"));
            this.registerLanguage(id("ar_ye"));
            this.registerLanguage(id("ar_az"));
            this.registerLanguage(id("be_by"));
            this.registerLanguage(id("bg_bg"));
            this.registerLanguage(id("bn_in"));
            this.registerLanguage(id("bs_ba"));
            this.registerLanguage(id("ca_ad"));
            this.registerLanguage(id("ca_es"));
            this.registerLanguage(id("cs_cz"));
            this.registerLanguage(id("cs_sk"));
            this.registerLanguage(id("cy_gb"));
            this.registerLanguage(id("da_dk"));
            this.registerLanguage(id("da_fo"));
            this.registerLanguage(id("da_gl"));
            this.registerLanguage(id("de_at"));
            this.registerLanguage(id("de_be"));
            this.registerLanguage(id("de_ch"));
            this.registerLanguage(id("de_de"));
            this.registerLanguage(id("de_li"));
            this.registerLanguage(id("de_lu"));
            this.registerLanguage(id("de_na"));
            this.registerLanguage(id("el_cy"));
            this.registerLanguage(id("el_gr"));
            this.registerLanguage(id("en_au"));
            this.registerLanguage(id("en_gb"));
            this.registerLanguage(id("en_pi"));
            this.registerLanguage(id("en_ud"));
            this.registerLanguage(id("en_us"));
            this.registerLanguage(id("eo_eo"));
            this.registerLanguage(id("es_ar"));
            this.registerLanguage(id("es_bo"));
            this.registerLanguage(id("es_cl"));
            this.registerLanguage(id("es_co"));
            this.registerLanguage(id("es_cr"));
            this.registerLanguage(id("es_cu"));
            this.registerLanguage(id("es_do"));
            this.registerLanguage(id("es_ec"));
            this.registerLanguage(id("es_es"));
            this.registerLanguage(id("es_gi"));
            this.registerLanguage(id("es_gq"));
            this.registerLanguage(id("es_gt"));
            this.registerLanguage(id("es_hn"));
            this.registerLanguage(id("es_la"));
            this.registerLanguage(id("es_mx"));
            this.registerLanguage(id("es_ni"));
            this.registerLanguage(id("es_pa"));
            this.registerLanguage(id("es_pe"));
            this.registerLanguage(id("es_pr"));
            this.registerLanguage(id("es_py"));
            this.registerLanguage(id("es_sv"));
            this.registerLanguage(id("es_us"));
            this.registerLanguage(id("es_uy"));
            this.registerLanguage(id("es_ve"));
            this.registerLanguage(id("et_ee"));
            this.registerLanguage(id("eu_es"));
            this.registerLanguage(id("fa_ir"));
            this.registerLanguage(id("fb_lt"));
            this.registerLanguage(id("fi_fi"));
            this.registerLanguage(id("fo_fo"));
            this.registerLanguage(id("fr_be"));
            this.registerLanguage(id("fr_bf"));
            this.registerLanguage(id("fr_bi"));
            this.registerLanguage(id("fr_bj"));
            this.registerLanguage(id("fr_ca"));
            this.registerLanguage(id("fr_cg"));
            this.registerLanguage(id("fr_ch"));
            this.registerLanguage(id("fr_fr"));
            this.registerLanguage(id("fr_fr"));
            this.registerLanguage(id("fr_ht"));
            this.registerLanguage(id("fr_td"));
            this.registerLanguage(id("fy_nl"));
            this.registerLanguage(id("ga_ie"));
            this.registerLanguage(id("gl_es"));
            this.registerLanguage(id("ge_il"));
            this.registerLanguage(id("hi_fj"));
            this.registerLanguage(id("hi_in"));
            this.registerLanguage(id("hi_pk"));
            this.registerLanguage(id("hr_ba"));
            this.registerLanguage(id("hr_hr"));
            this.registerLanguage(id("hu_hu"));
            this.registerLanguage(id("hy_am"));
            this.registerLanguage(id("id_id"));
            this.registerLanguage(id("is_is"));
            this.registerLanguage(id("it_ch"));
            this.registerLanguage(id("it_it"));
            this.registerLanguage(id("it_sm"));
            this.registerLanguage(id("ja_jp"));
            this.registerLanguage(id("ja_pw"));
            this.registerLanguage(id("ka_ge"));
            this.registerLanguage(id("km_kh"));
            this.registerLanguage(id("km_kh"));
            this.registerLanguage(id("ko_kp"));
            this.registerLanguage(id("ko_kr"));
            this.registerLanguage(id("ku_tr"));
            this.registerLanguage(id("la_va"));
            this.registerLanguage(id("la_va"));
            this.registerLanguage(id("it_lt"));
            this.registerLanguage(id("lv_lv"));
            this.registerLanguage(id("mk_mk"));
            this.registerLanguage(id("ml_in"));
            this.registerLanguage(id("ms_my"));
            this.registerLanguage(id("ms_sg"));
            this.registerLanguage(id("mt_mt"));
            this.registerLanguage(id("ne_np"));
            this.registerLanguage(id("nl_an"));
            this.registerLanguage(id("nl_aw"));
            this.registerLanguage(id("nl_be"));
            this.registerLanguage(id("nl_nl"));
            this.registerLanguage(id("nl_sr"));
            this.registerLanguage(id("nl_sx"));
            this.registerLanguage(id("nn_no"));
            this.registerLanguage(id("no_no"));
            this.registerLanguage(id("pa_in"));
            this.registerLanguage(id("pl_pl"));
            this.registerLanguage(id("pt_ao"));
            this.registerLanguage(id("pt_br"));
            this.registerLanguage(id("pt_cv"));
            this.registerLanguage(id("pt_gq"));
            this.registerLanguage(id("pt_gw"));
            this.registerLanguage(id("pt_mo"));
            this.registerLanguage(id("pt_mz"));
            this.registerLanguage(id("pt_pt"));
            this.registerLanguage(id("pt_st"));
            this.registerLanguage(id("pt_tl"));
            this.registerLanguage(id("ro_md"));
            this.registerLanguage(id("ro_ro"));
            this.registerLanguage(id("ru_by"));
            this.registerLanguage(id("ru_kg"));
            this.registerLanguage(id("ru_kz"));
            this.registerLanguage(id("ru_ru"));
            this.registerLanguage(id("ru_tj"));
            this.registerLanguage(id("sk_cz"));
            this.registerLanguage(id("sk_sk"));
            this.registerLanguage(id("sl_sl"));
            this.registerLanguage(id("sq_al"));
            this.registerLanguage(id("sq_ks"));
            this.registerLanguage(id("sr_ba"));
            this.registerLanguage(id("sr_me"));
            this.registerLanguage(id("sr_rs"));
            this.registerLanguage(id("sv_fi"));
            this.registerLanguage(id("sv_se"));
            this.registerLanguage(id("sw_ke"));
            this.registerLanguage(id("ta_in"));
            this.registerLanguage(id("te_in"));
            this.registerLanguage(id("th_th"));
            this.registerLanguage(id("tl_ph"));
            this.registerLanguage(id("tr_cy"));
            this.registerLanguage(id("tr_tr"));
            this.registerLanguage(id("uk_ua"));
            this.registerLanguage(id("ul_vn"));
            this.registerLanguage(id("zh_cn"));
            this.registerLanguage(id("zh_hk"));
            this.registerLanguage(id("zh_mo"));
            this.registerLanguage(id("zh_sg"));
            this.registerLanguage(id("zh_gw"));

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
            this.registerModels();

            //********************************************//
            // Post-initialize game content               //
            // Such as model baking and texture stitching //
            //********************************************//
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

            if (this.deferredWidth != null && this.deferredHeight != null) {
                this.camera.viewportWidth = this.deferredWidth;
                this.camera.viewportHeight = this.deferredHeight;
                this.camera.update();
            }

            this.windowTex = this.textureManager.getTexture(id("textures/gui/window.png"));

            //*************//
            // Final stuff //
            //*************//
            LOGGER.info("Opening title screen");
            this.showScreen(new TitleScreen());

            savedWorld = new SavedWorld(Gdx.files.external("world"));

            GamePlatform.instance.setupImGui();
        } catch (Throwable t) {
            crash(t);
            return;
        }

        booted = true;
        LOGGER.info("Game booted in " + (System.currentTimeMillis() - BOOT_TIMESTAMP) + "ms");
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
    }

    private void renderWindow(Renderer renderer, int width, int height) {
        renderer.draw9PatchTexture(this.windowTex, 0, 0, width, height, 0, 0, 18, 22, 256, 256);
    }

    private void renderGame(Renderer renderer, Screen screen, World world, float deltaTime) {
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
}
