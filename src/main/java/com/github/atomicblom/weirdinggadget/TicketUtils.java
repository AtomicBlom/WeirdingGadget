package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**
 * Created by codew on 17/01/2017.
 */
public class TicketUtils {

    public static boolean isTicketValid(IWorldReader blockAccess, WeirdingGadgetTicket ticket) {
        CompoundNBT modData = ticket.getModData();
        if (!modData.contains("blockPosition")) {
            return false;
        }
        BlockPos pos = NBTUtil.readBlockPos(modData.getCompound("blockPosition"));
        TileEntity te = blockAccess.getTileEntity(pos);
        return te instanceof WeirdingGadgetTileEntity;
    }

    public static void activateTicket(World world, WeirdingGadgetTicket ticket) {
        if (!isTicketValid(world, ticket)) return;

        CompoundNBT modData = ticket.getModData();
        BlockPos pos = NBTUtil.readBlockPos(modData.getCompound("blockPosition"));
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof WeirdingGadgetTileEntity)) {
            WeirdingGadgetMod.LOGGER.error("Expected a Weirding Gadget Tile Entity at {}, but it was a {}", pos, te);
            return;
        }

        WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)te;
        int size = modData.getInt("size");

        final ChunkPos chunk = new ChunkPos(pos);

        int minX = chunk.x - (int)(size / 2.0f);
        int maxX = chunk.x + (int)((size - 1) / 2.0f);
        int minZ = chunk.z - (int)(size/ 2.0f);
        int maxZ = chunk.z + (int)((size - 1) / 2.0f);

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                final ChunkPos ticketChunk = new ChunkPos(x, z);

                WeirdingGadgetChunkManager.forceChunk(world, ticket, ticketChunk);
            }
        }

        tileEntity.addTicket(ticket);

        String playerName = ticket.getPlayerName();

        final ServerPlayerEntity player = getOnlinePlayerByName(world.getServer(), playerName);
        if (player != null)
        {
            tileEntity.addTrackedPlayer(player);
        }
    }

    public static ServerPlayerEntity getOnlinePlayerByName(MinecraftServer server, String playerName) {
        ServerPlayerEntity locatedPlayer = null;
        if (server == null || playerName == null) {
            return null;
        }
        final PlayerList playerList = server.getPlayerList();

        for (final ServerPlayerEntity entityPlayerMP : playerList.getPlayers())
        {
            if (playerName.equals(entityPlayerMP.getName().getString())) {
                locatedPlayer = entityPlayerMP;
                break;
            }
        }
        return locatedPlayer;
    }
}
