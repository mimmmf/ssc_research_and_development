package net.mcreator.sscresearchanddevelopment.block;

import net.mcreator.sscresearchanddevelopment.block.entity.AutolatheBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.fabricator.AbstractFabricatorBlock;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static net.mcreator.sscresearchanddevelopment.block.ArtefactconsoleBlock.createTickerHelper;

public class AutolatheBlock extends AbstractFabricatorBlock {
	public AutolatheBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new AutolatheBlockEntity(blockPos, blockState);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, SscResearchAndDevelopmentModBlockEntities.AUTOLATHE.get(),
				level.isClientSide ? AutolatheBlockEntity::clientTick : AutolatheBlockEntity::tick);
	}

	@Override
	protected Function<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			return switch (state.getValue(FACING)) {
				case NORTH -> Shapes.or(
						box(0, 0, 4, 16, 9, 16),
						box(5, 12, 0, 16, 20, 16)
				);
				case EAST -> Shapes.or(
						box(0, 0, 0, 12, 9, 16),
						box(0, 12, 0, 16, 20, 11)
				);
				case WEST -> Shapes.or(
						box(4, 0, 0, 16, 9, 16),
						box(0, 12, 5, 16, 20, 16)
				);
				default -> Shapes.or(
						box(0, 0, 0, 16, 9, 12),
						box(0, 12, 0, 11, 20, 16)
				);
			};
		});
	}
}