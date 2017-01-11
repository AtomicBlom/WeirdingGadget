package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.block.TileEntity.WeirdingGadgetTileEntity;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

public class WGLoadingCallback implements ForgeChunkManager.PlayerOrderedLoadingCallback
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

            final BlockPos blockPosition = NBTUtil.getPosFromTag(modData.getCompoundTag("blockPosition"));

            final TileEntity te = world.getTileEntity(blockPosition);
            if (te instanceof WeirdingGadgetTileEntity) {
                final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)te;
                tileEntity.setTicket(ticket);

                final ChunkPos chunk = new ChunkPos(blockPosition);
                for (int z = chunk.chunkZPos - 1; z <= chunk.chunkZPos + 1; ++z) {
                    for (int x = chunk.chunkXPos - 1; x <= chunk.chunkXPos + 1; ++x) {
                        final ChunkPos ticketChunk = new ChunkPos(x, z);

                        ForgeChunkManager.forceChunk(ticket, ticketChunk);
                    }
                }
            }
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
                final NBTTagCompound modData = ticket.getModData();
                if (!modData.hasKey("blockPosition")) {
                    continue;
                }

                final BlockPos blockPosition = NBTUtil.getPosFromTag(modData.getCompoundTag("blockPosition"));

                final TileEntity te = world.getTileEntity(blockPosition);
                if (te instanceof WeirdingGadgetTileEntity) {
                    returnedTickets.put(player, ticket);
                }
            }
        }

        return returnedTickets;
    }
}
