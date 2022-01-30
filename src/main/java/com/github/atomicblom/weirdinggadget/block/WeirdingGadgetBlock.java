package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.tileentity.WeirdingGadgetTileEntity;
import com.github.atomicblom.weirdinggadget.chunkloading.Type;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetTicket;
import com.github.atomicblom.weirdinggadget.library.TileEntityTypeLibrary;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WeirdingGadgetBlock extends BaseEntityBlock
{
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<RenderType> RENDER = EnumProperty.create("render", RenderType.class);

    public WeirdingGadgetBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        registerDefaultState(this.getStateDefinition().any().setValue(ACTIVE, false).setValue(RENDER, RenderType.STATIC));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, TileEntityTypeLibrary.weirding_gadget, WeirdingGadgetTileEntity::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, RENDER);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter levelIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.title"));
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.description")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.hat")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.weird")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
    }

    @Override
    public void setPlacedBy(Level levelIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof Player)) {
            return;
        }

        activateChunkLoader(levelIn, pos, (Player)placer);
    }

    private static void activateChunkLoader(Level levelIn, BlockPos pos, Player placer)
    {
        if (levelIn.isClientSide) { return; }

        if (levelIn == null) {
            WeirdingGadgetMod.LOGGER.error("While attempting to active a Weirding Gadget, Somehow, the level was null?");
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

        final var ticket = WeirdingGadgetChunkManager.requestPlayerTicket(WeirdingGadgetMod.instance, placer.getName().getString(), levelIn, Type.NORMAL);

        if (ticket == null) {
            //Player has requested too many tickets. Forge will log an issue here.
            return;
        }

        final var modData = ticket.getModData();
        modData.put("blockPosition", NbtUtils.writeBlockPos(pos));
        modData.putInt("size", Settings.SERVER.chunkLoaderWidth.get());

        TicketUtils.activateTicket(levelIn, ticket);
    }

    @Override
    @Deprecated
    public InteractionResult use(BlockState state, Level levelIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (levelIn.isClientSide) { return InteractionResult.SUCCESS; }

        final var tileEntity = (WeirdingGadgetTileEntity)levelIn.getBlockEntity(pos);
        if (tileEntity == null) return InteractionResult.FAIL;

        if (tileEntity.isExpired() || !tileEntity.hasTicket(player)) {

            activateChunkLoader(levelIn, pos, player);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TileEntityTypeLibrary.weirding_gadget.create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_56255_) {
        return RenderShape.MODEL;
    }

    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(BlockState p_56265_) {
        return PushReaction.DESTROY;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 5;
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level levelIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == this) return;
        final var tileEntity = (WeirdingGadgetTileEntity)levelIn.getBlockEntity(pos);
        if (tileEntity == null) return;

        tileEntity.expireAllTickets();

        super.onRemove(state, levelIn, pos, newState, isMoving);
    }

    ///////////// Rendering //////////////

    final VoxelShape AABB = Shapes.create(
            3/16.0, 0/16.0, 3/16.0, 13/16.0, 16/16.0, 13/16.0
    );

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, BlockGetter levelIn, BlockPos pos, CollisionContext context) {
        return AABB;
    }
}
