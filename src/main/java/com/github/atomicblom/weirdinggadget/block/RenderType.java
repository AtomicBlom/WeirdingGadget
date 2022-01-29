package com.github.atomicblom.weirdinggadget.block;

import net.minecraft.util.StringRepresentable;

public enum RenderType implements StringRepresentable {
    STATIC,
    DYNAMIC;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
