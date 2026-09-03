package hi.block.entity;

import hi.block.AntiAircraftLauncherBlock;
import hi.entity.C75RocketEntity;
import hi.config.RadarServerConfig;
import hi.init.CreateTheAirWarsModBlockEntities;
import hi.init.CreateTheAirWarsModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.PacketDistributor;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.*;

public class AntiAircraftLauncherBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    private int rockets = 0;
    private boolean isBlacklist = true;
    private final Set<String> targets = new HashSet<>(); // Format: "modid" or "modid:entity_type" or "aeronautics"
    private int cooldown = 0;

    protected ScrollValueBehaviour detectionAngleBehaviour;

    // Track targets globally across all SAM launchers so only 1 rocket is launched per target
    private static final Map<UUID, UUID> GLOBAL_ACTIVE_ROCKET_TARGETS = new HashMap<>();

    public static boolean isTargetLockedByAnyLauncher(net.minecraft.server.level.ServerLevel serverLevel, UUID targetUUID) {
        if (targetUUID == null) return false;

        GLOBAL_ACTIVE_ROCKET_TARGETS.entrySet().removeIf(entry -> {
            Entity rocket = serverLevel.getEntity(entry.getValue());
            return rocket == null || !rocket.isAlive();
        });

        if (GLOBAL_ACTIVE_ROCKET_TARGETS.containsKey(targetUUID)) {
            return true;
        }

        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity.isAlive() && entity instanceof C75RocketEntity c75) {
                if (targetUUID.equals(c75.getTargetUUID()) || targetUUID.equals(c75.getTargetSubLevelId())) {
                    GLOBAL_ACTIVE_ROCKET_TARGETS.put(targetUUID, c75.getUUID());
                    return true;
                }
            }
        }
        return false;
    }

    public AntiAircraftLauncherBlockEntity(BlockPos pos, BlockState state) {
        super(CreateTheAirWarsModBlockEntities.ANTI_AIRCRAFT_LAUNCHER.get(), pos, state);
    }

    public boolean addRocket() {
        if (rockets < 2) {
            rockets++;
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return false;
    }

    public int getRockets() {
        return rockets;
    }

    public void setRockets(int count) {
        this.rockets = count;
        setChanged();
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public void setBlacklist(boolean blacklist) {
        isBlacklist = blacklist;
        setChanged();
    }

    public Set<String> getTargets() {
        return targets;
    }

    public void addTarget(String target) {
        targets.add(target);
        setChanged();
    }

    public void removeTarget(String target) {
        targets.remove(target);
        setChanged();
    }

    public void clearTargets() {
        targets.clear();
        setChanged();
    }

    public void openInterface(Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, inv, p) -> new hi.world.inventory.AntiAircraftLauncherMenu(id, inv, new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer()).writeBlockPos(worldPosition)),
                    Component.translatable("block.create_the_air_wars.anti_aircraft_launcher")
                ), buf -> buf.writeBlockPos(worldPosition));
            }
        }

    public void tick(Level lvl, BlockPos pos, BlockState state) {
        if (lvl.isClientSide) return;

        if (cooldown > 0) {
            cooldown--;
        }

        if (cooldown == 0 && (rockets > 0 || isCreativeCrateNearby(lvl, pos)) && (isBlacklist || !targets.isEmpty())) {
            scanAndLaunch(lvl, pos, state);
        }
    }

    private boolean isCreativeCrateNearby(Level lvl, BlockPos pos) {
        // Search in a 1 block radius for creative crate
        for (BlockPos offset : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            BlockState state = lvl.getBlockState(offset);
            String blockId = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
            if (blockId.contains("creative_crate") || blockId.contains("creative_box")) {
                return true;
            }
        }
        return false;
    }

    private void scanAndLaunch(Level lvl, BlockPos pos, BlockState state) {
        if (!(lvl instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

        double range = RadarServerConfig.getDetectionRange();
        double currentAngle = getDetectionAngle();
        double halfAngleRad = Math.toRadians(currentAngle / 2.0D);
        double cosLimit = Math.cos(halfAngleRad);

        AABB searchBox = new AABB(pos).inflate(range);
        List<Entity> candidates = lvl.getEntitiesOfClass(Entity.class, searchBox, e -> {
            if (e == null || !e.isAlive() || e.position().y < pos.getY()) return false;
            
            // Check if entity is already targeted by ANY SAM rocket from ANY launcher
            if (isTargetLockedByAnyLauncher(serverLevel, e.getUUID())) return false;

            // Check if within configured upward cone
            Vec3 toEntity = e.position().subtract(Vec3.atCenterOf(pos)).normalize();
            if (toEntity.y < cosLimit) return false;

            // Check blacklist/whitelist target match
            return isTargetAllowed(e);
        });
        if (!candidates.isEmpty()) {
            Entity target = candidates.get(0); // Select the first detected entity
            launchRocket(lvl, pos, state, target);
            return;
        }

        // Sublevel targeting
        boolean isAeronauticsAllowed = targets.contains("aeronautics");
        isAeronauticsAllowed = isBlacklist ? !isAeronauticsAllowed : isAeronauticsAllowed;

        if (isAeronauticsAllowed) {
            dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer container = dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer.getContainer(serverLevel);
            if (container != null) {
                hi.util.Aim9xTargetingHelper.TrackedSubLevelTarget bestSubLevel = null;
                double closestDistanceSqr = Double.MAX_VALUE;
                for (dev.ryanhcode.sable.sublevel.ServerSubLevel subLevel : container.getAllSubLevels()) {
                    if (subLevel == null || subLevel.isRemoved()) continue;
                    
                    // Check if already targeted by any SAM rocket across all launchers
                    if (isTargetLockedByAnyLauncher(serverLevel, subLevel.getUniqueId())) continue;

                    hi.util.Aim9xTargetingHelper.TrackedSubLevelTarget target = hi.util.Aim9xTargetingHelper.resolveTarget(serverLevel, subLevel.getUniqueId());
                    if (target == null) continue;

                    Vec3 targetCenter = target.position();
                    if (targetCenter.y < pos.getY()) continue;

                    double distSqr = targetCenter.distanceToSqr(Vec3.atCenterOf(pos));
                    if (distSqr > range * range) continue;

                    // Check cone
                    Vec3 toEntity = targetCenter.subtract(Vec3.atCenterOf(pos)).normalize();
                    if (toEntity.y < cosLimit) continue;

                    if (distSqr < closestDistanceSqr) {
                        closestDistanceSqr = distSqr;
                        bestSubLevel = target;
                    }
                }
                if (bestSubLevel != null) {
                    launchRocketAtSubLevel(serverLevel, pos, state, bestSubLevel);
                }
            }
        }
    }

    private boolean isTargetAllowed(Entity entity) {
        if (entity instanceof hi.entity.C75RocketEntity) {
            return false;
        }
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (entityId == null) return false;
        String modid = entityId.getNamespace();
        String typeId = entityId.toString();

        boolean matchesList = false;
        
        // Aeronautics contraption check
        boolean isAeronautics = entity.getClass().getSimpleName().contains("Contraption") 
                || entity.getClass().getSimpleName().contains("Ship")
                || modid.equals("aeronautics")
                || modid.equals("vs_aeronautics")
                || modid.equals("valkyrienskies");
                
        if (isAeronautics && targets.contains("aeronautics")) {
            matchesList = true;
        }

        if (targets.contains(modid) || targets.contains(typeId)) {
            matchesList = true;
        }

        // Projectiles check
        if (entity instanceof Projectile && targets.contains("projectiles")) {
            matchesList = true;
        }

        // Player check
        if (entity instanceof Player player) {
            String name = player.getGameProfile().getName();
            if (targets.contains("players") || targets.contains("players:" + name) || targets.contains("players:" + name.toLowerCase())) {
                matchesList = true;
            }
        }

        return isBlacklist ? !matchesList : matchesList;
    }

    private void launchRocket(Level lvl, BlockPos pos, BlockState state, Entity target) {
        Direction facing = state.getValue(AntiAircraftLauncherBlock.FACING);
        Vec3 localSpawn = Vec3.atCenterOf(pos).add(0.0D, 1.0D, 0.0D);

        // Angle 45 degrees up in the direction block is facing
        Vec3 horizontal = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
        Vec3 direction = horizontal.scale(0.7071D).add(0.0D, 0.7071D, 0.0D).normalize(); // 45 degrees pitch

        C75RocketEntity rocket = new C75RocketEntity(CreateTheAirWarsModEntities.C75_ROCKET.get(), lvl);
        rocket.setPos(localSpawn.x, localSpawn.y, localSpawn.z);
        rocket.setInitialDirection(direction);
        rocket.setLaunchStartPos(localSpawn);
        rocket.setTargetEntity(target);
        
        rocket.shoot(direction.x, direction.y, direction.z, (float) C75RocketEntity.INITIAL_FORWARD_SPEED, 0.0F);
        rocket.refreshOrientation();

        lvl.addFreshEntity(rocket);

        // Register global tracking lock
        GLOBAL_ACTIVE_ROCKET_TARGETS.put(target.getUUID(), rocket.getUUID());

        if (!isCreativeCrateNearby(lvl, pos)) {
            rockets--;
        }

        cooldown = 40; // 2 seconds cooldown (40 ticks)
        setChanged();
        lvl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void launchRocketAtSubLevel(Level lvl, BlockPos pos, BlockState state, hi.util.Aim9xTargetingHelper.TrackedSubLevelTarget target) {
        Direction facing = state.getValue(AntiAircraftLauncherBlock.FACING);
        Vec3 localSpawn = Vec3.atCenterOf(pos).add(0.0D, 1.0D, 0.0D);

        // Angle 45 degrees up in the direction block is facing
        Vec3 horizontal = Vec3.atLowerCornerOf(facing.getNormal()).normalize();
        Vec3 direction = horizontal.scale(0.7071D).add(0.0D, 0.7071D, 0.0D).normalize(); // 45 degrees pitch

        C75RocketEntity rocket = new C75RocketEntity(CreateTheAirWarsModEntities.C75_ROCKET.get(), lvl);
        rocket.setPos(localSpawn.x, localSpawn.y, localSpawn.z);
        rocket.setInitialDirection(direction);
        rocket.setLaunchStartPos(localSpawn);
        rocket.setTargetSubLevel(target.id());
        
        rocket.shoot(direction.x, direction.y, direction.z, (float) C75RocketEntity.INITIAL_FORWARD_SPEED, 0.0F);
        rocket.refreshOrientation();

        lvl.addFreshEntity(rocket);

        // Register global tracking lock
        GLOBAL_ACTIVE_ROCKET_TARGETS.put(target.id(), rocket.getUUID());

        if (!isCreativeCrateNearby(lvl, pos)) {
            rockets--;
        }

        cooldown = 40; // 2 seconds cooldown (40 ticks)
        setChanged();
        lvl.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        detectionAngleBehaviour = new ScrollValueBehaviour(
            Component.translatable("create_the_air_wars.recipe.detection_angle"),
            this,
            new LauncherValueBoxTransform()
        );
        detectionAngleBehaviour.between(10, 180);
        detectionAngleBehaviour.setValue(170);
        detectionAngleBehaviour.withCallback(val -> {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        });
        behaviours.add(detectionAngleBehaviour);
    }

    public double getDetectionAngle() {
        return detectionAngleBehaviour != null ? detectionAngleBehaviour.getValue() : 170.0D;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("create_the_air_wars.goggles.anti_aircraft_launcher.title").withStyle(ChatFormatting.WHITE));

        boolean creative = isCreativeCrateNearby(level, worldPosition);
        String rocketsStr = creative ? "∞" : String.valueOf(rockets);

        tooltip.add(Component.translatable("create_the_air_wars.goggles.anti_aircraft_launcher.rockets").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.literal(rocketsStr).withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.translatable("create_the_air_wars.goggles.anti_aircraft_launcher.detection_angle").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.literal(String.format("%d°", (int)getDetectionAngle())).withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.translatable("create_the_air_wars.goggles.anti_aircraft_launcher.filters").withStyle(ChatFormatting.GRAY)
            .append(Component.literal(" "))
            .append(Component.literal(String.valueOf(targets.size())).withStyle(ChatFormatting.AQUA)));

        if (isPlayerSneaking) {
            tooltip.add(Component.translatable("create_the_air_wars.goggles.anti_aircraft_launcher.hint.load").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("create_the_air_wars.goggles.anti_aircraft_launcher.hint.config").withStyle(ChatFormatting.DARK_GRAY));
        }

        return true;
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Rockets", rockets);
        tag.putBoolean("IsBlacklist", isBlacklist);
        ListTag targetList = new ListTag();
        for (String target : targets) {
            targetList.add(StringTag.valueOf(target));
        }
        tag.put("Targets", targetList);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        rockets = tag.getInt("Rockets");
        isBlacklist = tag.getBoolean("IsBlacklist");
        targets.clear();
        if (tag.contains("Targets", 9)) {
            ListTag targetList = tag.getList("Targets", 8);
            for (int i = 0; i < targetList.size(); i++) {
                targets.add(targetList.getString(i));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static class LauncherValueBoxTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return new Vec3(8.0D / 16.0D, 8.0D / 16.0D, 15.5D / 16.0D);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
