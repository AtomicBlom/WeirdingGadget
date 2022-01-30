package com.github.atomicblom.weirdinggadget.client;

import com.github.atomicblom.weirdinggadget.block.RenderType;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.function.Supplier;

public class WeirdingGadgetItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final Supplier<BlockEntityWithoutLevelRenderer> INSTANCE = Suppliers.memoize(
            () -> new WeirdingGadgetItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()
            )
    );
    public static final IItemRenderProperties USE_WEIRDING_GADGET_RENDER = new IItemRenderProperties()
    {
        @Override
        public BlockEntityWithoutLevelRenderer getItemStackRenderer()
        {
            return INSTANCE.get();
        }
    };

    public WeirdingGadgetItemRenderer(BlockEntityRenderDispatcher renderDispatcher, EntityModelSet entityModelSet) {
        super(renderDispatcher, entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, int combinedOverlay) {
        ModelBlockRenderer.enableCaching();
        poseStack.pushPose();
        float angle = 0;
        if (transformType == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND ||
                transformType == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND ||
                transformType == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND ||
                transformType == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND ||
                transformType == ItemTransforms.TransformType.HEAD
        ) {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();

        final var baseState = BlockLibrary.weirding_gadget.defaultBlockState()
                .setValue(WeirdingGadgetBlock.ACTIVE, true)
                .setValue(WeirdingGadgetBlock.RENDER, RenderType.STATIC);

        final var spinnerState = baseState
                .setValue(WeirdingGadgetBlock.RENDER, RenderType.DYNAMIC);

        blockRenderer.renderSingleBlock(baseState, poseStack, multiBufferSource, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(new Quaternion(Vector3f.YP, angle, true));
        poseStack.translate(-0.5f, 0, -0.5f);

        blockRenderer.renderSingleBlock(spinnerState, poseStack, multiBufferSource, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
        poseStack.popPose();

    }
}
