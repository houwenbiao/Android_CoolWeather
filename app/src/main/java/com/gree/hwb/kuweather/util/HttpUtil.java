package com.gree.hwb.kuweather.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2016/7/14.
 */
public class HttpUtil
{
	public static void sendHttpRequest(final String address,final HttpcallbackListener listener)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				HttpURLConnection conn = null;
				Log.i("zxsendHttpRequest","HttpURLConnection");
				try
				{
					URL url = new URL(address);
					Log.i("zxurl",address);
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");//请求方式
					conn.setConnectTimeout(8000);
					conn.setReadTimeout(8000);//超时等待时间
					Log.i("zxsendHttpRequest","setReadTimeout");
					//流中存放的就是服务器返回的数据
					InputStream is = conn.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));
					Log.i("zxsendHttpRequest","BufferedReader");
					StringBuilder sbResponse = new StringBuilder(1024*1024);//服务器返回数据缓冲
					String line = null;
					Log.i("zxsendHttpRequest","line:"+line+"");
					while((line = reader.readLine()) != null)
					{
						Log.i("zxreadLine前",line);
						sbResponse.append(line);
//						sbResponse.append("\n");
						Log.i("zxreadLine后",line);
					}
					reader.close();
					is.close();
					Log.i("zxCloseIO","关闭流");
					if(listener != null)
					{
						listener.onFinish(sbResponse.toString());
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					if(listener != null)
					{
						listener.onError(e);
					}
				}
				finally
				{
					if(conn != null)
					{
						conn.disconnect();
						Log.i("zxfinally","conn.disconnect()");
					}
				}
			}
		}).start();
	}
}
