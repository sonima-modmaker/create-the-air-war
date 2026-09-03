package hi.client.camera;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import hi.block.entity.CameraBlockEntity;
import hi.client.CtawShaders;
import hi.entity.FpvDroneEntity;
import hi.mixin.camera.CameraAccessor;
import hi.mixin.camera.GameRendererAccessor;
import hi.mixin.camera.LevelRendererAccessor;
import hi.mixin.camera.MinecraftAccessor;
import hi.util.SableCoordinateHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;

public final class CameraFeedRenderer {
    private static final Camera DUMMY_CAMERA = new Camera();
    private static boolean renderingFeed = false;
    private static final java.util.Map<net.minecraft.core.BlockPos, Long> LAST_RENDER_TIMES = new java.util.concurrent.ConcurrentHashMap<>();
    private static Class<?> entityCullingCullableClass;
    private static java.lang.reflect.Method entityCullingSetTimeoutMethod;
    private static java.lang.reflect.Method entityCullingSetCulledMethod;
    private static java.lang.reflect.Method entityCullingSetOutOfCameraMethod;

    private static java.lang.reflect.Field mainCameraField;
    static {
        try {
            for (java.lang.reflect.Field field : net.minecraft.client.renderer.GameRenderer.class.getDeclaredFields()) {
                if (field.getType() == net.minecraft.client.Camera.class) {
                    field.setAccessible(true);
                    mainCameraField = field;
                    break;
                }
            }
        } catch (Throwable t) {
            System.err.println("[CTAW] Failed to find mainCamera field: " + t);
        }
    }

    private static void setMainCamera(net.minecraft.client.renderer.GameRenderer gameRenderer, net.minecraft.client.Camera camera) {
        try {
            if (mainCameraField != null) {
                mainCameraField.set(gameRenderer, camera);
            }
        } catch (Throwable t) {
            System.err.println("[CTAW] Failed to set mainCamera field: " + t);
        }
    }

    private static java.lang.reflect.Field mainRenderTargetField;
    static {
        try {
            for (java.lang.reflect.Field field : net.minecraft.client.Minecraft.class.getDeclaredFields()) {
                if (field.getType() == com.mojang.blaze3d.pipeline.RenderTarget.class) {
                    field.setAccessible(true);
                    mainRenderTargetField = field;
                    break;
                }
            }
        } catch (Throwable t) {
            System.err.println("[CTAW] Failed to find mainRenderTarget field: " + t);
        }
    }

    static {
        try {
            entityCullingCullableClass = Class.forName("dev.tr7zw.entityculling.versionless.access.Cullable");
            entityCullingSetTimeoutMethod = entityCullingCullableClass.getMethod("setTimeout");
            entityCullingSetCulledMethod = entityCullingCullableClass.getMethod("setCulled", boolean.class);
            entityCullingSetOutOfCameraMethod = entityCullingCullableClass.getMethod("setOutOfCamera", boolean.class);
        } catch (Throwable ignored) {
        }
    }

    private static void setMainRenderTarget(net.minecraft.client.Minecraft mc, com.mojang.blaze3d.pipeline.RenderTarget target) {
        try {
            if (mainRenderTargetField != null) {
                mainRenderTargetField.set(mc, target);
            }
        } catch (Throwable t) {
            System.err.println("[CTAW] Failed to set mainRenderTarget field: " + t);
        }
    }

    // Isolated render states for the camera feed
    private static net.minecraft.client.renderer.ViewArea cameraViewArea;
    private static net.minecraft.client.renderer.SectionOcclusionGraph cameraOcclusionGraph;
    private static int cameraViewDistance = -1;
    private static int cameraLastViewDistance = -1;
    private static int cameraLastSectionX = Integer.MIN_VALUE;
    private static int cameraLastSectionY = Integer.MIN_VALUE;
    private static int cameraLastSectionZ = Integer.MIN_VALUE;
    private static double cameraPrevCamX = Double.NaN;
    private static double cameraPrevCamY = Double.NaN;
    private static double cameraPrevCamZ = Double.NaN;
    private static double cameraPrevCamRotX = Double.NaN;
    private static double cameraPrevCamRotY = Double.NaN;
    private static it.unimi.dsi.fastutil.objects.ObjectArrayList<net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection> cameraVisibleSections = new it.unimi.dsi.fastutil.objects.ObjectArrayList<>();

    public static net.minecraft.client.renderer.ViewArea getCameraViewArea() {
        return cameraViewArea;
    }

    public static void clear() {
        LAST_RENDER_TIMES.clear();
        if (cameraViewArea != null) {
            cameraViewArea.releaseAllBuffers();
            cameraViewArea = null;
        }
        if (cameraOcclusionGraph != null) {
            cameraOcclusionGraph = null;
        }
        cameraViewDistance = -1;
        cameraLastViewDistance = -1;
        cameraVisibleSections.clear();
        cameraLastSectionX = Integer.MIN_VALUE;
        cameraLastSectionY = Integer.MIN_VALUE;
        cameraLastSectionZ = Integer.MIN_VALUE;
        cameraPrevCamX = Double.NaN;
        cameraPrevCamY = Double.NaN;
        cameraPrevCamZ = Double.NaN;
        cameraPrevCamRotX = Double.NaN;
        cameraPrevCamRotY = Double.NaN;
    }

