package com.ultreon.craft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.utils.ScreenUtils;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.input.GameCamera;
import com.ultreon.craft.input.InputManager;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.ImGuiEx;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;
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

import java.util.List;

public class UltreonCraft extends ApplicationAdapter {
	public static final String NAMESPACE = "craft";
	private static final ImBoolean SHOW_INFO_WINDOW = new ImBoolean(false);
	private static final ImBoolean SHOW_UTILS = new ImBoolean(false);
	private final ImGuiImplGlfw imGuiGlfw;
	private final ImGuiImplGl3 imGuiGl3;
	public static final int TPS = 20;
	public BitmapFont font;
	public InputManager input;
	public World world;
	private static UltreonCraft instance;
	public Player player;
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
		this.spriteBatch = new SpriteBatch();
		this.font = new BitmapFont();
		DefaultShader.Config config = new DefaultShader.Config();
		config.defaultCullFace = GL20.GL_FRONT;
		this.modelBatch = new ModelBatch(new DefaultShaderProvider(config));
		this.camera = new GameCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.camera.near = 0.01f;
		this.camera.far = 1000;
		this.input = new InputManager(this, camera);
		Gdx.input.setInputProcessor(input);

		this.env = new Environment();
		this.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		this.env.add(new DirectionalLight().set(.5f, .5f, .5f, -0.4f, -1, -0.2f));
		this.env.add(new DirectionalLight().set(.5f, .5f, .5f, 0.4f, -1, 0.2f));

		Registries.nopInit();
		Bullet.init(true);

		Blocks.nopInit();
		NoiseSettingsInit.nopInit();
		Entities.nopInit();

		for (var registry : Registry.getRegistries()) {
			RegistryEvents.AUTO_REGISTER.factory().onAutoRegister(registry);
		}
		Registry.freeze();

		Texture texture = new Texture(Gdx.files.internal("data/g3d/tiles.png"));

		for (Block block : Registries.BLOCK.values()) {
			if (block == null) {
				break;
			}
			block.bake(texture);
		}

		MathUtils.random.setSeed(0);
		this.world = new World(texture, 16, 1, 16);
		this.world.generateWorld();

//		world.chunks
		respawn();

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

//	List<float[]> meshes = new ArrayList<float[]>();
//
//	{
//		ArrayList<Object> objects = new ArrayList<>();
//		world.getRenderables(objects, world.)
//		for (var mesh : world.meshArray) {
//			meshes.add(mesh.getVerticesBuffer().array());
//		}
//		meshes.add(verticesBody); meshes.add(verticesWings); meshes.add(verticesRudder)
//	}
//
//	public boolean intersectRayMeshes(Ray ray, List<float[]> meshes, Vector3 globalIntersection) {
//
//// presets
//		boolean intersectionOccured = false;
//		var localIntersection = camera.position;
//
//// for all Meshes in List
//		for (float[] mesh : meshes) {
//			if (Intersector.intersectRayTriangles(ray, mesh, localIntersection)) {
//				intersectionOccured = true;
//				Log.out("Intersection Occured!");
//				// update globalIntersection only if
//				// it is closer to the screen as the
//				// intersection point we got earlier
//				// and there was an intersection yet at all
//				if (globalIntersection != null) {
//					Log.out("Local intersection occured!");
//					if (ray.start.sub(localIntersection).len() < ray.start.sub(globalIntersection).len()) {
//						Log.out("updated global intersection");
//						globalIntersection.set(localIntersection);
//					}
//				} else {
//					Log.out("First time setting global intersection!");
//					globalIntersection.set(localIntersection);
//				}
//			}
//		}
//
//		if (intersectionOccured) {
//			return true;
//		} else {
//			return false;
//		}
//	}

    @Override
    public void render() {
		final var tickTime = 1f / TPS;

		float deltaTime = Gdx.graphics.getDeltaTime();
		this.timeUntilNextTick -= deltaTime;
		if (this.timeUntilNextTick < 0) {
			this.timeUntilNextTick = tickTime;

			tick();
		}

		ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1f, true);
        this.modelBatch.begin(this.camera);
        this.modelBatch.render(this.world, this.env);
        this.modelBatch.end();

        this.input.update();

        if (InputManager.isKeyDown(Input.Keys.F9)) {
            world.regen();
        }

        this.spriteBatch.begin();
        Gdx.graphics.setTitle("Ultreon Craft - " + Gdx.graphics.getFramesPerSecond() + " fps");

        this.font.draw(this.spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + this.world.renderedChunks + "/"
                + this.world.numChunks, 0, 20);
        this.font.draw(this.spriteBatch, "x: " + (int) this.camera.position.x + ", y: " + (int) this.camera.position.y + ", z: "
                + (int) this.camera.position.z, 0, 40);
        this.font.draw(this.spriteBatch, "chunk shown: " + (this.world.get(this.camera.position.x, this.camera.position.y, this.camera.position.z) != null), 0, 60);

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
						ImGui.menuItem("Show Info Window", null, SHOW_INFO_WINDOW);
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

			if (SHOW_INFO_WINDOW.get()) showPlayerUtilsWindow();
			if (SHOW_UTILS.get()) showUtils();

			ImGui.render();
			this.imGuiGl3.renderDrawData(ImGui.getDrawData());
		}

        this.spriteBatch.end();
    }

	public void tick() {
		WorldEvents.PRE_TICK.factory().onPreTick(this.world);
		this.world.tick();
		WorldEvents.POST_TICK.factory().onPostTick(this.world);

		this.camera.update(this.player);
		this.input.update();
	}

	private void showPlayerUtilsWindow() {
//		Screen currentScreen = getCurrentScreen();
		ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
		ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
		if (ImGui.begin("Player Utils", getDefaultFlags())) {
			ImGuiEx.text("Id:", () -> this.player.getId());
			ImGuiEx.editFloat("Speed:", "PlayerSpeed", this.player.getSpeed(), v -> this.player.setSpeed(v));
			ImGuiEx.editFloat("Running Speed:", "PlayerRunningSpeed", this.player.getRunningSpeed(), v -> this.player.setRunningSpeed(v));

			if (ImGui.collapsingHeader("Position")) {
				ImGui.treePush();
				ImGuiEx.editFloat("X:", "PlayerX", this.player.getX(), v -> this.player.setX(v));
				ImGuiEx.editFloat("Y:", "PlayerY", this.player.getY(), v -> this.player.setY(v));
				ImGuiEx.editFloat("Z:", "PlayerZ", this.player.getZ(), v -> this.player.setZ(v));
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

	private void respawn() {
		if (this.player != null && this.world.getEntity(this.player.getId()) == player) {
			this.world.despawn(player);
		}

		float spawnX = this.world.voxelsX / 2f;
		float spawnZ = this.world.voxelsZ / 2f;
		float spawnY = this.world.getHighest(1, 1) + 1;

		this.player = Entities.PLAYER.create(this.world);
		this.player.setPosition(spawnX + 0.5f, spawnY, spawnZ + 0.5f);
		this.world.spawn(this.player);
	}

	@Override
	public void resize(int width, int height) {
		this.spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		this.camera.viewportWidth = width;
		this.camera.viewportHeight = height;
		this.camera.update();
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
}
