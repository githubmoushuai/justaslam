package org.ros.android.android_tutorial_camera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.ros.android.android_tutorial_camera.hh.MatrixState;
import org.ros.android.android_tutorial_camera.hh.MyRender;
import org.ros.android.android_tutorial_camera.model.LineRender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ClientThread implements Runnable
{
	//For debug
	private final String TAG = "ClientThread";
	
	private Socket socket;
	private String ip;
	private int port;
	private Handler receiveHandler;
	public Handler sendHandler;
	BufferedReader bufferedReader;
	private InputStream inputStream;
	private OutputStream outputStream;
	public boolean isConnect = false;
	public boolean cubeInited=true;
	public ClientThread(Handler handler, String ip, String port) {
		this.receiveHandler = handler;
		this.ip = ip;
		this.port = Integer.valueOf(port);
		Log.e(TAG, "ClientThread's construct is OK!!");
	}

	public void run()
	{
		try 
		{
			Log.e(TAG, "Into the run()");
			socket = new Socket(ip, port);
			isConnect = socket.isConnected();
			Log.e(TAG,String.valueOf(isConnect));
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			
			//To monitor if receive Msg from Server
			new Thread()
			{
				@Override
				public void run()
				{
					byte[] buffer = new byte[1024];
					

					try
					{
						while(socket.isConnected())
						{
							final StringBuilder stringBuilder = new StringBuilder();
							int readSize = inputStream.read(buffer);
							Log.d(TAG, "readSize:" + readSize);
							
							//If Server is stopping
							if(readSize == -1)
							{
								inputStream.close();
								outputStream.close();
								continue;
							}
							if(readSize == 0)continue;
							
							//Update the receive editText
							stringBuilder.append(new String(buffer, 0, readSize));
							Message msg = new Message();
							msg.what = 0x123;
							msg.obj = stringBuilder.toString();
							receiveHandler.sendMessage(msg);
							String response=(String) msg.obj;
							Log.e("response",response);
							stringBuilder.delete(0,stringBuilder.length());

							String[] responseSplit=response.split(",");
							//Log.e("length",String.valueOf(responseSplit.length));

//							if(responseSplit.length>0&&responseSplit[0].equals("cube??"))
//							{
//								MatrixState.cubeViewList.clear();
//								for(int i=2;i<Integer.valueOf(responseSplit[1]);i+=3)
//								{
//									float []cubePos=new float[4];
//									cubePos[0]=Float.valueOf(responseSplit[i]);
//									cubePos[1]=Float.valueOf(responseSplit[i+1]);
//									cubePos[2]=Float.valueOf(responseSplit[i+2]);
//									cubePos[3]=1;
//									MatrixState.cubeViewList.add(cubePos);
//								}
//								cubeInited=true;
//								break;
//							}
//							else
								if (responseSplit.length>=16) {
								double[][] pose = new double[4][4];
								//System.out.println("one posematrix is below========");
								for (int i = 0; i < 4; i++) {
									for (int j = 0; j < 4; j++) {

										if (j == 3 && i != 3) {
											pose[i][j] = Float.valueOf(responseSplit[i * 4 + j]) * 10;
											//pose[i][j] = Float.valueOf(responseSplit[i * 4 + j]) ;
										} else {
											pose[i][j] = Float.valueOf(responseSplit[i * 4 + j]);
										}

									}

								}


								double[][] R = new double[3][3];
								double[] T = new double[3];

								for (int i = 0; i < 3; i++) {
									for (int j = 0; j < 3; j++) {
										R[i][j] = pose[i][j];
									}
								}
								for (int i = 0; i < 3; i++) {
									T[i] = pose[i][3];
								}
								RealMatrix rotation = new Array2DRowRealMatrix(R);
								RealMatrix translation = new Array2DRowRealMatrix(T);
								MatrixState.set_view_matrix(rotation, translation);
                                MatrixState.setCameraPose(rotation,translation);
								LineRender.flag = true;

							} else {
								//如果没有得到相机的位姿矩阵，就不画立方体
									LineRender.flag = false;
							}
						}
					}
					catch(IOException e)
					{
						Log.d(TAG, e.getMessage());
						e.printStackTrace();
					}
				}
				
			}.start();
			
			//To Send Msg to Server
			Looper.prepare();
			sendHandler = new Handler()
			{
				@Override
				public void handleMessage(Message msg)
				{
					if (msg.what == 0x852)
					{
						try
						{
							outputStream.write((msg.obj.toString() + "\r\n").getBytes());
							outputStream.flush();
						}
						catch (Exception e)
						{
							Log.d(TAG, e.getMessage());
							e.printStackTrace();
						}
					}
				}
			};
			Looper.loop();
			
		} catch (SocketTimeoutException e) 
		{
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}catch (UnknownHostException e) 
		{
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
	}
}
