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
import com.ultreon.craft.input.GameCamera;
import com.ultreon.craft.input.InputManager;
import com.ultreon.craft.entity.Entities;
import com.ultreon.craft.entity.Player;
import com.ultreon.craft.events.WorldEvents;
import com.ultreon.craft.input.PlayerInput;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.world.World;
import com.ultreon.craft.world.gen.noise.NoiseSettingsInit;
import com.ultreon.libs.commons.v0.Identifier;
import com.ultreon.libs.registries.v0.Registry;
import com.ultreon.libs.registries.v0.event.RegistryEvents;

public class UltreonCraft extends ApplicationAdapter {
	public static final String NAMESPACE = "ultreoncraft";
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

	public UltreonCraft() {
		Identifier.setDefaultNamespace(NAMESPACE);
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
		this.input = new InputManager(camera);
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

		float spawnX = this.world.voxelsX / 2f;
		float spawnZ = this.world.voxelsZ / 2f;
		float spawnY = this.world.getHighest(1, 1) + 2.5f;

		this.player = Entities.PLAYER.spawn(this.world);
		this.player.setPosition(spawnX + 0.5f, spawnY, spawnZ + 0.5f);
	}

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


        if (InputManager.isKeyDown(Input.Keys.F3)) {
            world.regen();
        }

        this.spriteBatch.begin();
        Gdx.graphics.setTitle("Ultreon Craft - " + Gdx.graphics.getFramesPerSecond() + " fps");

        this.font.draw(this.spriteBatch, "fps: " + Gdx.graphics.getFramesPerSecond() + ", #visible chunks: " + this.world.renderedChunks + "/"
                + this.world.numChunks, 0, 20);
        this.font.draw(this.spriteBatch, "x: " + (int) this.camera.position.x + ", y: " + (int) this.camera.position.y + ", z: "
                + (int) this.camera.position.z, 0, 40);
        this.font.draw(this.spriteBatch, "chunk shown: " + (this.world.get(this.camera.position.x, this.camera.position.y, this.camera.position.z) != null), 0, 60);
        this.spriteBatch.end();
    }

	public void tick() {
		this.playerInput.tick();

		WorldEvents.PRE_TICK.factory().onPreTick(this.world);
		this.world.tick();
		WorldEvents.POST_TICK.factory().onPostTick(this.world);

		this.camera.update(this.player);
		this.input.update();
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
		this.modelBatch.dispose();
		this.spriteBatch.dispose();
		this.font.dispose();
	}
}
