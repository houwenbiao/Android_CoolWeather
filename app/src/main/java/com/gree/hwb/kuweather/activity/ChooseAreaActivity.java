package com.gree.hwb.kuweather.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gree.hwb.kuweather.R;
import com.gree.hwb.kuweather.db.CoolWeatherDB;
import com.gree.hwb.kuweather.model.City;
import com.gree.hwb.kuweather.model.County;
import com.gree.hwb.kuweather.model.Province;
import com.gree.hwb.kuweather.util.HttpUtil;
import com.gree.hwb.kuweather.util.HttpcallbackListener;
import com.gree.hwb.kuweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/7/14.
 */
public class ChooseAreaActivity extends AppCompatActivity
{
	private ListView listView;
	private TextView tvTitle;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	private ProgressDialog progressDialog;
	//当前选中的级别
	private int currentLevel;
	//当前选中的省,市
	private Province selectProvince;
	private City selectCity;
	//省列表，市列表，县列表
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private List<String> dataList = new ArrayList<>();//用于存放省或者市或者县的名字
	private boolean isFromWeatherActivity;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
		SharedPreferences sharedPreferences = ChooseAreaActivity.this.getSharedPreferences("weatherInfo", Context.MODE_PRIVATE);
		if ((sharedPreferences.getBoolean("city_selected",false))&&(!isFromWeatherActivity))
		{
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.lv_list);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		//android.R.layout.simple_list_item_1:系统布局，仅用来显示文字
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long l)
			{
				Log.i("hwb","setOnItemClickListener");
				if(currentLevel == LEVEL_PROVINCE)//如果处于省级列表，查询选中省的所有城市
				{
					selectProvince = provinceList.get(index);
					queryCities();//查询所有城市
				}
				else if(currentLevel == LEVEL_CITY)//如果处于市级列表，查询选中省的所有乡镇
				{
					selectCity = cityList.get(index);
					queryCounties();
				}
				else if(currentLevel == LEVEL_COUNTY)
				{
					//获取城镇代码
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code",countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvince();//加载省级数据
	}

	//查询全国所有的省，优先在数据库中查询，无则进入服务器查询
	private void queryProvince()
	{
		provinceList = coolWeatherDB.loadProvinces();//获取所有省存放到provinceList列表中
		if (provinceList.size() > 0 )
		{
			dataList.clear();
			for (Province p : provinceList)
			{
				dataList.add(p.getProvinceName());//将所有省的名字存放到datalist列表中
			}
			//notifyDataSetChanged()可以在修改适配器绑定的数组后，
			// 不用重新刷新Activity，通知Activity更新ListView
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			tvTitle.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else
		{
			queryFromServer(null,"province");
		}
	}
	//从数据库中查询所有City
	private void queryCities()
	{
		cityList = coolWeatherDB.loadCitis(selectProvince.getId());
		if (cityList.size() > 0)
		{
			dataList.clear();
			for (City c:cityList)
			{
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			tvTitle.setText(selectProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else
		{
			queryFromServer(selectProvince.getProvinceCode(),"city");
		}
	}

	//查询所有County
	private void queryCounties()
	{
		countyList = coolWeatherDB.loadCountis(selectCity.getId());
		if (countyList.size() > 0)
		{
			dataList.clear();
			for (County county:countyList)
			{
				dataList.add(county.getCountyNmae());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			tvTitle.setText(selectCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else
		{
			queryFromServer(selectCity.getCityCode(),"county");
		}
	}
	//根据传入的代号和类型从服务器查询指定数据
	private void queryFromServer(final String code,final String type)
	{
		String address;
		if (!TextUtils.isEmpty(code))
		{
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		} else
		{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();//显示进度对话框
		HttpUtil.sendHttpRequest(address,new HttpcallbackListener()
		{
			@Override
			public void onFinish(String response)
			{
				boolean result = false;
				if ("province".equals(type))
				{
					//解析处理服务器返回的省级数据,并保存到数据库
					result = Utility.handleProvinceResponse(coolWeatherDB,response);
				}
				else if("city".equals(type))
				{
					result = Utility.handleCityResponse(coolWeatherDB,response,selectProvince.getId());
				}
				else if("county".equals(type))
				{
					result = Utility.handleCountyResponse(coolWeatherDB,response,selectCity.getId());
				}

				if(result)
				{
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							closeProgressDialog();
							if ("province".equals(type))
							{
								queryProvince();
							} else if("city".equals(type))
							{
								queryCities();
							}
							else if("county".equals(type))
							{
								queryCounties();
							}
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
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this,"加载失败",Toast.LENGTH_LONG).show();
					}
				});
			}
		});

	}
	//显示进度对话框
	private  void showProgressDialog()
	{
		if(progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载数据...");//显示提示内容
			//就是在loading的时候，如果你触摸屏幕其它区域，
			// 就会让这个progressDialog消失，然后可能出现崩溃问题
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setTitle("请等待数据加载，谢谢");//设置标题
			//progressDialog.setIcon(R.mipmap.weather);//设置一个图片
//			progressDialog.setIndeterminate(true);
			//setIndeterminate(false)这个属性对于ProgressDailog默认的转轮模式没有实际意义，
			// 默认下设置为true，它仅仅对带有ProgressBar的Dialog有作用。
			// 修改这个属性为false后可以实时更新进度条的进度。
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置进度对话框风格
		}
		progressDialog.show();//显示对话框
	}
	//关闭显示对话框
	private void closeProgressDialog()
	{
		if(progressDialog != null)
		{
			progressDialog.dismiss();
		}
	}
	//捕捉返回键，根据当前级别判断如何返回
	@Override
	public void onBackPressed()
	{
		if (currentLevel == LEVEL_COUNTY)
		{
			queryCities();
		}
		else if(currentLevel == LEVEL_CITY)
		{
			queryProvince();
		}
		else
		{
			if(isFromWeatherActivity)
			{
				Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}














































