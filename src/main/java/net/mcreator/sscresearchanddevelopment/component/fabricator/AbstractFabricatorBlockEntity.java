package net.mcreator.sscresearchanddevelopment.component.fabricator;

import com.mojang.serialization.Codec;
import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.ServerInstance;
import net.mcreator.sscresearchanddevelopment.interfaces.IServerLinked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;

import java.util.*;

import static net.mcreator.sscresearchanddevelopment.component.ResearchServerComponents.NetworkSavedData.UUID_CODEC;

public abstract class AbstractFabricatorBlockEntity extends BlockEntity implements MenuProvider, IServerLinked {

    public abstract String getFabricatorType();
    public abstract List<String> getSupportedMaterials();

    protected UUID linkedServerId = null;
    protected final List<ResourceLocation> craftQueue = new ArrayList<>();
    @Nullable protected ResourceLocation activeRecipeId = null;
    protected int craftProgress = 0;
    protected int craftTotalTime = 0;
    protected static final Codec<Map<String, Integer>> MATERIALS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
    protected final Map<String, Integer> materials = new HashMap<>();
    protected final Set<String> clientUnlockedTechs = new HashSet<>();

    public AbstractFabricatorBlockEntity(BlockEntityType<?> type, BlockPos position, BlockState state) {
        super(type, position, state);
        for (String mat : getSupportedMaterials()) {
            this.materials.put(mat, 0);
        }
    }

    public boolean supportsMaterial(String type) {
        return getSupportedMaterials().contains(type);
    }