    private CameraFeedRenderer() {
    }

    public static boolean isRenderingFeed() {
        return renderingFeed;
    }

    public static void render(CameraFeedTextureManager.FeedTexture feedTexture, CameraBlockEntity cameraBlockEntity) {
        if (renderingFeed) {
            return;
        }
        long now = System.currentTimeMillis();
        long lastRender = LAST_RENDER_TIMES.getOrDefault(cameraBlockEntity.getBlockPos(), 0L);
        long minimumFrameTime = Math.max(100L, 1000L / Math.max(1, CameraFeedRuntimeSettings.getFeedFps()));
        if (now - lastRender < minimumFrameTime) {
            return;
        }
        LAST_RENDER_TIMES.put(cameraBlockEntity.getBlockPos(), now);

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.levelRenderer == null) {
            return;
        }

        TextureTarget target = feedTexture.renderTarget();
        GameRenderer gameRenderer = mc.gameRenderer;
        LevelRenderer levelRenderer = mc.levelRenderer;
        RenderTarget originalMainTarget = mc.getMainRenderTarget();
        Camera originalMainCamera = gameRenderer.getMainCamera();
        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f oldModelView = new Matrix4f(RenderSystem.getModelViewStack());
        Vec3 oldCameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Quaternionf oldCameraRotation = mc.gameRenderer.getMainCamera().rotation().conjugate(new Quaternionf());
        Matrix4f oldCameraMatrix = new Matrix4f().rotation(oldCameraRotation);
        SodiumState sodiumState = SodiumState.capture();
        IrisState irisState = IrisState.capture(levelRenderer);
        if (irisState != null) {
            irisState.disableShaders();
        }
        DistantHorizonsState dhState = DistantHorizonsState.capture();

        hi.mixin.camera.LevelRendererAccessor accessor = (hi.mixin.camera.LevelRendererAccessor) levelRenderer;
        CameraFeedLevelRendererState playerRendererState = CameraFeedLevelRendererState.capture(levelRenderer);

        boolean useDedicatedCameraChunks = CameraFeedRuntimeSettings.shouldLoadChunksByCamera();

        // 2. Initialize or rebuild cameraViewArea only when camera chunk loading is enabled in config
        boolean rebuildCameraSections = false;
        if (useDedicatedCameraChunks) {
            int requestedViewDistance = Math.clamp(CameraFeedRuntimeSettings.getFeedViewDistance(), 1, 16);
            if (cameraViewArea == null || cameraViewDistance != requestedViewDistance) {
                if (cameraViewArea != null) {
                    cameraViewArea.releaseAllBuffers();
                }
                cameraViewArea = new net.minecraft.client.renderer.ViewArea(
                    accessor.ctaw$getSectionRenderDispatcher(),
                    mc.level,
                    requestedViewDistance,
                    levelRenderer
                );
                cameraOcclusionGraph = new net.minecraft.client.renderer.SectionOcclusionGraph();
                cameraOcclusionGraph.waitAndReset(cameraViewArea);
                cameraViewDistance = requestedViewDistance;
                cameraLastViewDistance = requestedViewDistance;
                cameraVisibleSections.clear();
                cameraLastSectionX = Integer.MIN_VALUE;
                cameraLastSectionY = Integer.MIN_VALUE;
                cameraLastSectionZ = Integer.MIN_VALUE;
                cameraPrevCamX = Double.NaN;
                cameraPrevCamY = Double.NaN;
                cameraPrevCamZ = Double.NaN;
                cameraPrevCamRotX = Double.NaN;
                cameraPrevCamRotY = Double.NaN;
                rebuildCameraSections = true;
            }

            // 3. Set levelRenderer fields to camera's isolated render state
            accessor.ctaw$setViewArea(cameraViewArea);
            ((LevelRendererStateBridge) levelRenderer).ctaw$replaceSectionOcclusionGraph(cameraOcclusionGraph);
            // renderLevel compares this field with Options.renderDistance(). During a
            // feed OptionsMixin reports the camera distance, so retaining the player's
            // value here caused allChanged() and a full chunk renderer rebuild every frame.
            accessor.ctaw$setLastViewDistance(cameraLastViewDistance);
            accessor.ctaw$setLastCameraSectionX(cameraLastSectionX);
            accessor.ctaw$setLastCameraSectionY(cameraLastSectionY);
            accessor.ctaw$setLastCameraSectionZ(cameraLastSectionZ);
            accessor.ctaw$setPrevCamX(cameraPrevCamX);
            accessor.ctaw$setPrevCamY(cameraPrevCamY);
            accessor.ctaw$setPrevCamZ(cameraPrevCamZ);
            accessor.ctaw$setPrevCamRotX(cameraPrevCamRotX);
            accessor.ctaw$setPrevCamRotY(cameraPrevCamRotY);
            accessor.ctaw$setVisibleSections(cameraVisibleSections);
        } else {
            clear();
            playerRendererState.apply(levelRenderer);
        }

