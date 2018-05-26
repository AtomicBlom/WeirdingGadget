package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.client.opengex.OpenGEXModelLoader;
import com.github.atomicblom.weirdinggadget.client.rendering.WeirdingGadgetTESR;
import com.github.atomicblom.weirdinggadget.library.ItemLibrary;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class ClientRegistration
{
    @SubscribeEvent
    public static void onModelRegistryReady(ModelRegistryEvent event) {
        OpenGEXModelLoader.INSTANCE.addDomain(WeirdingGadgetMod.MODID);
        ModelLoaderRegistry.registerLoader(OpenGEXModelLoader.INSTANCE);

        ClientRegistry.bindTileEntitySpecialRenderer(WeirdingGadgetTileEntity.class, new WeirdingGadgetTESR());

        ModelLoader.setCustomModelResourceLocation(
                ItemLibrary.weirding_gadget,
                0,
                new ModelResourceLocation(ItemLibrary.weirding_gadget.getRegistryName(), "inventory,type=normal")
        );

        ModelLoader.setCustomModelResourceLocation(
                ItemLibrary.weirding_gadget,
                1,
                new ModelResourceLocation(ItemLibrary.weirding_gadget.getRegistryName(), "inventory,type=spot")
        );
    }
}
