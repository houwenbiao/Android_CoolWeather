package com.gree.hwb.kuweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Administrator on 2016/7/14.
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper
{

	public static final String CREATE_PROVINCE = "create table Province ("
														 + "id integer primary key autoincrement,"
														 + "province_name text,"
														 + "province_code text)";
	public static final String CREATE_CITY = "create table City ("
													 	 + "id integer primary key autoincrement,"
													 	 + "city_name text,"
														 + "city_code text,"
													 	 + "province_id integer)";
	public static final String CREATE_COUNTY = "create table County ("
													   	 + "id integer primary key autoincrement,"
													   	 + "county_name text,"
													   	 + "county_code text,"
													   	 + "city_id integer)";

	//构造方法
	public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, name, factory, version);
	}

	//数据库创建时候调用
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		//在数据库中创建表
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	//数据库升级时候调用
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
	{

	}
}
