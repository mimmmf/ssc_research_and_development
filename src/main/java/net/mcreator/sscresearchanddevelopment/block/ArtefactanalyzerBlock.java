package net.mcreator.sscresearchanddevelopment.block;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModItems;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactanalyzerBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ArtefactanalyzerBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public ArtefactanalyzerBlock(BlockBehaviour.Properties properties) {
		super(properties.sound(SoundType.METAL)
				.strength(1.0f, 10.0f)
				.lightLevel(state -> 8)
				.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Block.box(0, 0, 0, 16, 4, 16);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (world.isClientSide) return InteractionResult.SUCCESS;

		if (world.getBlockEntity(pos) instanceof ArtefactanalyzerBlockEntity be) {
			ItemStack stackInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
			ItemStack stackInBlock = be.getItem(0);

			if (stackInHand.isEmpty() && !stackInBlock.isEmpty()) {
				player.setItemInHand(InteractionHand.MAIN_HAND, stackInBlock);
				be.setItem(0, ItemStack.EMPTY);
				return InteractionResult.SUCCESS;
			}

			if (stackInHand.is(SscResearchAndDevelopmentModItems.ARTEFACT.get()) && stackInBlock.isEmpty()) {
				be.setItem(0, stackInHand.split(1));
				return InteractionResult.SUCCESS;
			}

			if (stackInHand.is(SscResearchAndDevelopmentModItems.ARTEFACT.get()) && !stackInBlock.isEmpty()) {
				ItemStack fromBlock = stackInBlock.copy();
				be.setItem(0, stackInHand.split(1));
				if (stackInHand.isEmpty()) {
					player.setItemInHand(InteractionHand.MAIN_HAND, fromBlock);
				} else {
					if (!player.getInventory().add(fromBlock)) {
						player.drop(fromBlock, false);
					}
				}
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ArtefactanalyzerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : (lvl, pos, st, be) -> {
			if (be instanceof ArtefactanalyzerBlockEntity analyzer) analyzer.tick();
		};
	}

	@Override
	public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
		if (world instanceof Level level && !level.isClientSide) {
			ILinkable.staticNotifyRemoval(level, pos);
		}
		super.destroy(world, pos, state);
	}
}