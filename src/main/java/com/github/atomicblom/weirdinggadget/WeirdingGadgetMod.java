package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.github.atomicblom.weirdinggadget.client.ForceISTERModel;
import com.github.atomicblom.weirdinggadget.client.WeirdingGadgetTileEntityRenderer;
import com.github.atomicblom.weirdinggadget.library.TileEntityTypeLibrary;
import com.github.atomicblom.weirdinggadget.registration.CapabilityWeirdingGadgetTicketList;
import com.google.common.collect.ListMultimap;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Mod(WeirdingGadgetMod.MOD_ID)
public class WeirdingGadgetMod
{
    public static final String MOD_ID = "weirdinggadget";
    public static int MULTIPLIER = 20 * 60 * 60; //Hours in ticks

    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static WeirdingGadgetMod instance = null;
    public static boolean IS_CI_BUILD = false;

    public WeirdingGadgetMod() {
        if (Boolean.getBoolean("@IS_CI_BUILD@")) {
            IS_CI_BUILD = true;
            MULTIPLIER = 20 * 60 * 60; //Protect against me being an idiot
        }

        instance = this;

        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::modelRegistryEvent);
        MinecraftForge.EVENT_BUS.addGenericListener(World.class, this::attachWorldCapabilities);
        MinecraftForge.EVENT_BUS.addListener(this::worldLoaded);

        final ModLoadingContext modContext = ModLoadingContext.get();
        modContext.registerConfig(ModConfig.Type.COMMON, Settings.ServerSpec, "WeirdingGadget.toml");
        modContext.registerConfig(ModConfig.Type.COMMON, Settings.LimitSpec, "WeirdingGadgetLimits.toml");
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        CapabilityWeirdingGadgetTicketList.register();
    }

    private void modelRegistryEvent(final ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(Reference.ItemLoader, new ForceISTERModel.Loader());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(TileEntityTypeLibrary.weirding_gadget, WeirdingGadgetTileEntityRenderer::new);
    }

    public void worldLoaded(WorldEvent.Load worldLoadEvent) {
        IWorld world = worldLoadEvent.getWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = ((ServerWorld) world);
            serverWorld.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA).ifPresent(ticketList -> {
                Map<String, List<WeirdingGadgetTicket>> playerTickets = ticketList.getAllTickets()
                        .stream()
                        .collect(Collectors.groupingBy(WeirdingGadgetTicket::getPlayerName));

                final ListMultimap<String, WeirdingGadgetTicket> usedTickets = ChunkManagerCallback.playerTicketsLoaded(playerTickets, serverWorld);

                Set<UUID> knownTickets = ticketList.getAllTickets().stream().map(WeirdingGadgetTicket::getId).collect(Collectors.toSet());
                for (WeirdingGadgetTicket weirdingGadgetTicket : new ArrayList<>(usedTickets.values())) {
                    knownTickets.remove(weirdingGadgetTicket.getId());
                }

                for (UUID removedTicket : knownTickets) {
                    ticketList.removeTicket(removedTicket);
                }

                ChunkManagerCallback.ticketsLoaded(ticketList.getAllTickets(), serverWorld);
            });
        }
    }

    public void attachWorldCapabilities(AttachCapabilitiesEvent<World> worldCapabilities) {
        worldCapabilities.addCapability(Reference.Capability.weirding_gadget, new CapabilityWeirdingGadgetTicketList.WeirdingGadgetTicketListProvider());
    }
}
