package com.github.atomicblom.weirdinggadget.client;

import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
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

public class WeirdingGadgetTileEntityRenderer implements BlockEntityRenderer<WeirdingGadgetTileEntity> {
    private final BlockRenderDispatcher blockRenderer;
    private final BlockEntityRendererProvider.Context context;

    public WeirdingGadgetTileEntityRenderer(BlockEntityRendererProvider.Context context) {

        this.context = context;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(WeirdingGadgetTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        var level = tileEntityIn.getLevel();
        if (level == null) return;

        var type = RenderType.solid();

        final var player = Minecraft.getInstance().player;
        assert  player != null;

        float angle = 0;
        var highlightGadgets = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ItemLibrary.weirding_gadget;
        if (tileEntityIn.isActive()) {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }
        var renderBaseFirst = player.getY() + player.getEyeHeight() > tileEntityIn.getBlockPos().get(Direction.Axis.Y) + 7.0f/16.0f;

        if (renderBaseFirst) {
            renderBase(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, type, highlightGadgets);
        }

        ModelBlockRenderer.enableCaching();
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5f, 0, 0.5f);
        matrixStackIn.mulPose(new Quaternion(Vector3f.YP, angle, true));
        matrixStackIn.translate(-0.5f, 0, -0.5f);
        renderSpinner(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, type, highlightGadgets);
        matrixStackIn.popPose();

        if (!renderBaseFirst) {
            renderBase(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, type, highlightGadgets);
        }
    }

    private void renderSpinner(WeirdingGadgetTileEntity tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, RenderType type, boolean highlightGadgets) {
        final var dynamicState = tileEntityIn.getBlockState()
                .setValue(WeirdingGadgetBlock.ACTIVE, true)
                .setValue(WeirdingGadgetBlock.RENDER, com.github.atomicblom.weirdinggadget.block.RenderType.DYNAMIC);
        if (highlightGadgets) {
            final var buffer = bufferIn.getBuffer(type);

            final var blockModelRenderer = blockRenderer.getModelRenderer();
            var ibakedmodel = blockRenderer.getBlockModel(dynamicState);


            blockModelRenderer.renderModel(matrixStackIn.last(), buffer, dynamicState, ibakedmodel, 0.5f, 0.5f, 1f, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        }

        blockRenderer.renderSingleBlock(dynamicState, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
    }

    private void renderBase(WeirdingGadgetTileEntity tileEntityIn, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, RenderType type, boolean highlightGadgets) {
        matrixStackIn.pushPose();
        matrixStackIn.scale(1.01f, 1.01f, 1.01f);
        final var staticState = tileEntityIn.getBlockState()
                .setValue(WeirdingGadgetBlock.ACTIVE, true)
                .setValue(WeirdingGadgetBlock.RENDER, com.github.atomicblom.weirdinggadget.block.RenderType.STATIC);
        if (highlightGadgets) {

            //Render the base without the rotation
            final var buffer = bufferIn.getBuffer(type);
            final var blockModelRenderer = blockRenderer.getModelRenderer();


            var ibakedmodel = blockRenderer.getBlockModel(staticState);
            blockModelRenderer.renderModel(matrixStackIn.last(), buffer, staticState, ibakedmodel, 1, 1, 1, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

        }
        blockRenderer.renderSingleBlock(staticState, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.popPose();
    }
}
