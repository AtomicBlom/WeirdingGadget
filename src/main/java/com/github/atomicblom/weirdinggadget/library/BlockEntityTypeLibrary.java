package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.blockentity.WeirdingGadgetBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantConditions")
@ObjectHolder(WeirdingGadgetMod.MOD_ID)
public class BlockEntityTypeLibrary {
    @Nonnull
    public static final BlockEntityType<WeirdingGadgetBlockEntity> weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
