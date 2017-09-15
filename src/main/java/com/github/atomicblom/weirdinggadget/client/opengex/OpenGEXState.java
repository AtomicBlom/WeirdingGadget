package com.github.atomicblom.weirdinggadget.client.opengex;

import com.github.atomicblom.weirdinggadget.client.opengex.ogex.OgexAnimation;
import java.util.Optional;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

public class OpenGEXState implements IModelState
{
    private final OgexAnimation animation;
    private final float time;
    private final IModelState state;

    public OpenGEXState(OgexAnimation animation, float time) {
        this(animation, time, null);
    }

    public OpenGEXState(OgexAnimation animation, float time, IModelState state) {
        this.animation = animation;
        this.time = time;
        this.state = state;
    }

    @Override
    public Optional<TRSRTransformation> apply(Optional<? extends IModelPart> part) {
        return state.apply(part);
    }

    public OgexAnimation getAnimation()
    {
        return animation;
    }

    public float getTime()
    {
        return time;
    }

    public IModelState getState()
    {
        return state;
    }
}
