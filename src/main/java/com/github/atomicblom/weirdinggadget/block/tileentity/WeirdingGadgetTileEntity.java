package com.github.atomicblom.weirdinggadget.block.tileentity;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.WeirdingGadgetBlock;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.github.atomicblom.weirdinggadget.library.TileEntityTypeLibrary;
import com.google.common.collect.Lists;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WeirdingGadgetTileEntity extends BlockEntity
{
    private static final int ACTIVE_STATE_CHANGED = 1;

    @Nonnull
    private final List<WeirdingGadgetTicket> tickets = Lists.newArrayList();

    @Nonnull
    private final List<WeakReference<Player>> trackedPlayers = new LinkedList<>();

    private long expireTime = -1;

    private boolean isActive;

    public WeirdingGadgetTileEntity(BlockPos pos, BlockState state) {
        super(TileEntityTypeLibrary.weirding_gadget, pos, state);
    }

    public void addTicket(@Nonnull WeirdingGadgetTicket ticket)
    {
        assert level != null;

        //Release any existing tickets for this player.
        final String playerName = ticket.getPlayerName();
        final Iterator<WeirdingGadgetTicket> iterator = tickets.iterator();
        while (iterator.hasNext()) {
            final WeirdingGadgetTicket next = iterator.next();
            if (next.getPlayerName().equals(playerName)) {
                WeirdingGadgetChunkManager.releaseTicket(level, next);
                iterator.remove();
            }
        }
        tickets.add(ticket);

        getBlockState().triggerEvent(level, worldPosition, ACTIVE_STATE_CHANGED, 1);
        expireTime = -1;
        WeirdingGadgetMod.LOGGER.info("Waking up Chunk Loader at {} because {} placed/interacted with it", worldPosition, playerName);
    }

    public void addTrackedPlayer(@Nullable Player player)
    {
        for (final WeakReference<Player> trackedPlayerReference : trackedPlayers)
        {
            final Player trackedPlayer = trackedPlayerReference.get();
            if (trackedPlayer != null && player != null && player.getName() == trackedPlayer.getName()) {
                return;
            }
        }
        trackedPlayers.add(new WeakReference<>(player));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WeirdingGadgetTileEntity entity) {
        assert level != null;

        //Only process on the server
        if (level.isClientSide) return;
        //If there isn't any tickets, the device is already disabled and no work to be done
        //until it is reactivated next.
        if (entity.tickets.isEmpty()) {
            return;
        }

        final ResourceLocation dimensionName = level.dimension().getRegistryName();

        //Only process this chunk loader every 32 ticks
        // Should probably increase this to every minute or so.
        final long totalLevelTime = level.getGameTime();
        if ((totalLevelTime & 31) != 31) {
            return;
        }

        boolean noTrackedPlayers = true;

        final Iterator<WeakReference<Player>> trackedPlayerIterator = entity.trackedPlayers.iterator();
        while (trackedPlayerIterator.hasNext())
        {
            //If we're tracking the ticket owner, check to see if they're still online
            final WeakReference<Player> playerWeakReference = trackedPlayerIterator.next();
            Player player = playerWeakReference.get();

            if (player != null)
            {
                player = TicketUtils.getOnlinePlayerByName(level.getServer(), player.getName().getString());

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
            for (final WeirdingGadgetTicket ticket : entity.tickets)
            {
                //If we're no longer tracking the ticket owner, but there is a ticket,
                //Check to see if the player has come online.
                final ServerPlayer PlayerByName = TicketUtils.getOnlinePlayerByName(level.getServer(), ticket.getPlayerName());
                //If they're found
                if (PlayerByName != null)
                {
                    //reset the expiry
                    entity.expireTime = -1;
                    //start tracking the player
                    entity.trackedPlayers.add(new WeakReference<>(PlayerByName));
                    WeirdingGadgetMod.LOGGER.info("Chunk Loader at {} in {} is revived because {} returned", entity.worldPosition, dimensionName, PlayerByName.getName().getString());
                    //Start the animation again
                    state.triggerEvent(level, entity.worldPosition, ACTIVE_STATE_CHANGED, 1);
                    //Block any further processing
                    ticketNeedsExpiring = false;
                }
            }
        }

        //At this point, the player has not been found,
        //If there isn't an expiry time, it's time to set one.
        if (ticketNeedsExpiring && entity.expireTime == -1)
        {
            final int timeout = Settings.SERVER.hoursBeforeDeactivation.get() * WeirdingGadgetMod.MULTIPLIER;
            //final int timeout = 10 * 20;
            entity.expireTime = totalLevelTime + timeout;
            WeirdingGadgetMod.LOGGER.info("All players registered to this gadget at {} in {} have gone offline. Ticket is scheduled to expire at level time {}", pos, dimensionName, entity.expireTime);
        }

        //If the expire time has expired and we've reached this far
        //It's time to kill the ticket.
        if (entity.expireTime != -1 && totalLevelTime >= entity.expireTime) {
            WeirdingGadgetMod.LOGGER.info("Ticket for Weirding Gadget at {} in {} has expired.", entity.worldPosition, dimensionName);
            for (final WeirdingGadgetTicket ticket : entity.tickets)
            {
                WeirdingGadgetChunkManager.releaseTicket(level, ticket);
            }
            entity.tickets.clear();

            //Disable the animation
            state.triggerEvent(level, entity.worldPosition, ACTIVE_STATE_CHANGED, 0);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        expireTime = nbt.contains("expireTime") ? nbt.getLong("expireTime") : -1;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);
        compound.putLong("expireTime", expireTime);
        return compound;
    }

    @Override
    public boolean triggerEvent(int id, int type)
    {
        if (id == ACTIVE_STATE_CHANGED) {
            isActive = type == 1;
            assert level != null;
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(WeirdingGadgetBlock.ACTIVE, isActive));
            WeirdingGadgetMod.LOGGER.info("Active state of chunk loader at {} is now {}", worldPosition, isActive);

            return true;
        }
        return false;
    }

    @Override
    public CompoundTag getUpdateTag()
    {
        final CompoundTag updateTag = super.getUpdateTag();
        updateTag.putBoolean("isActive", isActive);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
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

    public boolean hasTicket(Player player)
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
            WeirdingGadgetChunkManager.releaseTicket(level, ticket);
        }
        tickets.clear();
        trackedPlayers.clear();
    }
}
