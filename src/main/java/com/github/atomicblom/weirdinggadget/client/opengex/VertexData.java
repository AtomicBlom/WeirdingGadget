package com.github.atomicblom.weirdinggadget.client.opengex;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class VertexData
{
    public final Vector4f vertex;
    public final Vector3f normal;
    public final float[] texcoordArray;
    public final float[] colorArray;

    public VertexData(Vector4f vertex, Vector3f normal, float[] texcoordArray, float[] colorArray)
    {

        this.vertex = vertex;
        this.normal = normal;
        this.texcoordArray = texcoordArray;
        this.colorArray = colorArray;
    }
}
