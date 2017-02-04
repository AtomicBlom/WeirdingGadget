package com.github.atomicblom.weirdinggadget.client.rendering;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;
import org.lwjgl.opengl.GL11;

/**
 * Created by codew on 20/12/2016.
 */
class ReusableVertexBuffer extends VertexBuffer {
    private static final EnumFacing[] modelSources = {
            EnumFacing.UP,
            EnumFacing.DOWN,
            EnumFacing.NORTH,
            EnumFacing.SOUTH,
            EnumFacing.EAST,
            EnumFacing.WEST,
            null
    };

    public ReusableVertexBuffer(int bufferSizeIn) {
        super(bufferSizeIn);
    }

    @Override
    public void reset() {

    }

    public void writeModel(IBakedModel model, IBlockState state) {
        begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

        final int color = 0xffffffff;
        for (final EnumFacing value : modelSources) {
            for (final BakedQuad quad : model.getQuads(state, value, 0)) {
                LightUtil.renderQuadColor(this, quad, color);
            }
        }
        finishDrawing();
    }

}
