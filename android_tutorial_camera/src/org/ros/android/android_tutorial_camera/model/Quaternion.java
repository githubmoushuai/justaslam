package org.ros.android.android_tutorial_camera.model;




class Quaternion {
    public static final Quaternion a = new Quaternion();

    private float x = 0.0F;

    private float y = 0.0F;

    private float z = 0.0F;

    private float w = 1.0F;

    public Quaternion() {
        this.a(0.0F, 0.0F, 0.0F, 1.0F);
    }

    private Quaternion(Quaternion var1) {
        this.a(var1.x, var1.y, var1.z, var1.w);
    }


    public Quaternion(float var1, float var2, float var3, float var4) {
        this.a(var1, var2, var3, var4);
    }

    private final void a(float var1, float var2, float var3, float var4) {
        this.x = var1;
        this.y = var2;
        this.z = var3;
        this.w = var4;
    }

    public final float a() {
        return this.x;
    }

    public final float b() {
        return this.y;
    }

    public final float c() {
        return this.z;
    }

    public final float d() {
        return this.w;
    }

    public final void a(float[] var1, int var2) {
        var1[var2] = this.x;
        var1[var2 + 1] = this.y;
        var1[var2 + 2] = this.z;
        var1[var2 + 3] = this.w;
    }

    public final Quaternion e() {
        return new Quaternion(-this.x, -this.y, -this.z, this.w);
    }

    public final Quaternion a(Quaternion var1) {
        Quaternion var2 = new Quaternion();
        var2.x = this.x * var1.w + this.y * var1.z - this.z * var1.y + this.w * var1.x;
        var2.y = -this.x * var1.z + this.y * var1.w + this.z * var1.x + this.w * var1.y;
        var2.z = this.x * var1.y - this.y * var1.x + this.z * var1.w + this.w * var1.z;
        var2.w = -this.x * var1.x - this.y * var1.y - this.z * var1.z + this.w * var1.w;
        return var2;
    }

    public static Quaternion a(Quaternion var0, Quaternion var1, float var2) {
        Quaternion var3 = new Quaternion();
        float var4;
        if ((var4 = var0.x * var1.x + var0.y * var1.y + var0.z * var1.z + var0.w * var1.w) < 0.0F) {
            var1 = new Quaternion(var1);
            var4 = -var4;
            var1.x = -var1.x;
            var1.y = -var1.y;
            var1.z = -var1.z;
            var1.w = -var1.w;
        }

        float var5 = (float)Math.acos((double)var4);
        float var6;
        float var7;
        float var8;
        if ((double)Math.abs(var6 = (float)Math.sqrt((double)(1.0F - var4 * var4))) > 0.001D) {
            float var9 = 1.0F / var6;
            var7 = (float)Math.sin((double)((1.0F - var2) * var5)) * var9;
            var8 = (float)Math.sin((double)(var2 * var5)) * var9;
        } else {
            var7 = 1.0F - var2;
            var8 = var2;
        }

        var3.x = var7 * var0.x + var8 * var1.x;
        var3.y = var7 * var0.y + var8 * var1.y;
        var3.z = var7 * var0.z + var8 * var1.z;
        var3.w = var7 * var0.w + var8 * var1.w;
        float var11 = (float)(1.0D / Math.sqrt((double)(var3.x * var3.x + var3.y * var3.y + var3.z * var3.z + var3.w * var3.w)));
        var3.x *= var11;
        var3.y *= var11;
        var3.z *= var11;
        var3.w *= var11;
        return var3;
    }

    public final void a(float[] var1, int var2, int var3) {
        float var4;
        float var5 = (var4 = this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w) > 0.0F ? 2.0F / var4 : 0.0F;
        float var6 = this.x * var5;
        float var7 = this.y * var5;
        float var8 = this.z * var5;
        float var9 = this.w * var6;
        float var10 = this.w * var7;
        float var11 = this.w * var8;
        float var12 = this.x * var6;
        float var13 = this.x * var7;
        float var14 = this.x * var8;
        float var15 = this.y * var7;
        float var16 = this.y * var8;
        float var17 = this.z * var8;
        var1[var2] = 1.0F - (var15 + var17);
        var1[var2 + 4] = var13 - var11;
        var1[var2 + 8] = var14 + var10;
        var1[var2 + 1] = var13 + var11;
        var1[var2 + 1 + 4] = 1.0F - (var12 + var17);
        var1[var2 + 1 + 8] = var16 - var9;
        var1[var2 + 2] = var14 - var10;
        var1[var2 + 2 + 4] = var16 + var9;
        var1[var2 + 2 + 8] = 1.0F - (var12 + var15);
    }

    public static void a(Quaternion var0, float[] var1, int var2, float[] var3, int var4) {
        float var5 = var1[var2];
        float var6 = var1[var2 + 1];
        float var7 = var1[var2 + 2];
        float var8 = var0.x;
        float var9 = var0.y;
        float var10 = var0.z;
        float var11 = var0.w;
        float var12 = var0.w * var5 + var9 * var7 - var10 * var6;
        float var13 = var11 * var6 + var10 * var5 - var8 * var7;
        float var14 = var11 * var7 + var8 * var6 - var9 * var5;
        float var15 = -var8 * var5 - var9 * var6 - var10 * var7;
        var3[var4] = var12 * var11 + var15 * -var8 + var13 * -var10 - var14 * -var9;
        var3[var4 + 1] = var13 * var11 + var15 * -var9 + var14 * -var8 - var12 * -var10;
        var3[var4 + 2] = var14 * var11 + var15 * -var10 + var12 * -var9 - var13 * -var8;
    }

    public String toString() {
        return String.format("[%.3f, %.3f, %.3f, %.3f]", this.x, this.y, this.z, this.w);
    }
}
