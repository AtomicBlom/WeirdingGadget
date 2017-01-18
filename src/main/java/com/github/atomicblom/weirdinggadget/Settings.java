package com.github.atomicblom.weirdinggadget;

import net.minecraftforge.common.config.Config;

@Config(modid = WeirdingGadgetMod.MODID, type = Config.Type.INSTANCE, name=WeirdingGadgetMod.MODID)
public class Settings {
    @Config.Comment("The number of hours the player will be offline before deactivating")
    public static final int hoursBeforeDeactivation = 7 * 24; // 7 days by default.

    @Config.Comment("Width/length of chunks to be loaded, it is recommend this is an odd number")
    public static final int chunkLoaderWidth = 3;
}
