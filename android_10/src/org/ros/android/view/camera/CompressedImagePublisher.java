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

package org.ros.android.view.camera;

import com.google.common.base.Preconditions;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;
import android.os.Handler;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.internal.message.MessageBuffers;
import org.ros.message.Time;
import org.ros.namespace.NameResolver;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;
import android.util.Log;
import android.os.Looper;
import android.os.Message;
/**
 * Publishes preview frames.
 * 
 * @author damonkohler@google.com (Damon Kohler)
 */
public class CompressedImagePublisher implements RawImageListener {

  private final ConnectedNode connectedNode;
  private final Publisher<sensor_msgs.CompressedImage> imagePublisher;
  private final Publisher<sensor_msgs.CameraInfo> cameraInfoPublisher;

  private byte[] rawImageBuffer;
  private Size rawImageSize;
  private YuvImage yuvImage;
  private Rect rect;
  private ChannelBufferOutputStream stream;
  private String ip="";
  private String port="";
  Handler handler;
  ClientThread clientThread;
  int count=0;

  public CompressedImagePublisher(ConnectedNode connectedNode) {
    this.connectedNode = connectedNode;
    NameResolver resolver = connectedNode.getResolver().newChild("android1");
    imagePublisher =
        connectedNode.newPublisher(resolver.resolve("image_raw/compressed"),
            sensor_msgs.CompressedImage._TYPE);
    cameraInfoPublisher =
        connectedNode.newPublisher(resolver.resolve("camera_info"), sensor_msgs.CameraInfo._TYPE);
    stream = new ChannelBufferOutputStream(MessageBuffers.dynamicBuffer());
    Looper.prepare();

    handler = new Handler()
    {
      @Override
      public void handleMessage(Message msg)
      {
        if(msg.what == 0x123)
        {
          Log.e("TCP","answer");
        }
      }
    };
    Log.e("hehe","hehe");
    ip=connectedNode.getMasterUri().getHost();
    port=String.valueOf(8088);
    clientThread = new ClientThread(handler, ip, port);
    new Thread(clientThread).start();
    // Looper.loop();
  }

  @Override
  public void onNewRawImage(byte[] data, Size size) {
    Preconditions.checkNotNull(data);
    Preconditions.checkNotNull(size);
    if (data != rawImageBuffer || !size.equals(rawImageSize)) {
      rawImageBuffer = data;
      rawImageSize = size;
      yuvImage = new YuvImage(rawImageBuffer, ImageFormat.NV21, size.width, size.height, null);
      rect = new Rect(0, 0, size.width, size.height);
    }

    Time currentTime = connectedNode.getCurrentTime();
    String frameId = "camera";

    sensor_msgs.CompressedImage image = imagePublisher.newMessage();
    image.setFormat("jpeg");
    image.getHeader().setStamp(currentTime);
    image.getHeader().setFrameId(frameId);

    Preconditions.checkState(yuvImage.compressToJpeg(rect, 20, stream));
    image.setData(stream.buffer().copy());
    stream.buffer().clear();

    imagePublisher.publish(image);

    sensor_msgs.CameraInfo cameraInfo = cameraInfoPublisher.newMessage();
    cameraInfo.getHeader().setStamp(currentTime);
    cameraInfo.getHeader().setFrameId(frameId);

    cameraInfo.setWidth(size.width);
    cameraInfo.setHeight(size.height);
    cameraInfoPublisher.publish(cameraInfo);
    if(clientThread.isConnect)
    {
      Message msg = new Message();
      msg.what = 0x852;
      msg.obj = "get"+String.valueOf(count);
      count++;
      clientThread.sendHandler.sendMessage(msg);
    }
    else
    {
      Log.e("TCP","连接失败");
    }
  }
}