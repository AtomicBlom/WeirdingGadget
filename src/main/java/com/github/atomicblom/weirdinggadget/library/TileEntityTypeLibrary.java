package com.github.atomicblom.weirdinggadget.library;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(WeirdingGadgetMod.MOD_ID)
public class TileEntityTypeLibrary {
    public static final BlockEntityType<WeirdingGadgetTileEntity> weirding_gadget;

    static {
        weirding_gadget = null;
    }
}
