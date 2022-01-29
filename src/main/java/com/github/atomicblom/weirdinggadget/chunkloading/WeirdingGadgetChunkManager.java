package com.github.atomicblom.weirdinggadget.chunkloading;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.registration.CapabilityWeirdingGadgetTicketList;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
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

    public static void releaseTicket(Level level, WeirdingGadgetTicket ticket) {
        if (!(level instanceof ServerLevel)) return;
        ServerLevel serverLevel = (ServerLevel)level;

        final ChunkSource chunkProvider = level.getChunkSource();
        if (!(chunkProvider instanceof ServerChunkCache)) throw new RuntimeException("Attempted to force a chunk on the wrong side");
        ServerChunkCache chunkManager = ((ServerChunkCache) chunkProvider);

        final CapabilityWeirdingGadgetTicketList.TicketListData capability = level.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA)
                .orElseThrow(() -> new RuntimeException("Missing Weirding Gadget Capability"));
        capability.removeTicket(ticket.getId());

        for (Map.Entry<Long, Ticket<ChunkPos>> vanillaTicket : ticket.getVanillaTickets()) {
            long chunkId = vanillaTicket.getKey();
            ChunkPos chunkPos = new ChunkPos(chunkId);

            WeirdingGadgetMod.LOGGER.debug(ChunkManager, "releasing ticket {} from chunk {} in {}", vanillaTicket.getValue(), level.dimension().getRegistryName(), chunkPos);
            chunkManager.distanceManager.removeTicket(chunkId, vanillaTicket.getValue());
            final SortedArraySet<Ticket<?>> ticketSet = chunkManager.distanceManager.getTickets(chunkId);
            if (ticketSet.size() == 0) {
                ForcedChunksSavedData forcedchunkssavedata = serverLevel.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
                WeirdingGadgetMod.LOGGER.debug(ChunkManager, "removing {} from forced chunks in {}", chunkPos, level.dimension().getRegistryName());
                boolean removed = forcedchunkssavedata.getChunks().remove(chunkId);
                forcedchunkssavedata.setDirty(removed);
            } else {
                WeirdingGadgetMod.LOGGER.debug(ChunkManager, "not removing {} from forced chunks in {} because there are {} tickets left", chunkPos, level.dimension().getRegistryName(), ticketSet.size());
            }
        }
    }

    @Nullable
    public static WeirdingGadgetTicket requestPlayerTicket(Object modInstance, String playerName, Level level, Type requestType) {
        Mod modAnnotation = modInstance.getClass().getAnnotation(Mod.class);
        String modName = modAnnotation.value();

        final CapabilityWeirdingGadgetTicketList.TicketListData capability = level.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA)
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

    public static void forceChunk(Level level, WeirdingGadgetTicket ticket, ChunkPos ticketChunk) {
        if (!(level instanceof ServerLevel)) return;
        ServerLevel serverLevel = (ServerLevel)level;

        final ChunkSource chunkProvider = level.getChunkSource();
        if (!(chunkProvider instanceof ServerChunkCache)) throw new RuntimeException("Attempted to force a chunk on the wrong side");
        ServerChunkCache chunkManager = ((ServerChunkCache) chunkProvider);

        if ((ticket.getChunkCount() + 1) > Settings.CHUNK_LOADER_LIMITS.maximumChunksPerTicket.get()) {
            WeirdingGadgetMod.LOGGER.warn("Could not allocate a chunk because the maximum number of tickets has been assigned to the ticket {}", ticket);
            return;
        }

        final CapabilityWeirdingGadgetTicketList.TicketListData capability = level.getCapability(CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA)
                .orElseThrow(() -> new RuntimeException("Missing Weirding Gadget Capability"));

        capability.addTicket(ticket);

        Ticket<ChunkPos> existingTicket = ticket.getOrCreateTicket(ticketChunk);

        ForcedChunksSavedData forcedchunkssavedata = serverLevel.getDataStorage().computeIfAbsent(ForcedChunksSavedData::load, ForcedChunksSavedData::new, "chunks");
        boolean added = forcedchunkssavedata.getChunks().add(ticketChunk.toLong());
        if (added) {
            WeirdingGadgetMod.LOGGER.debug(ChunkManager, "Added {} to the forced chunk list of {}", ticketChunk, level.dimension().getRegistryName());
            serverLevel.getChunk(ticketChunk.x, ticketChunk.z);
        }
        forcedchunkssavedata.setDirty(added);
        chunkManager.distanceManager.addTicket(ticketChunk.toLong(), existingTicket);
        WeirdingGadgetMod.LOGGER.debug(ChunkManager, "Registered {} to the ticket manager", existingTicket);
    }
}
