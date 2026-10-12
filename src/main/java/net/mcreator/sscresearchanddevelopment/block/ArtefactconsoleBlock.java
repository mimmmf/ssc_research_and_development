package net.mcreator.sscresearchanddevelopment.block;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSoundSets;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.*;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.sscresearchanddevelopment.block.entity.ArtefactconsoleBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ArtefactconsoleBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	public ArtefactconsoleBlock(BlockBehaviour.Properties properties) {
		super(properties.sound(SoundType.METAL)
				.strength(1.0f, 10.0f)
				.lightLevel(state -> 4)
				.noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Block.box(0, 0, 0, 16, 24, 16);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (world.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof ArtefactconsoleBlockEntity consoleBE) {
			SoundEvent randomSound = SscResearchAndDevelopmentSoundSets.KEYBOARD.get(world.getRandom().nextInt(SscResearchAndDevelopmentSoundSets.KEYBOARD.size())).get();
			world.playSound(null, pos, randomSound, SoundSource.BLOCKS, 1.0f, 1.0f);
			player.openMenu((MenuProvider) be, pos);
		}
		return InteractionResult.CONSUME;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ArtefactconsoleBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : createTickerHelper(type, SscResearchAndDevelopmentModBlockEntities.ARTEFACTCONSOLE.get(),
				ArtefactconsoleBlockEntity::tick);
	}

	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> actualType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
		return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
	}

	@Override
	public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
		if (world instanceof Level level && !level.isClientSide) {
			ILinkable.staticNotifyRemoval(level, pos);
		}
		super.destroy(world, pos, state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

}