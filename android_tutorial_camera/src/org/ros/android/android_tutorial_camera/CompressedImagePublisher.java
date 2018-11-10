package org.ros.android.android_tutorial_camera;

import com.google.common.base.Preconditions;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;
import android.os.Handler;

import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.ros.android.android_tutorial_camera.hh.MatrixState;
import org.ros.android.view.camera.RawImageListener;
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
    public void sendCubes()
    {



            if(clientThread.isConnect)
            {
                Message msg = new Message();
                msg.what = 0x852;
                String cubeList="";
                for(int i=0;i< MatrixState.cubeViewList.size();i++)
                {
                    float[] cube=MatrixState.cubeViewList.get(i);
                    cubeList=cubeList+String.valueOf(cube[0])+","+String.valueOf(cube[1])+","+String.valueOf(cube[2])+",";
                }
                msg.obj = "cube,"+cubeList;
                clientThread.sendHandler.sendMessage(msg);
            }
            else
            {
                Log.e("TCP","连接失败");
            }

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
        count++;
        if(clientThread.isConnect)
        {
            Message msg = new Message();
            msg.what = 0x852;
            msg.obj = "cube,"+String.valueOf(count);
            clientThread.sendHandler.sendMessage(msg);
        }
        else
        {
            Log.e("TCP","连接失败");
        }

    }
}