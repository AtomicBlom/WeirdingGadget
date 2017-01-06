package com.github.atomicblom.weirdinggadget;

import net.minecraft.util.ResourceLocation;

public class Reference {
    public static class Block {
        public static final ResourceLocation weirding_gadget = resource("weirding_gadget");


        private Block() {}
    }


    private static ResourceLocation resource(String name) {
        return new ResourceLocation(WeirdingGadgetMod.MODID, name);
    }

    private Reference() {}
}
