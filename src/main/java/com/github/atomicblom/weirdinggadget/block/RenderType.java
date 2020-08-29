package com.github.atomicblom.weirdinggadget.block;

import net.minecraft.util.IStringSerializable;

public enum RenderType implements IStringSerializable {
    STATIC,
    DYNAMIC;

    @Override
    public String getString() {
        return this.name().toLowerCase();
    }
}
