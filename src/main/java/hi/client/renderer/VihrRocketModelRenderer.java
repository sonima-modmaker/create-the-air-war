package hi.client.renderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hi.CreateTheAirWarsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class VihrRocketModelRenderer {
    private static final float WING_FACE_EPSILON = 0.01F;
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        CreateTheAirWarsMod.MODID,
        "textures/block/vihr_rocket.png"
    );

    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
        CreateTheAirWarsMod.MODID,
        "models/custom/vihr_rocket_exact.json"
    );

    private static volatile ModelData cachedModel;

    private VihrRocketModelRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        renderModel(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 62.5F);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderModel(poseStack, buffer, packedLight, packedOverlay, 62.5F);
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float wingAngle) {
        renderModel(poseStack, buffer, packedLight, packedOverlay, wingAngle);
    }

    public static void renderWings(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        renderWings(poseStack, buffer, packedLight, packedOverlay, 62.5F);
    }

    public static void renderWings(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float wingAngle) {
        renderFiltered(poseStack, buffer, packedLight, packedOverlay, wingAngle, true);
    }

    private static void renderModel(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float wingAngle) {
        renderFiltered(poseStack, buffer, packedLight, packedOverlay, wingAngle, false);
    }

    private static void renderFiltered(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, float wingAngle, boolean wingsOnly) {
        ModelData model = getModel();
        RenderType renderType = RenderType.entityCutoutNoCull(TEXTURE);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        float actualWingAngle = wingAngle;

        for (Element element : model.elements) {
            if (wingsOnly && !element.isWing) {
                continue;
            }
            renderElement(element, matrix, poseStack, consumer, packedLight, packedOverlay, actualWingAngle);
        }
    }

    public static void applyItemTransform(PoseStack poseStack, ItemDisplayContext context) {
        ModelData model = getModel();
        DisplayTransform transform = switch (context) {
            case GUI -> model.transforms.get(DisplayKey.GUI);
            case FIRST_PERSON_RIGHT_HAND -> model.transforms.get(DisplayKey.FIRSTPERSON_RIGHTHAND);
            case FIRST_PERSON_LEFT_HAND -> model.transforms.get(DisplayKey.FIRSTPERSON_LEFTHAND);
            case THIRD_PERSON_RIGHT_HAND -> model.transforms.get(DisplayKey.THIRDPERSON_RIGHTHAND);
            case THIRD_PERSON_LEFT_HAND -> model.transforms.get(DisplayKey.THIRDPERSON_LEFTHAND);
            case GROUND -> model.transforms.get(DisplayKey.GROUND);
            case FIXED -> model.transforms.get(DisplayKey.FIXED);
            default -> null;
        };

        if (transform == null) {
            return;
        }

        if (transform.rotX != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(transform.rotX));
        }
        if (transform.rotY != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(transform.rotY));
        }
        if (transform.rotZ != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(transform.rotZ));
        }
        if (transform.tx != 0.0F || transform.ty != 0.0F || transform.tz != 0.0F) {
            poseStack.translate(transform.tx / 16.0F, transform.ty / 16.0F, transform.tz / 16.0F);
        }
        poseStack.scale(transform.sx, transform.sy, transform.sz);
    }

    private static void renderElement(Element element, Matrix4f matrix, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float wingAngle) {
        for (Face face : element.faces) {
            Vector3f[] vertices = createFaceVertices(element, face.dir);
            if (element.isWing) {
                offsetWingFace(vertices, face.dir);
            }
            for (Vector3f vertex : vertices) {
                rotateVertex(vertex, element, wingAngle);
                vertex.sub(8.0F, 8.0F, 8.0F).mul(1.0F / 16.0F);
            }

            Vector3f normal = new Vector3f(face.dir.normalX, face.dir.normalY, face.dir.normalZ);
            rotateNormal(normal, element, wingAngle);
            normal.normalize();

            float[][] uvs = createFaceUvs(face, element.texW, element.texH);
            for (int i = 0; i < 4; i++) {
                consumer.addVertex(matrix, vertices[i].x(), vertices[i].y(), vertices[i].z())
                    .setColor(255, 255, 255, 255)
                    .setUv(uvs[i][0], uvs[i][1])
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(poseStack.last(), normal.x(), normal.y(), normal.z());
            }
        }
    }

    private static void offsetWingFace(Vector3f[] vertices, FaceDir dir) {
        float offset = switch (dir) {
            case EAST, UP -> WING_FACE_EPSILON;
            case WEST, DOWN -> -WING_FACE_EPSILON;
            default -> 0.0F;
        };
        if (offset == 0.0F) {
            return;
        }
        for (Vector3f vertex : vertices) {
            switch (dir) {
                case EAST, WEST -> vertex.x += offset;
                case UP, DOWN -> vertex.y += offset;
                default -> {
                }
            }
        }
    }

    private static Vector3f[] createFaceVertices(Element element, FaceDir dir) {
        float x1 = element.fromX;
        float y1 = element.fromY;
        float z1 = element.fromZ;
        float x2 = element.toX;
        float y2 = element.toY;
        float z2 = element.toZ;
        return switch (dir) {
            case NORTH -> new Vector3f[] {
                new Vector3f(x2, y2, z1),
                new Vector3f(x1, y2, z1),
                new Vector3f(x1, y1, z1),
                new Vector3f(x2, y1, z1)
            };
            case SOUTH -> new Vector3f[] {
                new Vector3f(x1, y2, z2),
                new Vector3f(x2, y2, z2),
                new Vector3f(x2, y1, z2),
                new Vector3f(x1, y1, z2)
            };
            case EAST -> new Vector3f[] {
                new Vector3f(x2, y2, z1),
                new Vector3f(x2, y2, z2),
                new Vector3f(x2, y1, z2),
                new Vector3f(x2, y1, z1)
            };
            case WEST -> new Vector3f[] {
                new Vector3f(x1, y2, z2),
                new Vector3f(x1, y2, z1),
                new Vector3f(x1, y1, z1),
                new Vector3f(x1, y1, z2)
            };
            case UP -> new Vector3f[] {
                new Vector3f(x1, y2, z1),
                new Vector3f(x2, y2, z1),
                new Vector3f(x2, y2, z2),
                new Vector3f(x1, y2, z2)
            };
            case DOWN -> new Vector3f[] {
                new Vector3f(x1, y1, z2),
                new Vector3f(x2, y1, z2),
                new Vector3f(x2, y1, z1),
                new Vector3f(x1, y1, z1)
            };
        };
    }

    private static float[][] createFaceUvs(Face face, float texW, float texH) {
        float[] uPair = normalizeUvPair(face.u1, face.u2, texW);
        float[] vPair = normalizeUvPair(face.v1, face.v2, texH);
        float u1 = uPair[0];
        float u2 = uPair[1];
        float v1 = vPair[0];
        float v2 = vPair[1];

        float[][] uvs = switch (face.dir) {
            case NORTH -> new float[][] {
                {u2, v1}, {u1, v1}, {u1, v2}, {u2, v2}
            };
            case SOUTH, EAST, WEST, UP, DOWN -> new float[][] {
                {u1, v1}, {u2, v1}, {u2, v2}, {u1, v2}
            };
        };

        int turns = Math.floorMod(face.rotation / 90, 4);
        for (int t = 0; t < turns; t++) {
            float[] last = uvs[3];
            uvs[3] = uvs[2];
            uvs[2] = uvs[1];
            uvs[1] = uvs[0];
            uvs[0] = last;
        }
        return uvs;
    }

    private static float[] normalizeUvPair(float a, float b, float textureSize) {
        float min = Math.min(a, b);
        float max = Math.max(a, b);
        float span = max - min;

        if (span <= 1.0F) {
            float center = (min + max) * 0.5F;
            min = center;
            max = center;
        } else {
            // Pull wider spans slightly inward to avoid sampling neighbor pixels on the atlas border.
            min += 0.01F;
            max -= 0.01F;
        }

        if (a <= b) {
            return new float[] {min / textureSize, max / textureSize};
        }
        return new float[] {max / textureSize, min / textureSize};
    }

    private static void rotateVertex(Vector3f vertex, Element element, float wingAngle) {
        vertex.sub(element.originX, element.originY, element.originZ);

        float rotX = element.isWing ? Math.copySign(wingAngle, element.rotX) : element.rotX;
        if (element.rotZ != 0.0F) {
            vertex.rotateZ((float) Math.toRadians(element.rotZ));
        }
        if (element.rotY != 0.0F) {
            vertex.rotateY((float) Math.toRadians(element.rotY));
        }
        if (rotX != 0.0F) {
            vertex.rotateX((float) Math.toRadians(rotX));
        }

        vertex.add(element.originX, element.originY, element.originZ);
    }

    private static void rotateNormal(Vector3f normal, Element element, float wingAngle) {
        float rotX = element.isWing ? Math.copySign(wingAngle, element.rotX) : element.rotX;
        if (element.rotZ != 0.0F) {
            normal.rotateZ((float) Math.toRadians(element.rotZ));
        }
        if (element.rotY != 0.0F) {
            normal.rotateY((float) Math.toRadians(element.rotY));
        }
        if (rotX != 0.0F) {
            normal.rotateX((float) Math.toRadians(rotX));
        }
    }

    private static ModelData getModel() {
        ModelData local = cachedModel;
        if (local != null) {
            return local;
        }

        synchronized (VihrRocketModelRenderer.class) {
            if (cachedModel != null) {
                return cachedModel;
            }
            cachedModel = loadModel();
            return cachedModel;
        }
    }

    private static ModelData loadModel() {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(MODEL);
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                float texW = getArrayValue(root.getAsJsonArray("texture_size"), 0, 32.0F);
                float texH = getArrayValue(root.getAsJsonArray("texture_size"), 1, 16.0F);
                List<Element> elements = parseElements(root.getAsJsonArray("elements"), texW, texH);
                Map<DisplayKey, DisplayTransform> transforms = parseDisplays(root.getAsJsonObject("display"));
                return new ModelData(texW, texH, elements, transforms, 62.5F);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load exact Vihr model from " + MODEL, e);
        }
    }

    private static List<Element> parseElements(JsonArray array, float texW, float texH) {
        List<Element> elements = new ArrayList<>();
        for (JsonElement jsonElement : array) {
            JsonObject obj = jsonElement.getAsJsonObject();
            JsonArray from = obj.getAsJsonArray("from");
            JsonArray to = obj.getAsJsonArray("to");
            JsonObject rotation = obj.has("ctaw_rotation")
                ? obj.getAsJsonObject("ctaw_rotation")
                : (obj.has("rotation") ? obj.getAsJsonObject("rotation") : null);
            JsonArray origin = rotation != null && rotation.has("origin") ? rotation.getAsJsonArray("origin") : null;

            float fromX = getArrayValue(from, 0, 0.0F);
            float fromY = getArrayValue(from, 1, 0.0F);
            float fromZ = getArrayValue(from, 2, 0.0F);
            float toX = getArrayValue(to, 0, 0.0F);
            float toY = getArrayValue(to, 1, 0.0F);
            float toZ = getArrayValue(to, 2, 0.0F);
            float originX = origin != null ? getArrayValue(origin, 0, 0.0F) : 0.0F;
            float originY = origin != null ? getArrayValue(origin, 1, 0.0F) : 0.0F;
            float originZ = origin != null ? getArrayValue(origin, 2, 0.0F) : 0.0F;

            float rotX = 0.0F;
            float rotY = 0.0F;
            float rotZ = 0.0F;
            if (rotation != null) {
                if (rotation.has("angle") && rotation.has("axis")) {
                    float angle = rotation.get("angle").getAsFloat();
                    String axis = rotation.get("axis").getAsString();
                    switch (axis) {
                        case "x" -> rotX = angle;
                        case "y" -> rotY = angle;
                        case "z" -> rotZ = angle;
                    }
                } else {
                    rotX = rotation.has("x") ? rotation.get("x").getAsFloat() : 0.0F;
                    rotY = rotation.has("y") ? rotation.get("y").getAsFloat() : 0.0F;
                    rotZ = rotation.has("z") ? rotation.get("z").getAsFloat() : 0.0F;
                }
            }

            String partName = obj.has("name") ? obj.get("name").getAsString() : "";
            if ("thruster".equals(partName)) {
                continue;
            }
            boolean isWing = "wing".equals(partName);
            List<Face> faces = parseFaces(obj.getAsJsonObject("faces"));
            if (!faces.isEmpty()) {
                elements.add(new Element(fromX, fromY, fromZ, toX, toY, toZ, originX, originY, originZ, rotX, rotY, rotZ, isWing, faces, texW, texH));
            }
        }
        return elements;
    }

    private static List<Face> parseFaces(JsonObject facesObject) {
        List<Face> faces = new ArrayList<>();
        for (FaceDir dir : FaceDir.values()) {
            String key = dir.name().toLowerCase();
            if (!facesObject.has(key)) {
                continue;
            }
            JsonObject faceObj = facesObject.getAsJsonObject(key);
            String texture = faceObj.has("texture") ? faceObj.get("texture").getAsString() : "";
            if ("#missing".equals(texture)) {
                continue;
            }
            JsonArray uv = faceObj.getAsJsonArray("uv");
            int rotation = faceObj.has("rotation") ? faceObj.get("rotation").getAsInt() : 0;
            faces.add(new Face(
                dir,
                getArrayValue(uv, 0, 0.0F),
                getArrayValue(uv, 1, 0.0F),
                getArrayValue(uv, 2, 0.0F),
                getArrayValue(uv, 3, 0.0F),
                rotation
            ));
        }
        return faces;
    }

    private static Map<DisplayKey, DisplayTransform> parseDisplays(JsonObject displayObject) {
        Map<DisplayKey, DisplayTransform> transforms = new EnumMap<>(DisplayKey.class);
        if (displayObject == null) {
            return transforms;
        }
        for (DisplayKey key : DisplayKey.values()) {
            if (!displayObject.has(key.jsonKey)) {
                continue;
            }
            JsonObject obj = displayObject.getAsJsonObject(key.jsonKey);
            JsonArray rotation = obj.has("rotation") ? obj.getAsJsonArray("rotation") : null;
            JsonArray translation = obj.has("translation") ? obj.getAsJsonArray("translation") : null;
            JsonArray scale = obj.has("scale") ? obj.getAsJsonArray("scale") : null;
            transforms.put(key, new DisplayTransform(
                rotation != null ? getArrayValue(rotation, 0, 0.0F) : 0.0F,
                rotation != null ? getArrayValue(rotation, 1, 0.0F) : 0.0F,
                rotation != null ? getArrayValue(rotation, 2, 0.0F) : 0.0F,
                translation != null ? getArrayValue(translation, 0, 0.0F) : 0.0F,
                translation != null ? getArrayValue(translation, 1, 0.0F) : 0.0F,
                translation != null ? getArrayValue(translation, 2, 0.0F) : 0.0F,
                scale != null ? getArrayValue(scale, 0, 1.0F) : 1.0F,
                scale != null ? getArrayValue(scale, 1, 1.0F) : 1.0F,
                scale != null ? getArrayValue(scale, 2, 1.0F) : 1.0F
            ));
        }
        return transforms;
    }

    private static float getArrayValue(JsonArray array, int index, float defaultValue) {
        return array != null && index < array.size() ? array.get(index).getAsFloat() : defaultValue;
    }

    private record ModelData(
        float texW,
        float texH,
        List<Element> elements,
        Map<DisplayKey, DisplayTransform> transforms,
        float closedWingAngle
    ) {
    }

    private record Element(
        float fromX,
        float fromY,
        float fromZ,
        float toX,
        float toY,
        float toZ,
        float originX,
        float originY,
        float originZ,
        float rotX,
        float rotY,
        float rotZ,
        boolean isWing,
        List<Face> faces,
        float texW,
        float texH
    ) {
    }

    private record Face(FaceDir dir, float u1, float v1, float u2, float v2, int rotation) {
    }

    private record DisplayTransform(
        float rotX,
        float rotY,
        float rotZ,
        float tx,
        float ty,
        float tz,
        float sx,
        float sy,
        float sz
    ) {
    }

    private enum DisplayKey {
        THIRDPERSON_RIGHTHAND("thirdperson_righthand"),
        THIRDPERSON_LEFTHAND("thirdperson_lefthand"),
        FIRSTPERSON_RIGHTHAND("firstperson_righthand"),
        FIRSTPERSON_LEFTHAND("firstperson_lefthand"),
        GUI("gui"),
        GROUND("ground"),
        FIXED("fixed");

        private final String jsonKey;

        DisplayKey(String jsonKey) {
            this.jsonKey = jsonKey;
        }
    }

    private enum FaceDir {
        NORTH(0.0F, 0.0F, -1.0F),
        SOUTH(0.0F, 0.0F, 1.0F),
        EAST(1.0F, 0.0F, 0.0F),
        WEST(-1.0F, 0.0F, 0.0F),
        UP(0.0F, 1.0F, 0.0F),
        DOWN(0.0F, -1.0F, 0.0F);

        private final float normalX;
        private final float normalY;
        private final float normalZ;

        FaceDir(float normalX, float normalY, float normalZ) {
            this.normalX = normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
        }
    }
}
