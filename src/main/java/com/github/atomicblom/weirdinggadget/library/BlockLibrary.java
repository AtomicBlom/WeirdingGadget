package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import net.minecraft.block.Block;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(WeirdingGadgetMod.MOD_ID)
public class BlockLibrary {
    public static final Block weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
