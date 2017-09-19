package com.github.atomicblom.weirdinggadget;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = WeirdingGadgetMod.MODID, version = WeirdingGadgetMod.VERSION, dependencies = "required-after:forge@[14.22.0.2447,)")
public class WeirdingGadgetMod
{
    public static final String MODID = "weirdinggadget";
    public static final String VERSION = "1.0";
    public static final int MULTIPLIER = 60 * 60 * 20; //Hours in ticks

    @Instance
    public static WeirdingGadgetMod INSTANCE;

    @EventHandler void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, new ChunkManagerCallback());
    }
}
