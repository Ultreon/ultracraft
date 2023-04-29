package com.ultreon.craft;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
		Identifier.setDefaultNamespace("ultreoncraft");
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
		float camX = world.voxelsX / 2f;
		float camZ = world.voxelsZ / 2f;
		float camY = world.getHighest(1, 1) + 2.5f;
		camera.position.set(.5f, camY, .5f);
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
        controller.update();


        if (controller.isKeyDown(Input.Keys.F3)) {
            world.regen();
        }

        spriteBatch.begin();
        Gdx.graphics.setTitle("Ultreon Craft - " + Gdx.graphics.getFramesPerSecond() + " fps");

        font.draw(spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + world.renderedChunks + "/"
                + world.numChunks, 0, 20);
        font.draw(spriteBatch, "x: " + (int) camera.position.x + ", y: " + (int) camera.position.y + ", z: "
                + (int) camera.position.z, 0, 40);
        font.draw(spriteBatch, "chunk shown: " + (world.get(camera.position.x, camera.position.y, camera.position.z) != null), 0, 60);
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
