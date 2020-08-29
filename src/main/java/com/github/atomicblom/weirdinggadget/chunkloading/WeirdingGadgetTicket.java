package com.github.atomicblom.weirdinggadget.chunkloading;

import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.Ticket;
import net.minecraft.world.server.TicketType;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WeirdingGadgetTicket {
    public static final TicketType<ChunkPos> WEIRDING_GADGET = TicketType.create("weirding_gadget", Comparator.comparingLong(ChunkPos::asLong));
    public static final int TICKET_LEVEL = 31;

    private final String playerName;
    private final UUID id;
    private final String modName;
    private CompoundNBT modData;

    private final Map<Long, Ticket<ChunkPos>> vanillaTickets = Maps.newHashMap();

    public WeirdingGadgetTicket(String playerName, String modName) {
        this.modName = modName;
        this.id = UUID.randomUUID();
        this.playerName = playerName;
    }

    public CompoundNBT getModData() {
        if (modData == null) {
            modData = new CompoundNBT();
        }
        return modData;
    }

    public void setModData(CompoundNBT modData) {
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
                        ticketChunk.asLong(),
                        (pos) -> new Ticket<>(WEIRDING_GADGET, TICKET_LEVEL, ticketChunk)
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
