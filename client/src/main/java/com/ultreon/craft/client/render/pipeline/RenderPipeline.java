package com.ultreon.craft.client.render.pipeline;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.TextureBinder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.*;
import com.ultreon.craft.client.UltracraftClient;
import com.ultreon.craft.client.input.GameCamera;
import com.ultreon.craft.client.world.ClientWorld;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.jetbrains.annotations.ApiStatus;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.zip.Deflater;

import static com.badlogic.gdx.Gdx.gl;
import static com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder.LRU;

public class RenderPipeline implements Disposable {
    private final Array<RenderNode> nodes = new Array<>();
    private final RenderNode main;
    private final GameCamera camera;


    public RenderPipeline(RenderNode main, GameCamera camera) {
        this.main = main;
        this.camera = camera;
    }

    public RenderPipeline node(RenderNode node) {
        this.nodes.add(node);
        return this;
    }

    public void render(ModelBatch modelBatch) {
        ClientWorld world = UltracraftClient.get().world;
        if (world != null) {
            ScreenUtils.clear(world.getSkyColor().toGdx(), true);
        } else {
            ScreenUtils.clear(0F, 0F, 0F, 1F, true);
        }

        var input = new Array<Renderable>();
        var textures = new ObjectMap<String, Texture>();
        for (var node : this.nodes) {
            if (node.requiresModel()) {
                input = this.modelRender(modelBatch, node, input, textures);
            } else {
                input = this.plainRender(modelBatch, node, input, textures);
            }
        }

        this.main.render(textures, modelBatch, this.camera, input);

        for (var node : this.nodes) {
            node.flush();
        }

        textures.clear();
    }

    private Array<Renderable> modelRender(ModelBatch modelBatch, RenderNode node, Array<Renderable> input, ObjectMap<String, Texture> textures) {
        FrameBuffer frameBuffer = node.getFrameBuffer();
        frameBuffer.begin();
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(this.camera);
        node.textureBinder.begin();
        node.time += Gdx.graphics.getDeltaTime();
        input = node.render(textures, modelBatch, this.camera, input);
        try {
            modelBatch.end();
        } catch (Exception e) {
            throw new GdxRuntimeException("Failed to render node: " + node.getClass().getSimpleName() + "\n" + node.dump(), e);
        }
        node.textureBinder.end();

        RenderPipeline.capture(node);

        frameBuffer.end();
        return input;
    }

    private Array<Renderable> plainRender(ModelBatch modelBatch, RenderNode node, Array<Renderable> input, ObjectMap<String, Texture> textures) {
        FrameBuffer frameBuffer = node.getFrameBuffer();
        frameBuffer.begin();
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        node.textureBinder.begin();
        node.time += Gdx.graphics.getDeltaTime();
        input = node.render(textures, modelBatch, this.camera, input);
        node.textureBinder.end();

        RenderPipeline.capture(node);

        frameBuffer.end();
        return input;
    }

    private static void capture(RenderNode node) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F8)) {
            Pixmap screenshot = Pixmap.createFromFrameBuffer(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            PixmapIO.writePNG(Gdx.files.local("FBO_" + node.getClass().getSimpleName() + ".png"), screenshot, Deflater.DEFAULT_COMPRESSION, true);
            try (var stream = new PrintStream(Gdx.files.local("INFO_" + node.getClass().getSimpleName() + ".txt").write(false))) {
                node.dumpInfo(stream);
            }
        }
    }

    public void resize(int width, int height) {
        for (var node : this.nodes) {
            node.resize(width, height);
        }

        this.main.resize(width, height);
    }

    @Override
    public void dispose() {
        for (var node : this.nodes) {
            node.dispose();
        }

        this.main.dispose();
        this.nodes.clear();
    }

    public abstract static class RenderNode {
        protected static final Matrix4 IDENTITY_MATRIX = new Matrix4();
        protected final TextureBinder textureBinder = new DefaultTextureBinder(LRU);
        private float time = 0;
        private final FlushablePool<Renderable> pool = new FlushablePool<>() {
            @Override
            protected Renderable newObject() {
                return new Renderable();
            }

            @Override
            public Renderable obtain() {
                Renderable obtain = super.obtain();
                obtain.shader = null;
                obtain.bones = null;
                obtain.material = null;
                obtain.userData = null;
                obtain.worldTransform.idt();

                return obtain;
            }
        };

        private FrameBuffer fbo = new FrameBuffer(this.getFormat(), Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        protected final UltracraftClient client = UltracraftClient.get();

        private Pixmap.Format getFormat() {
            return Pixmap.Format.RGBA8888;
        }

        public abstract @NewInstance Array<Renderable> render(ObjectMap<String, Texture> textures, ModelBatch modelBatch, GameCamera camera, Array<Renderable> input);

        public void resize(int width, int height) {
            this.fbo.dispose();
            this.fbo = new FrameBuffer(this.getFormat(), width, height, true);
        }

        protected Pool<Renderable> pool() {
            return this.pool;
        }

        public FrameBuffer getFrameBuffer() {
            return this.fbo;
        }

        public void dispose() {
            this.fbo.dispose();
        }

        public void flush() {
            this.pool.flush();
        }

        @ApiStatus.Internal
        public void dumpInfo(PrintStream stream) {

        }

        public String dump() {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (PrintStream printStream = new PrintStream(stream)) {
                this.dumpInfo(printStream);
            }
            return stream.toString();
        }

        public float getTime() {
            return this.time;
        }

        public boolean requiresModel() {
            return false;
        }
    }
}


