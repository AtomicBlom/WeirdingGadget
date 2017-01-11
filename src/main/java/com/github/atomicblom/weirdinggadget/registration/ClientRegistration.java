package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.library.ItemLibrary;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistration
{
    @SubscribeEvent
    public static void onModelRegistryReady(ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain(WeirdingGadgetMod.MODID);
        ModelLoaderRegistry.registerLoader(OBJLoader.INSTANCE);

        setItemModel(ItemLibrary.weirding_gadget);
    }

    private static void setItemModel(Item item)
    {
        ModelLoader.setCustomModelResourceLocation(
                item,
                0,
                new ModelResourceLocation(item.getRegistryName(), "inventory")
        );
    }}
