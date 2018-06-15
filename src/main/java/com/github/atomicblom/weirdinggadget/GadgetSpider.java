package com.github.atomicblom.weirdinggadget;

import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import java.util.*;

public class GadgetSpider
{
	@SuppressWarnings("NumericCastThatLosesPrecision")
	public static List<WeirdingGadgetTileEntity> findNearbyWeirdingGadgets(World world, BlockPos pos)
	{
		final ChunkPos chunkPos = new ChunkPos(pos);
		final Set<Long> visitedChunks = Sets.newHashSet();
		final List<WeirdingGadgetTileEntity> locatedChunkLoaders = Lists.newArrayList();
		final LinkedList<Chunk> chunksToVisit = Lists.newLinkedList();

		chunksToVisit.push(world.getChunkFromChunkCoords(chunkPos.x, chunkPos.z));

		while (!chunksToVisit.isEmpty())
		{
			final Chunk chunk = chunksToVisit.pop();
			final long l = ChunkPos.asLong(chunk.x, chunk.z);

			if (!chunk.isPopulated()) {
				visitedChunks.add(l);
				continue;
			}

			if (visitedChunks.contains(l)) continue;

			//Find the size of all nearby chunk loaders.
			Map<BlockPos, TileEntity> tileEntityMap = chunk.getTileEntityMap();
			int chunkLoaderRadius = 0;
			for (final TileEntity tileEntity : Lists.newArrayList(tileEntityMap.values()))
			{
				if (tileEntity instanceof WeirdingGadgetTileEntity)
				{
					final WeirdingGadgetTileEntity te = (WeirdingGadgetTileEntity) tileEntity;
					final int size = te.getLoaderRadius();
					if (size > chunkLoaderRadius) chunkLoaderRadius = size;
					locatedChunkLoaders.add(te);
				}
			}

			visitedChunks.add(l);

			//Now add all nearby chunks that have not yet been visited that we could potentially reach
			//based on config setting sizes.
			final int checkRadius = (int) (chunkLoaderRadius + Settings.chunkLoaderWidth / 2.0f);

			for (int z = -checkRadius; z <= checkRadius; ++z)
			{
				for (int x = -checkRadius; x <= checkRadius; ++x)
				{
					if (x == 0 && z == 0) continue;

					final long neighbourId = ChunkPos.asLong(chunk.x + x, chunk.z + z);
					if (visitedChunks.contains(neighbourId)) continue;

					tileEntityMap = chunk.getTileEntityMap();

					int otherLoaderRadius = 0;
					for (final TileEntity tileEntity : Lists.newArrayList(tileEntityMap.values()))
					{
						if (tileEntity instanceof WeirdingGadgetTileEntity)
						{
							final WeirdingGadgetTileEntity te = (WeirdingGadgetTileEntity) tileEntity;
							final int size = te.getLoaderRadius();
							if (size > otherLoaderRadius) otherLoaderRadius = size;
						}
					}

					final int blea = Math.max(Math.abs(x), Math.abs(z));
					final int checkSize = (int) (Math.floor(otherLoaderRadius / 2.0f) + 1 + Math.floor(chunkLoaderRadius / 2.0f) + 1);
					if (checkSize > blea)
					{
						chunksToVisit.push(world.getChunkFromChunkCoords(chunk.x + x, chunk.z + z));
					}
				}
			}
		}

		locatedChunkLoaders.removeIf(blockPos -> blockPos.getPos().equals(pos));

		return locatedChunkLoaders;
	}

	public static Iterable<WeirdingGadgetTileEntity> getChainedGadgets(WeirdingGadgetTileEntity weirdingGadget)
	{
		final Set<Long> gadgetPos = Sets.newHashSet();
		final List<WeirdingGadgetTileEntity> foundGadgets = Lists.newArrayList();

		final Stack<WeirdingGadgetTileEntity> gadgetsToSearch = new Stack<>();
		gadgetsToSearch.push(weirdingGadget);

		final World world = weirdingGadget.getWorld();
		while (!gadgetsToSearch.empty()) {
			final WeirdingGadgetTileEntity gadget = gadgetsToSearch.pop();
			final Long pos = gadget.getPos().toLong();
			if (gadgetPos.contains(pos)) continue;

			foundGadgets.add(gadget);
			gadgetPos.add(pos);
			for (final BlockPos blockPos : gadget.getNearbyGadgets())
			{
				if (gadgetPos.contains(blockPos.toLong())) continue;

				final TileEntity tileEntity = world.getTileEntity(blockPos);
				if (tileEntity instanceof WeirdingGadgetTileEntity) {
					gadgetsToSearch.push((WeirdingGadgetTileEntity)tileEntity);
				} else {
					gadget.removeNearbyGadget(blockPos);
				}
			}
		}
		return foundGadgets;
	}
}
