package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.TileEntity.WeirdingGadgetTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import javax.annotation.Nullable;

public class WeirdingGadgetBlock extends Block
{
    public WeirdingGadgetBlock()
    {
        super(Material.ROCK, MapColor.YELLOW);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (!(placer instanceof EntityPlayer)) {
            return;
        }

        activateChunkLoader(worldIn, pos, (EntityPlayer)placer);
    }

    private void activateChunkLoader(World worldIn, BlockPos pos, EntityPlayer placer)
    {
        final Ticket ticket = ForgeChunkManager.requestPlayerTicket(WeirdingGadgetMod.INSTANCE, "" + placer.getName(), worldIn, ForgeChunkManager.Type.NORMAL);

        if (ticket == null) {
            //Player has requested too many tickets. Forge will log an issue here.
            return;
        }


        final NBTTagCompound modData = ticket.getModData();
        modData.setTag("blockPosition", NBTUtil.createPosTag(pos));
        modData.setInteger("size", Settings.chunkLoaderWidth);

        TicketUtils.activateTicket(worldIn, ticket);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote) {return true;}

        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);

        if (tileEntity.isExpired()) {

            activateChunkLoader(worldIn, pos, playerIn);

            return true;
        }

        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new WeirdingGadgetTileEntity();
    }



    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);

        final Ticket ticket = tileEntity.getTicket();
        ForgeChunkManager.releaseTicket(ticket);
        super.breakBlock(worldIn, pos, state);
    }

    ///////////// Rendering //////////////

    @Override
    @Deprecated
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    @Deprecated
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        float one_pixel = 1/16f;

        return new AxisAlignedBB(3*one_pixel, 0, 3*one_pixel, 1-(3*one_pixel), 1, 1-(3*one_pixel));
    }

    ///////////// Networking /////////////////


    @Override
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param)
    {
        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        tileEntity.receiveClientEvent(id, param);
        return true;
    }
}
