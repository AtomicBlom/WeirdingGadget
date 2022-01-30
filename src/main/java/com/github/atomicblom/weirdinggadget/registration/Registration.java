package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.Reference;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.block.blockentity.WeirdingGadgetBlockEntity;
import com.github.atomicblom.weirdinggadget.client.WeirdingGadgetBlockEntityRenderer;
import com.github.atomicblom.weirdinggadget.item.WeirdingGadgetItem;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import com.github.atomicblom.weirdinggadget.library.BlockEntityTypeLibrary;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Registration
{
    @SubscribeEvent
    public static void registerBlocks(Register<Block> event) {
        final var registry = event.getRegistry();
        final var properties = BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_YELLOW)
                .strength(3.0f, 5.0f)
                .sound(SoundType.METAL);

        registry.register(new WeirdingGadgetBlock(properties).setRegistryName(Reference.Block.weirding_gadget));
    }

    @SubscribeEvent
    public static void registerBlockEntity(Register<BlockEntityType<?>> event) {
        final var registry = event.getRegistry();
        final var blockEntityType = BlockEntityType.Builder
                .of(WeirdingGadgetBlockEntity::new, BlockLibrary.weirding_gadget)
                .build( null)
                .setRegistryName(Reference.Block.weirding_gadget);
        registry.register(blockEntityType);
    }

    @SubscribeEvent
    public static void registerItems(Register<Item> event) {
        final var registry = event.getRegistry();
        registry.register(
                new WeirdingGadgetItem(
                        BlockLibrary.weirding_gadget,
                        new Item.Properties()
                                .tab(CreativeModeTab.TAB_MISC))

                    .setRegistryName(Reference.Block.weirding_gadget)
        );
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityTypeLibrary.weirding_gadget, WeirdingGadgetBlockEntityRenderer::new);

    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(CapabilityWeirdingGadgetTicketList.TicketListData.class);
    }
}

