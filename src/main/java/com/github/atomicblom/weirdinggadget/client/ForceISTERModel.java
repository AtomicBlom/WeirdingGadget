package com.github.atomicblom.weirdinggadget.client;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

public class ForceISTERModel implements BakedModel {
    private final IModelConfiguration owner;

    public ForceISTERModel(IModelConfiguration owner) {

        this.owner = owner;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return Lists.newArrayList();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        //noinspection ConstantConditions -- No texture, this is only used on your head and will never be called
        return null;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    public BakedModel handlePerspective(ItemTransforms.TransformType type, PoseStack mat)
    {
        var matrix =
                PerspectiveMapWrapper.getTransforms(owner.getCombinedTransform()).getOrDefault(type, Transformation.identity());

        if (type == ItemTransforms.TransformType.HEAD) {
            mat.translate(0, 0.71, 0);
            mat.scale(1.4f, 1.4f, 1.4f);
        }

        matrix.push(mat);
        return this;
    }

    public static class Loader implements IModelLoader<NullModelGeometry> {
        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {

        }

        @Override
        public NullModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            return new NullModelGeometry();
        }
    }

    public static class NullModelGeometry implements IModelGeometry<NullModelGeometry> {

        @Override
        public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return Lists.newArrayList();
        }

        @Override
        public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
            return new ForceISTERModel(owner);
        }
    }
}
