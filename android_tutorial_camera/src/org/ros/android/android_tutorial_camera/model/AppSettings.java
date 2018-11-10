package org.ros.android.android_tutorial_camera.model;



import javax.vecmath.Vector3f;

public class AppSettings {

    private static final Vector3f color = new Vector3f(1f, 1f, 1f);

    private static final float strokeDrawDistance = 13f;

    private static final float minDistance = 0.000001f;

    private static final float nearClip = 0.1f;

    private static final float farClip = 1000.0f;

    private static final float smoothing = 0.4f;

    private static final int smoothingCount = 1500;

    public enum LineWidth {
        SMALL(0.006f),
        MEDIUM(0.011f),
        LARGE(0.020f);

        private final float width;

        LineWidth(float i) {
            this.width = i;
        }

        public float getWidth() {
            return width;
        }
    }

    public static float getStrokeDrawDistance() {
        return strokeDrawDistance;
    }

    public static Vector3f getColor() {
        return color;
    }

    public static float getMinDistance() {
        return minDistance;
    }

    public static float getNearClip() {
        return nearClip;
    }

    public static float getFarClip() {
        return farClip;
    }

    public static float getSmoothing() {
        return smoothing;
    }

    public static int getSmoothingCount() {
        return smoothingCount;
    }
}