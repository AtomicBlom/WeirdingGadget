package com.github.atomicblom.weirdinggadget;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Settings {
    public static class ChunkLoaderLimits {
        public final ForgeConfigSpec.IntValue maximumModTickets;
        public final ForgeConfigSpec.IntValue maximumPlayerTickets;
        public final ForgeConfigSpec.IntValue maximumChunksPerTicket;

        ChunkLoaderLimits(ForgeConfigSpec.Builder builder) {
            builder.comment("Limits for chunk loading")
                .push("chunkLoadingLimits");

            maximumModTickets = builder
                    .comment("Maximum ticket count for the mod. Zero disables chunkloading capabilities.")
                    .translation("weirdinggadget.configui.maximumModTickets")
                    .worldRestart()
                    .defineInRange("maximumModTickets",  200,0, Integer.MAX_VALUE);

            maximumPlayerTickets = builder
                    .comment("The number of tickets a player can be assigned instead of a mod. This is shared across all mods.")
                    .translation("weirdinggadget.configui.maximumPlayerTickets")
                    .worldRestart()
                    .defineInRange("maximumPlayerTickets", 500, 0, Integer.MAX_VALUE);

            maximumChunksPerTicket = builder
                    .comment()
                    .translation("weirdinggadget.configui.maximumChunksPerTicket")
                    .worldRestart()
                    .defineInRange("maximumChunksPerTicket", 25, 0, 15*15);
        }
    }

    public static class Server {
        public final ForgeConfigSpec.IntValue hoursBeforeDeactivation;
        public final ForgeConfigSpec.IntValue chunkLoaderWidth;
        public final ForgeConfigSpec.BooleanValue emergencyMode;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment("Server configuration settings")
                    .push("server");

            hoursBeforeDeactivation = builder
                    .comment("The number of hours the player will be offline before deactivating (default 2 days)")
                    .translation("weirdinggadget.configgui.hoursBeforeDeactivation")
                    .worldRestart()
                    .defineInRange("hoursBeforeDeactivation", 2 * 24, 1, 365 * 24); // 2 days by default.

            chunkLoaderWidth = builder
                    .comment("Width/length of chunks to be loaded, it is recommend this is an odd number (max 25)")
                    .translation("weirdinggadget.configgui.chunkLoaderWidth")
                    .worldRestart()
                    .defineInRange("chunkLoaderWidth", 3, 1, 25);

            emergencyMode = builder
                    .comment("Disables Chunk loading, use if there is a crash happening in a chunk loaded area")
                    .translation("weirdinggadget.configgui.emergencyMode")
                    .worldRestart()
                    .define("emergencymode", false);

            builder.pop();
        }
    }

    static final ForgeConfigSpec ServerSpec;
    public static final Server SERVER;

    static final ForgeConfigSpec LimitSpec;
    public static final ChunkLoaderLimits CHUNK_LOADER_LIMITS;
    static {
        final var specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        ServerSpec = specPair.getRight();
        SERVER = specPair.getLeft();

        final var limitSpecPair = new ForgeConfigSpec.Builder().configure(ChunkLoaderLimits::new);
        LimitSpec = limitSpecPair.getRight();
        CHUNK_LOADER_LIMITS = limitSpecPair.getLeft();
    }

}
