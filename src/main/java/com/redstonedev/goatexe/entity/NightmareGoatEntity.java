package com.redstonedev.goatexe.entity;

import com.redstonedev.goatexe.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class NightmareGoatEntity extends Monster implements IAnimatable {

    private static final EntityDataAccessor<Boolean> DATA_CLIMBING =
            SynchedEntityData.defineId(NightmareGoatEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_AGGRESSIVE =
            SynchedEntityData.defineId(NightmareGoatEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double SPEED_STALK = 0.28D; // lurks, keeps distance
    private static final double SPEED_AGGRO = 0.36D; // fast, but player sprint (~0.43 eff) outruns it

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int blockBreakCooldown = 20;
    private int modeCooldown;       // ticks until next possible mode flip
    private int goatSoundCooldown;
    private boolean pendingDespawn = false;
    private boolean clientChaseSoundStarted = false;

    public NightmareGoatEntity(EntityType<? extends NightmareGoatEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.maxUpStep = 1.0F;
        this.modeCooldown = 200 + this.random.nextInt(200);
        this.goatSoundCooldown = 100 + this.random.nextInt(300);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10000.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, SPEED_STALK)
                .add(Attributes.FOLLOW_RANGE, 96.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CLIMBING, false);
        this.entityData.define(DATA_AGGRESSIVE, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 96.0F, 1.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isClimbing() { return this.entityData.get(DATA_CLIMBING); }
    public void setClimbing(boolean c) { this.entityData.set(DATA_CLIMBING, c); }
    public boolean isAggressiveMode() { return this.entityData.get(DATA_AGGRESSIVE); }
    public void setAggressiveMode(boolean a) { this.entityData.set(DATA_AGGRESSIVE, a); }

    @Override public boolean onClimbable() { return this.isClimbing(); }

    // === No fall damage, no suffocation =======================================

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false; // immune to fall damage
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source == DamageSource.IN_WALL || source == DamageSource.FALL
                || source == DamageSource.DROWN || source == DamageSource.CACTUS
                || source == DamageSource.SWEET_BERRY_BUSH) {
            return true;
        }
        return super.isInvulnerableTo(source);
    }

    // === Tick =================================================================

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            boolean aggro = isAggressiveMode();
            if (!clientChaseSoundStarted && aggro) {
                clientChaseSoundStarted = true;
                net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                        net.minecraftforge.api.distmarker.Dist.CLIENT,
                        () -> () -> com.redstonedev.goatexe.client.sound.ClientChaseSoundStarter.start(this));
            }
            if (clientChaseSoundStarted && !aggro) clientChaseSoundStarted = false;
        }
    }

    @Override
    public void aiStep() {
        // Clean deferred despawn before any AI ticks.
        if (!this.level.isClientSide && pendingDespawn) {
            this.discard();
            return;
        }
        super.aiStep();
        if (this.level.isClientSide) return;

        this.setClimbing(this.horizontalCollision);
        if (blockBreakCooldown > 0) blockBreakCooldown--;
        if (modeCooldown > 0) modeCooldown--;
        if (goatSoundCooldown > 0) goatSoundCooldown--;

        // Randomly flip between stalking and aggressive.
        if (modeCooldown <= 0) {
            if (isAggressiveMode()) {
                // Aggressive bursts are shorter; usually drop back to stalking.
                if (this.random.nextInt(100) < 70) setAggressiveMode(false);
                modeCooldown = 200 + this.random.nextInt(200);
            } else {
                // Mostly stalks; occasionally snaps into aggressive.
                if (this.random.nextInt(100) < 30) setAggressiveMode(true);
                modeCooldown = 200 + this.random.nextInt(300);
            }
            if (!isAggressiveMode()) stopChaseThemeForNearbyPlayers();
        }

        // Sync speed to mode.
        double target = isAggressiveMode() ? SPEED_AGGRO : SPEED_STALK;
        AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && Math.abs(attr.getBaseValue() - target) > 1e-6) {
            attr.setBaseValue(target);
        }

        // Stalking: keep some distance instead of attacking, and vanish if the player
        // looks directly at him (like the other stalkers).
        Player nearest = this.level.getNearestPlayer(this, 96.0D);
        if (!isAggressiveMode() && nearest != null) {
            if (isPlayerStaringAt(nearest)) {
                pendingDespawn = true; // disappears next tick when looked at
                return;
            }
            double dist = this.distanceTo(nearest);
            if (dist < 6.0D) {
                this.getNavigation().stop(); // lurk, don't close in
            }
        }

        // Breaks a bunch of blocks when blocked.
        if (blockBreakCooldown <= 0 && this.horizontalCollision && !isClimbing()) {
            tryBreakBlocks();
            blockBreakCooldown = 15 + this.random.nextInt(15);
        }

        // Randomly plays the vanilla Minecraft goat sound.
        if (goatSoundCooldown <= 0) {
            SoundEvent goat = this.random.nextInt(4) == 0
                    ? SoundEvents.GOAT_SCREAMING_AMBIENT : SoundEvents.GOAT_AMBIENT;
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    goat, SoundSource.HOSTILE, 1.2F, 0.8F + this.random.nextFloat() * 0.2F);
            goatSoundCooldown = 200 + this.random.nextInt(500);
        }
    }

    private boolean isPlayerStaringAt(Player p) {
        if (this.distanceTo(p) > 48.0D) return false;
        double dx = this.getX() - p.getX();
        double dy = this.getEyeY() - p.getEyeY();
        double dz = this.getZ() - p.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001D) return false;
        dx /= len; dy /= len; dz /= len;
        Vec3 look = p.getViewVector(1.0F);
        double dot = look.x * dx + look.y * dy + look.z * dz;
        return dot > 0.92D && p.hasLineOfSight(this);
    }

    private void tryBreakBlocks() {
        BlockPos center = this.blockPosition();
        BlockPos target = null;
        scan:
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = center.offset(dx, dy, dz);
                    if (canBreak(p)) { target = p; break scan; }
                }
            }
        }
        if (target != null) this.level.destroyBlock(target, true, this);
    }

    private boolean canBreak(BlockPos pos) {
        BlockState bs = this.level.getBlockState(pos);
        if (bs.isAir()) return false;
        if (bs.getDestroySpeed(this.level, pos) < 0) return false;
        Block b = bs.getBlock();
        return b != Blocks.BEDROCK && b != Blocks.BARRIER && b != Blocks.COMMAND_BLOCK
                && b != Blocks.STRUCTURE_BLOCK && b != Blocks.JIGSAW && b != Blocks.LIGHT
                && b != Blocks.END_PORTAL_FRAME && b != Blocks.END_PORTAL
                && b != Blocks.NETHER_PORTAL && b != Blocks.VOID_AIR;
    }

    // === Attack ===============================================================

    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        boolean ok = super.doHurtTarget(target);
        if (ok && target instanceof Player && !target.isAlive()) {
            pendingDespawn = true; // despawn next tick (safe), after killing a player
        }
        return ok;
    }

    // === Animations ===========================================================

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "loco", 4, this::locoPredicate));
    }

    private <E extends IAnimatable> PlayState locoPredicate(AnimationEvent<E> event) {
        boolean moving = event.isMoving();
        if (moving || isAggressiveMode()) {
            // NOTE: run key has no space in the source file.
            event.getController().setAnimation(
                    new AnimationBuilder().loop("animation.nightmare_goat.run"));
        } else {
            // NOTE: idle key DOES contain a space ("nightmare _goat") in the source file.
            event.getController().setAnimation(
                    new AnimationBuilder().loop("animation.nightmare _goat.idle"));
        }
        return PlayState.CONTINUE;
    }

    @Override public AnimationFactory getFactory() { return factory; }

    // === Sounds ===============================================================

    @Override protected float getSoundVolume() { return 1.0F; }

    private void stopChaseThemeForNearbyPlayers() {
        if (!(this.level instanceof ServerLevel)) return;
        ServerLevel sl = (ServerLevel) this.level;
        ResourceLocation sound = ModSounds.CHASE_THEME.get().getLocation();
        ClientboundStopSoundPacket pkt = new ClientboundStopSoundPacket(sound, SoundSource.HOSTILE);
        for (ServerPlayer sp : sl.players()) {
            if (sp.distanceToSqr(this) < 96.0D * 96.0D) sp.connection.send(pkt);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        stopChaseThemeForNearbyPlayers();
        super.remove(reason);
    }

    // === NBT ==================================================================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Aggressive", isAggressiveMode());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAggressiveMode(tag.getBoolean("Aggressive"));
    }
}
