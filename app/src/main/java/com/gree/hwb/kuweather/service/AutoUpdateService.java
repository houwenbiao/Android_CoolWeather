package com.gree.hwb.kuweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import com.gree.hwb.kuweather.activity.WeatherActivity;
import com.gree.hwb.kuweather.receiver.AutoUpdateReceiver;
import com.gree.hwb.kuweather.util.HttpUtil;
import com.gree.hwb.kuweather.util.HttpcallbackListener;
import com.gree.hwb.kuweather.util.Utility;

import java.util.Date;

/**
 * Created by Administrator on 2016/07/18.
 */
public class AutoUpdateService extends Service
{
	public class UpdataWeatherBinder extends Binder
	{
		public void update()
		{
			Log.i("zx","updataWeatherBinder");
		}
	}
	UpdataWeatherBinder updataWeatherBinder = new UpdataWeatherBinder();
	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return updataWeatherBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				updateWeather();//自动更新天气
				Log.i("zx","服务中的onStartCommand方法执行updateWeather()方法在：" + new Date().toString());
			}
		}).start();

		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		long triggerAtTime = SystemClock.elapsedRealtime()+100*1000;
		Intent i = new Intent(AutoUpdateService.this,AutoUpdateReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(AutoUpdateService.this,0,i,0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
		return super.onStartCommand(intent, flags, startId);
	}

	private void updateWeather()
	{
		SharedPreferences sharedPreferences = AutoUpdateService.this.getSharedPreferences("weatherInfo", Context.MODE_PRIVATE);
		String weatherCode =sharedPreferences.getString("weatherCode","");
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpcallbackListener()
		{
			@Override
			public void onFinish(String response)
			{
				Utility.handleWeatherResponse(AutoUpdateService.this,response);
			}

			@Override
			public void onError(Exception e)
			{
				e .printStackTrace();
			}
		});
	}
}
