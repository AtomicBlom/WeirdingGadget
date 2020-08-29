package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.chunkloading.Type;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.github.atomicblom.weirdinggadget.library.TileEntityTypeLibrary;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WeirdingGadgetBlock extends Block
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<RenderType> RENDER = EnumProperty.create("render", RenderType.class);

    public WeirdingGadgetBlock(Properties properties)
    {
        super(properties);
        setDefaultState(getDefaultState().with(ACTIVE, false).with(RENDER, RenderType.STATIC));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, RENDER);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent("block.weirdinggadget.weirding_gadget.tooltip.title"));
        tooltip.add(new TranslationTextComponent("block.weirdinggadget.weirding_gadget.tooltip.description")
                .setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)));
        tooltip.add(new TranslationTextComponent("block.weirdinggadget.weirding_gadget.tooltip.hat")
                .setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)));
        tooltip.add(new TranslationTextComponent("block.weirdinggadget.weirding_gadget.tooltip.weird")
                .setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY)));
    }



    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof PlayerEntity)) {
            return;
        }

        activateChunkLoader(worldIn, pos, (PlayerEntity)placer);
    }

    private static void activateChunkLoader(World worldIn, BlockPos pos, PlayerEntity placer)
    {
        if (worldIn.isRemote) { return; }

        if (worldIn == null) {
            WeirdingGadgetMod.LOGGER.error("While attempting to active a Weirding Gadget, Somehow, the world was null?");
            return;
        }

        if (placer == null) {
            WeirdingGadgetMod.LOGGER.error("While attempting to active a Weirding Gadget, Somehow, the player was null?");
            return;
        }

        if (placer.getName() == null) {
            WeirdingGadgetMod.LOGGER.error("While attempting to active a Weirding Gadget, Somehow, the player's NAME was null?");
            return;
        }

        final WeirdingGadgetTicket ticket = WeirdingGadgetChunkManager.requestPlayerTicket(WeirdingGadgetMod.instance, placer.getName().getString(), worldIn, Type.NORMAL);

        if (ticket == null) {
            //Player has requested too many tickets. Forge will log an issue here.
            return;
        }

        final CompoundNBT modData = ticket.getModData();
        modData.put("blockPosition", NBTUtil.writeBlockPos(pos));
        modData.putInt("size", Settings.SERVER.chunkLoaderWidth.get());

        TicketUtils.activateTicket(worldIn, ticket);
    }

    @Override
    @Deprecated
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) { return ActionResultType.SUCCESS; }

        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        if (tileEntity == null) return ActionResultType.FAIL;

        if (tileEntity.isExpired() || !tileEntity.hasTicket(player)) {

            activateChunkLoader(worldIn, pos, player);

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypeLibrary.weirding_gadget.create();
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return 5;
    }

    @Override
    @Deprecated
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == this) return;
        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        if (tileEntity == null) return;

        tileEntity.expireAllTickets();

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    ///////////// Rendering //////////////

    final VoxelShape AABB = Block.makeCuboidShape(
            3, 0, 3, 13, 16, 13
    );

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return AABB;
    }

    ///////////// Networking /////////////////


    @Override
    @Deprecated
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
        final WeirdingGadgetTileEntity tileEntity = (WeirdingGadgetTileEntity)worldIn.getTileEntity(pos);
        if (tileEntity == null) return false;

        tileEntity.receiveClientEvent(id, param);
        return true;
    }
}
