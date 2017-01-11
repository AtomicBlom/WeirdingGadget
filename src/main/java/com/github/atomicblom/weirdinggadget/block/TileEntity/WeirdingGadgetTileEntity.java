package com.github.atomicblom.weirdinggadget.block.TileEntity;

import com.github.atomicblom.weirdinggadget.Logger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class WeirdingGadgetTileEntity extends TileEntity implements ITickable
{

    private static final int ACTIVE_STATE_CHANGED = 1;

    @Nullable
    private Ticket ticket;

    @Nonnull
    private WeakReference<EntityPlayer> placer = new WeakReference<EntityPlayer>(null);

    private long expireTime = -1;

    private boolean isActive;

    public void setTicket(@Nonnull Ticket ticket)
    {
        this.ticket = ticket;
        world.addBlockEvent(pos, blockType, ACTIVE_STATE_CHANGED, 1);
        expireTime = -1;
        isActive = true;

        Logger.info("Waking up Chunk Loader at %s because %s placed/interacted with it", pos, ticket.getPlayerName());
    }

    public Ticket getTicket()
    {
        return ticket;
    }

    public void setPlacer(@Nonnull EntityPlayer placer)
    {
        this.placer = new WeakReference<EntityPlayer>(placer);
    }

    @Nullable
    public EntityPlayer getPlacer()
    {
        return placer.get();
    }

    @Override
    public void update()
    {
        if (world.isRemote) return;
        if (ticket == null) return;
        final long totalWorldTime = world.getTotalWorldTime();
        if ((totalWorldTime & 31) != 31) {
            return;
        }


        EntityPlayer placer = getPlacer();
        if (placer != null) {
            boolean located = false;
            for (final EntityPlayerMP entityPlayerMP : world.getMinecraftServer().getPlayerList().getPlayers())
            {
                if (entityPlayerMP.getName() == placer.getName()) {
                    located = true;
                    break;
                }
            }

            if (!located) {
                this.placer.clear();
                placer = null;
            }
        }

        if (placer == null && ticket != null) {
            //FIXME: Lookup list of online players, rather than dimension players

            final EntityPlayer playerEntityByName = world.getPlayerEntityByName(ticket.getPlayerName());
            if (playerEntityByName != null) {
                expireTime = -1;
                this.placer = new WeakReference<EntityPlayer>(playerEntityByName);
                Logger.info("Chunk Loader at %s is revived because %s returned", pos, playerEntityByName.getName());
                return;
            }

            if (expireTime == -1)
            {
                int timeout = 1 * 20 * 20;
                expireTime = totalWorldTime + timeout;
                Logger.info("Player %s has gone offline. Ticket is scheduled to expire at world time %d", ticket.getPlayerName(), expireTime);
            }
        }

        if (expireTime != -1 && totalWorldTime >= expireTime) {
            Logger.info("Ticket for Weirding Gadget at %s has expired.", pos);
            ForgeChunkManager.releaseTicket(ticket);
            ticket = null;
            world.addBlockEvent(pos, blockType, ACTIVE_STATE_CHANGED, 0);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        expireTime = compound.hasKey("expireTime") ? compound.getLong("expireTime") : -1;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setLong("expireTime", expireTime);
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

    public boolean isExpired()
    {
        return ticket == null;
    }
}
