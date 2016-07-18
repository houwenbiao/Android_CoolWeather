package com.gree.hwb.kuweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gree.hwb.kuweather.model.City;
import com.gree.hwb.kuweather.model.County;
import com.gree.hwb.kuweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/14.
 * 存放一些操作数据库的方法
 */
public class CoolWeatherDB
{
	//数据库名
	public static final String DB_NAME = "coolWeather.db";
	//数据库版本
	public static final int DB_VERSION = 1;
	private static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase db;
	//构造方法私有
	private CoolWeatherDB(Context context)
	{
		CoolWeatherOpenHelper coolWeatherOpenHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,DB_VERSION);
		db = coolWeatherOpenHelper.getWritableDatabase();
	}
	//获取CoolWeatherDB实例
	public synchronized static CoolWeatherDB getInstance(Context context)
	{
		if(coolWeatherDB == null)
		{
			coolWeatherDB = new CoolWeatherDB(context);
		}
		return coolWeatherDB;
	}

	//将Province实例存储到数据库

	public void saveProvince(Province province)
	{
		if(province != null)
		{
			ContentValues values = new ContentValues();
			values.put("province_name",province.getProvinceName());
			values.put("province_code",province.getProvinceCode());
			db.insert("Province",null,values);
		}
	}

	//从数据库读取全国的省份信息
	public List<Province> loadProvinces()
	{
		List<Province> List =new ArrayList<>();
		//获取结果集cursor
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if(cursor.moveToFirst())
		{
			do
			{
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				List.add(province);
			}
			while (cursor.moveToNext());
		}
		if(cursor != null)
		{
			cursor.close();
		}
		return List;
	}

	//将City实例存储到数据库
	public void saveCity(City city)
	{
		if(city != null)
		{
			ContentValues values = new ContentValues();
//			values.put("id",city.getId());//因为id已经设置为自增长，此处如果再设置就会出错
//			Log.i("zx",""+city.getId());
			values.put("city_name",city.getCityName());
			values.put("city_code",city.getCityCode());
			values.put("province_id",city.getProvinceId());
			db.insert("City",null,values);
		}
	}

	//从数据库中读取某省下的所有城市信息
	public List<City> loadCitis(int provinceId)
	{
		List<City> List = new ArrayList<>();
		Cursor cursor = db.query("City",null, "province_id = ?", new String [] {String.valueOf(provinceId)},null,null,null);
		if(cursor.moveToFirst())
		{
			do
			{
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				List.add(city);
			}
			while (cursor.moveToNext());
		}
		if(cursor != null)
		{
			cursor.close();
		}
		return List;
	}

	//存储County实例到数据库
	public void saveCounty(County county)
	{
		if(county != null)
		{
			ContentValues values = new ContentValues();
//			values.put("id",county.getId());
			values.put("county_name",county.getCountyNmae());
			values.put("county_code",county.getCountyCode());
			values.put("city_id",county.getCityId());
			db.insert("County",null,values);
		}
	}
	//从数据库中读取一个City下的所有County
	public List<County> loadCountis(int cityId)
	{
		List<County> List = new ArrayList<>();
		Cursor cursor = db.query("County",null,"city_id = ?",new String[]{String.valueOf(cityId)},null,null,null,null);
		while(cursor.moveToNext())
		{
			County county = new County();
			county.setId(cursor.getInt(cursor.getColumnIndex("id")));
			county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
			county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
			county.setCityId(cityId);
			List.add(county);
		}
		if(cursor != null)
		{
			cursor.close();
		}
		return List;
	}

}






























