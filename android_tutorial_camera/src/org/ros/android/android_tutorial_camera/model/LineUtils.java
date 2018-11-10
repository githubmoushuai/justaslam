package org.ros.android.android_tutorial_camera.model;


import android.opengl.Matrix;


import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;


public class LineUtils {

    public static Vector3f GetWorldCoords(Vector2f touchPoint, float screenWidth,
                                          float screenHeight, float[] projectionMatrix, float[] viewMatrix) {
        Ray touchRay = projectRay(new Vector2f(touchPoint), screenWidth, screenHeight, projectionMatrix,
                viewMatrix);
        touchRay.direction.scale(AppSettings.getStrokeDrawDistance());
        touchRay.origin.add(touchRay.direction);
        return touchRay.origin;
    }

    private static Ray screenPointToRay(Vector2f point, Vector2f viewportSize, float[] viewProjMtx) {
        point.y = viewportSize.y - point.y;
        float x = point.x * 2.0F / viewportSize.x - 1.0F;
        float y = point.y * 2.0F / viewportSize.y - 1.0F;
        float[] farScreenPoint = new float[]{x, y, 1.0F, 1.0F};
        float[] nearScreenPoint = new float[]{x, y, -1.0F, 1.0F};
        float[] nearPlanePoint = new float[4];
        float[] farPlanePoint = new float[4];
        float[] invertedProjectionMatrix = new float[16];
        Matrix.setIdentityM(invertedProjectionMatrix, 0);
        Matrix.invertM(invertedProjectionMatrix, 0, viewProjMtx, 0);
        Matrix.multiplyMV(nearPlanePoint, 0, invertedProjectionMatrix, 0, nearScreenPoint, 0);
        Matrix.multiplyMV(farPlanePoint, 0, invertedProjectionMatrix, 0, farScreenPoint, 0);
        Vector3f direction = new Vector3f(farPlanePoint[0] / farPlanePoint[3],
                farPlanePoint[1] / farPlanePoint[3], farPlanePoint[2] / farPlanePoint[3]);
        Vector3f origin = new Vector3f(new Vector3f(nearPlanePoint[0] / nearPlanePoint[3],
                nearPlanePoint[1] / nearPlanePoint[3], nearPlanePoint[2] / nearPlanePoint[3]));
        direction.sub(origin);
        direction.normalize();
        return new Ray(origin, direction);
    }

    private static Ray projectRay(Vector2f touchPoint, float screenWidth, float screenHeight,
                                  float[] projectionMatrix, float[] viewMatrix) {
        float[] viewProjMtx = new float[16];
        Matrix.multiplyMM(viewProjMtx, 0, projectionMatrix, 0, viewMatrix, 0);
        return screenPointToRay(touchPoint, new Vector2f(screenWidth, screenHeight),
                viewProjMtx);
    }

    public static boolean distanceCheck(Vector3f newPoint, Vector3f lastPoint) {
        Vector3f temp = new Vector3f();
        temp.sub(newPoint, lastPoint);
        return temp.lengthSquared() > AppSettings.getMinDistance();
    }


    /**
     * Transform a vector3f FROM anchor coordinates TO world coordinates
     */
    public static Vector3f TransformPointFromPose(Vector3f point, Pose anchorPose) {
        float[] position = new float[3];
        position[0] = point.x;
        position[1] = point.y;
        position[2] = point.z;

        position = anchorPose.transformPoint(position);
        return new Vector3f(position[0], position[1], position[2]);
    }

    /**
     * Transform a vector3f TO anchor coordinates FROM world coordinates
     */
    public static Vector3f TransformPointToPose(Vector3f point, Pose anchorPose) {
        // Recenter to anchor
        float[] position = new float[3];
        position[0] = point.x;
        position[1] = point.y;
        position[2] = point.z;

        position = anchorPose.inverse().transformPoint(position);
        return new Vector3f(position[0], position[1], position[2]);
    }

}