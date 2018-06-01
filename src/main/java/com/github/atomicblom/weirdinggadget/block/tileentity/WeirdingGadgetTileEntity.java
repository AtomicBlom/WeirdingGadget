package com.github.atomicblom.weirdinggadget.block.tileentity;

import com.github.atomicblom.weirdinggadget.*;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
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
    private long fuelExpireTime = -1;
    private boolean scheduleNeighbourCheck = true;
    private List<BlockPos> nearbyGadgets;

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

    public void addFuelTicks(long ticks)
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
        if (scheduleNeighbourCheck) {
            checkNeighbours();
        }

        checkFuel();

        boolean trackedPlayersOnline = checkOnlinePlayers();

        boolean ticketNeedsExpiring = checkPlayersLoggedOn(trackedPlayersOnline);

        //At this point, no players have been found,
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

    private void checkNeighbours()
    {
        final List<WeirdingGadgetTileEntity> nearbyGadgets = GadgetSpider.findNearbyWeirdingGadgets(world, pos);

        final List<BlockPos> gadgetsToRemove = Lists.newArrayList();

        for (final BlockPos nearbyGadget : this.nearbyGadgets)
        {
            if (nearbyGadgets.stream().noneMatch(te -> te.pos.equals(nearbyGadget))) {
                gadgetsToRemove.add(nearbyGadget);
            }
        }

        boolean isDirty = false;
        for (final WeirdingGadgetTileEntity nearbyGadget : nearbyGadgets)
        {
            nearbyGadget.notifyNeighbourAdded(pos);
            if (!this.nearbyGadgets.contains(nearbyGadget.pos))
            {
                this.nearbyGadgets.add(nearbyGadget.pos);
                isDirty = true;
            }
        }

        if (!gadgetsToRemove.isEmpty()) {
            isDirty = true;
            this.nearbyGadgets.removeAll(gadgetsToRemove);
        }

        if (isDirty)
        {
            markDirty();
        }
    }

    private void notifyNeighbourAdded(BlockPos pos)
    {
        if (!nearbyGadgets.contains(pos)) {
            nearbyGadgets.add(pos);
            markDirty();
        }
    }

    private boolean checkPlayersLoggedOn(boolean trackedPlayersOnline)
    {
        boolean ticketNeedsExpiring = trackedPlayersOnline;
        if (!trackedPlayersOnline) {
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
        return ticketNeedsExpiring;
    }

    private void checkFuel()
    {
        if (Settings.enableFuel && fuelExpireTime < world.getTotalWorldTime()) {
            Logger.info("Fuel has expired for weirding gadget at %s", pos);

            disable();
        }
    }

    private boolean checkOnlinePlayers()
    {
        boolean trackedPlayersOnline = false;

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
                    trackedPlayersOnline = true;
                }
            }
        }

        return trackedPlayersOnline;
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
        nearbyGadgets = Lists.newArrayList();
        if (compound.hasKey("knownNeighbours")) {
            final NBTTagList tagList = compound.getTagList("knownNeighbours", 10);
            for (int i = 0; i < tagList.tagCount(); i++)
            {
                nearbyGadgets.add(NBTUtil.getPosFromTag(tagList.getCompoundTagAt(i)));
            }
        }
        scheduleNeighbourCheck = true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setLong("expireTime", expireTime);
        compound.setLong("fuelExpireTime", fuelExpireTime);
        NBTTagList tagList = new NBTTagList();
        for (final BlockPos nearbyGadget : nearbyGadgets)
        {
            tagList.appendTag(NBTUtil.createPosTag(nearbyGadget));
        }
        compound.setTag("knownNeighbours", tagList);
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

    public int getLoaderRadius()
    {
        int size = 0;
        for (final Ticket ticket : tickets)
        {
            size = Math.max(size, ticket.getModData().getInteger("size"));
        }
        return size;
    }

    public List<BlockPos> getNearbyGadgets()
    {
        return nearbyGadgets;
    }
}
