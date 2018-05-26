package com.github.atomicblom.weirdinggadget.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import static net.minecraft.inventory.EntityEquipmentSlot.HEAD;

public class ItemWeirdingGadget extends ItemBlock
{
	public ItemWeirdingGadget(Block block)
	{
		super(block);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EntityEquipmentSlot armorType, Entity entity)
	{
		return armorType == HEAD;
	}
}
