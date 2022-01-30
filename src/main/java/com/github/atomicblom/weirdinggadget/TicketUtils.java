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
        var modData = ticket.getModData();
        if (!modData.contains("blockPosition")) {
            return false;
        }
        var pos = NbtUtils.readBlockPos(modData.getCompound("blockPosition"));
        var te = blockAccess.getBlockEntity(pos);
        return te instanceof WeirdingGadgetTileEntity;
    }

    public static void activateTicket(Level level, WeirdingGadgetTicket ticket) {
        if (!isTicketValid(level, ticket)) return;

        var modData = ticket.getModData();
        var pos = NbtUtils.readBlockPos(modData.getCompound("blockPosition"));
        var te = level.getBlockEntity(pos);
        if (!(te instanceof WeirdingGadgetTileEntity tileEntity)) {
            WeirdingGadgetMod.LOGGER.error("Expected a Weirding Gadget Tile Entity at {}, but it was a {}", pos, te);
            return;
        }

        var size = modData.getInt("size");

        final var chunk = new ChunkPos(pos);

        var minX = chunk.x - (int)(size / 2.0f);
        var maxX = chunk.x + (int)((size - 1) / 2.0f);
        var minZ = chunk.z - (int)(size/ 2.0f);
        var maxZ = chunk.z + (int)((size - 1) / 2.0f);

        for (var z = minZ; z <= maxZ; ++z) {
            for (var x = minX; x <= maxX; ++x) {
                final var ticketChunk = new ChunkPos(x, z);

                WeirdingGadgetChunkManager.forceChunk(level, ticket, ticketChunk);
            }
        }

        tileEntity.addTicket(ticket);

        var playerName = ticket.getPlayerName();

        final var player = getOnlinePlayerByName(level.getServer(), playerName);
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
        final var playerList = server.getPlayerList();

        for (final var entityPlayerMP : playerList.getPlayers())
        {
            if (playerName.equals(entityPlayerMP.getName().getString())) {
                locatedPlayer = entityPlayerMP;
                break;
            }
        }
        return locatedPlayer;
    }
}
