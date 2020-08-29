package com.github.atomicblom.weirdinggadget.block.tileentity;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.github.atomicblom.weirdinggadget.library.TileEntityTypeLibrary;
import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WeirdingGadgetTileEntity extends TileEntity implements ITickableTileEntity
{
    private static final int ACTIVE_STATE_CHANGED = 1;

    @Nonnull
    private final List<WeirdingGadgetTicket> tickets = Lists.newArrayList();

    @Nonnull
    private final List<WeakReference<PlayerEntity>> trackedPlayers = new LinkedList<>();

    private long expireTime = -1;

    private boolean isActive;

    public WeirdingGadgetTileEntity() {
        super(TileEntityTypeLibrary.weirding_gadget);
    }

    public void addTicket(@Nonnull WeirdingGadgetTicket ticket)
    {
        assert world != null;

        //Release any existing tickets for this player.
        final String playerName = ticket.getPlayerName();
        final Iterator<WeirdingGadgetTicket> iterator = tickets.iterator();
        while (iterator.hasNext()) {
            final WeirdingGadgetTicket next = iterator.next();
            if (next.getPlayerName().equals(playerName)) {
                WeirdingGadgetChunkManager.releaseTicket(world, next);
                iterator.remove();
            }
        }
        tickets.add(ticket);

        world.addBlockEvent(pos, getBlockState().getBlock(), ACTIVE_STATE_CHANGED, 1);
        expireTime = -1;
        WeirdingGadgetMod.LOGGER.info("Waking up Chunk Loader at {} because {} placed/interacted with it", pos, playerName);
    }

    public void addTrackedPlayer(@Nullable PlayerEntity player)
    {
        for (final WeakReference<PlayerEntity> trackedPlayerReference : trackedPlayers)
        {
            final PlayerEntity trackedPlayer = trackedPlayerReference.get();
            if (trackedPlayer != null && player != null && player.getName() == trackedPlayer.getName()) {
                return;
            }
        }
        trackedPlayers.add(new WeakReference<>(player));
    }

    @Override
    public void tick()
    {
        assert world != null;

        //Only process on the server
        if (world.isRemote) return;
        //If there isn't any tickets, the device is already disabled and no work to be done
        //until it is reactivated next.
        if (tickets.isEmpty()) {
            return;
        }

        final ResourceLocation dimensionName = world.getDimensionKey().func_240901_a_();

        //Only process this chunk loader every 32 ticks
        // Should probably increase this to every minute or so.
        final long totalWorldTime = world.getGameTime();
        if ((totalWorldTime & 31) != 31) {
            return;
        }

        boolean noTrackedPlayers = true;

        final Iterator<WeakReference<PlayerEntity>> trackedPlayerIterator = trackedPlayers.iterator();
        while (trackedPlayerIterator.hasNext())
        {
            //If we're tracking the ticket owner, check to see if they're still online
            final WeakReference<PlayerEntity> playerWeakReference = trackedPlayerIterator.next();
            PlayerEntity player = playerWeakReference.get();

            if (player != null)
            {
                player = TicketUtils.getOnlinePlayerByName(world.getServer(), player.getName().getString());

                //If we couldn't find them, clear the reference, we're not tracking them any more.
                if (player == null)
                {
                    playerWeakReference.clear();
                    trackedPlayerIterator.remove();
                } else
                {
                    noTrackedPlayers = false;
                }
            }
        }

        boolean ticketNeedsExpiring = noTrackedPlayers;
        if (noTrackedPlayers) {
            for (final WeirdingGadgetTicket ticket : tickets)
            {
                //If we're no longer tracking the ticket owner, but there is a ticket,
                //Check to see if the player has come online.
                final PlayerEntity playerEntityByName = TicketUtils.getOnlinePlayerByName(world.getServer(), ticket.getPlayerName());
                //If they're found
                if (playerEntityByName != null)
                {
                    //reset the expiry
                    expireTime = -1;
                    //start tracking the player
                    trackedPlayers.add(new WeakReference<>(playerEntityByName));
                    WeirdingGadgetMod.LOGGER.info("Chunk Loader at {} in {} is revived because {} returned", pos, dimensionName, playerEntityByName.getName().getString());
                    //Start the animation again
                    world.addBlockEvent(pos, getBlockState().getBlock(), ACTIVE_STATE_CHANGED, 1);
                    //Block any further processing
                    ticketNeedsExpiring = false;
                }
            }
        }

        //At this point, the player has not been found,
        //If there isn't an expiry time, it's time to set one.
        if (ticketNeedsExpiring && expireTime == -1)
        {
            final int timeout = Settings.SERVER.hoursBeforeDeactivation.get() * WeirdingGadgetMod.MULTIPLIER;
            //final int timeout = 10 * 20;
            expireTime = totalWorldTime + timeout;
            WeirdingGadgetMod.LOGGER.info("All players registered to this gadget at {} in {} have gone offline. Ticket is scheduled to expire at world time {}", pos, dimensionName, expireTime);
        }

        //If the expire time has expired and we've reached this far
        //It's time to kill the ticket.
        if (expireTime != -1 && totalWorldTime >= expireTime) {
            WeirdingGadgetMod.LOGGER.info("Ticket for Weirding Gadget at {} in {} has expired.", pos, dimensionName);
            for (final WeirdingGadgetTicket ticket : tickets)
            {
                WeirdingGadgetChunkManager.releaseTicket(world, ticket);
            }
            tickets.clear();

            //Disable the animation
            world.addBlockEvent(pos, getBlockState().getBlock(), ACTIVE_STATE_CHANGED, 0);
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        expireTime = nbt.contains("expireTime") ? nbt.getLong("expireTime") : -1;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putLong("expireTime", expireTime);
        return compound;
    }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        if (id == ACTIVE_STATE_CHANGED) {
            isActive = type == 1;
            assert world != null;
            world.setBlockState(pos, getBlockState().with(WeirdingGadgetBlock.ACTIVE, isActive));
            WeirdingGadgetMod.LOGGER.info("Active state of chunk loader at {} is now {}", pos, isActive);

            return true;
        }
        return false;
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        final CompoundNBT updateTag = super.getUpdateTag();
        updateTag.putBoolean("isActive", isActive);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        isActive = tag.getBoolean("isActive");
        super.handleUpdateTag(state, tag);
    }

    public boolean isExpired()
    {
        return tickets.isEmpty();
    }

    public boolean isActive()
    {
        return isActive;
    }

    public boolean hasTicket(PlayerEntity player)
    {
        for (final WeirdingGadgetTicket ticket : tickets)
        {
            if (ticket.getPlayerName().equals(player.getName().getString())) return true;
        }
        return false;
    }

    public void expireAllTickets()
    {
        for (final WeirdingGadgetTicket ticket : tickets)
        {
            WeirdingGadgetChunkManager.releaseTicket(world, ticket);
        }
        tickets.clear();
        trackedPlayers.clear();
    }
}
