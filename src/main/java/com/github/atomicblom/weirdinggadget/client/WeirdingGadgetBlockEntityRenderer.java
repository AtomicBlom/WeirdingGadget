package com.github.atomicblom.weirdinggadget.client;

import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.block.blockentity.WeirdingGadgetBlockEntity;
import com.github.atomicblom.weirdinggadget.library.ItemLibrary;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WeirdingGadgetBlockEntityRenderer implements BlockEntityRenderer<WeirdingGadgetBlockEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public WeirdingGadgetBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(WeirdingGadgetBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay) {
        var level = blockEntity.getLevel();
        if (level == null) return;

        var type = RenderType.create("weirding_gadget", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false,
                RenderType.CompositeState.builder()
                        .setShaderState(RenderStateShard.RENDERTYPE_SOLID_SHADER)
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                        .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .createCompositeState(false)
        );

        final var player = Minecraft.getInstance().player;
        assert  player != null;

        float angle = 0;
        var highlightGadgets = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ItemLibrary.weirding_gadget;
        if (blockEntity.isActive()) {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }
        var renderBaseFirst = player.getY() + player.getEyeHeight() > blockEntity.getBlockPos().get(Direction.Axis.Y) + 7.0f/16.0f;

        if (renderBaseFirst) {
            renderBase(blockEntity, poseStack, multiBufferSource, combinedLight, combinedOverlay, type, highlightGadgets);
        }

        ModelBlockRenderer.enableCaching();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(new Quaternion(Vector3f.YP, angle, true));
        poseStack.translate(-0.5f, 0, -0.5f);
        renderSpinner(blockEntity, poseStack, multiBufferSource, combinedLight, combinedOverlay, type, highlightGadgets);
        poseStack.popPose();

        if (!renderBaseFirst) {
            renderBase(blockEntity, poseStack, multiBufferSource, combinedLight, combinedOverlay, type, highlightGadgets);
        }
    }

    private void renderSpinner(WeirdingGadgetBlockEntity blockEntityIn, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay, RenderType type, boolean highlightGadgets) {
        final var dynamicState = blockEntityIn.getBlockState()
                .setValue(WeirdingGadgetBlock.ACTIVE, true)
                .setValue(WeirdingGadgetBlock.RENDER, com.github.atomicblom.weirdinggadget.block.RenderType.DYNAMIC);
        if (highlightGadgets) {
            final var buffer = multiBufferSource.getBuffer(type);

            final var blockModelRenderer = blockRenderer.getModelRenderer();
            var blockModel = blockRenderer.getBlockModel(dynamicState);


            blockModelRenderer.renderModel(poseStack.last(), buffer, dynamicState, blockModel, 0.5f, 0.5f, 1f, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
        }

        blockRenderer.renderSingleBlock(dynamicState, poseStack, multiBufferSource, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
    }

    private void renderBase(WeirdingGadgetBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay, RenderType type, boolean highlightGadgets) {
        poseStack.pushPose();
        poseStack.scale(1.01f, 1.01f, 1.01f);
        final var staticState = blockEntity.getBlockState()
                .setValue(WeirdingGadgetBlock.ACTIVE, true)
                .setValue(WeirdingGadgetBlock.RENDER, com.github.atomicblom.weirdinggadget.block.RenderType.STATIC);
        if (highlightGadgets) {

            //Render the base without the rotation
            final var buffer = multiBufferSource.getBuffer(type);
            final var blockModelRenderer = blockRenderer.getModelRenderer();

            var blockModel = blockRenderer.getBlockModel(staticState);
            blockModelRenderer.renderModel(poseStack.last(), buffer, staticState, blockModel, 1, 1, 1, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        }
        blockRenderer.renderSingleBlock(staticState, poseStack, multiBufferSource, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
        poseStack.popPose();
    }
}
