package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.Settings;
import net.minecraft.util.IStringSerializable;

public enum ChunkLoaderType implements IStringSerializable {
    NORMAL(0, "normal", Settings.chunkLoaderWidth),
    SPOT(1, "spot", 1);

    private final int meta;
    private final String name;
    private final int chunkDiameter;

    private static final ChunkLoaderType[] METADATA_LOOKUP = new ChunkLoaderType[values().length];

    ChunkLoaderType(int meta, String name, int chunkDiameter) {
        this.meta = meta;

        this.name = name;
        this.chunkDiameter = chunkDiameter;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getChunkDiameter() {
        return chunkDiameter;
    }

    public int getMetadata() {
        return meta;
    }

    public static ChunkLoaderType byMetadata(int metadata)
    {
        if (metadata < 0 || metadata >= METADATA_LOOKUP.length)
        {
            metadata = 0;
        }

        return METADATA_LOOKUP[metadata];
    }

    static
    {
        for (ChunkLoaderType type : values())
        {
            METADATA_LOOKUP[type.getMetadata()] = type;
        }
    }
}
