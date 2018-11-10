package org.ros.android.android_tutorial_camera;

import android.content.Context;
import android.util.AttributeSet;

import org.ros.android.android_tutorial_camera.PreviewView;
import org.ros.android.view.camera.CameraPreviewView;

import org.ros.android.view.camera.RawImageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;

/**
 * Displays and publishes preview frames from the camera.
 *
 * @author damonkohler@google.com (Damon Kohler)
 */
public class RosView extends PreviewView implements NodeMain {

    public RosView(Context context) {
        super(context);
    }

    public RosView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RosView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return GraphName.of("ros_camera_preview_view");
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        setRawImageListener(new CompressedImagePublisher(connectedNode));
    }
    public RawImageListener getRawImageListener()
    {
        return super.getRawImageListener();
    }
    @Override
    public void onShutdown(Node node) {
    }

    @Override
    public void onShutdownComplete(Node node) {
    }

    @Override
    public void onError(Node node, Throwable throwable) {
    }
}
