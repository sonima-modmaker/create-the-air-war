package hi.client.camera;

import com.mojang.blaze3d.pipeline.TextureTarget;
import hi.block.entity.CameraBlockEntity;
import hi.entity.FpvDroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CameraFeedTextureManager {
    private static final int MAX_CACHED_FEEDS = 12;
    private static final long STALE_FEED_TICKS = 40L;
    private static final long HOUSEKEEPING_INTERVAL_TICKS = 20L;
    private static final Map<Long, FeedTexture> FEEDS = new LinkedHashMap<>();
    private static final Map<Long, UUID> DRONE_FEEDS = new LinkedHashMap<>();
    private static final Set<Long> REQUESTED = new LinkedHashSet<>();
    private static long nextId = 0L;
    private static ResourceKey<Level> lastLevelKey;
    private static ResourceLocation blankFeedLocation;
    private static DynamicTexture blankFeedTexture;
    private static int blankFeedSize = -1;
    private static long lastHousekeepingTick = Long.MIN_VALUE;

    private CameraFeedTextureManager() {
    }

    public static ResourceLocation requestFeed(long cameraPosLong) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return getBlankFeedLocation();
        }
        ResourceKey<Level> currentKey = mc.level.dimension();
        if (lastLevelKey != currentKey) {
            clear();
            lastLevelKey = currentKey;
        }

        FeedTexture texture = FEEDS.computeIfAbsent(cameraPosLong, key -> createFeed());
        long gameTime = mc.level.getGameTime();
        texture.setLastRequestedTick(gameTime);
        REQUESTED.add(cameraPosLong);
        return texture.location();
    }

    public static ResourceLocation requestDroneFeed(UUID droneId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || droneId == null) {
            return getBlankFeedLocation();
        }
        ResourceKey<Level> currentKey = mc.level.dimension();
        if (lastLevelKey != currentKey) {
            clear();
            lastLevelKey = currentKey;
        }

        long key = droneFeedKey(droneId);
        FeedTexture texture = FEEDS.computeIfAbsent(key, ignored -> createFeed());
        long gameTime = mc.level.getGameTime();
        texture.setLastRequestedTick(gameTime);
        DRONE_FEEDS.put(key, droneId);
        REQUESTED.add(key);
        return texture.location();
    }

    public static ResourceLocation getExistingFeedOrBlank(long cameraPosLong) {
        FeedTexture texture = FEEDS.get(cameraPosLong);
        return texture != null ? texture.location() : getBlankFeedLocation();
    }

    public static ResourceLocation getBlankFeedLocation() {
        Minecraft mc = Minecraft.getInstance();
        int feedSize = CameraFeedRuntimeSettings.getFeedResolution();
        if (blankFeedLocation == null || blankFeedTexture == null || blankFeedSize != feedSize) {
            if (blankFeedTexture != null) {
                blankFeedTexture.close();
            }
            DynamicTexture texture = new DynamicTexture(feedSize, feedSize, true);
            if (texture.getPixels() != null) {
                texture.getPixels().fillRect(0, 0, feedSize, feedSize, 0xFF000000); // Solid black (ABGR)
                texture.upload();
            }
            blankFeedTexture = texture;
            blankFeedSize = feedSize;
            blankFeedLocation = mc.getTextureManager().register("ctaw_camera_feed/blank", texture);
        }
        return blankFeedLocation;
    }

    public static void renderRequestedFeeds() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            clear();
            return;
        }

        long gameTime = mc.level.getGameTime();
        pruneFeeds(gameTime);
        if (REQUESTED.isEmpty()) {
            return;
        }

        ArrayList<Long> stale = new ArrayList<>();

        for (Long key : REQUESTED) {
            FeedTexture feed = FEEDS.get(key);
            if (feed == null) {
                continue;
            }
            long baseUpdateInterval = Math.max(1L, Math.round(20.0D / (double) CameraFeedRuntimeSettings.getFeedFps()));
            if (gameTime - feed.lastRenderTick() < baseUpdateInterval) {
                continue;
            }
            UUID droneId = DRONE_FEEDS.get(key);
            if (droneId != null) {
                FpvDroneEntity drone = findDrone(mc, droneId);
                if (drone == null) {
                    stale.add(key);
                    continue;
                }
                CameraFeedRenderer.render(feed, drone);
                feed.setLastRenderTick(gameTime);
                feed.setLastRequestedTick(gameTime);
                break;
            }

            net.minecraft.core.BlockPos cameraPos = net.minecraft.core.BlockPos.of(key);
            if (!(mc.level.getBlockEntity(cameraPos) instanceof CameraBlockEntity cameraBlockEntity)) {
                stale.add(key);
                continue;
            }

            CameraFeedRenderer.render(feed, cameraBlockEntity);
            feed.setLastRenderTick(gameTime);
            feed.setLastRequestedTick(gameTime);
            break;
        }

        stale.forEach(CameraFeedTextureManager::removeFeed);
        REQUESTED.clear();
    }

    private static FeedTexture createFeed() {
        Minecraft mc = Minecraft.getInstance();
        int feedSize = CameraFeedRuntimeSettings.getFeedResolution();
        TextureTarget renderTarget = new TextureTarget(feedSize, feedSize, true, Minecraft.ON_OSX);
        TextureTarget displayTarget = new TextureTarget(feedSize, feedSize, false, Minecraft.ON_OSX);
        net.minecraft.client.renderer.texture.AbstractTexture texture = new FramebufferTexture(displayTarget);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("ctaw", "camera_feed_" + (nextId++));
        mc.getTextureManager().register(location, texture);
        return new FeedTexture(texture, renderTarget, displayTarget, location, -20L, Long.MIN_VALUE);
    }

    private static void removeFeed(Long key) {
        FeedTexture feed = FEEDS.remove(key);
        DRONE_FEEDS.remove(key);
        if (feed != null) {
            feed.close();
        }
        if (FEEDS.isEmpty()) {
            CameraFeedRenderer.clear();
        }
    }

    public static void clear() {
        FEEDS.values().forEach(FeedTexture::close);
        FEEDS.clear();
        DRONE_FEEDS.clear();
        REQUESTED.clear();
        lastHousekeepingTick = Long.MIN_VALUE;
        CameraFeedRenderer.clear();
    }

    private static FpvDroneEntity findDrone(Minecraft mc, UUID droneId) {
        if (mc.level == null) {
            return null;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof FpvDroneEntity drone && droneId.equals(entity.getUUID()) && drone.isAlive()) {
                return drone;
            }
        }
        return null;
    }

    private static long droneFeedKey(UUID droneId) {
        return Long.MIN_VALUE ^ droneId.getMostSignificantBits() ^ Long.rotateLeft(droneId.getLeastSignificantBits(), 17);
    }

    public static void onServerSettingsChanged() {
        clear();
        if (blankFeedTexture != null) {
            blankFeedTexture.close();
            blankFeedTexture = null;
        }
        blankFeedLocation = null;
        blankFeedSize = -1;
    }

    private static void pruneFeeds(long gameTime) {
        if (gameTime - lastHousekeepingTick < HOUSEKEEPING_INTERVAL_TICKS) {
            return;
        }
        lastHousekeepingTick = gameTime;

        ArrayList<Long> staleKeys = new ArrayList<>();
        for (Map.Entry<Long, FeedTexture> entry : FEEDS.entrySet()) {
            FeedTexture feed = entry.getValue();
            if (gameTime - feed.lastRequestedTick() > STALE_FEED_TICKS) {
                staleKeys.add(entry.getKey());
            }
        }
        staleKeys.forEach(CameraFeedTextureManager::removeFeed);

        if (FEEDS.size() <= MAX_CACHED_FEEDS) {
            return;
        }

        ArrayList<Long> overflowKeys = new ArrayList<>();
        for (Map.Entry<Long, FeedTexture> entry : FEEDS.entrySet()) {
            if (FEEDS.size() - overflowKeys.size() <= MAX_CACHED_FEEDS) {
                break;
            }
            overflowKeys.add(entry.getKey());
        }
        overflowKeys.forEach(CameraFeedTextureManager::removeFeed);
    }

    public static final class FeedTexture {
        private final net.minecraft.client.renderer.texture.AbstractTexture texture;
        private final TextureTarget renderTarget;
        private final TextureTarget displayTarget;
        private final ResourceLocation location;
        private long lastRenderTick;
        private long lastRequestedTick;

        private FeedTexture(net.minecraft.client.renderer.texture.AbstractTexture texture, TextureTarget renderTarget, TextureTarget displayTarget, ResourceLocation location, long lastRenderTick, long lastRequestedTick) {
            this.texture = texture;
            this.renderTarget = renderTarget;
            this.displayTarget = displayTarget;
            this.location = location;
            this.lastRenderTick = lastRenderTick;
            this.lastRequestedTick = lastRequestedTick;
        }

        public net.minecraft.client.renderer.texture.AbstractTexture texture() {
            return this.texture;
        }

        public TextureTarget renderTarget() {
            return this.renderTarget;
        }

        public TextureTarget displayTarget() {
            return this.displayTarget;
        }

        public ResourceLocation location() {
            return this.location;
        }

        public long lastRenderTick() {
            return this.lastRenderTick;
        }

        public void setLastRenderTick(long lastRenderTick) {
            this.lastRenderTick = lastRenderTick;
        }

        public long lastRequestedTick() {
            return this.lastRequestedTick;
        }

        public void setLastRequestedTick(long lastRequestedTick) {
            this.lastRequestedTick = lastRequestedTick;
        }

        public void close() {
            this.texture.close();
            this.renderTarget.destroyBuffers();
            this.displayTarget.destroyBuffers();
        }
    }
}
