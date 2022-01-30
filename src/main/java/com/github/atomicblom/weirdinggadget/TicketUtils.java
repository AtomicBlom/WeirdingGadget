package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.block.blockentity.WeirdingGadgetBlockEntity;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import javax.annotation.Nullable;

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
        return te instanceof WeirdingGadgetBlockEntity;
    }

    public static void activateTicket(Level level, WeirdingGadgetTicket ticket) {
        if (!isTicketValid(level, ticket)) return;

        var modData = ticket.getModData();
        var pos = NbtUtils.readBlockPos(modData.getCompound("blockPosition"));
        var te = level.getBlockEntity(pos);
        if (!(te instanceof WeirdingGadgetBlockEntity blockEntity)) {
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

        blockEntity.addTicket(ticket);

        var playerName = ticket.getPlayerName();

        MinecraftServer server = level.getServer();
        if (server == null) {
            WeirdingGadgetMod.LOGGER.error("Expected a server to be available from Weirding Gadget Tile Entity at {}, but it was null", pos);
            return;
        }
        final var player = getOnlinePlayerByName(server, playerName);
        if (player != null)
        {
            blockEntity.addTrackedPlayer(player);
        }
    }

    @Nullable
    public static ServerPlayer getOnlinePlayerByName(MinecraftServer server, String playerName) {
        ServerPlayer locatedPlayer = null;
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
