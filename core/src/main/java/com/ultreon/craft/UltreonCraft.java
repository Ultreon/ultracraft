package com.ultreon.craft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.GridPoint3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
import com.ultreon.craft.input.GameCamera;
import com.ultreon.craft.input.InputManager;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.item.Items;
import com.ultreon.craft.options.GameSettings;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.render.Color;
import com.ultreon.craft.render.Hud;
import com.ultreon.craft.render.Renderer;
import com.ultreon.craft.render.Shaders;
import com.ultreon.craft.render.gui.GuiComponent;
import com.ultreon.craft.render.gui.screens.PauseScreen;
import com.ultreon.craft.render.gui.screens.Screen;
import com.ultreon.craft.render.gui.screens.TitleScreen;
import com.ultreon.craft.render.gui.screens.WorldLoadScreen;
import com.ultreon.craft.util.HitResult;
import com.ultreon.craft.util.ImGuiEx;
import com.ultreon.craft.world.Chunk;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.events.v1.EventResult;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
import com.ultreon.libs.resources.v0.ResourceManager;
import com.ultreon.libs.translations.v0.LanguageManager;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.badlogic.gdx.math.MathUtils.ceil;

public class UltreonCraft extends ApplicationAdapter {
	public static final String NAMESPACE = "craft";
	private static final ImBoolean SHOW_PLAYER_UTILS = new ImBoolean(false);
	private static final ImBoolean SHOW_GUI_UTILS = new ImBoolean(false);
	private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
	private final ImGuiImplGlfw imGuiGlfw;
	private final ImGuiImplGl3 imGuiGl3;
	public static final int TPS = 20;
	public BitmapFont font;
	public BitmapFont largeFont;
	public BitmapFont xlFont;
	public InputManager input;
	public World world;
	private static UltreonCraft instance;
	public Player player;
	public int renderDistance = 8;
	private SpriteBatch spriteBatch;
	private ModelBatch modelBatch;
	private GameCamera camera;
	private Environment env;
	private float timeUntilNextTick;
	public final PlayerInput playerInput = new PlayerInput();
	private final boolean isDevMode;
	private final ImBoolean showImGui = new ImBoolean(false);
	private long windowHandle;
	private final ImFloat imGuiPosX = new ImFloat();
	private final ImFloat imGuiPosY = new ImFloat();
	private final ImFloat imGuiPosZ = new ImFloat();
	public Screen currentScreen;
	public final GameSettings settings = new GameSettings();
	private ShapeDrawer shapes;
	private TextureRegion white;
	private TextureManager textureManager;
	private ResourceManager resourceManager;
	private Texture tilesTex;
	private float guiScale = 2;
	public boolean renderWorld = false;
	private final List<Runnable> tasks = new CopyOnWriteArrayList<>();
	private Hud hud;
	private int chunkRefresh;
	public HitResult hitResult;
	private GridPoint3 breaking;
	private Block breakingBlock;

	public UltreonCraft(String[] args) {
		Identifier.setDefaultNamespace(NAMESPACE);
		imGuiGlfw = new ImGuiImplGlfw();
		imGuiGl3 = new ImGuiImplGl3();

		List<String> argList = List.of(args);
		isDevMode = argList.contains("--dev");

		instance = this;
	}

	public static UltreonCraft get() {
		return instance;
	}

	public static Identifier id(String path) {
		return new Identifier(path);
	}

