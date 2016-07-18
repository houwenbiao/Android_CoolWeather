package com.gree.hwb.kuweather.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.gree.hwb.kuweather.db.CoolWeatherDB;
import com.gree.hwb.kuweather.model.City;
import com.gree.hwb.kuweather.model.County;
import com.gree.hwb.kuweather.model.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Administrator on 2016/7/14.
 * 解析服务器中返回的数据
 */


public class Utility
{
	//解析处理服务器返回的省级数据
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB, String response)
	{
		if(!TextUtils.isEmpty(response))//服务器返回的数据不为空
		{
			String[] allProvince = response.split(",");
			//长度为0的数组 int[] arr = new int[0]，也称为空数组，
			// 虽然arr长度为0，但是依然是一个对象
			// null数组，int[] arr = null；arr是一个数组类型的空引用。
			if((allProvince != null) && (allProvince.length > 0))
			{
				for(String str : allProvince)
				{
					String sArray[] = str.split("\\|");//此处分割需要加\\
					Province p = new Province();
					p.setProvinceCode(sArray[0]);
					p.setProvinceName(sArray[1]);
					//解析出来的数据存放到Province表中
					coolWeatherDB.saveProvince(p);
				}
				return true;
			}
		}
		return false;
	}

	//解析服务器返回的City
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String[] allCity = response.split(",");//代码|城市，代码|城市
			if((allCity != null) && (allCity.length > 0))
			{
				for(String s : allCity)
				{
					String sArray[] = s.split("\\|");
					City c = new City();
					c.setCityCode(sArray[0]);
					c.setCityName(sArray[1]);
					//					Log.i("zx",sArray[1]);
					c.setProvinceId(provinceId);
					coolWeatherDB.saveCity(c);//将City保存到City表中
				}
				return true;
			}
		}
		return false;
	}

	//解析服务器返回的County
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB, String response, int cityId)
	{
		if(!TextUtils.isEmpty(response))
		{
			String allCounty[] = response.split(",");
			if((allCounty != null) && (allCounty.length > 0))
			{
				for(String s : allCounty)
				{
					String sArray[] = s.split("\\|");
					County county = new County();
					county.setCountyCode(sArray[0]);
					county.setCountyName(sArray[1]);
					county.setCityId(cityId);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;//此处可以放在else中
	}

	/*
	 *解析服务器返回的JSON数据
	 * 返回的天气信息为Json格式{"weatherinfo":{"city":"商丘","cityid":"1011..."}}
	 */
	public static void handleWeatherResponse(Context context , String response)
	{
		try
		{
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			//天气信息存放到SharedPreference中
			Log.i("zxhandleWeatherResponse",cityName+"  "+weatherDesp+"  "+temp1+"  "+temp2);
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	//将服务器返回的所有天气信息存放到SharedPreference中
	public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime)
	{
		//格式化日期,此处注意导包陷阱，不要使用icu
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒 E", Locale.CHINESE);
		SharedPreferences sharedPreferences = context.getSharedPreferences("weatherInfo", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("city_selected", true);
		editor.putString("cityName", cityName);
		editor.putString("weatherCode", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weatherDesp", weatherDesp);
		editor.putString("publishTime", publishTime);
		editor.putString("currentDate", simpleDateFormat.format(new Date()));
		editor.commit();//提交
		Log.i("zxsaveToPreferences", "saveToPreferences:" + cityName);
	}
}



















































