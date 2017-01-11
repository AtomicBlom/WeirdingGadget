package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@SuppressWarnings("Duplicates")
@ObjectHolder(WeirdingGadgetMod.MODID)
public class ItemLibrary
{
    public static final Item weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
