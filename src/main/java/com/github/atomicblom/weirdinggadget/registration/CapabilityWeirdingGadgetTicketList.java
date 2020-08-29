package com.github.atomicblom.weirdinggadget.registration;

import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class CapabilityWeirdingGadgetTicketList {
    @CapabilityInject(TicketListData.class)
    public final static Capability<TicketListData> TICKET_LIST_DATA;

    static {
        TICKET_LIST_DATA = null;
    }

    public CapabilityWeirdingGadgetTicketList() { }

    public static void register() {
        CapabilityManager.INSTANCE.register(TicketListData.class, new Capability.IStorage<TicketListData>() {
            public INBT writeNBT(Capability<TicketListData> capability, TicketListData instance, Direction side) {
                return instance.writeToNBT();

            }

            public void readNBT(Capability<TicketListData> capability, TicketListData instance, Direction side, INBT nbt) {
                if (nbt instanceof ListNBT) {
                    instance.readFromNBT((ListNBT)nbt);
                }
            }
        }, TicketListData::new);
    }

    public static class WeirdingGadgetTicketListProvider implements ICapabilityProvider, INBTSerializable<ListNBT> {
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
        public ListNBT serializeNBT() {
            return data.writeToNBT();
        }

        @Override
        public void deserializeNBT(ListNBT nbt) {
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

        public ListNBT writeToNBT() {
            ListNBT ticketList = new ListNBT();
            for (WeirdingGadgetTicket ticket : tickets.values()) {
                CompoundNBT ticketData = new CompoundNBT();
                if (ticket.getPlayerName() != null) {
                    ticketData.putString("playerName", ticket.getPlayerName());
                }
                ticketData.putString("modName", ticket.getModName());
                CompoundNBT modData = ticket.getModData();
                if (modData != null) {
                    ticketData.put("data", modData);
                }
                ticketList.add(ticketData);
            }

            return ticketList;
        }

        public void readFromNBT(ListNBT nbt) {
            for (INBT inbt : nbt) {
                if (!(inbt instanceof CompoundNBT)) continue;
                CompoundNBT ticketData = (CompoundNBT)inbt;

                String playerName = ticketData.getString("playerName");
                String modName = ticketData.getString("modName");
                WeirdingGadgetTicket ticket = new WeirdingGadgetTicket(playerName, modName);
                ticket.setModData(ticketData.getCompound("data"));

                addTicket(ticket);
            }
        }
    }
}