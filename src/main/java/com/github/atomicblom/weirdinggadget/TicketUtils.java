package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

/**
 * Created by codew on 17/01/2017.
 */
public class TicketUtils {

    public static boolean isTicketValid(IBlockAccess blockAccess, Ticket ticket) {
        NBTTagCompound modData = ticket.getModData();
        if (!modData.hasKey("blockPosition")) {
            return false;
        }
        BlockPos pos = NBTUtil.getPosFromTag(modData.getCompoundTag("blockPosition"));
        TileEntity te = blockAccess.getTileEntity(pos);
        return te instanceof WeirdingGadgetTileEntity;
    }

    public static void activateTicket(World world, Ticket ticket) {
        if (!isTicketValid(world, ticket)) return;

        NBTTagCompound modData = ticket.getModData();
        BlockPos pos = NBTUtil.getPosFromTag(modData.getCompoundTag("blockPosition"));
        TileEntity te = world.getTileEntity(pos);

        if (!(te instanceof WeirdingGadgetTileEntity)) {
            Logger.info("Warning: expected a weirding gadget at %s", pos.toString());
            return;
        }

        WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)te;

        int size = modData.getInteger("size");

        final ChunkPos chunk = new ChunkPos(pos);

        int minX = chunk.x - (int)(size / 2.0f);
        int maxX = chunk.x + (int)((size - 1) / 2.0f);
        int minZ = chunk.z - (int)(size/ 2.0f);
        int maxZ = chunk.z + (int)((size - 1) / 2.0f);

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                final ChunkPos ticketChunk = new ChunkPos(x, z);

                ForgeChunkManager.forceChunk(ticket, ticketChunk);
            }
        }

        tileEntity.setTicket(ticket);

        String playerName = ticket.getPlayerName();

        final EntityPlayerMP player = getOnlinePlayerByName(world.getMinecraftServer(), playerName);
        if (player != null)
        {
            tileEntity.addTrackedPlayer(player);
        }
    }

    public static EntityPlayerMP getOnlinePlayerByName(MinecraftServer server, String playerName) {
        EntityPlayerMP locatedPlayer = null;
        if (server == null || server.getPlayerList() == null || playerName == null) {
            return null;
        }
        final PlayerList playerList = server.getPlayerList();

        for (final EntityPlayerMP entityPlayerMP : playerList.getPlayers())
        {
            if (playerName.equals(entityPlayerMP.getName())) {
                locatedPlayer = entityPlayerMP;
                break;
            }
        }
        return locatedPlayer;
    }
}
