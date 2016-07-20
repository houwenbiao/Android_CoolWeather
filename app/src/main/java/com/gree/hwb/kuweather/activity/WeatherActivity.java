package com.gree.hwb.kuweather.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gree.hwb.kuweather.R;
import com.gree.hwb.kuweather.receiver.AutoUpdateReceiver;
import com.gree.hwb.kuweather.service.AutoUpdateService;
import com.gree.hwb.kuweather.util.HttpUtil;
import com.gree.hwb.kuweather.util.HttpcallbackListener;
import com.gree.hwb.kuweather.util.Utility;

/**
 * Created by Administrator on 2016/7/16.
 */
public class WeatherActivity extends AppCompatActivity
{
	private LinearLayout weatherInfoLayout;
	//用于显示城市名字
	private TextView tvCityName;
	//用于显示发布时间
	private TextView tvPublishTime;
	//用于显示天气描述信息
	private TextView tvWeatherDesp;
	//用于显示气温1,2
	private static TextView tvTemp1,tvTemp2;
	//用于显示当前日期
	private TextView tvCurrentDate;
	Button btn_switchCity,btn_refreshWeather;
	@Override
	protected void onCreate(@Nullable final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weather_layout);
		//关联组件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.llayout_weatherInfo);
		tvCityName = (TextView) findViewById(R.id.tv_cityName);
		tvPublishTime = (TextView) findViewById(R.id.tv_publish);
		tvWeatherDesp = (TextView) findViewById(R.id.tv_weatherDesp);
		tvTemp1= (TextView) findViewById(R.id.tv_temp1);
		tvTemp2 = (TextView) findViewById(R.id.tv_temp2);
		tvCurrentDate = (TextView) findViewById(R.id.tv_currentDate);
		btn_switchCity = (Button) findViewById(R.id.btn_changeCity);
		btn_refreshWeather = (Button) findViewById(R.id.btn_refresh);
		//另一个Activity中要put  county_code
		String countyCode = getIntent().getStringExtra("county_code");
		Log.i("zx","countyCode:"+countyCode);
		//有县级代码就去查询天气
		if (!TextUtils.isEmpty(countyCode))
		{
			tvPublishTime.setText("同步中...");
			//设置控件显示
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			tvCityName.setVisibility(View.VISIBLE);//显示城市名
			Log.i("zxqueryWeatherCode","queryWeatherCode");
			queryWeatherCode(countyCode);
		} else
		{
			showWeather();
		}
		btn_switchCity.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(WeatherActivity.this,ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity",true);
				startActivity(intent);
				finish();
			}
		});
		btn_refreshWeather.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				tvPublishTime.setText("同步中...");
				SharedPreferences sharedPreferences = WeatherActivity.this.getSharedPreferences("weatherInfo", Context.MODE_PRIVATE);
				String weatherCode = sharedPreferences.getString("weatherCode","");
				if(!TextUtils.isEmpty(weatherCode))
				{
					queryWeatherInfo(weatherCode);
				}
			}
		});
	}
	//查询县级代码对应的天气代号
	public void queryWeatherCode(String countyCode)
	{
		String address = "http://www.weather.com.cn/data/list3/city"+countyCode+".xml";
		Log.i("zxaddress",address+address);
		queryFromServer(address,"countyCode");

	}
	//根据天气代码查询对应的天气信息
	private void queryWeatherInfo(String weatherCode)
	{
//		String address = "http://m.weather.com.cn/data/zs/"+weatherCode+".html";
		String address = "http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		Log.i("zxqueryWeatherInfo","queryWeatherInfoaddress:"+address);
		queryFromServer(address,"weatherCode");
		Log.i("zx","queryWeatherInfo中的weatherCode:"+weatherCode);
	}
	//根据传入的地址和类型去服务器查询天气代码或者天气信息
	private void queryFromServer(final String address,final String type)
	{
		HttpUtil.sendHttpRequest(address, new HttpcallbackListener()
		{
			@Override
			public void onFinish(final String response)
			{
				Log.i("zxonFinish","type:"+type);
				if ("countyCode".equals(type))
				{
					if (!TextUtils.isEmpty(response))
					{
						//从服务器中返回的数据中解析出天气代码
						String array[] = response.split("\\|");//190404|1904040110后面就是天气代码
						if ((array!= null)&&(array.length == 2))
						{
							String weatherCode = array[1];//获取天气代码
							byte b[] = weatherCode.getBytes();
							String wcode = new String(b,0,9);//此处不转化时候会出现换行错误
							Log.i("zxonFinish","weatherCode:"+weatherCode);//打印获取的天气代码
							Log.i("zx",wcode.compareTo("101010100")+"");
							queryWeatherInfo(wcode);//根据天气代码查询天气信息
						}
					}
				}
				else if("weatherCode".equals(type))
				{
					//处理服务器返回的天气信息
					Log.i("zxweatherCode","下面由weatherCode查询天气信息");
					Utility.handleWeatherResponse(WeatherActivity.this,response);
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							showWeather();//获取天气信息从SharedPreferences中
						}
					});
				}
			}

			@Override
			public void onError(Exception e)
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						Log.i("zxonError","zxonError");
						tvPublishTime.setText("更新失败");
					}
				});
			}
		});
	}
	//从SharedPreferences中读取存储的天气信息
	public void showWeather()
	{
		SharedPreferences sharedPreferences = WeatherActivity.this.getSharedPreferences("weatherInfo", Context.MODE_PRIVATE);
		tvCityName.setText(sharedPreferences.getString("cityName",""));
		tvCurrentDate.setText(sharedPreferences.getString("currentDate",""));
		tvWeatherDesp.setText(sharedPreferences.getString("weatherDesp",""));
		tvPublishTime.setText("今天"+sharedPreferences.getString("publishTime","")+"发布");
		tvTemp2.setText(sharedPreferences.getString("temp2",""));
		tvTemp1.setText(sharedPreferences.getString("temp1",""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		tvCityName.setVisibility(View.VISIBLE);
		Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
		startService(intent);//开启服务
		Log.i("zxSharedPreferences",sharedPreferences.getString("cityName",""));
	}
}






































