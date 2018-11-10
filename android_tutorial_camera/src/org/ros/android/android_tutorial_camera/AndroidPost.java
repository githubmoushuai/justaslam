package org.ros.android.android_tutorial_camera;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
public class AndroidPost {


    public static String doPost(String urlString, Map<String, String> nameValuePairs)
            throws IOException {
        URL url = new URL(urlString);//先new出一个URL地址，定位网络资源
        URLConnection connection = url.openConnection();//打开连接
        connection.setDoOutput(true);//使能往远程写操作

        //把建立的http的连接流返回给PrintWriter
        try (PrintWriter out = new PrintWriter(connection.getOutputStream())) {

            boolean first = true;
            for (Map.Entry<String, String> pair : nameValuePairs.entrySet()) {
                if (first)
                    first = false;
                else
                    out.print('&');
                String name = pair.getKey().toString();
                String value = pair.getValue().toString();
                out.print(name);
                out.print('=');
                out.print(URLEncoder.encode(value, "UTF-8"));
            }

        }

        //下面的代码是去接收服务器端的反馈信号，将返回的信号全都存放在response这个对象中，
        // 看一下api文档的StringBuilder类，就明白下面的代码了

        StringBuilder response = new StringBuilder();
        try (Scanner in = new Scanner(connection.getInputStream())) {
            while (in.hasNextLine()) {
                response.append(in.nextLine());
                response.append("\n");
            }
        } catch (IOException e) {
            if (!(connection instanceof HttpURLConnection))
                throw e;
            InputStream err = ((HttpURLConnection) connection).getErrorStream();
            if (err == null)
                throw e;
            Scanner in = new Scanner(err);
            response.append(in.nextLine());
            response.append("\n");
            in.close();
        }

        return response.toString();
    }
}