        try {
            renderingFeed = true;
            setupDummyCamera(mc, cameraBlockEntity);
            setMainCamera(gameRenderer, DUMMY_CAMERA);
            setMainRenderTarget(mc, target);
            Vec3 feedCameraPos = DUMMY_CAMERA.getPosition();
            if (useDedicatedCameraChunks && cameraViewArea != null) {
                cameraViewArea.repositionCamera(feedCameraPos.x, feedCameraPos.z);
                if (rebuildCameraSections) {
                    markLoadedCameraSectionsDirty(mc, feedCameraPos);
                }
            }

            DeltaTracker deltaTracker = mc.getTimer();
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            mc.gameRenderer.lightTexture().updateLightTexture(partialTick);

            target.bindWrite(true);
            RenderSystem.viewport(0, 0, target.width, target.height);
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            FogRenderer.setupNoFog();

            RenderSystem.backupProjectionMatrix();
            Matrix4f projection = createProjectionMatrix(gameRenderer, target, CameraMonitorClientHandler.getFov(cameraBlockEntity.getBlockPos()));
            gameRenderer.resetProjectionMatrix(projection);

            Quaternionf cameraRotation = DUMMY_CAMERA.rotation().conjugate(new Quaternionf());
            Matrix4f cameraMatrix = new Matrix4f().rotation(cameraRotation);
            Vec3 cameraPos = DUMMY_CAMERA.getPosition();
            if (sodiumState != null) {
                sodiumState.prepareForCameraRender(cameraPos, DUMMY_CAMERA);
            }
            markEntitiesVisibleForCameraFeed(mc);

            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.identity();
            RenderSystem.applyModelViewMatrix();

            levelRenderer.prepareCullFrustum(cameraPos, cameraMatrix, projection);
            
            levelRenderer.renderLevel(deltaTracker, false, DUMMY_CAMERA, gameRenderer, gameRenderer.lightTexture(), cameraMatrix, projection);

            setMainRenderTarget(mc, originalMainTarget);
            setMainCamera(gameRenderer, originalMainCamera);
            applyGpuCameraEffect(feedTexture, mc, cameraBlockEntity);
        } finally {
            setMainRenderTarget(mc, originalMainTarget);
            setMainCamera(gameRenderer, originalMainCamera);
            // 4. Save the modified camera's render state only when a dedicated camera view area is active
            if (useDedicatedCameraChunks) {
                cameraLastSectionX = accessor.ctaw$getLastCameraSectionX();
                cameraLastViewDistance = accessor.ctaw$getLastViewDistance();
                cameraLastSectionY = accessor.ctaw$getLastCameraSectionY();
                cameraLastSectionZ = accessor.ctaw$getLastCameraSectionZ();
                cameraPrevCamX = accessor.ctaw$getPrevCamX();
                cameraPrevCamY = accessor.ctaw$getPrevCamY();
                cameraPrevCamZ = accessor.ctaw$getPrevCamZ();
                cameraPrevCamRotX = accessor.ctaw$getPrevCamRotX();
                cameraPrevCamRotY = accessor.ctaw$getPrevCamRotY();
                // Keep the graph-owned list itself. Copying it disconnects the feed graph
                // and can also poison the player's visible-section state on restoration.
                cameraVisibleSections = accessor.ctaw$getVisibleSections();
            }

            // 5. Restore the player's render state
            playerRendererState.apply(levelRenderer);
            hi.mixin.camera.LevelRendererAccessor restoredAccessor = (hi.mixin.camera.LevelRendererAccessor) levelRenderer;
            if (restoredAccessor.ctaw$getViewArea() != null) {
                restoredAccessor.ctaw$getViewArea().repositionCamera(oldCameraPos.x, oldCameraPos.z);
            }
            restoredAccessor.ctaw$getSectionRenderDispatcher().setCamera(oldCameraPos);

            renderingFeed = false;

            if (mc.level != null) {
                float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
                mc.gameRenderer.lightTexture().updateLightTexture(partialTick);
            }
            if (dhState != null) {
                dhState.restore();
            }
            if (irisState != null) {
                irisState.restore();
            }
            if (sodiumState != null) {
                sodiumState.restore();
            }
            gameRenderer.resetProjectionMatrix(oldProjection);
            RenderSystem.restoreProjectionMatrix();
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.identity();
            modelViewStack.mul(oldModelView);
            RenderSystem.applyModelViewMatrix();
            originalMainTarget.bindWrite(true);
            RenderSystem.viewport(0, 0, originalMainTarget.width, originalMainTarget.height);
            levelRenderer.prepareCullFrustum(oldCameraPos, oldCameraMatrix, oldProjection);
        }
    }

    private static void markLoadedCameraSectionsDirty(Minecraft mc, Vec3 cameraPos) {
        if (mc.level == null || cameraViewArea == null) return;
        hi.mixin.camera.ViewAreaAccessor view = (hi.mixin.camera.ViewAreaAccessor) cameraViewArea;
        int centerX = net.minecraft.util.Mth.floor(cameraPos.x) >> 4;
        int centerZ = net.minecraft.util.Mth.floor(cameraPos.z) >> 4;
        int radius = Math.max(1, cameraViewDistance);
        for (int chunkX = centerX - radius; chunkX <= centerX + radius; ++chunkX) {
            for (int chunkZ = centerZ - radius; chunkZ <= centerZ + radius; ++chunkZ) {
                if (!mc.level.getChunkSource().hasChunk(chunkX, chunkZ)) continue;
                for (int sectionY = mc.level.getMinSection(); sectionY < mc.level.getMaxSection(); ++sectionY) {
                    net.minecraft.core.BlockPos origin = new net.minecraft.core.BlockPos(
                        chunkX << 4, net.minecraft.core.SectionPos.sectionToBlockCoord(sectionY), chunkZ << 4);
                    net.minecraft.client.renderer.chunk.SectionRenderDispatcher.RenderSection section =
                        view.ctaw$invokeGetRenderSectionAt(origin);
                    if (section != null && section.getOrigin().equals(origin)) {
                        section.setDirty(true);
                    }
                }
            }
        }
    }

    public static void render(CameraFeedTextureManager.FeedTexture feedTexture, FpvDroneEntity drone) {
        if (renderingFeed) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.levelRenderer == null || drone == null || drone.isRemoved()) {
            return;
        }

        TextureTarget target = feedTexture.renderTarget();
        GameRenderer gameRenderer = mc.gameRenderer;
        LevelRenderer levelRenderer = mc.levelRenderer;
        RenderTarget originalMainTarget = mc.getMainRenderTarget();
        Camera originalMainCamera = gameRenderer.getMainCamera();
        Matrix4f oldProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f oldModelView = new Matrix4f(RenderSystem.getModelViewStack());
        Vec3 oldCameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Quaternionf oldCameraRotation = mc.gameRenderer.getMainCamera().rotation().conjugate(new Quaternionf());
        Matrix4f oldCameraMatrix = new Matrix4f().rotation(oldCameraRotation);
        SodiumState sodiumState = SodiumState.capture();
        IrisState irisState = IrisState.capture(levelRenderer);
        if (irisState != null) {
            irisState.disableShaders();
        }
        DistantHorizonsState dhState = DistantHorizonsState.capture();
        CameraFeedLevelRendererState playerRendererState = CameraFeedLevelRendererState.capture(levelRenderer);

        try {
            renderingFeed = true;
            setupDummyDroneCamera(mc, drone);
            setMainCamera(gameRenderer, DUMMY_CAMERA);
            setMainRenderTarget(mc, target);

            DeltaTracker deltaTracker = mc.getTimer();
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            mc.gameRenderer.lightTexture().updateLightTexture(partialTick);

            target.bindWrite(true);
            RenderSystem.viewport(0, 0, target.width, target.height);
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            FogRenderer.setupNoFog();

            RenderSystem.backupProjectionMatrix();
            Matrix4f projection = createProjectionMatrix(gameRenderer, target, 72.0F);
            gameRenderer.resetProjectionMatrix(projection);

            Quaternionf cameraRotation = DUMMY_CAMERA.rotation().conjugate(new Quaternionf());
            Matrix4f cameraMatrix = new Matrix4f().rotation(cameraRotation);
            Vec3 cameraPos = DUMMY_CAMERA.getPosition();
            if (sodiumState != null) {
                sodiumState.prepareForCameraRender(cameraPos, DUMMY_CAMERA);
            }
            markEntitiesVisibleForCameraFeed(mc);

            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.identity();
            RenderSystem.applyModelViewMatrix();

            levelRenderer.prepareCullFrustum(cameraPos, cameraMatrix, projection);
            levelRenderer.renderLevel(deltaTracker, false, DUMMY_CAMERA, gameRenderer, gameRenderer.lightTexture(), cameraMatrix, projection);

            setMainRenderTarget(mc, originalMainTarget);
            setMainCamera(gameRenderer, originalMainCamera);
            applyGpuCameraEffect(feedTexture, mc, false, false);
        } finally {
            setMainRenderTarget(mc, originalMainTarget);
            setMainCamera(gameRenderer, originalMainCamera);
            playerRendererState.apply(levelRenderer);
            hi.mixin.camera.LevelRendererAccessor restoredAccessor = (hi.mixin.camera.LevelRendererAccessor) levelRenderer;
            if (restoredAccessor.ctaw$getViewArea() != null) {
                restoredAccessor.ctaw$getViewArea().repositionCamera(oldCameraPos.x, oldCameraPos.z);
            }
            restoredAccessor.ctaw$getSectionRenderDispatcher().setCamera(oldCameraPos);
            renderingFeed = false;

            if (mc.level != null) {
                float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
                mc.gameRenderer.lightTexture().updateLightTexture(partialTick);
            }
            if (dhState != null) {
                dhState.restore();
            }
            if (irisState != null) {
                irisState.restore();
            }
            if (sodiumState != null) {
                sodiumState.restore();
            }
            gameRenderer.resetProjectionMatrix(oldProjection);
            RenderSystem.restoreProjectionMatrix();
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.identity();
            modelViewStack.mul(oldModelView);
            RenderSystem.applyModelViewMatrix();
            originalMainTarget.bindWrite(true);
            RenderSystem.viewport(0, 0, originalMainTarget.width, originalMainTarget.height);
            levelRenderer.prepareCullFrustum(oldCameraPos, oldCameraMatrix, oldProjection);
        }
    }

    private static void markEntitiesVisibleForCameraFeed(Minecraft mc) {
        if (mc.level == null || entityCullingCullableClass == null) {
            return;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == null || entity.isRemoved() || !entityCullingCullableClass.isInstance(entity)) {
                continue;
            }
            if (!mc.level.hasChunkAt(entity.blockPosition())) continue;
            net.minecraft.world.level.ChunkPos cameraChunk = new net.minecraft.world.level.ChunkPos(net.minecraft.core.BlockPos.containing(DUMMY_CAMERA.getPosition()));
            net.minecraft.world.level.ChunkPos entityChunk = entity.chunkPosition();
            int viewDistance = CameraFeedRuntimeSettings.getFeedViewDistance();
            if (Math.abs(entityChunk.x - cameraChunk.x) > viewDistance || Math.abs(entityChunk.z - cameraChunk.z) > viewDistance) continue;
            try {
                entityCullingSetCulledMethod.invoke(entity, false);
                entityCullingSetOutOfCameraMethod.invoke(entity, false);
                entityCullingSetTimeoutMethod.invoke(entity);
            } catch (Throwable ignored) {
            }
        }
    }

    private static void applyGpuCameraEffect(CameraFeedTextureManager.FeedTexture feedTexture, Minecraft mc, CameraBlockEntity cameraBlockEntity) {
        applyGpuCameraEffect(feedTexture, mc, CameraMonitorClientHandler.isThermalModeEnabled(), cameraBlockEntity.isLocked());
    }

    private static void applyGpuCameraEffect(CameraFeedTextureManager.FeedTexture feedTexture, Minecraft mc, boolean thermalMode, boolean locked) {
        ShaderInstance shader = CtawShaders.getCameraPostShader();
        TextureTarget source = feedTexture.renderTarget();
        TextureTarget output = feedTexture.displayTarget();
        if (shader == null) {
            source.unbindWrite();
            output.bindWrite(true);
            source.blitToScreen(output.width, output.height);
            output.unbindWrite();
            return;
        }

        source.unbindWrite();
        output.bindWrite(true);
        RenderSystem.viewport(0, 0, output.width, output.height);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();

        shader.setSampler("Sampler0", source.getColorTextureId());
        shader.safeGetUniform("ModelViewMat").set(new Matrix4f());
        shader.safeGetUniform("ProjMat").set(new Matrix4f());
        shader.safeGetUniform("ColorModulator").set(1.0F, 1.0F, 1.0F, 1.0F);
        shader.safeGetUniform("ScreenSize").set((float) output.width, (float) output.height);
        shader.safeGetUniform("Mode").set(thermalMode ? 1.0F : 0.0F);
        shader.safeGetUniform("Time").set(mc.level != null ? (float) mc.level.getGameTime() : 0.0F);
        shader.safeGetUniform("IsLocked").set(locked ? 1.0F : 0.0F);
        shader.apply();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
        buffer.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
        buffer.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
        buffer.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
        BufferUploader.draw(buffer.buildOrThrow());

        shader.clear();
        output.unbindWrite();
        source.unbindRead();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void setupDummyDroneCamera(Minecraft mc, FpvDroneEntity drone) {
        Entity entity = ((CameraAccessor) DUMMY_CAMERA).ctaw$getEntity();
        if (entity == null || entity.level() != mc.level) {
            entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, mc.level);
            ((CameraAccessor) DUMMY_CAMERA).ctaw$setEntity(entity);
        }

        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        float yaw = Mth.rotLerp(partialTick, drone.yRotO, drone.getYRot());
        float pitch = Mth.lerp(partialTick, drone.xRotO, drone.getXRot());
        Vec3 forward = drone.getForwardVector();
        Vec3 localPos = drone.position().add(0.0D, 0.18D, 0.0D).add(forward.scale(0.42D));

        entity.setPos(localPos);
        entity.setYRot(yaw);
        entity.setXRot(pitch);

        ((CameraAccessor) DUMMY_CAMERA).ctaw$invokeSetPosition(localPos);
        ((CameraAccessor) DUMMY_CAMERA).ctaw$invokeSetRotation(yaw, pitch);
    }

    private static void setupDummyCamera(Minecraft mc, CameraBlockEntity cameraBlockEntity) {
        Entity entity = ((CameraAccessor) DUMMY_CAMERA).ctaw$getEntity();
        if (entity == null || entity.level() != mc.level) {
            entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, mc.level);
            ((CameraAccessor) DUMMY_CAMERA).ctaw$setEntity(entity);
        }

        net.minecraft.core.Direction facing = cameraBlockEntity.getBlockState().getValue(hi.block.CameraBlock.FACING);
        Vec3 localPos = cameraBlockEntity.getBlockPos().getCenter().add(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.82D));
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(false);
        float yaw = cameraBlockEntity.getRenderYaw(partialTick);
        float pitch = cameraBlockEntity.getRenderPitch(partialTick);
        Vec3 renderPos = localPos;
        float renderYaw = yaw;
        float renderPitch = pitch;

        if (cameraBlockEntity.getLevel() != null) {
            renderPos = SableCoordinateHelper.projectOut(cameraBlockEntity.getLevel(), localPos);
            Vec3 worldLook = SableCoordinateHelper.projectDirectionOut(
                cameraBlockEntity.getLevel(),
                localPos,
                Vec3.directionFromRotation(pitch, yaw)
            );
            if (worldLook.lengthSqr() > 1.0E-6D) {
                worldLook = worldLook.normalize();
                renderYaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(-worldLook.x, worldLook.z)));
                renderPitch = (float) Mth.clamp(-Math.toDegrees(Math.asin(worldLook.y)), -89.9D, 89.9D);
            }
        }

        entity.setPos(renderPos);
        entity.setYRot(renderYaw + 180.0f);
        entity.setXRot(renderPitch);

        ((CameraAccessor) DUMMY_CAMERA).ctaw$invokeSetPosition(renderPos);
        ((CameraAccessor) DUMMY_CAMERA).ctaw$invokeSetRotation(renderYaw, renderPitch);
    }

    private static Matrix4f createProjectionMatrix(GameRenderer gameRenderer, RenderTarget target, float fov) {
        Matrix4f matrix = new Matrix4f();
        return matrix.perspective(fov * Mth.DEG_TO_RAD, (float) target.width / (float) target.height, 0.18f, gameRenderer.getDepthFar());
    }

    private static class SodiumState {
        private static boolean initialized = false;
        private static java.lang.reflect.Method instanceMethod;
        private static java.lang.reflect.Field renderSectionManagerField;
        
        private static java.lang.reflect.Field lastCameraPosField;
        private static java.lang.reflect.Field lastCameraPitchField;
        private static java.lang.reflect.Field lastCameraYawField;
        private static java.lang.reflect.Field lastFogDistanceField;
        private static java.lang.reflect.Field lastProjectionMatrixField;
        
        private static java.lang.reflect.Field renderListsField;
        private static java.lang.reflect.Field lastUpdatedFrameField;
        private static java.lang.reflect.Field needsGraphUpdateField;
        private static java.lang.reflect.Field cameraBlockPosField;
        private static java.lang.reflect.Field cameraPositionField;
        
        static {
            try {
                Class<?> rendererClass = Class.forName("net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer");
                instanceMethod = rendererClass.getMethod("instance");
                
                lastCameraPosField = rendererClass.getDeclaredField("lastCameraPos");
                lastCameraPosField.setAccessible(true);
                lastCameraPitchField = rendererClass.getDeclaredField("lastCameraPitch");
                lastCameraPitchField.setAccessible(true);
                lastCameraYawField = rendererClass.getDeclaredField("lastCameraYaw");
                lastCameraYawField.setAccessible(true);
                lastFogDistanceField = rendererClass.getDeclaredField("lastFogDistance");
                lastFogDistanceField.setAccessible(true);
                lastProjectionMatrixField = rendererClass.getDeclaredField("lastProjectionMatrix");
                lastProjectionMatrixField.setAccessible(true);
                
                renderSectionManagerField = rendererClass.getDeclaredField("renderSectionManager");
                renderSectionManagerField.setAccessible(true);
                
                Class<?> managerClass = Class.forName("net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager");
                renderListsField = managerClass.getDeclaredField("renderLists");
                renderListsField.setAccessible(true);
                lastUpdatedFrameField = managerClass.getDeclaredField("lastUpdatedFrame");
                lastUpdatedFrameField.setAccessible(true);
                needsGraphUpdateField = managerClass.getDeclaredField("needsGraphUpdate");
                needsGraphUpdateField.setAccessible(true);
                cameraBlockPosField = managerClass.getDeclaredField("cameraBlockPos");
                cameraBlockPosField.setAccessible(true);
                cameraPositionField = managerClass.getDeclaredField("cameraPosition");
                cameraPositionField.setAccessible(true);
                
                initialized = true;
            } catch (Throwable ignored) {
            }
        }
        
        private Object renderer;
        private Object manager;
        
        private Object lastCameraPosObj;
        private double[] lastCameraPosCoords;
        private double lastCameraPitch;
        private double lastCameraYaw;
        private float lastFogDistance;
        private Object lastProjectionMatrixObj;
        private float[] lastProjectionMatrixValues;
        
        private Object renderLists;
        private int lastUpdatedFrame;
        private boolean needsGraphUpdate;
        private Object cameraBlockPos;
        private Object cameraPositionObj;
        private double[] cameraPositionCoords;
        
        public static SodiumState capture() {
            if (!initialized) return null;
            try {
                Object renderer = instanceMethod.invoke(null);
                if (renderer == null) return null;
                Object manager = renderSectionManagerField.get(renderer);
                
                SodiumState state = new SodiumState();
                state.renderer = renderer;
                state.manager = manager;
                
                state.lastCameraPosObj = lastCameraPosField.get(renderer);
                state.lastCameraPosCoords = getVectorCoords(state.lastCameraPosObj);
                state.lastCameraPitch = lastCameraPitchField.getDouble(renderer);
                state.lastCameraYaw = lastCameraYawField.getDouble(renderer);
                state.lastFogDistance = lastFogDistanceField.getFloat(renderer);
                state.lastProjectionMatrixObj = lastProjectionMatrixField.get(renderer);
                state.lastProjectionMatrixValues = getMatrixFloats(state.lastProjectionMatrixObj);
                
                if (manager != null) {
                    state.renderLists = renderListsField.get(manager);
                    state.lastUpdatedFrame = lastUpdatedFrameField.getInt(manager);
                    state.needsGraphUpdate = needsGraphUpdateField.getBoolean(manager);
                    state.cameraBlockPos = cameraBlockPosField.get(manager);
                    state.cameraPositionObj = cameraPositionField.get(manager);
                    state.cameraPositionCoords = getVectorCoords(state.cameraPositionObj);
                }
                return state;
            } catch (Throwable ignored) {
                return null;
            }
        }
        
        public void restore() {
            try {
                setVectorCoords(lastCameraPosObj, lastCameraPosCoords);
                lastCameraPitchField.setDouble(renderer, lastCameraPitch);
                lastCameraYawField.setDouble(renderer, lastCameraYaw);
                lastFogDistanceField.setFloat(renderer, lastFogDistance);
                setMatrixFloats(lastProjectionMatrixObj, lastProjectionMatrixValues);
                
                if (manager != null) {
                    renderListsField.set(manager, renderLists);
                    lastUpdatedFrameField.setInt(manager, lastUpdatedFrame);
                    needsGraphUpdateField.setBoolean(manager, needsGraphUpdate);
                    cameraBlockPosField.set(manager, cameraBlockPos);
                    setVectorCoords(cameraPositionObj, cameraPositionCoords);
                }
            } catch (Throwable ignored) {
            }
        }

        public void prepareForCameraRender(Vec3 cameraPos, Camera feedCamera) {
            // Do not modify Sodium's camera position or trigger graph updates.
            // Sodium's update() is cancelled during camera feed rendering
            // (via SodiumRenderSectionManagerMixin), so the existing player
            // render lists are used to render terrain from the camera's angle.
            // Modifying coordinates here would desync Sodium's internal state.
        }

        private static double[] getVectorCoords(Object vec) {
            if (vec == null) return null;
            try {
                Class<?> clazz = vec.getClass();
                double x = clazz.getField("x").getDouble(vec);
                double y = clazz.getField("y").getDouble(vec);
                double z = clazz.getField("z").getDouble(vec);
                return new double[]{x, y, z};
            } catch (Throwable ignored) {
                return null;
            }
        }
        
        private static void setVectorCoords(Object vec, double[] coords) {
            if (vec == null || coords == null) return;
            try {
                Class<?> clazz = vec.getClass();
                clazz.getField("x").setDouble(vec, coords[0]);
                clazz.getField("y").setDouble(vec, coords[1]);
                clazz.getField("z").setDouble(vec, coords[2]);
            } catch (Throwable ignored) {
            }
        }

        private static float[] getMatrixFloats(Object mat) {
            if (mat == null) return null;
            try {
                Class<?> clazz = mat.getClass();
                float[] values = new float[16];
                int idx = 0;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        values[idx++] = clazz.getField("m" + i + j).getFloat(mat);
                    }
                }
                return values;
            } catch (Throwable ignored) {
                return null;
            }
        }
        
        private static void setMatrixFloats(Object mat, float[] values) {
            if (mat == null || values == null) return;
            try {
                Class<?> clazz = mat.getClass();
                int idx = 0;
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        clazz.getField("m" + i + j).setFloat(mat, values[idx++]);
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static class IrisState {
        private static Class<?> irisClass;
        private static java.lang.reflect.Field irisCurrentPackField;
        private static java.lang.reflect.Field irisCurrentPackNameField;
        
        private static class FieldBackup {
            private final java.lang.reflect.Field field;
            private final Object value;
            
            public FieldBackup(java.lang.reflect.Field field, Object value) {
                this.field = field;
                this.value = value;
            }
            
            public void restore(Object target) {
                try {
                    field.set(target, value);
                } catch (Throwable ignored) {
                }
            }
        }
        
        private final LevelRenderer levelRenderer;
        private Object pipelineManager;
        private java.lang.reflect.Field pmPipelineField;
        private final java.util.List<FieldBackup> irisClassBackups = new java.util.ArrayList<>();
        private final java.util.List<FieldBackup> levelRendererBackups = new java.util.ArrayList<>();
        private final java.util.List<FieldBackup> pipelineManagerBackups = new java.util.ArrayList<>();
        
        private IrisState(LevelRenderer levelRenderer) {
            this.levelRenderer = levelRenderer;
        }
        
        public static IrisState capture(LevelRenderer levelRenderer) {
            try {
                IrisState state = new IrisState(levelRenderer);
                
                // Lazy initialize Iris static fields
                if (irisClass == null) {
                    try {
                        irisClass = Class.forName("net.irisshaders.iris.Iris", true, levelRenderer.getClass().getClassLoader());
                        irisCurrentPackField = irisClass.getDeclaredField("currentPack");
                        irisCurrentPackField.setAccessible(true);
                        irisCurrentPackNameField = irisClass.getDeclaredField("currentPackName");
                        irisCurrentPackNameField.setAccessible(true);
                    } catch (Throwable ignored) {}
                }
                
                // Backup static fields on Iris
                if (irisClass != null) {
                    if (irisCurrentPackField != null) {
                        state.irisClassBackups.add(new FieldBackup(irisCurrentPackField, irisCurrentPackField.get(null)));
                    }
                    if (irisCurrentPackNameField != null) {
                        state.irisClassBackups.add(new FieldBackup(irisCurrentPackNameField, irisCurrentPackNameField.get(null)));
                    }
                }
                
                // Backup fields on LevelRenderer
                for (java.lang.reflect.Field field : levelRenderer.getClass().getDeclaredFields()) {
                    String typeName = field.getType().getName();
                    String fieldName = field.getName();
                    if (typeName.contains("irisshaders") || fieldName.contains("iris") || fieldName.equals("pipeline")) {
                        field.setAccessible(true);
                        state.levelRendererBackups.add(new FieldBackup(field, field.get(levelRenderer)));
                    }
                    if (typeName.contains("PipelineManager")) {
                        field.setAccessible(true);
                        state.pipelineManager = field.get(levelRenderer);
                    }
                }
                
                // Backup fields on PipelineManager
                if (state.pipelineManager != null) {
                    for (java.lang.reflect.Field field : state.pipelineManager.getClass().getDeclaredFields()) {
                        String typeName = field.getType().getName();
                        String fieldName = field.getName();
                        if (typeName.contains("irisshaders") || fieldName.contains("iris") || fieldName.equals("pipeline")) {
                            field.setAccessible(true);
                            state.pipelineManagerBackups.add(new FieldBackup(field, field.get(state.pipelineManager)));
                            if (fieldName.equals("pipeline")) {
                                state.pmPipelineField = field;
                            }
                        }
                    }
                }
                
                return state;
            } catch (Throwable t) {
                System.err.println("[CTAW] Error capturing Iris state: " + t);
                return null;
            }
        }
        
        public void disableShaders() {
        }
        
        public void restore() {
            try {
                for (FieldBackup backup : irisClassBackups) {
                    backup.restore(null);
                }
                for (FieldBackup backup : levelRendererBackups) {
                    backup.restore(levelRenderer);
                }
                if (pipelineManager != null) {
                    for (FieldBackup backup : pipelineManagerBackups) {
                        backup.restore(pipelineManager);
                    }
                }
            } catch (Throwable t) {
                System.err.println("[CTAW] Error restoring Iris state: " + t);
            }
        }
    }

    private static class DistantHorizonsState {
        private static boolean initialized = false;
        private static boolean failed = false;
        private static java.lang.reflect.Field rendererModeField = null;
        private static Object disabledMode = null;

        private static void initializeLazy() {
            if (initialized || failed) {
                return;
            }
            try {
                Class<?> clientApiClass = Class.forName("com.seibel.distanthorizons.core.api.internal.ClientApi");
                rendererModeField = clientApiClass.getDeclaredField("rendererMode");
                rendererModeField.setAccessible(true);

                Class<?> modeEnum = Class.forName("com.seibel.distanthorizons.api.enums.rendering.EDhApiRendererMode");
                for (Object constant : modeEnum.getEnumConstants()) {
                    if ("DISABLED".equals(constant.toString())) {
                        disabledMode = constant;
                        break;
                    }
                }
                initialized = true;
            } catch (Throwable t) {
                failed = true;
                System.err.println("[CTAW-DH] DistantHorizonsState initialization failed: " + t);
            }
        }

        private Object previousMode;

        public static DistantHorizonsState capture() {
            initializeLazy();
            if (!initialized) {
                return null;
            }
            try {
                DistantHorizonsState state = new DistantHorizonsState();
                state.previousMode = rendererModeField.get(null);
                rendererModeField.set(null, disabledMode);
                return state;
            } catch (Throwable t) {
                System.err.println("[CTAW-DH] DistantHorizonsState capture failed: " + t);
                return null;
            }
        }

        public void restore() {
            try {
                rendererModeField.set(null, previousMode);
            } catch (Throwable t) {
                System.err.println("[CTAW-DH] DistantHorizonsState restore failed: " + t);
            }
        }
    }
}
