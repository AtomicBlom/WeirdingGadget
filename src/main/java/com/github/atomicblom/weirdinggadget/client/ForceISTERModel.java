package com.github.atomicblom.weirdinggadget.client;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.*;
import java.util.function.Function;

public class ForceISTERModel implements IBakedModel {
    private final IModelConfiguration owner;

    public ForceISTERModel(IModelConfiguration owner, ItemOverrideList overrides) {

        this.owner = owner;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand) {
        return Lists.newArrayList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType type, MatrixStack mat)
    {
        TransformationMatrix matrix =
                PerspectiveMapWrapper.getTransforms(owner.getCombinedTransform()).getOrDefault(type, TransformationMatrix.identity());

        if (type == ItemCameraTransforms.TransformType.HEAD) {
            mat.translate(0, 0.71, 0);
            mat.scale(1.4f, 1.4f, 1.4f);
        }

        matrix.push(mat);
        return this;
    }

    public static class Loader implements IModelLoader<NullModelGeometry> {
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {

        }

        @Override
        public NullModelGeometry read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
            return new NullModelGeometry();
        }
    }

    public static class NullModelGeometry implements IModelGeometry<NullModelGeometry> {

        @Override
        public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation) {
            return new ForceISTERModel(owner, overrides);
        }

        @Override
        public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function modelGetter, Set missingTextureErrors) {
            return Lists.newArrayList();
        }
    }
}
