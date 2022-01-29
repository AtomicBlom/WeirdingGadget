package com.github.atomicblom.weirdinggadget.chunkloading;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WeirdingGadgetTicket {
    public static final TicketType<ChunkPos> WEIRDING_GADGET = TicketType.create("weirding_gadget", Comparator.comparingLong(ChunkPos::toLong));
    public static final int TICKET_LEVEL = 31;

    private final String playerName;
    private final UUID id;
    private final String modName;
    private CompoundTag modData;

    private final Map<Long, Ticket<ChunkPos>> vanillaTickets = Maps.newHashMap();

    public WeirdingGadgetTicket(String playerName, String modName) {
        this.modName = modName;
        this.id = UUID.randomUUID();
        this.playerName = playerName;
    }

    public CompoundTag getModData() {
        if (modData == null) {
            modData = new CompoundTag();
        }
        return modData;
    }

    public void setModData(CompoundTag modData) {
        this.modData = modData;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Set<Map.Entry<Long, Ticket<ChunkPos>>> getVanillaTickets() {
        return vanillaTickets.entrySet();
    }

    public Ticket<ChunkPos> getOrCreateTicket(ChunkPos ticketChunk) {
        return vanillaTickets
                .computeIfAbsent(
                        ticketChunk.toLong(),
                        (pos) -> new Ticket<>(WEIRDING_GADGET, TICKET_LEVEL, ticketChunk, true)
                );
    }

    public UUID getId() {
        return id;
    }

    public int getChunkCount() {
        return this.vanillaTickets.size();
    }

    public String getModName() {
        return modName;
    }

    @Override
    public String toString() {
        return "WeirdingGadgetTicket{" +
                "playerName='" + playerName + '\'' +
                ", modName='" + modName + '\'' +
                ", modData=" + modData +
                '}';
    }
}
