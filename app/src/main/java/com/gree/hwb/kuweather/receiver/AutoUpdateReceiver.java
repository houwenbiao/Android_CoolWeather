package com.gree.hwb.kuweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gree.hwb.kuweather.activity.WeatherActivity;
import com.gree.hwb.kuweather.service.AutoUpdateService;

/**
 * Created by Administrator on 2016/07/19.
 */
public class AutoUpdateReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Intent intent1 = new Intent(context,AutoUpdateService.class);
		context.startService(intent1);
		Toast.makeText(context,"CoolWeather接收到自定义广播",Toast.LENGTH_SHORT).show();
	}
}
