package org.ros.android.android_tutorial_camera.model;

import java.util.Locale;

public class Pose {
    public static final Pose IDENTITY;
    static final Pose START_OF_SERVICE_T_GL_WORLD;
    static final Pose GL_WORLD_T_START_OF_SERVICE;

    private final Quaternion quaternion;

    private final float[] translation;

    private Pose(float var1, float var2, float var3, float var4, float var5, float var6, float var7) {
        this.quaternion = new Quaternion(var4, var5, var6, var7);
        this.translation = new float[]{var1, var2, var3};
    }

    public Pose(float[] var1, float[] var2) {
        this(var1[0], var1[1], var1[2], var2[0], var2[1], var2[2], var2[3]);
    }


    private Pose(float[] var1, Quaternion var2) {
        this.translation = var1;
        this.quaternion = var2;
    }

    public static Pose makeTranslation(float var0, float var1, float var2) {
        return new Pose(new float[]{var0, var1, var2}, IDENTITY.quaternion);
    }

    public static Pose makeTranslation(float[] var0) {
        return makeTranslation(var0[0], var0[1], var0[2]);
    }

    public static Pose makeRotation(float var0, float var1, float var2, float var3) {
        return new Pose(IDENTITY.translation, new Quaternion(var0, var1, var2, var3));
    }

    public static Pose makeRotation(float[] var0) {
        return makeRotation(var0[0], var0[1], var0[2], var0[3]);
    }

    public Pose compose(Pose var1) {
        float[] var3 = new float[3];
        Quaternion.a(this.quaternion, var1.translation, 0, var3, 0);
        var3[0] += this.translation[0];
        var3[1] += this.translation[1];
        var3[2] += this.translation[2];
        return new Pose(var3, this.quaternion.a(var1.quaternion));
    }

    public static Pose makeInterpolated(Pose var0, Pose var1, float var2) {
        if (var2 == 0.0F) {
            return var0;
        } else if (var2 == 1.0F) {
            return var1;
        } else {
            float[] var3 = new float[3];

            for(int var4 = 0; var4 < 3; ++var4) {
                var3[var4] = var0.translation[var4] * (1.0F - var2) + var1.translation[var4] * var2;
            }

            Quaternion var5 = Quaternion.a(var0.quaternion, var1.quaternion, var2);
            return new Pose(var3, var5);
        }
    }

    public Pose inverse() {
        float[] var1 = new float[3];
        Quaternion var2;
        Quaternion.a(var2 = this.quaternion.e(), this.translation, 0, var1, 0);
        var1[0] = -var1[0];
        var1[1] = -var1[1];
        var1[2] = -var1[2];
        return new Pose(var1, var2);
    }

    public Pose extractRotation() {
        return new Pose(IDENTITY.translation, this.quaternion);
    }

    public Pose extractTranslation() {
        return new Pose(this.translation, IDENTITY.quaternion);
    }

    public void toMatrix(float[] var1, int var2) {
        this.quaternion.a(var1, var2, 4);
        var1[var2 + 12] = this.translation[0];
        var1[var2 + 1 + 12] = this.translation[1];
        var1[var2 + 2 + 12] = this.translation[2];
        var1[var2 + 3] = 0.0F;
        var1[var2 + 7] = 0.0F;
        var1[var2 + 11] = 0.0F;
        var1[var2 + 15] = 1.0F;
    }

    public float tx() {
        return this.translation[0];
    }

    public float ty() {
        return this.translation[1];
    }

    public float tz() {
        return this.translation[2];
    }

    public float qx() {
        return this.quaternion.a();
    }

    public float qy() {
        return this.quaternion.b();
    }

    public float qz() {
        return this.quaternion.c();
    }

    public float qw() {
        return this.quaternion.d();
    }

    public void getTranslation(float[] var1, int var2) {
        System.arraycopy(this.translation, 0, var1, var2, 3);
    }

    public float[] getTranslation() {
        float[] var1 = new float[3];
        this.getTranslation(var1, 0);
        return var1;
    }

    public void getRotationQuaternion(float[] var1, int var2) {
        this.quaternion.a(var1, var2);
    }

    public float[] getRotationQuaternion() {
        float[] var1 = new float[4];
        this.getRotationQuaternion(var1, 0);
        return var1;
    }

    public void rotateVector(float[] var1, int var2, float[] var3, int var4) {
        Quaternion.a(this.quaternion, var1, var2, var3, var4);
    }

    public float[] rotateVector(float[] var1) {
        float[] var2 = new float[3];
        this.rotateVector(var1, 0, var2, 0);
        return var2;
    }

    public void transformPoint(float[] var1, int var2, float[] var3, int var4) {
        this.rotateVector(var1, var2, var3, var4);

        for(int var5 = 0; var5 < 3; ++var5) {
            var3[var5 + var4] += this.translation[var5];
        }

    }

    public float[] transformPoint(float[] var1) {
        float[] var2 = new float[3];
        this.transformPoint(var1, 0, var2, 0);
        return var2;
    }

    public void getTransformedAxis(int var1, float var2, float[] var3, int var4) {
        Quaternion var5 = this.quaternion;
        float[] var10;
        (var10 = new float[]{0.0F, 0.0F, 0.0F})[var1] = var2;
        Quaternion.a(var5, var10, 0, var3, var4);
    }

    public float[] getTransformedAxis(int var1, float var2) {
        float[] var3 = new float[3];
        this.getTransformedAxis(var1, var2, var3, 0);
        return var3;
    }

    public float[] getXAxis() {
        return this.getTransformedAxis(0, 1.0F);
    }

    public float[] getYAxis() {
        return this.getTransformedAxis(1, 1.0F);
    }

    public float[] getZAxis() {
        return this.getTransformedAxis(2, 1.0F);
    }

    public String toString() {
        return String.format(Locale.ENGLISH, "t:[x:%.3f, y:%.3f, z:%.3f], q:[x:%.2f, y:%.2f, z:%.2f, w:%.2f]", this.translation[0], this.translation[1], this.translation[2], this.quaternion.a(), this.quaternion.b(), this.quaternion.c(), this.quaternion.d());
    }

    Quaternion getQuaternion() {
        return this.quaternion;
    }

    static {
        IDENTITY = new Pose(new float[]{0.0F, 0.0F, 0.0F}, Quaternion.a);
        START_OF_SERVICE_T_GL_WORLD = new Pose(new float[]{0.0F, 0.0F, 0.0F}, new float[]{(float)Math.sqrt(2.0D) / 2.0F, 0.0F, 0.0F, (float)Math.sqrt(2.0D) / 2.0F});
        GL_WORLD_T_START_OF_SERVICE = new Pose(new float[]{0.0F, 0.0F, 0.0F}, new float[]{(float)(-Math.sqrt(2.0D)) / 2.0F, 0.0F, 0.0F, (float)Math.sqrt(2.0D) / 2.0F});
    }
}
