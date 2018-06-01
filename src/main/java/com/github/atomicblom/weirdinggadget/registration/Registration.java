package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.Reference;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.item.ItemWeirdingGadget;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

//@EventBusSubscriber(modid = WeirdingGadgetMod.MODID)
@EventBusSubscriber
public class Registration
{
    @SubscribeEvent
    public static void registerBlocks(Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(configure(new WeirdingGadgetBlock(), Reference.Block.weirding_gadget));
        GameRegistry.registerTileEntity(WeirdingGadgetTileEntity.class, Reference.Block.weirding_gadget);
    }

    @SubscribeEvent
    public static void registerItems(Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(configureBlockItem(new ItemWeirdingGadget(BlockLibrary.weirding_gadget)).setHasSubtypes(true));

        registerRecipes();
    }

    private static void registerRecipes()
    {
        /*GameRegistry.addRecipe(new ItemStack(BlockLibrary.weirding_gadget),
                new String[] {"ggg", "GeG", "GiG"},
                'g', Items.GOLD_INGOT,
                'G', Blocks.GOLD_BLOCK,
                'i', Items.IRON_INGOT,
                'e', Items.ENDER_EYE
                );
                */
    }

    static <B extends Block> B configure(B block, ResourceLocation registryName) {
        block.setRegistryName(registryName)
                .setTranslationKey(registryName.toString())
                .setCreativeTab(CreativeTabs.MISC);

        return block;
    }

    static <B extends ItemBlock> ItemBlock configureBlockItem(B itemBlock) {

        Block block = itemBlock.getBlock();
        itemBlock.setRegistryName(block.getRegistryName())
                .setTranslationKey(block.getTranslationKey())
                .setCreativeTab(CreativeTabs.MISC);

        return itemBlock;
    }
}
