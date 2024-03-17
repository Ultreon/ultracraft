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

/**
 * Register rendering for entities, blocks, etc.
 *
 * @since 0.1.0
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 */
public class RenderingRegistration {
    /**
     * Register rendering for entities and blocks
     * @param client the UltracraftClient instance
     * @param modelLoader the model loader
     */
    public static void registerRendering(UltracraftClient client, ModelLoader<?> modelLoader) {
        // Register block and entity models
        registerBlockModels();
        registerBlockEntityModels(client);
        registerEntityModels();

        // Register entity and block renderers
        registerEntityRenderers();
        registerBlockRenderers();
        registerBlockRenderTypes();

        // Iterate through all entity types and register their models and renderers
        for (var e : Registries.ENTITY_TYPE.entries()) {
            EntityType<?> type = e.getValue();
            EntityRenderer<?> renderer = RendererRegistry.get(type);
            EntityModel<?> entityModel = ModelRegistry.get(type);

            Identifier key = e.getKey().element();
            FileHandle handle = UltracraftClient.resource(key.mapPath(path -> "models/entity/" + path + ".g3dj"));

            // Load and register the model if it exists
            if (handle.exists()) {
                Model model = UltracraftClient.invokeAndWait(() -> modelLoader.loadModel(handle, fileName -> {
                    String filePath = fileName.substring(("assets/" + key.namespace() + "/models/entity/").length());
                    return new Texture(UltracraftClient.resource(key.mapPath(path -> "textures/entity/" + filePath)));
                }));
                if (model == null)
                    throw new RuntimeException("Failed to load entity model: " + key.mapPath(path -> "models/entity/" + path + ".g3dj"));
                // Set blending and alpha test attributes for the model materials
                model.materials.forEach(modelModel -> {
                    modelModel.set(new BlendingAttribute(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA));
                    modelModel.set(FloatAttribute.createAlphaTest(0.5f));
                });
                ModelRegistry.registerFinished(type, model);
            } else {
                // If the model does not exist, use the entity model and renderer
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

        // Load the renderer registry
        RendererRegistry.load();
    }

    /**
     * Registers block entity models.
     * @param client The UltracraftClient instance.
     */
    private static void registerBlockEntityModels(UltracraftClient client) {
        // Call the onRegister() method of the BLOCK_ENTITY_MODELS factory.
        ClientRegistrationEvents.BLOCK_ENTITY_MODELS.factory().onRegister();

        // Load block entity models using the client instance.
        BlockEntityModelRegistry.load(client);
    }

    /**
     * Registers block render types.
     */
    private static void registerBlockRenderTypes() {
        // Register the RenderType.WATER render type for the Blocks.WATER block.
        BlockRenderTypeRegistry.register(Blocks.WATER, RenderType.WATER);

        // Call the onRegister() method of the BLOCK_RENDER_TYPES factory.
        ClientRegistrationEvents.BLOCK_RENDER_TYPES.factory().onRegister();
    }

    /**
     * Registers block renderers.
     */
    private static void registerBlockRenderers() {
        // Call the onRegister() method of the BLOCK_RENDERERS factory.
        ClientRegistrationEvents.BLOCK_RENDERERS.factory().onRegister();
    }

    /**
     * Registers block models for various blocks in the game.
     */
    private static void registerBlockModels() {
        // Register block models for grass block, log, and crafting bench
        BlockModelRegistry.register(Blocks.GRASS_BLOCK, meta -> true, CubeModel.of(UltracraftClient.id("blocks/grass_top"), UltracraftClient.id("blocks/dirt"), UltracraftClient.id("blocks/grass_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.LOG, meta -> true , CubeModel.of(UltracraftClient.id("blocks/log"), UltracraftClient.id("blocks/log"), UltracraftClient.id("blocks/log_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));
        BlockModelRegistry.register(Blocks.CRAFTING_BENCH, meta -> true, CubeModel.of(UltracraftClient.id("blocks/crafting_bench_top"), UltracraftClient.id("blocks/crafting_bench_bottom"), UltracraftClient.id("blocks/crafting_bench_side"), ModelProperties.builder().top(FaceProperties.builder().randomRotation().build()).build()));

        // Register block models for switch test block based on meta data
        BlockModelRegistry.register(Blocks.META_SWITCH_TEST, meta -> meta.<Boolean>getEntry("on").value, CubeModel.of(UltracraftClient.id("blocks/switch_on")));
        BlockModelRegistry.register(Blocks.META_SWITCH_TEST, meta -> !meta.<Boolean>getEntry("on").value, CubeModel.of(UltracraftClient.id("blocks/switch_off")));

        // Register block models for blast furnace with different rotations based on metadata
        for (CubicDirection direction : CubicDirection.HORIZONTAL) {
            BlockModelRegistry.register(Blocks.BLAST_FURNACE, meta -> meta.<Boolean>getEntry("lit").value && meta.<CubicDirection>getEntry("facing").value == direction, CubeModel.of(UltracraftClient.id("blocks/blast_furnace_top"), UltracraftClient.id("blocks/blast_furnace_bottom"), UltracraftClient.id("blocks/blast_furnace_side"), UltracraftClient.id("blocks/blast_furnace_front_lit"), ModelProperties.builder().rotateHorizontal(direction).build()));
            BlockModelRegistry.register(Blocks.BLAST_FURNACE, meta -> !meta.<Boolean>getEntry("lit").value && meta.<CubicDirection>getEntry("facing").value == direction, CubeModel.of(UltracraftClient.id("blocks/blast_furnace_top"), UltracraftClient.id("blocks/blast_furnace_bottom"), UltracraftClient.id("blocks/blast_furnace_side"), UltracraftClient.id("blocks/blast_furnace_front"), ModelProperties.builder().rotateHorizontal(direction).build()));
        }

        // Trigger the block models factory registration event
        ClientRegistrationEvents.BLOCK_MODELS.factory().onRegister();

        // Register default block models for common blocks
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

    /**
     * Registers entity models.
     */
    private static void registerEntityModels() {
        // Call the onRegister method of the factory in ENTITY_MODELS
        ClientRegistrationEvents.ENTITY_MODELS.factory().onRegister();
    }

    /**
     * Registers entity renderers.
     */
    private static void registerEntityRenderers() {
        // Register the player entity renderer
        RendererRegistry.register(EntityTypes.PLAYER, PlayerRenderer::new);

        // Register the dropped item entity renderer
        RendererRegistry.register(EntityTypes.DROPPED_ITEM, DroppedItemRenderer::new);

        // Call the onRegister method of the factory in ENTITY_RENDERERS
        ClientRegistrationEvents.ENTITY_RENDERERS.factory().onRegister();
    }
}
