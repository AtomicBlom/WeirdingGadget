package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@SuppressWarnings("Duplicates")
@ObjectHolder(WeirdingGadgetMod.MODID)
public class BlockLibrary {
    public static final Block weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
