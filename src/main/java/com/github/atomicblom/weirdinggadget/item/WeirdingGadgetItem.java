package com.github.atomicblom.weirdinggadget.item;

import net.minecraft.block.Block;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;

public class WeirdingGadgetItem extends BlockItem implements IForgeItem {
    public WeirdingGadgetItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    @Override
    public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
        return EquipmentSlotType.HEAD;
    }
}
