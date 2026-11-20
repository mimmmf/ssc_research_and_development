package net.mcreator.sscresearchanddevelopment.entity;

import net.mcreator.sscresearchanddevelopment.init.SscResearchAndDevelopmentSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PortalEntity extends Entity {
    private static final int LIGHT_LEVEL = 12;
    private static final double EXIT_MARGIN = 0.2;

    @Nullable
    private UUID partnerId;
    private final Set<UUID> occupants = new HashSet<>();

    public PortalEntity(EntityType<? extends PortalEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    /** Связывает два портала в пару с обеих сторон. */
    public void linkTo(PortalEntity other) {
        this.partnerId = other.getUUID();
        other.partnerId = this.getUUID();
    }

    /**
     * Вызывается сразу после добавления в мир. Все сущности внутри зоны
     * получают статус "занятых", чтобы не телепортироваться при установке.
     */
    public void captureOccupants() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (Entity e : serverLevel.getEntities(this, getBoundingBox(),
                entity -> !(entity instanceof PortalEntity))) {
            occupants.add(e.getUUID());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        BlockPos pos = this.blockPosition();
        if (this.level().getBlockState(pos).isAir()) {
            this.level().setBlock(pos,
                    Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, LIGHT_LEVEL), 3);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            PortalEntity partner = findPartner(serverLevel);
            if (partner != null) {
                processEntities(serverLevel, partner);
            }
        }
    }

    private PortalEntity findPartner(ServerLevel serverLevel) {
        if (partnerId == null) {
            return null;
        }
        return serverLevel.getEntity(partnerId) instanceof PortalEntity portal && portal.isAlive()
                ? portal
                : null;
    }

    private void processEntities(ServerLevel serverLevel, PortalEntity partner) {
        AABB zone = this.getBoundingBox();

        occupants.removeIf(id -> {
            Entity e = serverLevel.getEntity(id);
            return e == null || !e.isAlive() || !e.getBoundingBox().intersects(zone);
        });

        for (Entity e : serverLevel.getEntities(this, zone, this::canTeleport)) {
            if (!occupants.contains(e.getUUID())) {
                transport(serverLevel, e, partner);
                serverLevel.playSound(null,
                        new BlockPos((int)partner.position().x, (int)partner.position().y, (int)partner.position().z),
                        SscResearchAndDevelopmentSounds.TELEPORT_ARRIVAL.get(), SoundSource.NEUTRAL);
                serverLevel.playSound(null,
                        new BlockPos((int)e.position().x, (int)e.position().y, (int)e.position().z),
                        SscResearchAndDevelopmentSounds.TELEPORT_ARRIVAL.get(), SoundSource.NEUTRAL);
            }
        }
    }

    private boolean canTeleport(Entity e) {
        if (e instanceof PortalEntity || e.isRemoved() || !e.isAlive() || e.isPassenger()) {
            return false;
        }
        return !(e instanceof Player player && player.isSpectator());
    }

    /**
     * Телепортирует сущность в парный портал, сохраняя её текущий поворот
     * (головы и тела) и вектор скорости без изменений.
     */
    private void transport(ServerLevel serverLevel, Entity e, PortalEntity to) {
        float yaw = e.getYRot();
        float pitch = e.getXRot();

        double destX = to.getX();
        double destY = to.getY();
        double destZ = to.getZ();

        Vec3 v = e.getDeltaMovement();
        Vec3 newVelocity = new Vec3(v.x, v.y, v.z);

        if (e.isVehicle()) {
            e.ejectPassengers();
        }

        if (e instanceof ServerPlayer player) {
            player.connection.teleport(destX, destY, destZ, yaw, pitch);
            player.setDeltaMovement(newVelocity);
            player.setYHeadRot(yaw);
            player.yBodyRot = yaw;
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        } else {
            e.move(MoverType.SELF, new Vec3(destX - e.getX(), destY - e.getY(), destZ - e.getZ()));
            if (e instanceof LivingEntity living) {
                living.setYHeadRot(yaw);
                living.yBodyRot = yaw;
            }
            e.setDeltaMovement(newVelocity);
            e.hasImpulse = true;
        }
        e.resetFallDistance();

        occupants.add(e.getUUID());
        to.occupants.add(e.getUUID());
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()
                && (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED)) {
            BlockPos pos = this.blockPosition();
            if (this.level().getBlockState(pos).is(Blocks.LIGHT) && !hasOtherPortalAt(pos)) {
                this.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        super.remove(reason);
    }

    private boolean hasOtherPortalAt(BlockPos pos) {
        return !this.level().getEntitiesOfClass(PortalEntity.class, new AABB(pos),
                e -> e != this && e.isAlive()).isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float v) {
        return false;
    }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.partnerId = parseUuid(valueInput.getStringOr("Partner", ""));
        occupants.clear();
        for (String s : valueInput.getStringOr("Occupants", "").split(",")) {
            UUID id = parseUuid(s);
            if (id != null) {
                occupants.add(id);
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putString("Partner", partnerId == null ? "" : partnerId.toString());
        valueOutput.putString("Occupants", occupants.stream()
                .map(UUID::toString)
                .collect(Collectors.joining(",")));
    }

    @Nullable
    private static UUID parseUuid(String s) {
        if (s == null || s.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}