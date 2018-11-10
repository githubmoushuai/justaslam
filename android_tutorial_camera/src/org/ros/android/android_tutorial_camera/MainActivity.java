/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_tutorial_camera;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import org.ros.android.RosActivity;
import org.ros.android.android_tutorial_camera.hh.MatrixState;
import org.ros.android.android_tutorial_camera.hh.MyRender;
import org.ros.android.android_tutorial_camera.model.LineRender;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;
import android.util.Log;
import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Vector2f;

import static org.ros.android.android_tutorial_camera.model.LineRender.flag;

/**
 * @author ethan.rublee@gmail.com (Ethan Rublee)
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

  private int cameraId;
  private RosView rosCameraPreviewView;
  private GLSurfaceView glSurfaceView;
  private LineRender earthRender;
  public MainActivity() {
    super("CameraTutorial", "CameraTutorial");
  }
  private CompressedImagePublisher rawImageListener;

  private boolean touched=false;
  private float[] currentPose=new float[16];
  private Button btn;

  private int displayWidth=0;
  private int displayHeight=0;


  private  Handler handler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      Bundle data = msg.getData();
      String val = data.getString("count");
      btn.setText(val);
    }
  };


  /**
   * 发送添加方块
   */
  Runnable sendCube = new Runnable() {
    @Override
    public void run() {
      String poseString="";
      for(int i=0;i<16;i++)
      {
        poseString=poseString+currentPose[i]+",";
      }
      String host=getMasterUri().getHost();
      String url = "http://"+host+":5000/add";
      HashMap<String, String> param = new HashMap<>();
      param.put("add", poseString);
      String result = "";
      try {
        result = AndroidPost.doPost(url, param);

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  };

  /**
   * 请求方块数据
   */
  Runnable reqCube = new Runnable() {

    @Override
    public void run() {
      String host=getMasterUri().getHost();
      String url = "http://"+host+":5000/cube";
      HashMap<String, String> param = new HashMap<>();
      param.put("req", "cube");
      String result = "";
      try {
        result = AndroidPost.doPost(url, param);
      } catch (IOException e) {
        e.printStackTrace();
      }
      String[] resultSplit=result.split(",");
      MatrixState.cubeViewList.clear();
      int cubeCount=Integer.valueOf(resultSplit[0]);
      for(int i=0;i<cubeCount;i++)
      {
        float[] pose=new float[16];
        for(int j=0;j<16;j++)
        {
          pose[j]=Float.valueOf(resultSplit[16*i+j+1]); //挪位的1是开头计数的1位
        }
        MatrixState.cubeViewList.add(pose);
      }
      Message msg = new Message();
      Bundle data = new Bundle();
      data.putString("count",String.valueOf(cubeCount));
      msg.setData(data);
      handler.sendMessage(msg);
    }
  };

  private void setViewSize()
  {

    DisplayMetrics  dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    int screenWidth = dm.widthPixels;
    int screenHeight = dm.heightPixels;
    ViewGroup.LayoutParams params = glSurfaceView.getLayoutParams();
    params.width =screenHeight*4/3;
    params.height=screenHeight;
    glSurfaceView.setLayoutParams(params);

    params=rosCameraPreviewView.getLayoutParams();
    params.width =screenHeight*4/3;
    params.height=screenHeight;
    rosCameraPreviewView.setLayoutParams(params);

    displayHeight=screenHeight;
    displayWidth=screenHeight*4/3;
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.main);
    rosCameraPreviewView = (RosView) findViewById(R.id.ros_view);


    MatrixState.set_projection_matrix(445f, 445f, 320f, 240f, 640, 480, 0.1f, 1000f);
    MatrixState.setmModelMatrix(0);

    //opengl图层
    glSurfaceView = (GLSurfaceView) findViewById(R.id.glSurfaceView);
    //OpenGL ES 2.0
    glSurfaceView.setEGLContextClientVersion(2);
    //设置透明背景
    glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
    glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
    earthRender = new LineRender(this);
    glSurfaceView.setRenderer(earthRender);
    // 设置渲染模式为主动渲染
    glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    glSurfaceView.setZOrderOnTop(true);
    glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent tap) {
        if (tap != null) {
          int action = tap.getAction();

          if(earthRender.flag==true)      {
            Log.e("touch","touch");
            float x=tap.getX()/displayWidth*640;
            float y=tap.getY()/displayHeight*480;
            btn.setText(String.valueOf(x)+","+String.valueOf(y));
            if (action == MotionEvent.ACTION_DOWN) {
              earthRender.touchQueue.set(0, new Vector2f(x, y));
              earthRender.bNewStroke.set(true);
              earthRender.bTouchDown.set(true);
              earthRender.touchQueueSize.set(1);

              earthRender.bNewStroke.set(true);
              earthRender.bTouchDown.set(true);

              return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
              if (earthRender.bTouchDown.get()) {
                int numTouches = earthRender.touchQueueSize.addAndGet(1);
                if (numTouches <=earthRender. TOUCH_QUEUE_SIZE) {
                  earthRender.touchQueue.set(numTouches - 1, new Vector2f(x, y));
                }
              }
              return true;
            } else if (action == MotionEvent.ACTION_UP
                    || tap.getAction() == MotionEvent.ACTION_CANCEL) {
              earthRender.bTouchDown.set(false);
              return true;
            }
          }
          return true;
        } else {
          return false;
        }

    }});


    btn=(Button)findViewById(R.id.button);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        btn.setText("hehe");
        new Thread(reqCube).start();
      }
    });
    setViewSize();
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {

    }
    return true;
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {
    cameraId = 0;

    rosCameraPreviewView.setCamera(getCamera());
    try {
      java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
      java.net.InetAddress local_network_address = socket.getLocalAddress();
      socket.close();
      NodeConfiguration nodeConfiguration =
              NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());
      nodeMainExecutor.execute(rosCameraPreviewView, nodeConfiguration);
    } catch (IOException e) {
      // Socket problem
      Log.e("Camera Tutorial", "socket error trying to get networking information from the master uri");
    }


  }

  @Override
  protected void onResume() {
    super.onResume();
    glSurfaceView.onResume();

  }

  @Override
  protected void onPause() {
    super.onPause();
    glSurfaceView.onPause();
  }

  private Camera getCamera() {
    Camera cam = Camera.open(cameraId);
    Camera.Parameters camParams = cam.getParameters();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      if (camParams.getSupportedFocusModes().contains(
              Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
        camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
      } else {
        camParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
      }
    }
    cam.setParameters(camParams);
    return cam;
  }

}