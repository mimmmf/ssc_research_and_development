package net.mcreator.sscresearchanddevelopment.block;

import net.mcreator.sscresearchanddevelopment.block.entity.ResearchConsoleBlockEntity;
import net.mcreator.sscresearchanddevelopment.component.subcomponents.InteractionStage;
import net.mcreator.sscresearchanddevelopment.enums.ToolType;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlocks;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModItems;
import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSoundSets;
import net.mcreator.sscresearchanddevelopment.interfaces.IInteractable;
import net.mcreator.sscresearchanddevelopment.interfaces.IMultiStageBlock;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ResearchConsoleBlock extends Block implements EntityBlock, IMultiStageBlock {
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	private final Function<BlockState, VoxelShape> shapes = this.makeShapes();
	public static final BooleanProperty PANEL_OPEN = BooleanProperty.create("panel_open");
	private final List<InteractionStage> deconstructionStages = new ArrayList<>();
	private final List<InteractionStage> constructionStages = new ArrayList<>();

	public ResearchConsoleBlock(BlockBehaviour.Properties properties) {
		super(properties.sound(SoundType.METAL).strength(1f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PANEL_OPEN, false));
	}

	private Function<BlockState, VoxelShape> makeShapes() {
		return this.getShapeForEachState(state -> {
			return switch (state.getValue(FACING)) {
				default -> box(0, 0, 0, 16, 24, 16);
				case NORTH -> box(0, 0, 0, 16, 24, 16);
				case EAST -> box(0, 0, 0, 16, 24, 16);
				case WEST -> box(0, 0, 0, 16, 24, 16);
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
		builder.add(FACING, PANEL_OPEN);
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
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
		return new ResearchConsoleBlockEntity(blockPos, blockState);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof ResearchConsoleBlockEntity consoleBE) {
			SoundEvent randomSound = SscResearchAndDevelopmentSoundSets.KEYBOARD.get(level.getRandom().nextInt(SscResearchAndDevelopmentSoundSets.KEYBOARD.size())).get();
			level.playSound(null, pos, randomSound, SoundSource.BLOCKS, 1.0f, 1.0f);
			player.openMenu((MenuProvider) be, pos);
		}
		return InteractionResult.CONSUME;
	}
	private void setupStages() {
		deconstructionStages.add(new InteractionStage(
				ToolType.SCREWING, null, 20, new ItemStack(SscResearchAndDevelopmentModItems.MULTITOOL.get()),
				state -> state.setValue(PANEL_OPEN, true)
		));

		deconstructionStages.add(new InteractionStage(
				ToolType.CUTTING, null, 30, new ItemStack(SscResearchAndDevelopmentModItems.MULTITOOL.get()),
				state -> SscResearchAndDevelopmentModBlocks.AUTOLATHE.get().defaultBlockState()
		));
	}

	@Override
	public InteractionStage getCurrentStage(BlockState state, ToolType tool, ItemStack heldItem) {
		if (this.deconstructionStages.isEmpty() && this.constructionStages.isEmpty()) {
			this.setupStages();
		}
		boolean isPanelOpen = state.getValue(PANEL_OPEN);

		if (!isPanelOpen) {
			if (deconstructionStages.get(0).matches(tool, heldItem)) {
				return deconstructionStages.get(0);
			}
		}
		else {
			if (deconstructionStages.get(1).matches(tool, heldItem)) {
				return deconstructionStages.get(1);
			}
		}

		return null;
	}

	@Override
	public List<InteractionStage> getDeconstructionStages() { return deconstructionStages; }
	@Override
	public List<InteractionStage> getConstructionStages() { return constructionStages; }
}