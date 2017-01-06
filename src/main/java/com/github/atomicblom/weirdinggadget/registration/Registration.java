package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.Reference;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.TileEntity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistry;

@EventBusSubscriber(modid = WeirdingGadgetMod.MODID)
public class Registration
{
    @SubscribeEvent
    public static void registerBlocks(Register<Block> blockRegister) {
        final IForgeRegistry<Block> registry = blockRegister.getRegistry();
        registry.register(configure(new WeirdingGadgetBlock(), Reference.Block.weirding_gadget));
        GameRegistry.registerTileEntity(WeirdingGadgetTileEntity.class, "tile." + Reference.Block.weirding_gadget);
    }

    static <B extends Block> B configure(B block, ResourceLocation registryName) {
        block.setRegistryName(registryName)
                .setUnlocalizedName(registryName.toString())
                .setCreativeTab(CreativeTabs.MISC);

        return block;
    }

    static <B extends Item> B configure(B block) {

        block.setRegistryName(registryName)
                .setUnlocalizedName(registryName.toString())
                .setCreativeTab(CreativeTabs.MISC);

        return block;
    }

    @SubscribeEvent
    public static void registerItems(Register<Item> event) {

    }
}
