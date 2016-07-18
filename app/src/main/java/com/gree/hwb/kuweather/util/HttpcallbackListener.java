package com.gree.hwb.kuweather.util;

/**
 * Created by Administrator on 2016/7/14.
 */
public interface HttpcallbackListener
{
	void onFinish(String response);
	void onError(Exception e);
}
