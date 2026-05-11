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
import org.jetbrains.annotations.Nullable;

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
}