    public void addMaterial(String type, int amount) {
        if (!supportsMaterial(type)) return;
        this.materials.put(type, this.materials.getOrDefault(type, 0) + amount);
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public Map<String, Integer> getMaterials() { return this.materials; }
    public Set<String> getClientUnlockedTechs() { return this.clientUnlockedTechs; }

    @Override
    @Nullable
    public UUID getConnectedServerId() {
        return this.linkedServerId;
    }

    @Override
    public void setConnectedServerId(@Nullable UUID id) {
        this.linkedServerId = id;
        this.setChanged();
        this.syncToClients();
    }

    @Override
    @Nullable
    public Level getLinkedLevel() {
        return this.level;
    }
    @Nullable
    public ServerInstance getLinkedServer() {
        return this.resolveServer();
    }

    public boolean canCraftRecipe(FabricatorRecipe recipe) {
        if (!recipe.getCategory().equals(this.getFabricatorType())) {
            return false;
        }

        List<String> requiredTechs = recipe.getRequiredTechnologies();
        if (requiredTechs != null && !requiredTechs.isEmpty()) {
            if (this.level.isClientSide) {
                return clientUnlockedTechs.containsAll(requiredTechs);
            } else {
                ServerInstance server = getLinkedServer();
                if (server == null) return false;
                return server.getUnlockedTechs().containsAll(requiredTechs);
            }
        }
        return true;
    }

    public void tryQueueRecipe(ServerPlayer player, ResourceLocation recipeId) {
        var recipe = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().get(recipeId);
        if (recipe == null) return;

        if (!canCraftRecipe(recipe)) return;
        if (!hasEnoughResources(recipe)) return;

        for (MaterialCost cost : recipe.getMaterials()) {
            var values = cost.ingredient().getValues();
            if (values.size() == 0) continue;
            Item item = values.get(0).value();
            String key = FabricatorManager.getStorageKeyFromItem(new ItemStack(item));
            if (key != null) {
                this.materials.put(key, Math.max(0, this.materials.get(key) - cost.count()));
            }
        }

        this.craftQueue.add(recipeId);
        this.setChanged();
        this.syncToClients();
    }

    private boolean hasEnoughResources(FabricatorRecipe recipe) {
        for (MaterialCost cost : recipe.getMaterials()) {
            var values = cost.ingredient().getValues();
            if (values.size() == 0) return false;
            Item item = values.get(0).value();
            String key = FabricatorManager.getStorageKeyFromItem(new ItemStack(item));
            if (key != null) {
                if (this.materials.getOrDefault(key, 0) < cost.count()) return false;
            } else {
                return false;
            }
        }
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AbstractFabricatorBlockEntity be) {
        if (level.isClientSide) return;

        boolean changed = false;

        if (be.activeRecipeId == null && !be.craftQueue.isEmpty()) {
            be.activeRecipeId = be.craftQueue.remove(0);
            var recipe = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().get(be.activeRecipeId);
            if (recipe != null) {
                be.craftTotalTime = recipe.getCraftTime();
                be.craftProgress = 0;
                changed = true;
            } else {
                be.activeRecipeId = null;
            }
        }

        if (be.activeRecipeId != null) {
            be.craftProgress++;
            if (be.craftProgress >= be.craftTotalTime) {
                var recipe = SscResearchAndDevelopmentMod.FabricatorDataHandler.MANAGER.getRecipes().get(be.activeRecipeId);
                if (recipe != null) {
                    Direction facing = state.getValue(AbstractFabricatorBlock.FACING);
                    double spawnX = pos.getX() + 0.5 + facing.getStepX() * 0.7;
                    double spawnY = pos.getY() + 0.3;
                    double spawnZ = pos.getZ() + 0.5 + facing.getStepZ() * 0.7;

                    ItemEntity itemEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, recipe.getResult().copy());
                    itemEntity.setDeltaMovement(facing.getStepX() * 0.1, 0.1, facing.getStepZ() * 0.1);
                    level.addFreshEntity(itemEntity);
                }

                be.activeRecipeId = null;
                be.craftProgress = 0;
                be.craftTotalTime = 0;
                changed = true;
            }

            if (be.craftProgress % 10 == 0 || changed) {
                be.syncToClients();
            }
        } else if (changed) {
            be.syncToClients();
        }

        if (changed) {
            be.setChanged();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, AbstractFabricatorBlockEntity be) {
        if (be.activeRecipeId != null && be.craftProgress < be.craftTotalTime) {
            be.craftProgress++;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("materials_map", MATERIALS_CODEC, this.materials);
        if (this.linkedServerId != null) {
            output.store("linked_server_id", UUID_CODEC, this.linkedServerId);
        }
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.materials.clear();
        this.materials.putAll(valueInput.read("materials_map", MATERIALS_CODEC).orElse(new HashMap<>()));
        this.linkedServerId = valueInput.read("linked_server_id", UUID_CODEC).orElse(null);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithFullMetadata(registries); }

    public void syncToClients() {
        if (this.level instanceof ServerLevel serverLevel) {
            List<String> techs = new ArrayList<>();
            ServerInstance server = getLinkedServer();
            if (server != null) {
                techs.addAll(server.getUnlockedTechs());
            }

            S2CSyncCraftingState packet = new S2CSyncCraftingState(
                    this.worldPosition,
                    this.materials,
                    this.craftQueue,
                    Optional.ofNullable(this.activeRecipeId),
                    this.craftProgress,
                    this.craftTotalTime,
                    techs
            );
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel, new ChunkPos(this.worldPosition), packet
            );
        }
    }

    public void syncFromPacket(Map<String, Integer> mats, List<ResourceLocation> q, @Nullable ResourceLocation active, int prog, int total, List<String> techs) {
        this.materials.clear();
        this.materials.putAll(mats);
        this.craftQueue.clear();
        this.craftQueue.addAll(q);
        this.activeRecipeId = active;
        this.craftProgress = prog;
        this.craftTotalTime = total;

        this.clientUnlockedTechs.clear();
        this.clientUnlockedTechs.addAll(techs);
    }

    public float getCraftProgressPercent(float partialTicks) {
        if (craftTotalTime <= 0) return 0f;
        float current = craftProgress;
        if (activeRecipeId != null && craftProgress < craftTotalTime) {
            current += partialTicks;
        }
        return Math.min(1.0f, current / (float) craftTotalTime);
    }

    @Nullable
    public ResourceLocation getActiveRecipeId() { return activeRecipeId; }
    public int getCraftProgress() { return craftProgress; }
    public int getCraftTotalTime() { return craftTotalTime; }

    public void withdrawMaterial(ServerPlayer player, String type, int requestedAmount) {
        int currentAmount = this.materials.getOrDefault(type, 0);
        if (currentAmount <= 0) return;

        int toWithdraw = Math.min(currentAmount, requestedAmount);

        ItemStack resultStack = FabricatorManager.getItemFromStorageKey(type);

        if (!resultStack.isEmpty() && toWithdraw > 0) {
            this.materials.put(type, currentAmount - toWithdraw);

            resultStack.setCount(toWithdraw);

            Direction facing = getBlockState().getValue(AbstractFabricatorBlock.FACING);
            double x = worldPosition.getX() + 0.5 + facing.getStepX() * 0.7;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5 + facing.getStepZ() * 0.7;

            ItemEntity entity = new ItemEntity(level, x, y, z, resultStack);
            entity.setDeltaMovement(facing.getStepX() * 0.2, 0.1, facing.getStepZ() * 0.2);
            level.addFreshEntity(entity);

            this.setChanged();
            this.syncToClients();
        }
    }
}