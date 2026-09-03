package hi.util;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Missile steering/propulsion model adapted for CTAW from SuperbWarfare's
 * MissileProjectile implementations (GPL-3.0).
 * Source: https://github.com/Mercurows/SuperbWarfare
 */
public final class SuperbWarfareFlightModel {
    private SuperbWarfareFlightModel() {}

    public static Vec3 turnToward(Vec3 current, Vec3 desired, double maxDegrees) {
        if (desired == null || desired.lengthSqr() < 1.0E-8D) return normalizedOr(current, new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 from = normalizedOr(current, desired);
        Vec3 to = desired.normalize();
        double dot = Mth.clamp(from.dot(to), -1.0D, 1.0D);
        double angle = Math.acos(dot);
        double maxAngle = Math.toRadians(Math.max(0.0D, maxDegrees));
        if (angle <= maxAngle || angle < 1.0E-6D) return to;

        double t = maxAngle / angle;
        double sin = Math.sin(angle);
        if (Math.abs(sin) < 1.0E-5D) return from.lerp(to, t).normalize();
        double fromWeight = Math.sin((1.0D - t) * angle) / sin;
        double toWeight = Math.sin(t * angle) / sin;
        return from.scale(fromWeight).add(to.scale(toWeight)).normalize();
    }

    public static Vec3 applyThrustAndDrag(Vec3 current, Vec3 forward, double thrust, double drag,
                                          double minSpeed, double maxSpeed) {
        Vec3 direction = normalizedOr(forward, current);
        Vec3 velocity = current.add(direction.scale(thrust)).scale(drag);
        double speed = velocity.length();
        if (speed < 1.0E-6D) return direction.scale(minSpeed);
        double clamped = Mth.clamp(speed, minSpeed, maxSpeed);
        return velocity.scale(clamped / speed);
    }

    private static Vec3 normalizedOr(Vec3 value, Vec3 fallback) {
        if (value != null && value.lengthSqr() > 1.0E-8D) return value.normalize();
        if (fallback != null && fallback.lengthSqr() > 1.0E-8D) return fallback.normalize();
        return new Vec3(0.0D, 0.0D, 1.0D);
    }
}
