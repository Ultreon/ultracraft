package com.ultreon.craft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
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
import com.ultreon.craft.camera.CameraController;
import com.ultreon.craft.registry.Registries;
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
	public BitmapFont font;
	public CameraController controller;
	public World world;
	private static UltreonCraft instance;
	private SpriteBatch spriteBatch;
	private ModelBatch modelBatch;
	private PerspectiveCamera camera;
	private Environment lights;
	private final boolean isDevMode;
	private final ImBoolean showImGui = new ImBoolean(false);
	private long windowHandle;
	private final ImFloat imGuiPosX = new ImFloat();
	private final ImFloat imGuiPosY = new ImFloat();
	private final ImFloat imGuiPosZ = new ImFloat();

	public UltreonCraft(String[] args) {
		Identifier.setDefaultNamespace("ultreoncraft");
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
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		DefaultShader.Config config = new DefaultShader.Config();
		config.defaultCullFace = GL20.GL_FRONT;
		modelBatch = new ModelBatch(new DefaultShaderProvider(config));
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.near = 0.01f;
		camera.far = 1000;
		controller = new CameraController(camera);
		Gdx.input.setInputProcessor(controller);

		lights = new Environment();
		lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		lights.add(new DirectionalLight().set(.5f, .5f, .5f, -0.4f, -1, -0.2f));
		lights.add(new DirectionalLight().set(.5f, .5f, .5f, 0.4f, -1, 0.2f));

		Registries.init();
		Bullet.init(true);

		Blocks.register();
		NoiseSettingsInit.register();

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
		world = new World(texture, 16, 1, 16);
//		PerlinNoiseGenerator.generateVoxels(world, 0, 63, 10);
		world.generateWorld();

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
        ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1f, true);
        modelBatch.begin(camera);
        modelBatch.render(world, lights);
        modelBatch.end();

		camera.position.x = imGuiPosX.get();
		camera.position.y = imGuiPosY.get();
		camera.position.z = imGuiPosZ.get();

        controller.update();

		imGuiPosX.set(camera.position.x);
		imGuiPosY.set(camera.position.y);
		imGuiPosZ.set(camera.position.z);

        if (controller.isKeyDown(Input.Keys.F9)) {
            world.regen();
        }

        spriteBatch.begin();
        Gdx.graphics.setTitle("Ultreon Craft - " + Gdx.graphics.getFramesPerSecond() + " fps");

        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + world.renderedChunks + "/"
                + world.numChunks, 0, 20);
        font.draw(spriteBatch, "x: " + (int) camera.position.x + ", y: " + (int) camera.position.y + ", z: "
                + (int) camera.position.z, 0, 40);
        font.draw(spriteBatch, "chunk shown: " + (world.get(camera.position.x, camera.position.y, camera.position.z) != null), 0, 60);

		if (showImGui.get()) {
			// render 3D scene
			imGuiGlfw.newFrame();

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

					ImGui.text("Frames Per Second: " + Gdx.graphics.getFramesPerSecond() + "  Frames ID: " + Gdx.graphics.getFrameId());
					ImGui.endMenuBar();
				}
				ImGui.end();
			}

			if (SHOW_INFO_WINDOW.get()) showInfoWindow();
			if (SHOW_UTILS.get()) showUtils();

			ImGui.render();
			imGuiGl3.renderDrawData(ImGui.getDrawData());
		}

        spriteBatch.end();
    }

	private void showInfoWindow() {
//		Screen currentScreen = getCurrentScreen();
		ImGui.setNextWindowSize(400, 200, ImGuiCond.Once);
		ImGui.setNextWindowPos(ImGui.getMainViewport().getPosX() + 100, ImGui.getMainViewport().getPosY() + 100, ImGuiCond.Once);
		if (ImGui.begin("Debug Info", getDefaultFlags())) {
			ImGui.text("X:");
			ImGui.sameLine();
			ImGui.inputFloat("##PlayerX", imGuiPosX);
			ImGui.text("Y:");
			ImGui.sameLine();
			ImGui.inputFloat("##PlayerY", imGuiPosY);
			ImGui.text("Z:");
			ImGui.sameLine();
			ImGui.inputFloat("##PlayerZ", imGuiPosZ);

			ImGui.text("Rot: " + camera.direction);
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
		float camX = world.voxelsX / 2f;
		float camZ = world.voxelsZ / 2f;
		float camY = world.getHighest(1, 1) + 2.5f;
		camera.position.set(.5f, camY, .5f);
		imGuiPosX.set(camera.position.x);
		imGuiPosY.set(camera.position.y);
		imGuiPosZ.set(camera.position.z);
	}

	@Override
	public void resize(int width, int height) {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}

	@Override
	public void dispose() {
		imGuiGl3.dispose();
		imGuiGlfw.dispose();
		ImGui.destroyContext();

		modelBatch.dispose();
		spriteBatch.dispose();
		font.dispose();
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
