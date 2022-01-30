package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created by codew on 17/01/2017.
 */
public class TicketUtils {

    public static boolean isTicketValid(LevelAccessor blockAccess, WeirdingGadgetTicket ticket) {
        CompoundTag modData = ticket.getModData();
        if (!modData.contains("blockPosition")) {
            return false;
        }
        BlockPos pos = NbtUtils.readBlockPos(modData.getCompound("blockPosition"));
        BlockEntity te = blockAccess.getBlockEntity(pos);
        return te instanceof WeirdingGadgetTileEntity;
    }

    public static void activateTicket(Level level, WeirdingGadgetTicket ticket) {
        if (!isTicketValid(level, ticket)) return;

        CompoundTag modData = ticket.getModData();
        BlockPos pos = NbtUtils.readBlockPos(modData.getCompound("blockPosition"));
        BlockEntity te = level.getBlockEntity(pos);
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

                WeirdingGadgetChunkManager.forceChunk(level, ticket, ticketChunk);
            }
        }

        tileEntity.addTicket(ticket);

        String playerName = ticket.getPlayerName();

        final ServerPlayer player = getOnlinePlayerByName(level.getServer(), playerName);
        if (player != null)
        {
            tileEntity.addTrackedPlayer(player);
        }
    }

    public static ServerPlayer getOnlinePlayerByName(MinecraftServer server, String playerName) {
        ServerPlayer locatedPlayer = null;
        if (server == null || playerName == null) {
            return null;
        }
        final PlayerList playerList = server.getPlayerList();

        for (final ServerPlayer entityPlayerMP : playerList.getPlayers())
        {
            if (playerName.equals(entityPlayerMP.getName().getString())) {
                locatedPlayer = entityPlayerMP;
                break;
            }
        }
        return locatedPlayer;
    }
}
