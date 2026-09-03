package hi.client.renderer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
import java.util.Set;

public final class ExactJsonModelRenderer {
    private final ResourceLocation modelLocation;
    private final ResourceLocation textureLocation;
    private final boolean skipFlatDuplicateFaces;
    private final Set<String> hiddenElementNames;
    private volatile ModelData cachedModel;

    public ExactJsonModelRenderer(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        this(modelLocation, textureLocation, false);
    }

    public ExactJsonModelRenderer(ResourceLocation modelLocation, ResourceLocation textureLocation, boolean skipFlatDuplicateFaces) {
        this(modelLocation, textureLocation, skipFlatDuplicateFaces, Set.of());
    }

    public ExactJsonModelRenderer(ResourceLocation modelLocation, ResourceLocation textureLocation, boolean skipFlatDuplicateFaces, Set<String> hiddenElementNames) {
        this.modelLocation = modelLocation;
        this.textureLocation = textureLocation;
        this.skipFlatDuplicateFaces = skipFlatDuplicateFaces;
        this.hiddenElementNames = hiddenElementNames;
    }

    public ResourceLocation texture() {
        return this.textureLocation;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ModelData model = getModel();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.textureLocation));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        for (Element element : model.elements) {
            renderElement(element, matrix, poseStack, consumer, packedLight, packedOverlay);
        }
    }

    public void applyItemTransform(PoseStack poseStack, ItemDisplayContext context) {
        ModelData model = getModel();
        DisplayTransform transform = switch (context) {
            case GUI -> model.transforms.get(DisplayKey.GUI);
            case FIRST_PERSON_RIGHT_HAND -> model.transforms.get(DisplayKey.FIRSTPERSON_RIGHTHAND);
            case FIRST_PERSON_LEFT_HAND -> model.transforms.get(DisplayKey.FIRSTPERSON_LEFTHAND);
            case THIRD_PERSON_RIGHT_HAND -> model.transforms.get(DisplayKey.THIRDPERSON_RIGHTHAND);
            case THIRD_PERSON_LEFT_HAND -> model.transforms.get(DisplayKey.THIRDPERSON_LEFTHAND);
            case GROUND -> model.transforms.get(DisplayKey.GROUND);
            case FIXED -> model.transforms.get(DisplayKey.FIXED);
            case HEAD -> model.transforms.get(DisplayKey.HEAD);
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

    private void renderElement(Element element, Matrix4f matrix, PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay) {
        for (Face face : element.faces) {
            if (this.skipFlatDuplicateFaces && isFlatDuplicateFace(element, face.dir)) {
                continue;
            }
            Vector3f[] vertices = createFaceVertices(element, face.dir);
            for (Vector3f vertex : vertices) {
                rotateVertex(vertex, element);
                vertex.sub(8.0F, 8.0F, 8.0F).mul(1.0F / 16.0F);
            }

            Vector3f normal = new Vector3f(face.dir.normalX, face.dir.normalY, face.dir.normalZ);
            rotateNormal(normal, element);
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

    private static boolean isFlatDuplicateFace(Element element, FaceDir dir) {
        if (element.fromX == element.toX && dir == FaceDir.WEST) {
            return true;
        }
        if (element.fromY == element.toY && dir == FaceDir.DOWN) {
            return true;
        }
        return element.fromZ == element.toZ && dir == FaceDir.NORTH;
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
        float u1 = face.u1 / texW;
        float v1 = face.v1 / texH;
        float u2 = face.u2 / texW;
        float v2 = face.v2 / texH;

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

    private static void rotateVertex(Vector3f vertex, Element element) {
        vertex.sub(element.originX, element.originY, element.originZ);
        if (element.rotZ != 0.0F) {
            vertex.rotateZ((float) Math.toRadians(element.rotZ));
        }
        if (element.rotY != 0.0F) {
            vertex.rotateY((float) Math.toRadians(element.rotY));
        }
        if (element.rotX != 0.0F) {
            vertex.rotateX((float) Math.toRadians(element.rotX));
        }
        vertex.add(element.originX, element.originY, element.originZ);
    }

    private static void rotateNormal(Vector3f normal, Element element) {
        if (element.rotZ != 0.0F) {
            normal.rotateZ((float) Math.toRadians(element.rotZ));
        }
        if (element.rotY != 0.0F) {
            normal.rotateY((float) Math.toRadians(element.rotY));
        }
        if (element.rotX != 0.0F) {
            normal.rotateX((float) Math.toRadians(element.rotX));
        }
    }

    private ModelData getModel() {
        ModelData local = this.cachedModel;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (this.cachedModel == null) {
                this.cachedModel = loadModel();
            }
            return this.cachedModel;
        }
    }

    private ModelData loadModel() {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(this.modelLocation);
            try (InputStreamReader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                // Vanilla block/item JSON always uses a logical 16x16 UV grid. Blockbench
                // may still emit the source PNG dimensions in texture_size; using those
                // values here would shrink and repeat the texture on high-resolution skins.
                float texW = 16.0F;
                float texH = 16.0F;
                List<Element> elements = parseElements(root.getAsJsonArray("elements"), texW, texH, this.hiddenElementNames);
                Map<DisplayKey, DisplayTransform> transforms = parseDisplays(root.getAsJsonObject("display"));
                return new ModelData(elements, transforms);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load exact model from " + this.modelLocation, e);
        }
    }

    private static List<Element> parseElements(JsonArray array, float texW, float texH, Set<String> hiddenElementNames) {
        List<Element> elements = new ArrayList<>();
        if (array == null) {
            return elements;
        }
        for (JsonElement jsonElement : array) {
            JsonObject obj = jsonElement.getAsJsonObject();
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            if (hiddenElementNames.contains(name.toLowerCase(java.util.Locale.ROOT))) {
                continue;
            }
            JsonArray from = obj.getAsJsonArray("from");
            JsonArray to = obj.getAsJsonArray("to");
            JsonObject rotation = obj.has("ctaw_rotation")
                ? obj.getAsJsonObject("ctaw_rotation")
                : (obj.has("rotation") ? obj.getAsJsonObject("rotation") : null);
            JsonArray origin = rotation != null && rotation.has("origin") ? rotation.getAsJsonArray("origin") : null;

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

            List<Face> faces = parseFaces(obj.getAsJsonObject("faces"));
            if (!faces.isEmpty()) {
                elements.add(new Element(
                    getArrayValue(from, 0, 0.0F),
                    getArrayValue(from, 1, 0.0F),
                    getArrayValue(from, 2, 0.0F),
                    getArrayValue(to, 0, 0.0F),
                    getArrayValue(to, 1, 0.0F),
                    getArrayValue(to, 2, 0.0F),
                    origin != null ? getArrayValue(origin, 0, 0.0F) : 0.0F,
                    origin != null ? getArrayValue(origin, 1, 0.0F) : 0.0F,
                    origin != null ? getArrayValue(origin, 2, 0.0F) : 0.0F,
                    rotX,
                    rotY,
                    rotZ,
                    faces,
                    texW,
                    texH
                ));
            }
        }
        return elements;
    }

    private static List<Face> parseFaces(JsonObject facesObject) {
        List<Face> faces = new ArrayList<>();
        if (facesObject == null) {
            return faces;
        }
        for (FaceDir dir : FaceDir.values()) {
            String key = dir.name().toLowerCase();
            if (!facesObject.has(key)) {
                continue;
            }
            JsonObject faceObj = facesObject.getAsJsonObject(key);
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

    private static float getArrayValue(JsonArray array, int index, float fallback) {
        return array != null && array.size() > index ? array.get(index).getAsFloat() : fallback;
    }

    private record ModelData(List<Element> elements, Map<DisplayKey, DisplayTransform> transforms) {
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
        List<Face> faces,
        float texW,
        float texH
    ) {
    }

    private record Face(FaceDir dir, float u1, float v1, float u2, float v2, int rotation) {
    }

    private enum FaceDir {
        NORTH(0.0F, 0.0F, -1.0F),
        SOUTH(0.0F, 0.0F, 1.0F),
        EAST(1.0F, 0.0F, 0.0F),
        WEST(-1.0F, 0.0F, 0.0F),
        UP(0.0F, 1.0F, 0.0F),
        DOWN(0.0F, -1.0F, 0.0F);

        final float normalX;
        final float normalY;
        final float normalZ;

        FaceDir(float normalX, float normalY, float normalZ) {
            this.normalX = normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
        }
    }

    private enum DisplayKey {
        GUI("gui"),
        GROUND("ground"),
        FIXED("fixed"),
        HEAD("head"),
        THIRDPERSON_RIGHTHAND("thirdperson_righthand"),
        THIRDPERSON_LEFTHAND("thirdperson_lefthand"),
        FIRSTPERSON_RIGHTHAND("firstperson_righthand"),
        FIRSTPERSON_LEFTHAND("firstperson_lefthand");

        final String jsonKey;

        DisplayKey(String jsonKey) {
            this.jsonKey = jsonKey;
        }
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
}
