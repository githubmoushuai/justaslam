package org.ros.android.android_tutorial_camera.model;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.ros.android.android_tutorial_camera.hh.MatrixState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class LineRender implements GLSurfaceView.Renderer {

    public static boolean flag=false;

    public List<Stroke> mStrokes;
    public static final int TOUCH_QUEUE_SIZE = 10;
    public AtomicInteger touchQueueSize;
    public AtomicReferenceArray<Vector2f> touchQueue;
    public AtomicBoolean bNewStroke = new AtomicBoolean(false);
    public AtomicBoolean bTouchDown = new AtomicBoolean(false);
    public Map<String, Stroke> mSharedStrokes = new HashMap<>();
    public LineShaderRenderer mLineShaderRenderer = new LineShaderRenderer();
    public Vector2f mLastTouch;
    public boolean isDrawing=false;

    private Context context;
    public LineRender(Context context){
        this.context = context;

        mStrokes = new ArrayList<>();
        touchQueueSize = new AtomicInteger(0);
        touchQueue = new AtomicReferenceArray<>(TOUCH_QUEUE_SIZE);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //设置屏幕背景色RGBA
        glClearColor(0.01f,0.01f,0.01f,0.01f);
//      //打开深度检测
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        //打开背面剪裁
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        try {
            mLineShaderRenderer.createOnGlThread(context);
        } catch (IOException e) {
            Log.e("nooo","nooo");
            e.printStackTrace();
        }
        mLineShaderRenderer.bNeedsUpdate.set(true);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0,0,width,height);
    }


    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        update();

        renderScene();
    }
    private void addStroke() {


        Stroke stroke = new Stroke();
        stroke.localLine = true;
        stroke.setLineWidth(3f);
        mStrokes.add(stroke);

        Log.e("draw","draw");

    }

    /**
     * addPoint2f adds a point to the current stroke
     *
     * @param touchPoint a 2D point in screen space and is projected into 3D world space
     */
    private void addPoint2f(Vector2f... touchPoint) {
        Vector3f[] newPoints = new Vector3f[touchPoint.length];
        for (int i = 0; i < touchPoint.length; i++) {
            newPoints[i] = LineUtils
                    .GetWorldCoords(touchPoint[i], 640, 480, MatrixState.getProjection_matrix(), MatrixState.getView_matrix());
        }

        addPoint3f(newPoints);
    }

    /**
     * addPoint3f adds a point to the current stroke
     *
     * @param newPoint a 3D point in world space
     */
    private void addPoint3f(Vector3f... newPoint) {
        Vector3f point;
        Log.e("line",String.valueOf(newPoint[0])+","+String.valueOf(newPoint[1])+","+String.valueOf(newPoint[2]));
        int index = mStrokes.size() - 1;

        if (index < 0)
            return;

        for (int i = 0; i < newPoint.length; i++) {
            mStrokes.get(index).add(newPoint[i]);
        }
        isDrawing = true;
    }
    private void update() {
        try {

            // Add points to strokes from touch queue
            int numPoints = touchQueueSize.get();
            if (numPoints > TOUCH_QUEUE_SIZE) {
                numPoints = TOUCH_QUEUE_SIZE;
            }

            if (numPoints > 0) {
                if (bNewStroke.get()) {
                    bNewStroke.set(false);
                    addStroke();
                }

                Vector2f[] points = new Vector2f[numPoints];
                for (int i = 0; i < numPoints; i++) {
                    points[i] = touchQueue.get(i);
                    mLastTouch = new Vector2f(points[i].x, points[i].y);
                }
                addPoint2f(points);
            }

            // If no new points have been added, and touch is down, add last point again
            if (numPoints == 0 && bTouchDown.get()) {
                addPoint2f(mLastTouch);
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }

            if (numPoints > 0) {
                touchQueueSize.set(0);
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }


            // Check if we are still drawing, otherwise finish line
            if (isDrawing && !bTouchDown.get()) {
                isDrawing = false;
                Log.e("count",String.valueOf(mStrokes.size()));
                if (!mStrokes.isEmpty()) {
                    mStrokes.get(mStrokes.size() - 1).finishStroke();
                }
            }

            // Update line animation
//            for (int i = 0; i < mStrokes.size(); i++) {
//                mStrokes.get(i).update();
//            }
            boolean renderNeedsUpdate = false;
            for (Stroke stroke : mSharedStrokes.values()) {
                if (stroke.update()) {
                    renderNeedsUpdate = true;
                }
            }
            if (renderNeedsUpdate) {
                mLineShaderRenderer.bNeedsUpdate.set(true);
            }


            if (mLineShaderRenderer.bNeedsUpdate.get()) {
                mLineShaderRenderer.setColor(AppSettings.getColor());
                mLineShaderRenderer.mDrawDistance = AppSettings.getStrokeDrawDistance();
                float distanceScale = 0.0f;
                mLineShaderRenderer.setDistanceScale(distanceScale);
                mLineShaderRenderer.setLineWidth(3f);
                mLineShaderRenderer.clear();
                mLineShaderRenderer.updateStrokes(mStrokes, mSharedStrokes);
                mLineShaderRenderer.upload();
            }



        } catch (Exception e) {

        }
    }
    private void renderScene() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Render the lines
        if(flag) {mLineShaderRenderer
                .draw(MatrixState.getView_matrix(), MatrixState.getProjection_matrix(), 640, 480,
                        AppSettings.getNearClip(),
                        AppSettings.getFarClip());}
    }
}
