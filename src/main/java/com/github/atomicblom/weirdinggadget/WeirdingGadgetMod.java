package com.github.atomicblom.weirdinggadget;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
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
    public void receiveIMC(FMLInterModComms.IMCEvent event) {
        for (final FMLInterModComms.IMCMessage message : event.getMessages())
        {
            String fuelDefinition = "";
            try {
                if ("registerFuel".equals(message.key)) {
                    if (message.isNBTMessage()) {
                        NBTTagCompound value = message.getNBTValue();
                        ItemStack itemStack = new ItemStack(value);
                        int hours = value.getInteger("hours");

                        Item item = itemStack.getItem();
                        ResourceLocation registryName = item.getRegistryName();

                        assert registryName != null;


                        WeirdingGadgetFuel fuel = new WeirdingGadgetFuel(registryName.getResourceDomain(), registryName.getResourcePath(), itemStack.getMetadata(), hours);
                        fuelDefinition = fuel.toString();
                        Settings.addModConfiguredFuel(fuel);
                    } else if (message.isStringMessage()) {
                        fuelDefinition = message.getStringValue();
                        WeirdingGadgetFuel fuel = WeirdingGadgetFuel.fromConfig(fuelDefinition);
                        Settings.addModConfiguredFuel(fuel);
                    }
                }
            } catch (Exception e) {
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
