package net.mcreator.sscresearchanddevelopment.block;

import net.mcreator.sscresearchanddevelopment.block.entity.RNDServerBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentDataComponents;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class RNDServerBlock extends Block implements EntityBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	private final Function<BlockState, VoxelShape> shapes = this.makeShapes();

	public RNDServerBlock(BlockBehaviour.Properties properties) {
		super(properties.sound(SoundType.METAL).strength(1f, 10f).noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	private Function<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			return switch (state.getValue(FACING)) {
				case NORTH -> Shapes.or(
						box(2, 0, 2, 14, 12, 15),
						box(1, 12, 1, 15, 20, 15)
				);
				case EAST -> Shapes.or(
						box(1, 0, 2, 14, 12, 14),
						box(1, 12, 1, 15, 20, 15)
				);
				case WEST -> Shapes.or(
						box(2, 0, 2, 15, 12, 14),
						box(1, 12, 1, 15, 20, 15)
				);
				default -> Shapes.or(
						box(2, 0, 1, 14, 12, 14),
						box(1, 12, 1, 15, 20, 15)
				);
			};
		});
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shapes.apply(state);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if(!level.isClientSide && level.getBlockEntity(pos) instanceof RNDServerBlockEntity be) {
			be.initializeServer();;
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new RNDServerBlockEntity(blockPos, blockState);
	}

	@Override
	public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
		if (world instanceof Level level && !level.isClientSide) {
			NetworkSavedData data = NetworkSavedData.get(level);
			if (data != null) {
				data.removeServerAt(pos);
			}
		}
		super.destroy(world, pos, state);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) {
			return stack.is(SscResearchAndDevelopmentModItems.RESEARCHDISK.get()) ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}
		if (stack.is(SscResearchAndDevelopmentModItems.RESEARCHDISK.get())) {
			if (level.getBlockEntity(pos) instanceof RNDServerBlockEntity be) {
				NetworkSavedData data = NetworkSavedData.get(level);
				ServerInstance server = data.getServer(be.serverId);
				if (server != null) {
					int pointsToAdd = stack.getOrDefault(SscResearchAndDevelopmentDataComponents.SCORE.get(), 0);
					server.setPoints(server.getPoints() + pointsToAdd);
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);
					}

					return InteractionResult.SUCCESS;
				}
			}
		}
		return InteractionResult.PASS;
	}
}