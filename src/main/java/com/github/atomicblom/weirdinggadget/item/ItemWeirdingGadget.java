package com.github.atomicblom.weirdinggadget.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemWeirdingGadget extends ItemBlock {

    public ItemWeirdingGadget(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
