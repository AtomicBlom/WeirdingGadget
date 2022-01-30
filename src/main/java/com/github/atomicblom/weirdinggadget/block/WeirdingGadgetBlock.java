package com.github.atomicblom.weirdinggadget.block;

import com.github.atomicblom.weirdinggadget.Settings;
import com.github.atomicblom.weirdinggadget.TicketUtils;
import com.github.atomicblom.weirdinggadget.WeirdingGadgetMod;
import com.github.atomicblom.weirdinggadget.block.blockentity.WeirdingGadgetBlockEntity;
import com.github.atomicblom.weirdinggadget.chunkloading.Type;
import com.github.atomicblom.weirdinggadget.chunkloading.WeirdingGadgetChunkManager;
import com.github.atomicblom.weirdinggadget.library.BlockEntityTypeLibrary;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import java.util.List;

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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, BlockEntityTypeLibrary.weirding_gadget, WeirdingGadgetBlockEntity::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, RENDER);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.title"));
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.description")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.hat")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
        tooltip.add(new TranslatableComponent("block.weirdinggadget.weirding_gadget.tooltip.weird")
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof Player)) {
            return;
        }

        activateChunkLoader(level, pos, (Player)placer);
    }

    private static void activateChunkLoader(Level level, BlockPos pos, Player placer)
    {
        if (level.isClientSide) { return; }

        if (level == null) {
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

        final var ticket = WeirdingGadgetChunkManager.requestPlayerTicket(WeirdingGadgetMod.instance, placer.getName().getString(), level, Type.NORMAL);

        if (ticket == null) {
            //Player has requested too many tickets. Forge will log an issue here.
            return;
        }

        final var modData = ticket.getModData();
        modData.put("blockPosition", NbtUtils.writeBlockPos(pos));
        modData.putInt("size", Settings.SERVER.chunkLoaderWidth.get());

        TicketUtils.activateTicket(level, ticket);
    }

    @Override
    @Deprecated
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) { return InteractionResult.SUCCESS; }

        final var blockEntity = (WeirdingGadgetBlockEntity)level.getBlockEntity(pos);
        if (blockEntity == null) return InteractionResult.FAIL;

        if (blockEntity.isExpired() || !blockEntity.hasTicket(player)) {

            activateChunkLoader(level, pos, player);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntityTypeLibrary.weirding_gadget.create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Deprecated
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 5;
    }

    @Override
    @Deprecated
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (newState.getBlock() == this) return;
        final var blockEntity = (WeirdingGadgetBlockEntity)level.getBlockEntity(pos);
        if (blockEntity == null) return;

        blockEntity.expireAllTickets();

        super.onRemove(state, level, pos, newState, isMoving);
    }

    ///////////// Rendering //////////////

    final VoxelShape AABB = Shapes.create(
            3/16.0, 0/16.0, 3/16.0, 13/16.0, 16/16.0, 13/16.0
    );

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AABB;
    }
}
