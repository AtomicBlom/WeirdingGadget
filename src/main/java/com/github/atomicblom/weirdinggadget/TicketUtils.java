package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.block.TileEntity.WeirdingGadgetTileEntity;
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

/**
 * Created by codew on 17/01/2017.
 */
public class TicketUtils {

    public static boolean isTicketValid(IBlockAccess blockAccess, ForgeChunkManager.Ticket ticket) {
        NBTTagCompound modData = ticket.getModData();
        if (!modData.hasKey("blockPosition")) {
            return false;
        }
        BlockPos pos = NBTUtil.getPosFromTag(modData.getCompoundTag("blockPosition"));
        TileEntity te = blockAccess.getTileEntity(pos);
        if (!(te instanceof WeirdingGadgetTileEntity)) {
            return false;
        }

        return true;
    }

    public static void activateTicket(World world, ForgeChunkManager.Ticket ticket) {
        if (!isTicketValid(world, ticket)) return;

        NBTTagCompound modData = ticket.getModData();
        BlockPos pos = NBTUtil.getPosFromTag(modData.getCompoundTag("blockPosition"));
        TileEntity te = world.getTileEntity(pos);
        WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)te;
        int size = modData.getInteger("size");

        final ChunkPos chunk = new ChunkPos(pos);

        int minX = chunk.chunkXPos - (int)(size / 2.0f);
        int maxX = chunk.chunkXPos - (int)((size - 1) / 2.0f);
        int minZ = chunk.chunkZPos - (int)(size/ 2.0f);
        int maxZ = chunk.chunkZPos - (int)((size - 1) / 2.0f);

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                final ChunkPos ticketChunk = new ChunkPos(x, z);

                ForgeChunkManager.forceChunk(ticket, ticketChunk);
            }
        }

        tileEntity.setTicket(ticket);

        String playerName = ticket.getPlayerName();

        tileEntity.setPlacer(getOnlinePlayerByName(world.getMinecraftServer(), playerName));
    }

    public static EntityPlayerMP getOnlinePlayerByName(MinecraftServer server, String playerName) {
        EntityPlayerMP locatedPlayer = null;
        if (server == null || server.getPlayerList() == null) {
            return null;
        }
        final PlayerList playerList = server.getPlayerList();

        for (final EntityPlayerMP entityPlayerMP : playerList.getPlayers())
        {
            if (entityPlayerMP.getName() == playerName) {
                locatedPlayer = entityPlayerMP;
                break;
            }
        }
        return locatedPlayer;
    }
}
