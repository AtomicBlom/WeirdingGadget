package com.github.atomicblom.weirdinggadget;

import net.minecraft.util.ResourceLocation;

public class Reference {
    public static final ResourceLocation ItemLoader = resource("force_ister");

    public static class Block {
        public static final ResourceLocation weirding_gadget = resource("weirding_gadget");

        private Block() {}
    }

    public static class Capability {
        public static final ResourceLocation weirding_gadget = resource("weirding_gadget_capability");

        private Capability() {}
    }


    private static ResourceLocation resource(String name) {
        return new ResourceLocation(WeirdingGadgetMod.MOD_ID, name);
    }

    private Reference() {}
}
