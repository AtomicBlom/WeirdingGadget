package com.github.atomicblom.weirdinggadget.client;

import com.github.atomicblom.weirdinggadget.block.RenderType;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WeirdingGadgetItemRenderer extends ItemStackTileEntityRenderer {
    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType p_239207_2_, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        BlockModelRenderer.enableCache();
        matrixStack.push();
        float angle = 0;
        if (p_239207_2_ == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND ||
                p_239207_2_ == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND ||
                p_239207_2_ == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND ||
                p_239207_2_ == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND ||
                p_239207_2_ == ItemCameraTransforms.TransformType.HEAD
        ) {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }

        BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();

        final BlockState baseState = BlockLibrary.weirding_gadget.getDefaultState()
                .with(WeirdingGadgetBlock.ACTIVE, true)
                .with(WeirdingGadgetBlock.RENDER, RenderType.STATIC);

        final BlockState spinnerState = baseState
                .with(WeirdingGadgetBlock.RENDER, RenderType.DYNAMIC);

        blockRenderer.renderBlock(baseState, matrixStack, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        matrixStack.translate(0.5f, 0, 0.5f);
        matrixStack.rotate(new Quaternion(Vector3f.YP, angle, true));
        matrixStack.translate(-0.5f, 0, -0.5f);

        blockRenderer.renderBlock(spinnerState, matrixStack, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
        matrixStack.pop();

    }
}
