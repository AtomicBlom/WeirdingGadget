package com.github.atomicblom.weirdinggadget.client.opengex;

import com.github.atomicblom.weirdinggadget.Logger;
import com.github.atomicblom.weirdinggadget.client.opengex.ogex.*;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;
import org.apache.logging.log4j.Level;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public enum OpenGEXModelLoader implements ICustomModelLoader {
    INSTANCE;

    private IResourceManager manager;
    private final Map<ResourceLocation, OgexScene> cache = Maps.newHashMap();

    OpenGEXModelLoader() {
        enabledDomains.add("steamnsteel_opengex");
    }

    private Set<String> enabledDomains = Sets.newHashSet();

    public void addDomain(String domain)
    {
        enabledDomains.add(domain.toLowerCase());
    }

    public boolean accepts(ResourceLocation modelLocation) {
        return enabledDomains.contains(modelLocation.getNamespace().toLowerCase()) &&
                modelLocation.getPath().toLowerCase().endsWith(".ogex");
    }

    public void onResourceManagerReload(IResourceManager manager)
    {
        this.manager = manager;
        cache.clear();
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException {
        ResourceLocation file = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath());
        if(!cache.containsKey(file))
        {
            try
            {
                IResource resource;
                try
                {
                    resource = manager.getResource(file);
                }
                catch(FileNotFoundException e)
                {
                    if(modelLocation.getPath().startsWith("models/block/"))
                        resource = manager.getResource(new ResourceLocation(file.getNamespace(), "models/item/" + file.getPath().substring("models/block/".length())));
                    else if(modelLocation.getPath().startsWith("models/item/"))
                        resource = manager.getResource(new ResourceLocation(file.getNamespace(), "models/block/" + file.getPath().substring("models/item/".length())));
                    else throw e;
                }

                final OgexParser ogexParser = new OgexParser();
                Reader reader = new InputStreamReader(resource.getInputStream());
                final OgexScene ogexScene = ogexParser.parseScene(reader);


                cache.put(file, ogexScene);
            }
            catch(IOException e)
            {
                FMLLog.log(Level.ERROR, e, "Exception loading model %s with OGEX loader, skipping", modelLocation);
                cache.put(file, null);
                throw e;
            }
        }

        final OgexScene scene = cache.get(file);
        if (scene == null) {
            return ModelLoaderRegistry.getMissingModel();
        }



        if (scene.getMetrics().getUp() == Axis.Z) {
            boolean useNormalCycleMatrix = true;

            ModContainer ctm = null;
            ModContainer foamfix = null;

            for (ModContainer modContainer : Loader.instance().getModList()) {
                if ("ctm".equals(modContainer.getModId())) {
                    ctm = modContainer;
                    Logger.info(ctm.toString());
                } else if ("foamfix".equals(modContainer.getModId())) {
                    foamfix = modContainer;
                    Logger.info(foamfix.toString());
                }
                if (foamfix != null && ctm != null) break;
            }

            try {
                if (ctm != null && foamfix != null) {
                    Logger.warning("Detected ctm and foamfix, we might need to hack the model");
                    //First detect if we have a bad version of CTM
                    //Chisel versions have the format of MC<version>-<chiselversion>
                    String ctmVersionString = ctm.getVersion();
                    if (ctmVersionString.contains("-")) {
                        int dashOffset = ctmVersionString.indexOf("-") + 1;
                        ctmVersionString = ctmVersionString.substring(dashOffset);
                    }
                    ArtifactVersion ctmVersion = new DefaultArtifactVersion(ctmVersionString);
                    VersionRange badVersions;
                    try {
                        badVersions = VersionRange.createFromVersionSpec("[0.3.3,)");
                    } catch (Exception e) {
                        throw new RuntimeException("Well that didn't work");
                    }

                    if (badVersions.containsVersion(ctmVersion)) {
                        Logger.warning("Look out, we have CTM > v3.3, better check for foamfix anarchy.");

                        try {
                            Class foamFixShared = Class.forName("pl.asie.foamfix.shared.FoamFixShared");
                            boolean isCoreMod = foamFixShared.getField("isCoremod").getBoolean(null);
                            Object foamFixConfig = foamFixShared.getField("config").get(null);
                            boolean clWipeModelCache = foamFixConfig.getClass().getField("clWipeModelCache").getBoolean(foamFixConfig);

                            if (isCoreMod && clWipeModelCache) {
                                Logger.warning("Detected dastardly combo of foamfix anarchy + ctm 2.3, using alternative rotations");
                                useNormalCycleMatrix = false;
                            } else {
                                Logger.info("Foamfix was not configured as a coremod or clWipeModelCache was not enabled.");
                            }
                        } catch (Exception e) {
                            Logger.warning(e.toString());
                            Logger.warning("Nevermind, we couldn't find FoamFixShared.");
                        }

                    } else {
                        Logger.warning("Nope. It's an older version of ctm lib. *phew*");
                    }
                }
            } catch (Exception e) {
                Logger.severe("There was an error detecting if we needed to work around a ctm 3.3/foamfix anarchy bug.");
                Logger.severe(e.toString());
            }


            float[] foamFixCycleMatrix = {
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    1, 0, 0, 0,
                    0, 0, 0, 1
            };

            float[] normalcycleMatrix = {
                    0, 0, 1, 0,
                    1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 0, 1
            };

            float[] cycleMatrix = useNormalCycleMatrix ? normalcycleMatrix : foamFixCycleMatrix;

            for (final OgexNode ogexNode : scene)
            {
                OgexMatrixTransform matrixTransform = new OgexMatrixTransform();
                matrixTransform.setMatrix(cycleMatrix);
                ogexNode.getTransforms().add(0, matrixTransform);
            }
        }

        //Just making it compile.
        return new OpenGEXModel(file, scene);
    }



}
