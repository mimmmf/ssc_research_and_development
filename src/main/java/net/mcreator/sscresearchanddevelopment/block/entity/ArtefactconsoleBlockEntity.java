package net.mcreator.sscresearchanddevelopment.block.entity;

import net.mcreator.sscresearchanddevelopment.SscResearchAndDevelopmentMod;
import net.mcreator.sscresearchanddevelopment.interfaces.ILinkable;
import net.mcreator.sscresearchanddevelopment.world.inventory.ArtefactConsoleGUIMenu;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentModBlockEntities;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

import java.util.Optional;
import java.util.stream.IntStream;

public class ArtefactconsoleBlockEntity extends BlockEntity implements ILinkable, MenuProvider {
	private BlockPos targetPos = null;
	private boolean connected = false;

	public ArtefactconsoleBlockEntity(BlockPos position, BlockState state) {
		super(SscResearchAndDevelopmentModBlockEntities.ARTEFACTCONSOLE.get(), position, state);
	}

	// --- СВЯЗЬ С АНАЛИЗАТОРОМ ---

	/**
	 * Безопасно достает предмет из привязанного анализатора.
	 * Используется в GUI для отображения артефакта.
	 */
	public ItemStack getItemFromLinkedAnalyzer() {
		if (this.level == null || this.targetPos == null || !this.connected) {
			return ItemStack.EMPTY;
		}

		// Проверяем чанк, чтобы не грузить лишнего
		if (!this.level.isLoaded(this.targetPos)) {
			return ItemStack.EMPTY;
		}

		// Обращаемся к анализатору как к Container
		if (this.level.getBlockEntity(this.targetPos) instanceof Container container) {
			return container.getItem(0);
		}

		return ItemStack.EMPTY;
	}

	// --- СОХРАНЕНИЕ ДАННЫХ (Исправленный NBT) ---
	@Override
	protected void saveAdditional(ValueOutput valueOutput) {
		super.saveAdditional(valueOutput);
		if (this.targetPos != null) {
			valueOutput.putLong("linked_target_pos", this.targetPos.asLong());
		}
		valueOutput.putBoolean("connected", this.connected);
	}

	@Override
	protected void loadAdditional(ValueInput valueInput) {
		super.loadAdditional(valueInput);
		var optPos = valueInput.getLong("linked_target_pos");

		if (optPos.isPresent()) {
			this.targetPos = BlockPos.of(optPos.get());
			this.connected = true;
		} else {
			this.targetPos = null;
			this.connected = false;
		}
	}

	// --- СЕТЕВАЯ СИНХРОНИЗАЦИЯ ---
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	// --- ИНТЕРФЕЙС ILinkable ---
	@Override
	public void setLinkedPos(@Nullable BlockPos pos) {
		this.targetPos = pos;
		this.connected = (pos != null);
		this.setChanged();
		if (this.level != null) {
			this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public Optional<BlockPos> getLinkedPos() {
		return Optional.ofNullable(this.targetPos);
	}

	@Override
	public boolean tryLink(Player player, ItemStack tool) {
		// Логика: если консоль уже занята, нельзя привязать новую без разрыва старой
		if (this.connected) return false;
		this.playLinkEffects(true);
		return true;
	}

	@Override
	public boolean tryUnlink(Player player, ItemStack tool) {
		if (!this.connected) return false;
		this.setLinkedPos(null);
		this.playLinkEffects(false);
		return true;
	}

	@Override
	public void onLinkedBlockRemoved(BlockPos sourcePos) {
		if (sourcePos.equals(this.targetPos)) {
			this.setLinkedPos(null); // Сбрасываем связь, если анализатор сломали
		}
	}

	private void playLinkEffects(boolean linked) {
		if (level instanceof ServerLevel serverLevel) {
			ParticleOptions particle = linked ? ParticleTypes.HAPPY_VILLAGER : ParticleTypes.SMOKE;
			serverLevel.sendParticles(particle,
					worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5,
					15, 0.2, 0.2, 0.2, 0.05);

			SoundEvent sound = linked ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.LEAF_LITTER_BREAK;
			level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	// --- ТИКЕР ---
	public static void tick(Level level, BlockPos pos, BlockState state, ArtefactconsoleBlockEntity blockEntity) {
		if (level.isClientSide) return;

		// Периодическая проверка связи (например, раз в секунду)
		if (level.getGameTime() % 20 == 0 && blockEntity.connected && blockEntity.targetPos != null) {
			// Если блок по координатам связи перестал быть анализатором — рвем связь
			if (!(level.getBlockEntity(blockEntity.targetPos) instanceof ArtefactanalyzerBlockEntity)) {
				blockEntity.setLinkedPos(null);
			}
		}
	}
	@org.jetbrains.annotations.Nullable
	@Override
	public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return new ArtefactConsoleGUIMenu(i, inventory, this.worldPosition);
	}
	@Override
	public Component getDisplayName() {
		return Component.literal("Консоль артефактов");
	}
//	@org.jetbrains.annotations.Nullable
//	@Override
//	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
//		return new ArtefactConsoleGUIMenu(id, inventory, this.worldPosition);
//	}
}