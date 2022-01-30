package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantConditions")
@ObjectHolder(WeirdingGadgetMod.MOD_ID)
public class BlockLibrary {
    @Nonnull
    public static final Block weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
