package com.ultreon.craft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.ultreon.craft.block.Block;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.camera.CameraController;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.registry.Registry;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;

public class UltreonCraft extends ApplicationAdapter {
	public static final String NAMESPACE = "craft";
	public BitmapFont font;
	public CameraController controller;
	public World world;
	private static UltreonCraft instance;
	private SpriteBatch spriteBatch;
	private ModelBatch modelBatch;
	private PerspectiveCamera camera;
	private Environment lights;

	public UltreonCraft() {
		instance = this;
	}

	public static UltreonCraft get() {
		return instance;
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

		Blocks.register();
		NoiseSettingsInit.register();

		Registry.postEvents();
		Registry.freezeAll();

		Texture texture = new Texture(Gdx.files.internal("data/g3d/tiles.png"));

		for (Block block : Registries.BLOCK.getValues()) {
			if (block == null) {
				break;
			}
			block.bake(texture);
		}

		MathUtils.random.setSeed(0);
		world = new World(texture, 8, 1, 8);
//		PerlinNoiseGenerator.generateVoxels(world, 0, 63, 10);
		world.generateWorld();

//		world.chunks
		float camX = world.voxelsX / 2f;
		float camZ = world.voxelsZ / 2f;
		float camY = world.getHighest(camX, camZ) + 1.5f;
		camera.position.set(camX, camY, camZ);
	}

	@Override
	public void render() {
		ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1f, true);
		modelBatch.begin(camera);
		modelBatch.render(world, lights);
		modelBatch.end();
		controller.update();

		spriteBatch.begin();
		font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + world.renderedChunks + "/"
				+ world.numChunks, 0, 20);
		spriteBatch.end();
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
		modelBatch.dispose();
		spriteBatch.dispose();
		font.dispose();
	}
}
