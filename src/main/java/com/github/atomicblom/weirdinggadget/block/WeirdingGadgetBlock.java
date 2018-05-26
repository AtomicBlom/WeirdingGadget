package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetFuel;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.client.opengex.OpenGEXAnimationFrameProperty;
import com.github.atomicblom.weirdinggadget.library.BlockLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
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
    public static final PropertyEnum<ChunkLoaderType> LOADER_TYPE =  PropertyEnum.create("type", ChunkLoaderType.class);

    public WeirdingGadgetBlock()
    {
        super(Material.ROCK, MapColor.YELLOW);
        setDefaultState(
                blockState
                        .getBaseState()
                        .withProperty(LOADER_TYPE, ChunkLoaderType.NORMAL)
                        .withProperty(RENDER_DYNAMIC, false)
        );
        setHardness(3.0f);
        setResistance(5.0f);
        setSoundType(SoundType.STONE);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, ChunkLoaderType.NORMAL.getMetadata()));
        items.add(new ItemStack(this, 1, ChunkLoaderType.SPOT.getMetadata()));
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new ExtendedBlockState(
                this,
                new IProperty[]{RENDER_DYNAMIC, LOADER_TYPE},
                new IUnlistedProperty[]{OpenGEXAnimationFrameProperty.instance}
                );
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
        if (meta > 1) meta = 0;
        return getDefaultState().withProperty(LOADER_TYPE, ChunkLoaderType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(LOADER_TYPE).getMetadata();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(LOADER_TYPE, ChunkLoaderType.byMetadata(meta));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        if (!(placer instanceof EntityPlayer)) {
            return;
        }
        if (!Settings.enableFuel) {
            activateChunkLoader(worldIn, pos, (EntityPlayer) placer);
        }
    }

    private static void activateChunkLoader(World worldIn, BlockPos pos, EntityPlayer placer)
    {
        final Ticket ticket = ForgeChunkManager.requestPlayerTicket(WeirdingGadgetMod.INSTANCE, placer.getName(), worldIn, Type.NORMAL);

        if (ticket == null) {
            //Player has requested too many tickets. Forge will log an issue here.
            return;
        }

        IBlockState blockState = worldIn.getBlockState(pos);
        if (blockState.getBlock() != BlockLibrary.weirding_gadget) return;

        ChunkLoaderType loaderType = blockState.getValue(LOADER_TYPE);

        final NBTTagCompound modData = ticket.getModData();
        modData.setTag("blockPosition", NBTUtil.createPosTag(pos));
        modData.setInteger("size", loaderType.getChunkDiameter());

        TicketUtils.activateTicket(worldIn, ticket);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (worldIn.isRemote) {return true;}

        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        if (tileEntity == null) return false;

        boolean success = false;
        if (Settings.enableFuel) {
            final ItemStack heldItem = playerIn.getHeldItem(hand);
            final ResourceLocation registryName = heldItem.getItem().getRegistryName();
            assert registryName != null;
            final String resourceDomain = registryName.getResourceDomain();
            final String resourcePath = registryName.getResourcePath();

            for (final WeirdingGadgetFuel weirdingGadgetFuel : Settings.getFuelList())
            {
                if (resourceDomain.equals(weirdingGadgetFuel.domain) &&
                        resourcePath.equals(weirdingGadgetFuel.item)) {
                    if (weirdingGadgetFuel.ignoreMetadata || weirdingGadgetFuel.metadata == heldItem.getMetadata()) {
                        tileEntity.addFuelTicks(weirdingGadgetFuel.ticks);
                        if (!playerIn.isCreative()) {
                            heldItem.shrink(1);
                        }
                        success = true;
                        break;
                    }
                }
            }

            long ticksRemaining = tileEntity.getFuelTicks();
            ticksRemaining /= (20 * 60); //ticks per minute

            long minutes = ticksRemaining % 60;
            ticksRemaining /= 60;
            long hours = ticksRemaining % 24;
            ticksRemaining /= 24;
            long days = ticksRemaining;

            playerIn.sendStatusMessage(new TextComponentTranslation("tile.weirdinggadget:weirding_gadget.time_remaining", days, hours, minutes), true);
        }

        if (tileEntity.canActivate() && (tileEntity.isExpired() || !tileEntity.hasTicket(playerIn))) {
            activateChunkLoader(worldIn, pos, playerIn);

            success = true;
        }

        return success;
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

        tileEntity.expireAllTickets();

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
