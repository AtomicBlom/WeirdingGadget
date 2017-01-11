package com.github.atomicblom.weirdinggadget;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = WeirdingGadgetMod.MODID, version = WeirdingGadgetMod.VERSION)
public class WeirdingGadgetMod
{
    public static final String MODID = "weirdinggadget";
    public static final String VERSION = "1.0";

    @Mod.Instance
    public static WeirdingGadgetMod INSTANCE;

    public static final PlayerTicketManager ticketManager = new PlayerTicketManager();

    @EventHandler void init(FMLInitializationEvent event) {

    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, new WGLoadingCallback());
    }
}