	@Override
	public void create() {
		this.textureManager = new TextureManager();
		this.spriteBatch = new SpriteBatch();

		createDir("screenshots/");

		this.resourceManager = new ResourceManager("assets");
		try {
			this.resourceManager.importPackage(getClass().getProtectionDomain().getCodeSource().getLocation());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		final int breakStages = 6;
		Texture texture = this.textureManager.getTexture(new Identifier("textures/break_stages.png"));

		for (int i = 0; i < breakStages; i++) {
			TextureRegion region = new TextureRegion(texture, 0.0F, (float) i / breakStages, 1.0F, (float) (i + 1) / breakStages);
			Chunk.BREAK_TEX.add(region);
		}

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

		DefaultShader.Config config = new DefaultShader.Config();
		config.defaultCullFace = GL20.GL_FRONT;
		this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
		this.camera = new GameCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.camera.near = 0.01f;
		this.camera.far = 1000;
		this.input = new InputManager(this, this.camera);
		Gdx.input.setInputProcessor(this.input);

		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(1F, 1F, 1F, 1F);
		pixmap.drawPixel(0, 0);
		this.white = new TextureRegion(new Texture(pixmap));

		this.shapes = new ShapeDrawer(this.spriteBatch, this.white);

		this.env = new Environment();
		this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		this.env.add(new DirectionalLight().set(.5f, .5f, .5f, -0.4f, -1, -0.2f));
		this.env.add(new DirectionalLight().set(.5f, .5f, .5f, 0.4f, -1, 0.2f));

		LanguageManager.INSTANCE.load(new Locale("en"), id("english"), resourceManager);
		LanguageManager.INSTANCE.load(new Locale("nl"), id("dutch"), resourceManager);
		LanguageManager.INSTANCE.load(new Locale("de"), id("german"), resourceManager);

		Registries.nopInit();

		Blocks.nopInit();
		Items.nopInit();
		NoiseSettingsInit.nopInit();
		Entities.nopInit();
		Fonts.nopInit();
		Sounds.nopInit();
		Shaders.nopInit();

		for (var registry : Registry.getRegistries()) {
			RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
		}
		Registry.freeze();

		this.tilesTex = textureManager.registerTexture(id("textures/blocks.png"));

		for (SoundEvent sound : Registries.SOUNDS.values()) {
			if (sound == null) {
				break;
			}
			sound.register();
		}

		for (Block block : Registries.BLOCK.values()) {
			if (block == null) {
				break;
			}
			block.bake(this.tilesTex);
		}

		this.hud = new Hud(this);

		showScreen(new TitleScreen());

		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}
		ImGui.createContext();
		final ImGuiIO io = ImGui.getIO();
		io.setIniFilename(null);
		io.getFonts().addFontDefault();

		windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();

		imGuiGlfw.init(windowHandle, true);
		imGuiGl3.init("#version 150");
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
			showScreen(new PauseScreen());
		}
	}

	@Override
	public void resume() {
		super.resume();

		if (this.world != null) {
			showScreen(null);
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

			cur.hide();
			this.currentScreen = null;
			Gdx.input.setCursorCatched(true);

			return true;
		}
		var openResult = ScreenEvents.OPEN.factory().onOpenScreen(open);
		if (openResult.isCanceled()) {
			return false;
		}

		Screen openInstead = openResult.getValue();
		if (openInstead != null) {
			open = openInstead;
		}

		if (cur != null) {
			EventResult closeResult = ScreenEvents.CLOSE.factory().onCloseScreen(this.currentScreen);
			if (closeResult.isCanceled()) return false;

			cur.hide();
		} else {
			Gdx.input.setCursorCatched(false);
		}
		this.currentScreen = open;
		this.currentScreen.show();

		return true;
	}

    @Override
    public void render() {
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

		Player player = this.player;
		this.hitResult = player == null ? null : player.rayCast();
		this.input.update();

		ScreenUtils.clear(0.6F, 0.7F, 1.0F, 1.0F, true);
		World world = this.world;
		Gdx.graphics.setTitle("Ultreon Craft - " + Gdx.graphics.getFramesPerSecond() + " fps");

		if (this.renderWorld && world != null) {
			this.modelBatch.begin(this.camera);
			this.modelBatch.render(world, this.env);
			this.modelBatch.end();

			HitResult hitResult = this.hitResult;
			GridPoint3 blockPos = hitResult.getPos();

			var blockX = blockPos.x;
			var blockY = blockPos.y;
			var blockZ = blockPos.z;

			drawOutline(blockX, blockY, blockZ);

			if (InputManager.isKeyDown(Input.Keys.F9)) {
				world.regen();
			}
		}

		this.spriteBatch.begin();

		Screen screen = this.currentScreen;
		Renderer renderer = new Renderer(this.shapes);
		this.spriteBatch.setTransformMatrix(this.spriteBatch.getTransformMatrix().scale(this.guiScale, this.guiScale, 1));
		if (world != null) {
			this.font.setColor(Color.rgb(0xffffff).toGdx());
			this.font.draw(this.spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + world.getRenderedChunks() + "/"
					+ world.getTotalChunks(), 20, 20);
			if (player != null) {
				this.font.draw(this.spriteBatch, "xyz: " + player.blockPosition(), 20, 30);
				this.font.draw(this.spriteBatch, "chunk shown: " + (world.getChunkAt(player.blockPosition()) != null), 20, 40);
				this.font.draw(this.spriteBatch, "break progress: " + world.getBreakProgress(this.hitResult.getPos()), 20, 50);
			}
			this.hud.render(renderer, deltaTime);
		}
		if (screen != null) {
			screen.render(renderer, (int) (Gdx.input.getX() / getGuiScale()), (int) (Gdx.input.getY() / getGuiScale()), deltaTime);
		}
		this.spriteBatch.setTransformMatrix(this.spriteBatch.getTransformMatrix().scale(1F / this.guiScale, 1F / this.guiScale, 1));

		if (this.showImGui.get()) {
			// render 3D scene
			this.imGuiGlfw.newFrame();

			ImGui.newFrame();
			ImGui.setNextWindowPos(0, 0);
			ImGui.setNextWindowSize(Gdx.graphics.getWidth(), 18);
			ImGui.setNextWindowCollapsed(true);

			if (Gdx.input.isCursorCatched()) {
				ImGui.getIO().setMouseDown(new boolean[5]);
				ImGui.getIO().setMousePos(Integer.MAX_VALUE, Integer.MAX_VALUE);
			}

			if (ImGui.begin("MenuBar", ImGuiWindowFlags.NoMove |
					ImGuiWindowFlags.NoCollapse |
					ImGuiWindowFlags.AlwaysAutoResize |
					ImGuiWindowFlags.NoTitleBar |
					ImGuiWindowFlags.MenuBar |
					ImGuiInputTextFlags.AllowTabInput)) {
				if (ImGui.beginMenuBar()) {
					if (ImGui.beginMenu("View")) {
						ImGui.menuItem("Show Player Utils", null, SHOW_PLAYER_UTILS, player != null);
						ImGui.menuItem("Show Gui Utils", null, SHOW_GUI_UTILS, currentScreen != null);
						ImGui.endMenu();
					}
					if (ImGui.beginMenu("Debug")) {
						ImGui.menuItem("Utils", null, SHOW_UTILS);
						ImGui.endMenu();
					}

					ImGui.text(" Frames Per Second: " + Gdx.graphics.getFramesPerSecond() + "   Frames ID: " + Gdx.graphics.getFrameId());
					ImGui.endMenuBar();
				}
				ImGui.end();
			}

			if (SHOW_PLAYER_UTILS.get()) showPlayerUtilsWindow();
			if (SHOW_GUI_UTILS.get()) showGuiUtilsWindow();
			if (SHOW_UTILS.get()) showUtils();

			ImGui.render();
			this.imGuiGl3.renderDrawData(ImGui.getDrawData());
		}

        this.spriteBatch.end();
    }

	private void drawOutline(int blockX, int blockY, int blockZ) {
		// Create a new mesh that contains only the vertices for the desired block
		Mesh blockMesh = new Mesh(true, 24, 36, VertexAttribute.Position(), VertexAttribute.Normal());
		float[] vertices = {
				// Bottom face
				blockX, blockY, blockZ,
				blockX, blockY, blockZ + 1,
				blockX + 1, blockY, blockZ + 1,
				blockX + 1, blockY, blockZ,

				// Top face
				blockX, blockY + 1, blockZ,
				blockX + 1, blockY + 1, blockZ,
				blockX + 1, blockY + 1, blockZ + 1,
				blockX, blockY + 1, blockZ + 1,

				// Right face
				blockX + 1, blockY, blockZ,
				blockX + 1, blockY, blockZ + 1,
				blockX + 1, blockY + 1, blockZ + 1,
				blockX + 1, blockY + 1, blockZ,

				// Left face
				blockX, blockY, blockZ,
				blockX, blockY + 1, blockZ,
				blockX, blockY + 1, blockZ + 1,
				blockX, blockY, blockZ + 1,

				// Back face
				blockX, blockY, blockZ,
				blockX + 1, blockY, blockZ,
				blockX + 1, blockY + 1, blockZ,
				blockX, blockY + 1, blockZ,

				// Front face
				blockX, blockY, blockZ + 1,
				blockX, blockY + 1, blockZ + 1,
				blockX + 1, blockY + 1, blockZ + 1,
				blockX + 1, blockY, blockZ + 1,
		};
		short[] indices = {
				0, 1, 2,
				0, 2, 3,
				4, 6, 5,
				4, 7, 6,
				8, 10, 9,
				8, 11, 10,
				12, 13, 14,
				12, 14, 15,
				16, 17, 18,
				16, 18, 19,
				20, 22, 21,
				20, 23, 22
		};
		blockMesh.setVertices(vertices);
		blockMesh.setIndices(indices);

		// Draw the outline using the new block mesh
		Gdx.gl.glEnable(GL20.GL_POLYGON_OFFSET_FILL);
		Gdx.gl.glPolygonOffset(-1f, -1f);
		ShaderProgram outlineShader = Shaders.OUTLINE;
		outlineShader.setUniformMatrix("u_projTrans", this.camera.combined);
		outlineShader.setUniformf("u_thickness", 0.05f);
		outlineShader.setUniformf("u_blockPosition", new Vector3(blockX, blockY, blockZ));
//		outlineShader.setUniformf("u_color", Color.black.toGdx());
		Gdx.gl.glLineWidth(2);
		blockMesh.render(outlineShader, GL30.GL_LINES);
		Gdx.gl.glDisable(GL20.GL_POLYGON_OFFSET_FILL);

		// Dispose of the new mesh when you're done with it
		blockMesh.dispose();
	}

	public void tick() {
		World world = this.world;
		if (world != null) {
			WorldEvents.PRE_TICK.factory().onPreTick(world);
			world.tick();
			WorldEvents.POST_TICK.factory().onPostTick(world);
		}

		if (this.world != null && this.breaking != null) {
			var hitResult = this.hitResult;
			GridPoint3 breakNow = null;

			if (!hitResult.getPos().equals(this.breaking) || !hitResult.getBlock().equals(this.breakingBlock)) {
				resetBreaking(hitResult);
			} else {
				if (this.breaking != null) {
					if (!this.world.continueBreaking(this.breaking, 1.0F / (Math.max(breakingBlock.getHardness() * TPS, 0) + 1))) {
						stopBreaking();
					}
				}
			}
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

	private void resetBreaking(HitResult hitResult) {
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

	private void showPlayerUtilsWindow() {
//		Screen currentScreen = getCurrentScreen();
		ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
		ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
		if (this.player != null && ImGui.begin("Player Utils", getDefaultFlags())) {
			ImGuiEx.text("Id:", () -> this.player.getId());
//			ImGuiEx.text("'Direction':", () -> this.player.getFacing());
			ImGuiEx.editFloat("Walking Speed:", "PlayerWalkingSpeed", this.player.getWalkingSpeed(), v -> this.player.setWalkingSpeed(v));
			ImGuiEx.editFloat("Flying Speed:", "PlayerFlyingSpeed", this.player.getFlyingSpeed(), v -> this.player.setFlyingSpeed(v));
			ImGuiEx.editFloat("Gravity:", "PlayerGravity", this.player.gravity, v -> this.player.gravity = v);
			ImGuiEx.editFloat("Jump Velocity:", "PlayerJumpVelocity", this.player.jumpVel, v -> this.player.jumpVel = v);
			ImGuiEx.editFloat("Health:", "PlayerHealth", this.player.getHealth(), v -> this.player.setHealth(v));
			ImGuiEx.editFloat("Max Health:", "PlayerMaxHealth", this.player.getMaxHeath(), v -> this.player.setMaxHeath(v));
			ImGuiEx.editBool("No Gravity:", "PlayerNoGravity", this.player.noGravity, v -> this.player.noGravity = v);
			ImGuiEx.editBool("Flying:", "PlayerFlying", this.player.isFlying(), v -> this.player.setFlying(v));
			ImGuiEx.editBool("Spectating:", "PlayerSpectating", this.player.isSpectating(), v -> this.player.setSpectating(v));
			ImGuiEx.bool("On Ground:", () -> this.player.onGround);
			ImGuiEx.bool("Colliding:", () -> this.player.isColliding);
			ImGuiEx.bool("Colliding X:", () -> this.player.isCollidingX);
			ImGuiEx.bool("Colliding Y:", () -> this.player.isCollidingY);
			ImGuiEx.bool("Colliding Z:", () -> this.player.isCollidingZ);

			if (ImGui.collapsingHeader("Position")) {
				ImGui.treePush();
				ImGuiEx.editFloat("X:", "PlayerX", this.player.getX(), v -> this.player.setX(v));
				ImGuiEx.editFloat("Y:", "PlayerY", this.player.getY(), v -> this.player.setY(v));
				ImGuiEx.editFloat("Z:", "PlayerZ", this.player.getZ(), v -> this.player.setZ(v));
				ImGui.treePop();
			}
			if (ImGui.collapsingHeader("Velocity")) {
				ImGui.treePush();
				ImGuiEx.editFloat("X:", "PlayerVelocityX", this.player.velocityX, v -> this.player.velocityX = v);
				ImGuiEx.editFloat("Y:", "PlayerVelocityY", this.player.velocityY, v -> this.player.velocityY = v);
				ImGuiEx.editFloat("Z:", "PlayerVelocityZ", this.player.velocityZ, v -> this.player.velocityZ = v);
				ImGui.treePop();
			}
			if (ImGui.collapsingHeader("Rotation")) {
				ImGui.treePush();
				ImGuiEx.editFloat("X:", "PlayerXRot", this.player.getXRot(), v -> this.player.setXRot(v));
				ImGuiEx.editFloat("Y:", "PlayerYRot", this.player.getYRot(), v -> this.player.setYRot(v));
				ImGui.treePop();
			}
			if (ImGui.collapsingHeader("Player Input")) {
				ImGui.treePush();
				ImGuiEx.bool("Forward", () -> this.playerInput.forward);
				ImGuiEx.bool("Backward", () -> this.playerInput.backward);
				ImGuiEx.bool("Left", () -> this.playerInput.strafeLeft);
				ImGuiEx.bool("Right", () -> this.playerInput.strafeRight);
				ImGuiEx.bool("Up", () -> this.playerInput.up);
				ImGuiEx.bool("Down", () -> this.playerInput.down);
				ImGui.treePop();
			}
		}
		ImGui.end();
	}

	private void showGuiUtilsWindow() {
//		Screen currentScreen = getCurrentScreen();
		ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
		ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
		if (ImGui.begin("Player Utils", getDefaultFlags())) {
			Screen currentScreen = this.currentScreen;
			ImGuiEx.text("Classname:", () -> currentScreen == null ? null : currentScreen.getClass().getSimpleName());
			if (currentScreen != null) {
				GuiComponent exactWidgetAt = currentScreen.getExactWidgetAt((int) (Gdx.input.getX() / getGuiScale()), (int) (Gdx.input.getY() / getGuiScale()));
				if (exactWidgetAt != null) {
					this.shapes.setColor(1.0F, 0.0F, 1.0F, 1.0F);
					this.shapes.rectangle(
							exactWidgetAt.getX() * getGuiScale(), exactWidgetAt.getY() * getGuiScale(),
							exactWidgetAt.getWidth() * getGuiScale(), exactWidgetAt.getHeight() * getGuiScale()
					);
				}
				ImGuiEx.text("Widget:", () -> exactWidgetAt == null ? null : exactWidgetAt.getClass().getSimpleName());
			}
		}
		ImGui.end();
	}

	private void showUtils() {
//		Screen currentScreen = getCurrentScreen();
		ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
		ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
		if (ImGui.begin("Utils", getDefaultFlags())) {
			ImGui.button("Respawn");
			if (ImGui.isItemClicked()) {
				respawn();
			}
			ImGuiEx.slider("FOV", "GameFOV", (int) camera.fieldOfView, 10, 150, i -> camera.fieldOfView = i);
		}
		ImGui.end();
	}

	private int getDefaultFlags() {
		boolean cursorCaught = Gdx.input.isCursorCatched();
		var flags = ImGuiWindowFlags.None;
		if (cursorCaught) flags |= ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoInputs;
		return flags;
	}

	public void respawn() {
		if (this.player != null && this.world.getEntity(this.player.getId()) == player) {
			this.world.despawn(player);
		}

		int spawnX = 0;
		int spawnZ = 0;

		this.world.updateChunksForPlayer(spawnX, spawnZ);

		float spawnY = this.world.getHighest(spawnX, spawnZ) + 1;

		this.player = Entities.PLAYER.create(this.world);
		this.player.setHealth(this.player.getMaxHeath());
		this.player.setPosition(spawnX + 0.5f, spawnY, spawnZ + 0.5f);
		this.world.spawn(this.player);
	}

	@Override
	public void resize(int width, int height) {
		this.spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		this.camera.viewportWidth = width;
		this.camera.viewportHeight = height;
		this.camera.update();

		Screen cur = this.currentScreen;
		if (cur != null) {
			cur.resize(ceil(width / getGuiScale()), ceil(height / getGuiScale()));
		}
	}

	@Override
	public void dispose() {
		this.imGuiGl3.dispose();
		this.imGuiGlfw.dispose();
		ImGui.destroyContext();

		this.modelBatch.dispose();
		this.spriteBatch.dispose();
		this.font.dispose();
	}

	public long getWindowHandle() {
		return windowHandle;
	}

	public boolean isDevMode() {
		return isDevMode;
	}

	public boolean isShowingImGui() {
		return showImGui.get();
	}

	public void setShowingImGui(boolean value) {
		showImGui.set(value);
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

	public Texture getTilesTex() {
		return tilesTex;
	}

	public void startWorld() {
		this.showScreen(new WorldLoadScreen());
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

	public void runLater(Runnable task) {
		tasks.add(task);
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

	public void startBreaking() {
		HitResult hitResult = this.hitResult;
		if (hitResult == null) return;
		this.world.startBreaking(hitResult.getPos());
		this.breaking = hitResult.getPos();
		this.breakingBlock = hitResult.getBlock();
	}

	public void stopBreaking() {
		HitResult hitResult = this.hitResult;
		if (hitResult == null) return;
		this.world.stopBreaking(hitResult.getPos());
		this.breaking = null;
		this.breakingBlock = null;
	}
}
