package com.github.atomicblom.weirdinggadget.client;

import com.github.atomicblom.weirdinggadget.block.RenderType;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
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
    public void renderByItem(ItemStack stack, ItemTransforms.TransformType p_239207_2_, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ModelBlockRenderer.enableCaching();
        matrixStack.pushPose();
        float angle = 0;
        if (p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND ||
                p_239207_2_ == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND ||
                p_239207_2_ == ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND ||
                p_239207_2_ == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND ||
                p_239207_2_ == ItemTransforms.TransformType.HEAD
        ) {
            angle = System.currentTimeMillis() % (360 * 4) / 4.0f;
        }

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();

        final var baseState = BlockLibrary.weirding_gadget.defaultBlockState()
                .setValue(WeirdingGadgetBlock.ACTIVE, true)
                .setValue(WeirdingGadgetBlock.RENDER, RenderType.STATIC);

        final var spinnerState = baseState
                .setValue(WeirdingGadgetBlock.RENDER, RenderType.DYNAMIC);

        blockRenderer.renderSingleBlock(baseState, matrixStack, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);

        matrixStack.translate(0.5f, 0, 0.5f);
        matrixStack.mulPose(new Quaternion(Vector3f.YP, angle, true));
        matrixStack.translate(-0.5f, 0, -0.5f);

        blockRenderer.renderSingleBlock(spinnerState, matrixStack, buffer, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
        matrixStack.popPose();

    }
}
