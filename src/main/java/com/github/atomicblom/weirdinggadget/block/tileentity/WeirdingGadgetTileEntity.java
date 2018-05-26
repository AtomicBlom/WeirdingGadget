package com.github.atomicblom.weirdinggadget.block.tileentity;

import com.github.atomicblom.weirdinggadget.Logger;
import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class WeirdingGadgetTileEntity extends TileEntity implements ITickable
{
    private static final int ACTIVE_STATE_CHANGED = 1;

    @Nonnull
    private final List<Ticket> tickets = Lists.newArrayList();

    @Nonnull
    private final List<WeakReference<EntityPlayer>> trackedPlayers = new LinkedList<>();

    private long expireTime = -1;

    private boolean isActive;
    private long fuelExpireTime;

    public void setTicket(@Nonnull Ticket ticket)
    {
        //Release any existing tickets for this player.
        final String playerName = ticket.getPlayerName();
        final Iterator<Ticket> iterator = tickets.iterator();
        while (iterator.hasNext()) {
            final Ticket next = iterator.next();
            if (next.getPlayerName() == playerName) {
                ForgeChunkManager.releaseTicket(next);
                iterator.remove();
            }
        }
        tickets.add(ticket);

        world.addBlockEvent(pos, getBlockType(), ACTIVE_STATE_CHANGED, 1);
        expireTime = -1;
        Logger.info("Waking up Chunk Loader at %s because %s placed/interacted with it", pos, playerName);
    }

    public void addTrackedPlayer(@Nullable EntityPlayer player)
    {
        for (final WeakReference<EntityPlayer> trackedPlayerReference : trackedPlayers)
        {
            final EntityPlayer trackedPlayer = trackedPlayerReference.get();
            if (trackedPlayer != null && player != null && player.getName() == trackedPlayer.getName()) {
                return;
            }
        }
        trackedPlayers.add(new WeakReference<>(player));
    }

    public void addFuelTicks(int ticks)
    {
        //TODO: spider loaders and split accordingly
        long totalWorldTime = world.getTotalWorldTime();
        if (fuelExpireTime < totalWorldTime) {
            fuelExpireTime = totalWorldTime + ticks;
        } else {
            fuelExpireTime += ticks;
        }
    }

    @Override
    public void update()
    {
        //Only process on the server
        if (world.isRemote) return;
        //If there isn't any tickets, the device is already disabled and no work to be done
        //until it is reactivated next.
        if (tickets.isEmpty()) return;

        //Only process this chunk loader every 32 ticks
        // Should probably increase this to every minute or so.
        final long totalWorldTime = world.getTotalWorldTime();
        if ((totalWorldTime & 31) != 31) {
            return;
        }

        if (Settings.enableFuel && fuelExpireTime < world.getTotalWorldTime()) {
            Logger.info("Fuel has expired for weirding gadget at %s", pos);

            disable();
        }

        boolean noTrackedPlayers = true;

        final Iterator<WeakReference<EntityPlayer>> trackedPlayerIterator = trackedPlayers.iterator();
        while (trackedPlayerIterator.hasNext())
        {
            //If we're tracking the ticket owner, check to see if they're still online
            final WeakReference<EntityPlayer> playerWeakReference = trackedPlayerIterator.next();
            EntityPlayer player = playerWeakReference.get();

            if (player != null)
            {
                player = TicketUtils.getOnlinePlayerByName(world.getMinecraftServer(), player.getName());

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
            for (final Ticket ticket : tickets)
            {
                //If we're no longer tracking the ticket owner, but there is a ticket,
                //Check to see if the player has come online.

                final EntityPlayer playerEntityByName = TicketUtils.getOnlinePlayerByName(world.getMinecraftServer(), ticket.getPlayerName());
                //If they're found
                if (playerEntityByName != null)
                {
                    //reset the expiry
                    expireTime = -1;
                    //start tracking the player
                    trackedPlayers.add(new WeakReference<>(playerEntityByName));
                    Logger.info("Chunk Loader at %s is revived because %s returned", pos, playerEntityByName.getName());
                    //Start the animation again
                    world.addBlockEvent(pos, getBlockType(), ACTIVE_STATE_CHANGED, 1);
                    //Block any further processing
                    ticketNeedsExpiring = false;
                }
            }
        }

        //At this point, the player has not been found,
        //If there isn't an expiry time, it's time to set one.
        if (ticketNeedsExpiring && expireTime == -1)
        {
            final int timeout = Settings.hoursBeforeDeactivation * WeirdingGadgetMod.MULTIPLIER;
            //final int timeout = 10 * 20;
            expireTime = totalWorldTime + timeout;
            Logger.info("All players have gone offline. Ticket is scheduled to expire at world time %d", expireTime);
        }

        //If the expire time has expired and we've reached this far
        //It's time to kill the ticket.
        if (expireTime != -1 && totalWorldTime >= expireTime) {
            Logger.info("Ticket for Weirding Gadget at %s has expired.", pos);
            disable();
        }
    }

    private void disable() {
        for (final Ticket ticket : tickets)
        {
            ForgeChunkManager.releaseTicket(ticket);
        }
        tickets.clear();

        //Disable the animation
        world.addBlockEvent(pos, getBlockType(), ACTIVE_STATE_CHANGED, 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        expireTime = compound.hasKey("expireTime") ? compound.getLong("expireTime") : -1;
        fuelExpireTime = compound.hasKey("fuelExpireTime") ? compound.getLong("fuelExpireTime") : -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setLong("expireTime", expireTime);
        compound.setLong("fuelExpireTime", fuelExpireTime);
        return compound;
    }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        if (id == ACTIVE_STATE_CHANGED) {
            isActive = type == 1;
            Logger.info("Active state of chunk loader at %s is now %b", pos, isActive);

            return true;
        }
        return false;
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        final NBTTagCompound updateTag = super.getUpdateTag();
        updateTag.setBoolean("isActive", isActive);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        isActive = tag.getBoolean("isActive");
        super.handleUpdateTag(tag);
    }

    public boolean isExpired()
    {
        return tickets.isEmpty();
    }

    public boolean isActive()
    {
        return isActive;
    }

    public boolean hasTicket(EntityPlayer player)
    {
        if (player == null) return false;
        for (final Ticket ticket : tickets)
        {
            if (ticket.getPlayerName() == player.getName()) return true;
        }
        return false;
    }

    public void expireAllTickets()
    {
        for (final Ticket ticket : tickets)
        {
            ForgeChunkManager.releaseTicket(ticket);
        }
        tickets.clear();
        trackedPlayers.clear();
    }

    public boolean canActivate()
    {
        return !Settings.enableFuel || fuelExpireTime > world.getTotalWorldTime();
    }

    public long getFuelTicks()
    {
        long totalWorldTime = world.getTotalWorldTime();
        long ticksRemaining = fuelExpireTime - totalWorldTime;
        if (ticksRemaining < 0) ticksRemaining = 0;
        return ticksRemaining;
    }
}
