package com.github.atomicblom.weirdinggadget.chunkloading;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.registration.CapabilityWeirdingGadgetTicketList;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ForcedChunksSaveData;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.Ticket;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WeirdingGadgetChunkManager {
    static Marker ChunkManager = new MarkerManager.Log4jMarker("WGChunkManager");

    public static void releaseTicket(World world, WeirdingGadgetTicket ticket) {
        if (!(world instanceof ServerWorld)) return;
        ServerWorld serverWorld = (ServerWorld)world;

        final AbstractChunkProvider chunkProvider = world.getChunkProvider();
        if (!(chunkProvider instanceof ServerChunkProvider)) throw new RuntimeException("Attempted to force a chunk on the wrong side");
        ServerChunkProvider chunkManager = ((ServerChunkProvider) chunkProvider);

        final CapabilityWeirdingGadgetTicketList.TicketListData capability = world.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA)
                .orElseThrow(() -> new RuntimeException("Missing Weirding Gadget Capability"));
        capability.removeTicket(ticket.getId());

        for (Map.Entry<Long, Ticket<ChunkPos>> vanillaTicket : ticket.getVanillaTickets()) {
            long chunkId = vanillaTicket.getKey();
            ChunkPos chunkPos = new ChunkPos(chunkId);

            WeirdingGadgetMod.LOGGER.debug(ChunkManager, "releasing ticket {} from chunk {} in {}", vanillaTicket.getValue(), world.getDimensionKey().func_240901_a_(), chunkPos);
            chunkManager.ticketManager.release(chunkId, vanillaTicket.getValue());
            final SortedArraySet<Ticket<?>> ticketSet = chunkManager.ticketManager.getTicketSet(chunkId);
            if (ticketSet.size() == 0) {
                ForcedChunksSaveData forcedchunkssavedata = serverWorld.getSavedData().getOrCreate(ForcedChunksSaveData::new, "chunks");
                WeirdingGadgetMod.LOGGER.debug(ChunkManager, "removing {} from forced chunks in {}", chunkPos, world.getDimensionKey().func_240901_a_());
                boolean removed = forcedchunkssavedata.getChunks().remove(chunkId);
                forcedchunkssavedata.setDirty(removed);
            } else {
                WeirdingGadgetMod.LOGGER.debug(ChunkManager, "not removing {} from forced chunks in {} because there are {} tickets left", chunkPos, world.getDimensionKey().func_240901_a_(), ticketSet.size());
            }
        }
    }

    @Nullable
    public static WeirdingGadgetTicket requestPlayerTicket(Object modInstance, String playerName, World world, Type requestType) {
        Mod modAnnotation = modInstance.getClass().getAnnotation(Mod.class);
        String modName = modAnnotation.value();

        final CapabilityWeirdingGadgetTicketList.TicketListData capability = world.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA)
                .orElseThrow(() -> new RuntimeException("Missing Weirding Gadget Capability"));

        //Verify that mod has enough ticket quota available
        int maximumModTickets = Settings.CHUNK_LOADER_LIMITS.maximumModTickets.get(); //Default
        //TODO: override mod if needed from config.

        if ((capability.getTotalModTicketCount(modName) + 1) == maximumModTickets) {
            WeirdingGadgetMod.LOGGER.warn("Could not allocate a ticket to the mod {} because it has reached it's allotted maximum number of tickets {}", modName, maximumModTickets);
            return null;
        }

        //Verify that player has enough ticket quota available
        final int maximumPlayerTickets = Settings.CHUNK_LOADER_LIMITS.maximumPlayerTickets.get(); //Default
        //TODO: override mod if needed from config.

        if ((capability.getTotalPlayerTicketCount(playerName) + 1) == maximumPlayerTickets) {
            WeirdingGadgetMod.LOGGER.warn("Could not allocate a ticket to the player {} because they have already requested the maximum number of tickets {}", playerName, maximumPlayerTickets);
            return null;
        }

        return new WeirdingGadgetTicket(playerName, modName);
    }

    public static void forceChunk(World world, WeirdingGadgetTicket ticket, ChunkPos ticketChunk) {
        if (!(world instanceof ServerWorld)) return;
        ServerWorld serverWorld = (ServerWorld)world;

        final AbstractChunkProvider chunkProvider = world.getChunkProvider();
        if (!(chunkProvider instanceof ServerChunkProvider)) throw new RuntimeException("Attempted to force a chunk on the wrong side");
        ServerChunkProvider chunkManager = ((ServerChunkProvider) chunkProvider);

        if ((ticket.getChunkCount() + 1) > Settings.CHUNK_LOADER_LIMITS.maximumChunksPerTicket.get()) {
            WeirdingGadgetMod.LOGGER.warn("Could not allocate a chunk because the maximum number of tickets has been assigned to the ticket {}", ticket);
            return;
        }

        final CapabilityWeirdingGadgetTicketList.TicketListData capability = world.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA)
                .orElseThrow(() -> new RuntimeException("Missing Weirding Gadget Capability"));

        capability.addTicket(ticket);

        Ticket<ChunkPos> existingTicket = ticket.getOrCreateTicket(ticketChunk);

        ForcedChunksSaveData forcedchunkssavedata = serverWorld.getSavedData().getOrCreate(ForcedChunksSaveData::new, "chunks");
        boolean added = forcedchunkssavedata.getChunks().add(ticketChunk.asLong());
        if (added) {
            WeirdingGadgetMod.LOGGER.debug(ChunkManager, "Added {} to the forced chunk list of {}", ticketChunk, world.getDimensionKey().func_240901_a_());
            serverWorld.getChunk(ticketChunk.x, ticketChunk.z);
        }
        forcedchunkssavedata.setDirty(added);
        chunkManager.ticketManager.register(ticketChunk.asLong(), existingTicket);
        WeirdingGadgetMod.LOGGER.debug(ChunkManager, "Registered {} to the ticket manager", existingTicket);
    }
}
