package com.github.atomicblom.weirdinggadget;

import com.google.common.collect.Lists;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryManager;

import java.util.List;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = WeirdingGadgetMod.MODID, type = Type.INSTANCE, name=WeirdingGadgetMod.MODID)
public class Settings {
    @Ignore
    private final static String config = WeirdingGadgetMod.MODID + ".config.";

    @LangKey(config + "hours_before_deactivation")
    @Comment("The number of hours the player will be offline before deactivating (default 2 days)")
    public static int hoursBeforeDeactivation = 2 * 24; // 2 days by default.

    @LangKey(config + "chunk_loader_width")
    @Comment("Width/length of chunks to be loaded, it is recommend this is an odd number")
    public static int chunkLoaderWidth = 3;

    @LangKey(config + "emergency_mode")
    @Comment("Disables Chunk loading, use if there is a crash happening in a chunk loaded area")
    public static boolean emergencyMode = false;

    @LangKey(config + "enable_fuel")
    @Comment("Enable requiring fuel to run the chunk loader. This does not replace the requirement that players log in. default: disabled")
    public static boolean enableFuel = false;

    @LangKey(config + "override_fuel_list")
    @Comment("Clears any fuels defined by other mods and uses the fuel definitions setting to override the list, otherwise the list is additive")
    public static boolean overrideFuelList = false;

    @LangKey(config + "fuel_definitions")
    @Comment("definitions for fuel in the format {modid}:{itemname}@{metadata}+{time}{timeunit}. time unit can be either in minutes(m), hours(h), or days(d)")
    public static String[] fuelDefinitions = new String[] {
            "minecraft:nether_star+5d",
            "minecraft:stick+2m"
    };


    private static List<WeirdingGadgetFuel> settingsConfiguredFuels = Lists.newArrayList();
    private static List<WeirdingGadgetFuel> modConfiguredFuels = Lists.newArrayList();
    private static List<WeirdingGadgetFuel> runtimeFuelList;
    public static void addModConfiguredFuel(WeirdingGadgetFuel fuel) {
        modConfiguredFuels.add(fuel);
    }
    public static Iterable<WeirdingGadgetFuel> getFuelList() {
        regenerateFuelList();
        return runtimeFuelList;
    }

    public static void regenerateFuelList() {
        short WILDCARD = 32767;
        runtimeFuelList = Lists.newArrayList();
        settingsConfiguredFuels = Lists.newArrayList();
        for (String fuelDefinition : fuelDefinitions) {
            try {
                WeirdingGadgetFuel e = WeirdingGadgetFuel.fromConfig(fuelDefinition);
                ForgeRegistry<Item> registry = RegistryManager.ACTIVE.getRegistry(GameData.ITEMS);
                boolean itemExists = registry.containsKey(new ResourceLocation(e.domain, e.item));
                if (!itemExists) {
                    Logger.info("Warning: Item %s could not be resolved", fuelDefinition);
                } else {
                    settingsConfiguredFuels.add(e);
                }
            } catch (Exception e) {
                Logger.info("Unable to parse fuel %s: %s", fuelDefinition, e.getMessage());
            }
        }
        runtimeFuelList.addAll(settingsConfiguredFuels);
        if (!overrideFuelList) {
            // might need to worry about collisions
            for (WeirdingGadgetFuel modConfiguredFuel : modConfiguredFuels) {
                boolean addFuel = true;
                for (WeirdingGadgetFuel settingsConfiguredFuel : settingsConfiguredFuels) {
                    boolean domainMatches = modConfiguredFuel.getDomain().equals(settingsConfiguredFuel.getDomain());
                    boolean itemMatches = modConfiguredFuel.getItem().equals(settingsConfiguredFuel.getItem());
                    boolean metaMatches = settingsConfiguredFuel.getMetadata() == WILDCARD ||
                            modConfiguredFuel.getMetadata() == settingsConfiguredFuel.getMetadata();
                    if (domainMatches && itemMatches && metaMatches) {
                        addFuel = false;
                        break;
                    }
                }

                if (addFuel) {
                    runtimeFuelList.add(modConfiguredFuel);
                }
            }
        }
    }
}
