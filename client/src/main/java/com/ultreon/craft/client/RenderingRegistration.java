package com.ultreon.craft.client;

import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.api.events.ClientRegistrationEvents;
import com.ultreon.craft.client.model.block.BlockModelRegistry;
import com.ultreon.craft.client.model.block.CubeModel;
import com.ultreon.craft.client.model.block.ModelProperties;
import com.ultreon.craft.client.model.entity.EntityModel;
import com.ultreon.craft.client.model.entity.renderer.DroppedItemRenderer;
import com.ultreon.craft.client.model.entity.renderer.EntityRenderer;
import com.ultreon.craft.client.model.entity.renderer.PlayerRenderer;
import com.ultreon.craft.client.registry.BlockEntityModelRegistry;
import com.ultreon.craft.client.registry.BlockRenderTypeRegistry;
import com.ultreon.craft.client.registry.ModelRegistry;
import com.ultreon.craft.client.registry.RendererRegistry;
import com.ultreon.craft.client.render.RenderType;
import com.ultreon.craft.client.world.FaceProperties;
import com.ultreon.craft.entity.EntityType;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.registry.Registries;
import com.ultreon.craft.util.Identifier;
import com.ultreon.craft.world.CubicDirection;

import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;

public class RenderingRegistration {
    public static void registerRendering(UltracraftClient client, ModelLoader<?> modelLoader) {
        registerBlockModels();
        registerBlockEntityModels(client);
        registerEntityModels();
        registerEntityRenderers();
        registerBlockRenderers();
        registerBlockRenderTypes();

        for (var e : Registries.ENTITY_TYPE.entries()) {
            EntityType<?> type = e.getValue();
            EntityRenderer<?> renderer = RendererRegistry.get(type);
            EntityModel<?> entityModel = ModelRegistry.get(type);

            Identifier key = e.getKey().element();
            FileHandle handle = UltracraftClient.resource(key.mapPath(path -> "models/entity/" + path + ".g3dj"));
            if (handle.exists()) {
                Model model = UltracraftClient.invokeAndWait(() -> modelLoader.loadModel(handle, fileName -> {
                    String filePath = fileName.substring(("assets/" + key.namespace() + "/models/entity/").length());
                    return new Texture(UltracraftClient.resource(key.mapPath(path -> "textures/entity/" + filePath)));
                }));
                if (model == null)
                    throw new RuntimeException("Failed to load entity model: " + key.mapPath(path -> "models/entity/" + path + ".g3dj"));
                model.materials.forEach(modelModel -> {
                    modelModel.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
                    modelModel.set(FloatAttribute.createAlphaTest(0.5f));
                });
                ModelRegistry.registerFinished(type, model);
            } else {
                if (entityModel == null) {
                    UltracraftClient.LOGGER.warn("Model not found for entity {}", type.getId());
                    continue;
                }

                if (renderer == null) {
                    UltracraftClient.LOGGER.warn("Renderer not found for entity {}", type.getId());
                    continue;
                }

                ModelRegistry.registerFinished(type, entityModel.finish(renderer.getTextures()));
            }
        }

        RendererRegistry.load();
    }

    private static void registerBlockEntityModels(UltracraftClient client) {
        ClientRegistrationEvents.BLOCK_ENTITY_MODELS.factory().onRegister();

        BlockEntityModelRegistry.load(client);
    }

    private static void registerBlockRenderTypes() {
        BlockRenderTypeRegistry.register(Blocks.WATER, RenderType.WATER);

        ClientRegistrationEvents.BLOCK_RENDER_TYPES.factory().onRegister();
    }

    private static void registerBlockRenderers() {
        ClientRegistrationEvents.BLOCK_RENDERERS.factory().onRegister();
    }

    private static void registerBlockModels() {
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, meta -> true, CubeModel.of(UltracraftClient.id("blocks/grass_top"), UltracraftClient.id("blocks/dirt"), UltracraftClient.id("blocks/grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.LOG, meta -> true , CubeModel.of(UltracraftClient.id("blocks/log"), UltracraftClient.id("blocks/log"), UltracraftClient.id("blocks/log_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.CRAFTING_BENCH, meta -> true, CubeModel.of(UltracraftClient.id("blocks/crafting_bench_top"), UltracraftClient.id("blocks/crafting_bench_bottom"), UltracraftClient.id("blocks/crafting_bench_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));

        BlockModelRegistry.register(Blocks.META_SWITCH_TEST, meta -> meta.<Boolean>getEntry("on").value, CubeModel.of(UltracraftClient.id("blocks/switch_on")));
        BlockModelRegistry.register(Blocks.META_SWITCH_TEST, meta -> !meta.<Boolean>getEntry("on").value, CubeModel.of(UltracraftClient.id("blocks/switch_off")));

        for (CubicDirection direction : CubicDirection.HORIZONTAL) {
            BlockModelRegistry.register(Blocks.BLAST_FURNACE, meta -> meta.<Boolean>getEntry("lit").value && meta.<CubicDirection>getEntry("facing").value == direction, CubeModel.of(UltracraftClient.id("blocks/blast_furnace_top"), UltracraftClient.id("blocks/blast_furnace_bottom"), UltracraftClient.id("blocks/blast_furnace_side"), UltracraftClient.id("blocks/blast_furnace_front_lit"), ModelProperties.builder().rotateHorizontal(direction).build()));
            BlockModelRegistry.register(Blocks.BLAST_FURNACE, meta -> !meta.<Boolean>getEntry("lit").value && meta.<CubicDirection>getEntry("facing").value == direction, CubeModel.of(UltracraftClient.id("blocks/blast_furnace_top"), UltracraftClient.id("blocks/blast_furnace_bottom"), UltracraftClient.id("blocks/blast_furnace_side"), UltracraftClient.id("blocks/blast_furnace_front"), ModelProperties.builder().rotateHorizontal(direction).build()));
        }

        ClientRegistrationEvents.BLOCK_MODELS.factory().onRegister();

        BlockModelRegistry.registerDefault(Blocks.VOIDGUARD);
        BlockModelRegistry.registerDefault(Blocks.ERROR);
        BlockModelRegistry.registerDefault(Blocks.DIRT);
        BlockModelRegistry.registerDefault(Blocks.SAND);
        BlockModelRegistry.registerDefault(Blocks.SANDSTONE);
        BlockModelRegistry.registerDefault(Blocks.GRAVEL);
        BlockModelRegistry.registerDefault(Blocks.WATER);
        BlockModelRegistry.registerDefault(Blocks.STONE);
        BlockModelRegistry.registerDefault(Blocks.LEAVES);
        BlockModelRegistry.registerDefault(Blocks.PLANKS);
        BlockModelRegistry.registerDefault(Blocks.COBBLESTONE);
        BlockModelRegistry.registerDefault(Blocks.TALL_GRASS);
    }

    private static void registerEntityModels() {
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }

    private static void registerEntityRenderers() {
        RendererRegistry.register(EntityTypes.PLAYER, PlayerRenderer::new);
        RendererRegistry.register(EntityTypes.DROPPED_ITEM, DroppedItemRenderer::new);

        ClientRegistrationEvents.ENTITY_RENDERERS.factory().onRegister();
    }
}
