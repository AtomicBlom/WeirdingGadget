package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ListMultimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;

public final class ChunkManagerCallback
{
    public static void ticketsLoaded(ImmutableCollection<WeirdingGadgetTicket> tickets, Level level)
    {
        for (final WeirdingGadgetTicket ticket : tickets)
        {
            final CompoundTag modData = ticket.getModData();
            if (!modData.contains("blockPosition")) {
                continue;
            }

            TicketUtils.activateTicket(level, ticket);
        }
    }

    public static ListMultimap<String, WeirdingGadgetTicket> playerTicketsLoaded(Map<String, List<WeirdingGadgetTicket>> tickets, Level level)
    {
        final ListMultimap<String, WeirdingGadgetTicket> returnedTickets = ArrayListMultimap.create();

        if (Settings.SERVER.emergencyMode.get()) {
            return returnedTickets;
        }

        for (final Map.Entry<String, List<WeirdingGadgetTicket>> playerTicketMap : tickets.entrySet())
        {
            final String player = playerTicketMap.getKey();
            for (final WeirdingGadgetTicket ticket : playerTicketMap.getValue())
            {
                if (TicketUtils.isTicketValid(level, ticket)) {
                    returnedTickets.put(player, ticket);
                }
            }
        }

        return returnedTickets;
    }
}
