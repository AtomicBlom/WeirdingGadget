package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class ChunkManagerCallback
{
    public static void ticketsLoaded(ImmutableCollection<WeirdingGadgetTicket> tickets, World world)
    {
        for (final WeirdingGadgetTicket ticket : tickets)
        {
            final CompoundNBT modData = ticket.getModData();
            if (!modData.contains("blockPosition")) {
                continue;
            }

            TicketUtils.activateTicket(world, ticket);
        }
    }

    public static ListMultimap<String, WeirdingGadgetTicket> playerTicketsLoaded(Map<String, List<WeirdingGadgetTicket>> tickets, World world)
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
                if (TicketUtils.isTicketValid(world, ticket)) {
                    returnedTickets.put(player, ticket);
                }
            }
        }

        return returnedTickets;
    }
}
