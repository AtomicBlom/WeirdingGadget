package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.Reference;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.client.WeirdingGadgetItemRenderer;
import com.github.atomicblom.weirdinggadget.client.WeirdingGadgetTileEntityRenderer;
import com.github.atomicblom.weirdinggadget.item.WeirdingGadgetItem;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Registration
{
    @SubscribeEvent
    public static void registerBlocks(Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        final AbstractBlock.Properties properties = AbstractBlock.Properties.create(Material.IRON, MaterialColor.YELLOW)
                .hardnessAndResistance(3.0f, 5.0f)
                .sound(SoundType.METAL)
                .harvestTool(ToolType.AXE);

        registry.register(new WeirdingGadgetBlock(properties).setRegistryName(Reference.Block.weirding_gadget));
    }

    @SubscribeEvent
    public static void registerTileEntity(Register<TileEntityType<?>> event) {
        final IForgeRegistry<TileEntityType<?>> registry = event.getRegistry();
        final TileEntityType<?> tileEntityType = TileEntityType.Builder
                .create(WeirdingGadgetTileEntity::new, BlockLibrary.weirding_gadget)
                .build(null)
                .setRegistryName(Reference.Block.weirding_gadget);
        registry.register(tileEntityType);
    }

    @SubscribeEvent
    public static void registerItems(Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(
                new WeirdingGadgetItem(
                        BlockLibrary.weirding_gadget,
                        new Item.Properties()
                                .group(ItemGroup.MISC)
                                .setISTER(() -> WeirdingGadgetItemRenderer::new))

                    .setRegistryName(Reference.Block.weirding_gadget)
        );
    }
}

