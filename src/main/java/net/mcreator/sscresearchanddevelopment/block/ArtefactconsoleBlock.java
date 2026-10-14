package net.mcreator.sscresearchanddevelopment.block;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSoundSets;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.Shapes;
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

import java.util.function.Function;

public class ArtefactconsoleBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	private final Function<BlockState, VoxelShape> shapes = this.makeShapes();

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
		return shapes.apply(state);
	}
	private Function<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			return switch (state.getValue(FACING)) {
				case NORTH -> Shapes.or(box(2, 0, 12, 14, 18, 16), box(1, 0, 4, 15, 13, 11), box(1, 13, 1, 15, 16, 13), box(0.5, 13.77111, 7.39283, 15.5, 24.77111, 15));
				case EAST -> Shapes.or(box(0, 0, 2, 4, 18, 14), box(5, 0, 1, 12, 13, 15), box(3, 13, 1, 15, 16, 15), box(1, 13.77111, 0.5, 8.60717, 24.77111, 15.5));
				case WEST -> Shapes.or(box(12, 0, 2, 16, 18, 14), box(4, 0, 1, 11, 13, 15), box(1, 13, 1, 13, 16, 15), box(7.39283, 13.77111, 0.5, 15, 24.77111, 15.5));
				default -> Shapes.or(box(2, 0, 0, 14, 18, 4), box(1, 0, 5, 15, 13, 12), box(1, 13, 3, 15, 16, 15), box(0.5, 13.77111, 1, 15.5, 24.77111, 8.60717));
			};
		});
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