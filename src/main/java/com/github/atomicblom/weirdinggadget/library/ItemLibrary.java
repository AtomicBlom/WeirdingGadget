package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

@SuppressWarnings("ConstantConditions")
@ObjectHolder(WeirdingGadgetMod.MOD_ID)
public class ItemLibrary
{
    @Nonnull
    public static final Item weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
