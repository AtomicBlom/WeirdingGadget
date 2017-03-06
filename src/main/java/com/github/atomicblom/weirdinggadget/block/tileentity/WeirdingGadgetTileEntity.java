package com.github.atomicblom.weirdinggadget.block.tileentity;

import com.github.atomicblom.weirdinggadget.Logger;
import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class WeirdingGadgetTileEntity extends TileEntity implements ITickable
{

    public WeirdingGadgetTileEntity()
    {
        ticket = null;
    }

    private static final int ACTIVE_STATE_CHANGED = 1;

    @Nullable
    private Ticket ticket;

    @Nonnull
    private WeakReference<EntityPlayer> placer = new WeakReference<>(null);

    private long expireTime = -1;

    private boolean isActive;

    public void setTicket(@Nonnull Ticket ticket)
    {
        if (this.ticket != null) {
            ForgeChunkManager.releaseTicket(this.ticket);
        }

        this.ticket = ticket;
        worldObj.addBlockEvent(pos, getBlockType(), ACTIVE_STATE_CHANGED, 1);
        expireTime = -1;
        Logger.info("Waking up Chunk Loader at %s because %s placed/interacted with it", pos, ticket.getPlayerName());
    }

    public Ticket getTicket()
    {
        return ticket;
    }

    public void setPlacer(@Nullable EntityPlayer placer)
    {
        this.placer = new WeakReference<>(placer);
    }

    @Nullable
    private EntityPlayer getPlacer()
    {
        return placer.get();
    }

    @Override
    public void update()
    {
        if (worldObj.isRemote) return;
        if (ticket == null) return;
        final long totalWorldTime = worldObj.getTotalWorldTime();
        if ((totalWorldTime & 31) != 31) {
            return;
        }

        EntityPlayer player = getPlacer();
        if (player != null) {
            player = TicketUtils.getOnlinePlayerByName(worldObj.getMinecraftServer(), player.getName());

            if (player == null) {
                placer.clear();
            }
        }

        if (player == null && ticket != null) {
            final EntityPlayer playerEntityByName = TicketUtils.getOnlinePlayerByName(worldObj.getMinecraftServer(), ticket.getPlayerName());
            if (playerEntityByName != null) {
                expireTime = -1;
                placer = new WeakReference<>(playerEntityByName);
                Logger.info("Chunk Loader at %s is revived because %s returned", pos, playerEntityByName.getName());
                worldObj.addBlockEvent(pos, getBlockType(), ACTIVE_STATE_CHANGED, 1);
                return;
            }

            if (expireTime == -1)
            {
                final int timeout = Settings.hoursBeforeDeactivation * WeirdingGadgetMod.MULTIPLIER;
                //final int timeout = 10 * 20;
                expireTime = totalWorldTime + timeout;
                Logger.info("Player %s has gone offline. Ticket is scheduled to expire at world time %d", ticket.getPlayerName(), expireTime);
            }
        }

        if (expireTime != -1 && totalWorldTime >= expireTime) {
            Logger.info("Ticket for Weirding Gadget at %s has expired.", pos);
            ForgeChunkManager.releaseTicket(ticket);
            ticket = null;
            worldObj.addBlockEvent(pos, getBlockType(), ACTIVE_STATE_CHANGED, 0);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        Logger.info("readFromNBT()");
        super.readFromNBT(compound);
        expireTime = compound.hasKey("expireTime") ? compound.getLong("expireTime") : -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        Logger.info("writeToNBT()");
        super.writeToNBT(compound);
        compound.setLong("expireTime", expireTime);
        return compound;
    }

    @Override
    public boolean receiveClientEvent(int id, int type)
    {
        Logger.info("receiveClientEvent()");
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
        return ticket == null;
    }

    public boolean isActive()
    {
        return isActive;
    }
}
