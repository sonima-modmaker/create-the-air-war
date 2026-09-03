package hi.util;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.UUID;

public final class SableCoordinateHelper {
    private SableCoordinateHelper() {
    }

    public static Vec3 projectOut(Level level, Vec3 localPos) {
        if (level == null || localPos == null) {
            return localPos;
        }
        try {
            Vector3d projected = dev.ryanhcode.sable.Sable.HELPER.projectOutOfSubLevel(level, new Vector3d(localPos.x, localPos.y, localPos.z));
            return new Vec3(projected.x, projected.y, projected.z);
        } catch (Throwable ignored) {
            return localPos;
        }
    }

    public static Vec3 projectDirectionOut(Level level, Vec3 localOrigin, Vec3 localDirection) {
        if (level == null || localOrigin == null || localDirection == null) {
            return localDirection;
        }
        Vec3 worldOrigin = projectOut(level, localOrigin);
        Vec3 worldTip = projectOut(level, localOrigin.add(localDirection));
        Vec3 worldDirection = worldTip.subtract(worldOrigin);
        if (worldDirection.lengthSqr() < 1.0E-8D) {
            return localDirection;
        }
        return worldDirection.normalize();
    }

    public static Vec3 projectDirectionIn(Level level, Vec3 localOrigin, Vec3 worldDirection) {
        if (level == null || localOrigin == null || worldDirection == null) {
            return worldDirection;
        }
        try {
            SubLevel subLevel = dev.ryanhcode.sable.Sable.HELPER.getContaining(level, localOrigin);
            if (subLevel == null || subLevel.logicalPose() == null) {
                return worldDirection;
            }
            Vec3 localDirection = subLevel.logicalPose().transformNormalInverse(worldDirection);
            return localDirection.lengthSqr() > 1.0E-8D ? localDirection.normalize() : worldDirection;
        } catch (Throwable ignored) {
            return worldDirection;
        }
    }

    public static Vec3 localToWorld(SubLevel subLevel, Vec3 localPos) {
        if (subLevel == null || localPos == null) {
            return localPos;
        }
        try {
            Pose3dc pose = subLevel.logicalPose();
            return pose != null ? pose.transformPosition(localPos) : localPos;
        } catch (Throwable ignored) {
            return localPos;
        }
    }

    public static Vec3 worldToLocal(SubLevel subLevel, Vec3 worldPos) {
        if (subLevel == null || worldPos == null) {
            return worldPos;
        }
        try {
            Pose3dc pose = subLevel.logicalPose();
            return pose != null ? pose.transformPositionInverse(worldPos) : worldPos;
        } catch (Throwable ignored) {
            return worldPos;
        }
    }

    public static ServerLevel resolveCollisionLevel(Level level) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        if (serverLevel.getServer() == null) {
            return serverLevel;
        }

        ServerLevel sameDimension = serverLevel.getServer().getLevel(serverLevel.dimension());
        if (sameDimension != null && sameDimension != serverLevel) {
            return sameDimension;
        }

        ServerLevel overworld = serverLevel.getServer().getLevel(Level.OVERWORLD);
        if (overworld != null && overworld != serverLevel) {
            return overworld;
        }

        return serverLevel;
    }

    public static SubLevel resolveSubLevel(ServerLevel level, UUID subLevelId) {
        if (level == null || subLevelId == null) {
            return null;
        }
        try {
            ServerSubLevelContainer container = ServerSubLevelContainer.getContainer(level);
            return container != null ? container.getSubLevel(subLevelId) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
