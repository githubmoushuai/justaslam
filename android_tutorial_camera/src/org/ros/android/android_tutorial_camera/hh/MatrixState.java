package org.ros.android.android_tutorial_camera.hh;
/**
 * Created by dell on 2017/12/20.
 */
import android.opengl.Matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;
//import org.opencv.core.Mat;

//存储系统矩阵状态的类
public class MatrixState {
//    public  static float r=16;
//    public static double angle=0;
//    public static float[] mProjMatrix = new float[16];// 4x4矩阵 存储投影矩阵
//    public static float[] mVMatrix = new float[16];// 摄像机位置朝向9参数矩阵
//    public static float[] finalMatrix= new float[16];
//
//    // 设置摄像机
//    public static void setCamera(float cx, // 摄像机位置x
//                                 float cy, // 摄像机位置y
//                                 float cz, // 摄像机位置z
//                                 float tx, // 摄像机目标点x
//                                 float ty, // 摄像机目标点y
//                                 float tz, // 摄像机目标点z
//                                 float upx, // 摄像机UP向量X分量
//                                 float upy, // 摄像机UP向量Y分量
//                                 float upz // 摄像机UP向量Z分量
//    ) {
//        Matrix.setLookAtM(mVMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
//    }
//
//    // 设置透视投影参数
//    public static void setProjectFrustum(float left, // near面的left
//                                         float right, // near面的right
//                                         float bottom, // near面的bottom
//                                         float top, // near面的top
//                                         float near, // near面距离
//                                         float far // far面距离
//    ) {
//        Matrix.frustumM(mProjMatrix, 0, left, right, bottom, top, near, far);
//    }
//
//    // 获取具体物体的总变换矩阵
//    static float[] mMVPMatrix = new float[16];
//
//    public static float[] getFinalMatrix() {
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
//        finalMatrix=mMVPMatrix;
//        return mMVPMatrix;
//    }

    public static boolean poseChanged=false;

    public static double angle=0;
    public static float x=0;
    public static float y=0;
    public static float z=0;


    public static float[] mModelMatrix=new float[16];//模型矩阵
    public static float[] view_matrix =new float[16];
    public static float[] projection_matrix=new float[16];
    public static float[] final_matrix=new float[16];

    public static float[] cameraPose=new float[16];




    public static List<float[]> cubeViewList=new ArrayList<>();
    public static void addCubeView(float[] cameraPose)
    {
        float temp[]=cameraPose.clone();
        cubeViewList.add(temp);
    }
    public static void setmModelMatrix(double angle)
    {
        mModelMatrix[0]= (float) Math.cos(angle);
        mModelMatrix[1]=0;
        mModelMatrix[2]= (float) Math.sin(angle);
        mModelMatrix[3]=0;

        mModelMatrix[4]=0;
        mModelMatrix[5]=1;
        mModelMatrix[6]=0;
        mModelMatrix[7]=0;

        mModelMatrix[8]= (float) -Math.sin(angle);
        mModelMatrix[9]=0;
        mModelMatrix[10]= (float) Math.cos(angle);
        mModelMatrix[11]=0;

        mModelMatrix[12]=x;
        mModelMatrix[13]=y;
        mModelMatrix[14]=z;
        mModelMatrix[15]=1;

    }
    public static void setModelMatrix(float[] cameraPose)
    {
        mModelMatrix[0]= cameraPose[0];
        mModelMatrix[1]=cameraPose[1];
        mModelMatrix[2]= cameraPose[2];
        mModelMatrix[3]=cameraPose[3];

        mModelMatrix[4]=cameraPose[4];
        mModelMatrix[5]=cameraPose[5];
        mModelMatrix[6]=cameraPose[6];
        mModelMatrix[7]=cameraPose[7];

        mModelMatrix[8]= cameraPose[8];
        mModelMatrix[9]=cameraPose[9];
        mModelMatrix[10]= cameraPose[10];
        mModelMatrix[11]=cameraPose[11];

        mModelMatrix[12]=cameraPose[12];
        mModelMatrix[13]=cameraPose[13];
        mModelMatrix[14]=cameraPose[14];
        mModelMatrix[15]=cameraPose[15];

    }

