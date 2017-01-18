package com.github.atomicblom.weirdinggadget;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class ChunkManagerCallback implements ForgeChunkManager.PlayerOrderedLoadingCallback
{
    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world)
    {
        for (final Ticket ticket : tickets)
        {
            final NBTTagCompound modData = ticket.getModData();
            if (!modData.hasKey("blockPosition")) {
                continue;
            }

            TicketUtils.activateTicket(world, ticket);
        }
    }

    @Override
    public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, World world)
    {
        final ListMultimap<String, Ticket> returnedTickets = ArrayListMultimap.create();

        for (final Entry<String, Collection<Ticket>> playerTicketMap : tickets.asMap().entrySet())
        {
            final String player = playerTicketMap.getKey();
            for (final Ticket ticket : playerTicketMap.getValue())
            {
                if (TicketUtils.isTicketValid(world, ticket)) {
                    returnedTickets.put(player, ticket);
                }
            }
        }

        return returnedTickets;
    }
}
