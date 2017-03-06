package com.github.atomicblom.weirdinggadget;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Type;

@Config(modid = WeirdingGadgetMod.MODID, type = Type.INSTANCE, name=WeirdingGadgetMod.MODID)
public class Settings {
    @Comment("The number of hours the player will be offline before deactivating (default 2 days)")
    public static int hoursBeforeDeactivation = 2 * 24; // 2 days by default.

    @Comment("Width/length of chunks to be loaded, it is recommend this is an odd number")
    public static int chunkLoaderWidth = 3;
}
