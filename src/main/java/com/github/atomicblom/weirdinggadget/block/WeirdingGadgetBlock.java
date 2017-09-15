package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.client.opengex.OpenGEXAnimationFrameProperty;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import javax.annotation.Nullable;
import java.util.List;

public class WeirdingGadgetBlock extends Block
{
    public static final PropertyBool RENDER_DYNAMIC = PropertyBool.create("render_dynamic");

    public WeirdingGadgetBlock()
    {
        super(Material.ROCK, MapColor.YELLOW);
        setDefaultState(
                blockState
                        .getBaseState()
                        .withProperty(RENDER_DYNAMIC, false)
        );
        setHardness(3.0f);
        setResistance(5.0f);
        setSoundType(SoundType.STONE);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(this, new IProperty[]{RENDER_DYNAMIC}, new IUnlistedProperty[]{OpenGEXAnimationFrameProperty.instance});
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced)
    {
        tooltip.add(I18n.format("tile.weirdinggadget:weirding_gadget.tooltip"));
    }

    @Override
    @Deprecated
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (!(placer instanceof EntityPlayer)) {
            return;
        }

        activateChunkLoader(worldIn, pos, (EntityPlayer)placer);
    }

    private static void activateChunkLoader(World worldIn, BlockPos pos, EntityPlayer placer)
    {
        final Ticket ticket = ForgeChunkManager.requestPlayerTicket(WeirdingGadgetMod.INSTANCE, placer.getName(), worldIn, Type.NORMAL);

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
        if (tileEntity == null) return false;

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
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return 5;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        if (tileEntity == null) return;

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
        final float onePixel = 1/16.0f;

        return new AxisAlignedBB(3*onePixel, 0, 3*onePixel, 1- 3*onePixel, 1, 1- 3*onePixel);
    }

    ///////////// Networking /////////////////

    @Override
    @Deprecated
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int id, int param)
    {
        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        if (tileEntity == null) return false;

        tileEntity.receiveClientEvent(id, param);
        return true;
    }
}
