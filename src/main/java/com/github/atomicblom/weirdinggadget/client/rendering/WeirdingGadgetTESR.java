package com.github.atomicblom.weirdinggadget.client.rendering;

import com.github.atomicblom.weirdinggadget.block.TileEntity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.client.opengex.OpenGEXAnimationFrameProperty;
import com.github.atomicblom.weirdinggadget.client.opengex.OpenGEXModelInstance;
import com.github.atomicblom.weirdinggadget.client.opengex.OpenGEXState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.opengl.GL11;

/**
 * Created by codew on 5/11/2015.
 */
public class WeirdingGadgetTESR extends TileEntitySpecialRenderer<WeirdingGadgetTileEntity>
{
    @Override
    public void renderTileEntityAt(WeirdingGadgetTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
    {
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBlockState blockState = te.getWorld().getBlockState(te.getPos());
        BlockPos blockpos = te.getPos();
        final OpenGEXState openGEXState = new OpenGEXState(null, getWorld().getTotalWorldTime() / 20.0f * 1.5f);
        blockState = blockState.withProperty(WeirdingGadgetBlock.RENDER_DYNAMIC, true);
        IBakedModel model = blockRenderer.getModelForState(blockState);
        blockState = ((IExtendedBlockState)blockState).withProperty(OpenGEXAnimationFrameProperty.instance, openGEXState);

        if (model instanceof OpenGEXModelInstance) {
            model = ((OpenGEXModelInstance)model).getAnimatedModel(blockState);
        }



        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexBuffer = tessellator.getBuffer();
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();

        if (Minecraft.isAmbientOcclusionEnabled())
        {
            GlStateManager.shadeModel(7425);
        }
        else
        {
            GlStateManager.shadeModel(7424);
        }

        vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        vertexBuffer.setTranslation(x - blockpos.getX(), y - blockpos.getY(), z - blockpos.getZ());
        vertexBuffer.color(255, 255, 255, 255);

        blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, blockState, te.getPos(), vertexBuffer, true);

        vertexBuffer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
    }
}
