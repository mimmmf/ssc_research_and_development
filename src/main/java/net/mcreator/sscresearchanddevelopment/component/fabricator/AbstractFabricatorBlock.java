package net.mcreator.sscresearchanddevelopment.component.fabricator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public abstract class AbstractFabricatorBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    protected final Function<BlockState, VoxelShape> shapes = this.makeShapes();

    public AbstractFabricatorBlock(BlockBehaviour.Properties properties) {
        super(properties.sound(SoundType.METAL).strength(1.5f, 10f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    protected Function<BlockState, VoxelShape> makeShapes() {
        return this.getShapeForEachState(state -> box(0, 0, 0, 16, 24, 16));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return shapes.apply(state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) { return true; }

    @Override
    public int getLightBlock(BlockState state) { return 0; }

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

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof AbstractFabricatorBlockEntity be) {
            ItemStack stackInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            String materialType = FabricatorManager.getStorageKeyFromItem(stackInHand);

            if (materialType != null && be.supportsMaterial(materialType)) {
                if (!world.isClientSide) {
                    int amount = stackInHand.getCount();
                    be.addMaterial(materialType, amount);
                    stackInHand.shrink(amount);
                }
                return InteractionResult.SUCCESS;
            }

            if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
                be.resolveServer();
                player.openMenu(be, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}