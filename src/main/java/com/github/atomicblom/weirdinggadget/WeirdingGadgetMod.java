package com.github.atomicblom.weirdinggadget;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mod(modid = WeirdingGadgetMod.MODID, version = WeirdingGadgetMod.VERSION, dependencies = "required-after:forge@[14.22.0.2447,)")
public class WeirdingGadgetMod
{
    public static final String MODID = "weirdinggadget";
    public static final String VERSION = "1.0";
    public static final int MULTIPLIER = 60 * 60 * 20; //Hours in ticks
    private static final int DATA_FIXER_VERSION = 1;


    @Instance
    public static WeirdingGadgetMod INSTANCE;

    @EventHandler void init(FMLInitializationEvent event) {
        final ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(MODID, DATA_FIXER_VERSION);
        fixes.registerFix(FixTypes.BLOCK_ENTITY, new IFixableData() {
            // array only needs to cover legacy tile entity ids, no need to add future tile entity ids to list.
            private final Map<String, String> tileEntityNames;

            {
                final Builder<String, String> nameMap = ImmutableMap.builder();

                nameMap.put("tile.weirdinggadget:weirding_gadget", "weirdinggadget:weirding_gadget");

                tileEntityNames = nameMap.build();
            }

            @Override
            public int getFixVersion() {
                return 1;
            }

            @Override
            public NBTTagCompound fixTagCompound(NBTTagCompound compound) {
                final String tileEntityLocation = compound.getString("id");

                compound.setString("id", tileEntityNames.getOrDefault(tileEntityLocation, tileEntityLocation));

                return compound;
            }
        });
    }

    @EventHandler
    public void receiveIMC(IMCEvent event) {
        for (final IMCMessage message : event.getMessages())
        {
            String fuelDefinition = "";
            try {
                if ("registerFuel".equals(message.key)) {
                    if (message.isNBTMessage()) {
                        final NBTTagCompound value = message.getNBTValue();
                        final ItemStack itemStack = new ItemStack(value);
                        final int hours = value.getInteger("hours");

                        final Item item = itemStack.getItem();
                        final ResourceLocation registryName = item.getRegistryName();

                        assert registryName != null;


                        final WeirdingGadgetFuel fuel = new WeirdingGadgetFuel(registryName.getResourceDomain(), registryName.getResourcePath(), itemStack.getMetadata(), hours);
                        fuelDefinition = fuel.toString();
                        Settings.addModConfiguredFuel(fuel);
                    } else if (message.isStringMessage()) {
                        fuelDefinition = message.getStringValue();
                        final WeirdingGadgetFuel fuel = WeirdingGadgetFuel.fromConfig(fuelDefinition);
                        Settings.addModConfiguredFuel(fuel);
                    }
                }
            } catch (final Exception e) {
                Logger.info("IMC: Unable to parse fuel %s: %s", fuelDefinition, e.getMessage());
            }
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, new ChunkManagerCallback());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        Settings.regenerateFuelList();
    }
}