    public static void set_view_matrix(RealMatrix rotation, RealMatrix translation)
    {
        final double d[][]={
                {1,0,0},
                {0,-1,0},
                {0,0,-1}
        };
        RealMatrix rx=new Array2DRowRealMatrix(d);
        rotation=rx.multiply(rotation);
        translation=rx.multiply(translation);
        double R[][]= rotation.getData();
        double T[][]=translation.getData();

        view_matrix[0]=(float) R[0][0];
        view_matrix[1]=(float) R[1][0];
        view_matrix[2]=(float) R[2][0];
        view_matrix[3]=0.0f;

        view_matrix[4]=(float) R[0][1];
        view_matrix[5]=(float) R[1][1];
        view_matrix[6]=(float) R[2][1];
        view_matrix[7]=0.0f;

        view_matrix[8]=(float) R[0][2];
        view_matrix[9]=(float) R[1][2];
        view_matrix[10]=(float) R[2][2];
        view_matrix[11]=0.0f;

        view_matrix[12]=(float) T[0][0];
        view_matrix[13]=(float) T[1][0];
        view_matrix[14]=(float) T[2][0];
        view_matrix[15]=1.0f;

    }

    public static void setCameraPose(RealMatrix rotation, RealMatrix translation)
    {
        RealMatrix rx=rotation.transpose();
        RealMatrix tx=rx.multiply(translation);
        double R[][]= rx.getData();
        double T[][]=tx.getData();


        cameraPose[0]=(float) R[0][0];
        cameraPose[1]=(float) R[1][0];
        cameraPose[2]=(float) R[2][0];
        cameraPose[3]=0f;

        cameraPose[4]=(float) R[0][1];
        cameraPose[5]=(float) R[1][1];
        cameraPose[6]=(float) R[2][1];
        cameraPose[7]=0f;

        cameraPose[8]=(float) R[0][2];
        cameraPose[9]=(float) R[1][2];
        cameraPose[10]=(float) R[2][2];
        cameraPose[11]=0f;

        cameraPose[12]=(float) -T[0][0];
        cameraPose[13]=(float) -T[1][0];
        cameraPose[14]=(float) -T[2][0];
        cameraPose[15]=1.0f;

        poseChanged=true;

    }
    public static float[] getCameraPose()
    {
//        float[] camPose={0f,0f,0f,1f};
//
//        Matrix.multiplyMV(camPose, 0, camera2world, 0, camPose, 0);

        return cameraPose;

    }

    public static void set_projection_matrix(float f_x,float f_y, float c_x,float c_y, float width, float height, float near_plane, float far_plane)
    {
        projection_matrix[0] = 2*f_x/width;
        projection_matrix[1] = 0.0f;
        projection_matrix[2] = 0.0f;
        projection_matrix[3] = 0.0f;

        projection_matrix[4] = 0.0f;
        projection_matrix[5] = 2*f_y/height;
        projection_matrix[6] = 0.0f;
        projection_matrix[7] = 0.0f;

        projection_matrix[8] = 1.0f - 2*c_x/width;
        projection_matrix[9] = 2*c_y/height - 1.0f;
        projection_matrix[10] = -(far_plane + near_plane)/(far_plane - near_plane);
        projection_matrix[11] = -1.0f;

        projection_matrix[12] = 0.0f;
        projection_matrix[13] = 0.0f;
        projection_matrix[14] = -2.0f*far_plane*near_plane/(far_plane - near_plane);
        projection_matrix[15] = 0.0f;
    }

    public static float[] getFinalMatrix() {
        Matrix.multiplyMM(final_matrix,0, view_matrix,0,mModelMatrix,0);
        Matrix.multiplyMM(final_matrix, 0, projection_matrix, 0, final_matrix, 0);
        return final_matrix;
    }
    public static float[] getProjection_matrix()
    {
        return projection_matrix.clone();
    }
    public static float[] getView_matrix()
    {
        return view_matrix.clone();
    }
}
