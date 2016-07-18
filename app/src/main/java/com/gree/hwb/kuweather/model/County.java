package com.gree.hwb.kuweather.model;

/**
 * Created by Administrator on 2016/7/14.
 */
public class County
{
	private int id;
	private String countyNmae;
	private String countyCode;
	private int cityId;

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String getCountyNmae()
	{
		return countyNmae;
	}

	public void setCountyName(String countyNmae)
	{
		this.countyNmae = countyNmae;
	}

	public String getCountyCode()
	{
		return countyCode;
	}

	public void setCountyCode(String countyCode)
	{
		this.countyCode = countyCode;
	}

	public int getCityId()
	{
		return cityId;
	}

	public void setCityId(int cityId)
	{
		this.cityId = cityId;
	}
}
