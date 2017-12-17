package com.github.atomicblom.weirdinggadget.client.rendering;

import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class WeirdingGadgetTESR extends TileEntitySpecialRenderer<WeirdingGadgetTileEntity>
{
    private ReusableBufferBuilder vertexBuffer = null;
    private final WorldVertexBufferUploader vertexBufferUploader = new WorldVertexBufferUploader();

    @Override
    public void render(WeirdingGadgetTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
    {
        if (vertexBuffer == null)
        {
            final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
            IBlockState blockState = BlockLibrary.weirding_gadget.getDefaultState().withProperty(WeirdingGadgetBlock.RENDER_DYNAMIC, true);
            final IBakedModel model = blockRenderer.getModelForState(blockState);

            vertexBuffer = new ReusableBufferBuilder(2097152);
            vertexBuffer.writeModel(model, blockState);
        }
        float angle = 0;

        if (te.isActive())
        {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.pushMatrix();

        GlStateManager.translate(1, 0, 1);
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(-0.5f, 0, -0.5f);
        GlStateManager.rotate(angle, 0, 1, 0);
        GlStateManager.translate(-0.5f, 0, -0.5f);

        vertexBufferUploader.draw(vertexBuffer);
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }
}
