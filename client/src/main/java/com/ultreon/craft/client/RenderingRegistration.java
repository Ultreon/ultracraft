package com.ultreon.craft.client;

import com.ultreon.craft.block.Blocks;
import com.ultreon.craft.client.api.events.ClientRegistrationEvents;
import com.ultreon.craft.client.model.block.BlockModelRegistry;
import com.ultreon.craft.client.model.block.CubeModel;
import com.ultreon.craft.client.model.block.ModelProperties;
import com.ultreon.craft.client.model.entity.renderer.DroppedItemRenderer;
import com.ultreon.craft.client.model.entity.renderer.PlayerRenderer;
import com.ultreon.craft.client.model.entity.renderer.SomethingRenderer;
import com.ultreon.craft.client.registry.BlockEntityModelRegistry;
import com.ultreon.craft.client.registry.BlockRenderTypeRegistry;
import com.ultreon.craft.client.registry.EntityModelManager;
import com.ultreon.craft.client.registry.EntityRendererManager;
import com.ultreon.craft.client.render.RenderType;
import com.ultreon.craft.client.world.FaceProperties;
import com.ultreon.craft.entity.EntityTypes;
import com.ultreon.craft.world.CubicDirection;

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
     */
    public static void registerRendering(UltracraftClient client) {
        // Register block and entity models
        registerBlockModels();
        registerBlockEntityModels(client);

        // Register block renderers
        registerBlockRenderers();
        registerBlockRenderTypes();

        registerEntityRenderers();
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

        // Register block models for switch test block based on metadata
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
    public static void registerEntityRendering(EntityModelManager entityModelManager) {
    }

    /**
     * Registers entity renderers.
     */
    public static void registerEntityRenderers() {
        // Register the player entity renderer
        EntityRendererManager.register(EntityTypes.PLAYER, PlayerRenderer::new);

        // Register the dropped item entity renderer
        EntityRendererManager.register(EntityTypes.DROPPED_ITEM, DroppedItemRenderer::new);
        EntityRendererManager.register(EntityTypes.SOMETHING, SomethingRenderer::new);

        // Call the onRegister method of the factory in ENTITY_RENDERERS
        ClientRegistrationEvents.ENTITY_RENDERERS.factory().onRegister();
    }
}
