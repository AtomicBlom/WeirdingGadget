package com.github.atomicblom.weirdinggadget.item;

import com.github.atomicblom.weirdinggadget.client.WeirdingGadgetItemRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.function.Consumer;

public class WeirdingGadgetItem extends BlockItem implements IForgeItem {
    public WeirdingGadgetItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(WeirdingGadgetItemRenderer.USE_WEIRDING_GADGET_RENDER);
    }

    @Override
    public EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EquipmentSlot.HEAD;
    }
}
