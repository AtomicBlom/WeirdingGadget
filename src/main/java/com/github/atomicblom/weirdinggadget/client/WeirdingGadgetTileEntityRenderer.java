package com.github.atomicblom.weirdinggadget.client;

import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.library.ItemLibrary;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;

public class WeirdingGadgetTileEntityRenderer extends TileEntityRenderer<WeirdingGadgetTileEntity> {
    private final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

    public WeirdingGadgetTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(WeirdingGadgetTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        World world = tileEntityIn.getWorld();
        if (world == null) return;

        RenderType type = RenderType.makeType("weirding_gadget", DefaultVertexFormats.BLOCK, 7, 2097152, true, false,
                RenderType.State.getBuilder()
                        .shadeModel(RenderState.SHADE_ENABLED)
                        .lightmap(RenderState.LIGHTMAP_ENABLED)
                        .texture(RenderState.BLOCK_SHEET_MIPPED)
                        .depthTest(RenderState.DEPTH_ALWAYS)
                        .build(false)
        );

        final ClientPlayerEntity player = Minecraft.getInstance().player;
        assert  player != null;

        float angle = 0;
        boolean highlightGadgets = player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem() == ItemLibrary.weirding_gadget;
        if (tileEntityIn.isActive()) {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }
        boolean renderBaseFirst = player.getPosY() + player.getEyeHeight() > tileEntityIn.getPos().getY() + 7.0f/16.0f;

        if (renderBaseFirst) {
            renderBase(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, type, highlightGadgets);
        }

        BlockModelRenderer.enableCache();
        matrixStackIn.push();
        matrixStackIn.translate(0.5f, 0, 0.5f);
        matrixStackIn.rotate(new Quaternion(Vector3f.YP, angle, true));
        matrixStackIn.translate(-0.5f, 0, -0.5f);
        renderSpinner(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, type, highlightGadgets);
        matrixStackIn.pop();

        if (!renderBaseFirst) {
            renderBase(tileEntityIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, type, highlightGadgets);
        }
    }

    private void renderSpinner(WeirdingGadgetTileEntity tileEntityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, RenderType type, boolean highlightGadgets) {
        final BlockState dynamicState = tileEntityIn.getBlockState()
                .with(WeirdingGadgetBlock.ACTIVE, true)
                .with(WeirdingGadgetBlock.RENDER, com.github.atomicblom.weirdinggadget.block.RenderType.DYNAMIC);
        if (highlightGadgets) {
            final IVertexBuilder buffer = bufferIn.getBuffer(type);

            final BlockModelRenderer blockModelRenderer = blockRenderer.getBlockModelRenderer();
            IBakedModel ibakedmodel = blockRenderer.getModelForState(dynamicState);

            blockModelRenderer.renderModel(matrixStackIn.getLast(), buffer, dynamicState, ibakedmodel, 0.5f, 0.5f, 1f, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        }
        blockRenderer.renderBlock(dynamicState, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
    }

    private void renderBase(WeirdingGadgetTileEntity tileEntityIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, RenderType type, boolean highlightGadgets) {
        matrixStackIn.push();
        matrixStackIn.scale(1.01f, 1.01f, 1.01f);
        final BlockState staticState = tileEntityIn.getBlockState()
                .with(WeirdingGadgetBlock.ACTIVE, true)
                .with(WeirdingGadgetBlock.RENDER, com.github.atomicblom.weirdinggadget.block.RenderType.STATIC);
        if (highlightGadgets) {

            //Render the base without the rotation
            final IVertexBuilder buffer = bufferIn.getBuffer(type);
            final BlockModelRenderer blockModelRenderer = blockRenderer.getBlockModelRenderer();


            IBakedModel ibakedmodel = blockRenderer.getModelForState(staticState);
            blockModelRenderer.renderModel(matrixStackIn.getLast(), buffer, staticState, ibakedmodel, 1, 1, 1, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);

        }
        blockRenderer.renderBlock(staticState, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE);
        matrixStackIn.pop();
    }
}
