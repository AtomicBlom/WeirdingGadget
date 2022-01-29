package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class CapabilityWeirdingGadgetTicketList {

    public final static Capability<TicketListData> TICKET_LIST_DATA = CapabilityManager.get(new CapabilityToken<>() {});

    public CapabilityWeirdingGadgetTicketList() { }

    public static class WeirdingGadgetTicketListProvider implements ICapabilityProvider, INBTSerializable<ListTag> {
        final TicketListData data = new TicketListData();
        final LazyOptional<TicketListData> opt;

        public WeirdingGadgetTicketListProvider() {
            this.opt = LazyOptional.of(() -> this.data);
        }

        @Nonnull
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
            return capability == CapabilityWeirdingGadgetTicketList.TICKET_LIST_DATA ? this.opt.cast() : LazyOptional.empty();
        }

        @Override
        public ListTag serializeNBT() {
            return data.writeToNBT();
        }

        @Override
        public void deserializeNBT(ListTag nbt) {
            data.readFromNBT(nbt);
        }
    }

    public static class TicketListData {
        private final HashMap<UUID, WeirdingGadgetTicket> tickets = Maps.newHashMap();

        private final HashMap<String, Set<UUID>> playerTickets = Maps.newHashMap();
        private final HashMap<String, Set<UUID>> modTickets = Maps.newHashMap();

        public void addTicket(WeirdingGadgetTicket ticket) {
            if (!tickets.containsKey(ticket.getId())) {
                tickets.put(ticket.getId(), ticket);

                if (ticket.getPlayerName() != null) {
                    final Set<UUID> playerGadgets = playerTickets.computeIfAbsent(ticket.getPlayerName(), p -> Sets.newHashSet());
                    playerGadgets.add(ticket.getId());
                }

                final Set<UUID> modGadgets = modTickets.computeIfAbsent(ticket.getModName(), p -> Sets.newHashSet());
                modGadgets.add(ticket.getId());
            }
        }

        public ImmutableCollection<WeirdingGadgetTicket> getAllTickets() {
            return ImmutableList.copyOf(tickets.values());
        }

        public int getTotalPlayerTicketCount(String playerName) {
            final Set<UUID> playerTickets = this.playerTickets.getOrDefault(playerName, Sets.newHashSet());
            return playerTickets.size();
        }

        public int getTotalModTicketCount(String modName) {

            final Set<UUID> modTickets = this.modTickets.getOrDefault(modName, Sets.newHashSet());
            return modTickets.size();
        }

        public void removeTicket(UUID ticketId) {
            tickets.remove(ticketId);
            for (Set<UUID> playerSet : playerTickets.values()) {
                playerSet.remove(ticketId);
            }
            for (Set<UUID> modSet : modTickets.values()) {
                modSet.remove(ticketId);
            }
        }

        public ListTag writeToNBT() {
            ListTag ticketList = new ListTag();
            for (WeirdingGadgetTicket ticket : tickets.values()) {
                CompoundTag ticketData = new CompoundTag();
                if (ticket.getPlayerName() != null) {
                    ticketData.putString("playerName", ticket.getPlayerName());
                }
                ticketData.putString("modName", ticket.getModName());
                CompoundTag modData = ticket.getModData();
                if (modData != null) {
                    ticketData.put("data", modData);
                }
                ticketList.add(ticketData);
            }

            return ticketList;
        }

        public void readFromNBT(ListTag nbt) {
            for (Tag inbt : nbt) {
                if (!(inbt instanceof CompoundTag)) continue;
                CompoundTag ticketData = (CompoundTag)inbt;

                String playerName = ticketData.getString("playerName");
                String modName = ticketData.getString("modName");
                WeirdingGadgetTicket ticket = new WeirdingGadgetTicket(playerName, modName);
                ticket.setModData(ticketData.getCompound("data"));

                addTicket(ticket);
            }
        }
    }
}