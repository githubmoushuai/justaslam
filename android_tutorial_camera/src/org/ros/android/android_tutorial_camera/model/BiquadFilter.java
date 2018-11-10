package org.ros.android.android_tutorial_camera.model;

import javax.vecmath.Vector3f;


/**
 * BiquadFilter is a object for easily lowpass filtering incomming values.
 */
public class BiquadFilter {
    private BiquadFilterInstance[] inst;

    public BiquadFilter(double Fc, int dimensions) {
        inst = new BiquadFilterInstance[dimensions];
        for (int i = 0; i < dimensions; i++) {
            inst[i] = new BiquadFilterInstance(Fc);
        }
    }

    public float update(float in) {
        if (inst.length != 1) {
            throw new Error("Expected 1 dimension");
        }

        return (float) inst[0].process(in);
    }

    public Vector3f update(Vector3f in) {
        if (inst.length != 3) {
            throw new Error("Expected 3 dimensions");
        }

        Vector3f val = new Vector3f();
        val.x = (float) inst[0].process(in.x);
        val.y = (float) inst[1].process(in.y);
        val.z = (float) inst[2].process(in.z);
        return val;
    }


    private class BiquadFilterInstance {
        double a0, a1, a2, b1, b2;
        double Fc = 0.5, Q = 0.707;
        double z1 = 0.0, z2 = 0.0;

        BiquadFilterInstance(double fc) {
            Fc = fc;
            calcBiquad();
        }

        double process(double in) {
            double out = in * a0 + z1;
            z1 = in * a1 + z2 - b1 * out;
            z2 = in * a2 - b2 * out;
            return out;
        }

        void calcBiquad() {
            double norm;
            double K = Math.tan(Math.PI * Fc);
            norm = 1 / (1 + K / Q + K * K);
            a0 = K * K * norm;
            a1 = 2 * a0;
            a2 = a0;
            b1 = 2 * (K * K - 1) * norm;
            b2 = (1 - K / Q + K * K) * norm;
        }
    }
}
