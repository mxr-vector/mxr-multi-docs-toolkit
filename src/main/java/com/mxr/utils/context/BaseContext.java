package com.mxr.utils.context;

import java.io.Serializable;
import java.util.Map;

public interface BaseContext<T> extends Serializable {

	// 获取变量
	T getProperty(String key);

	// 添加变量
	void addProperty(String key, T value);

	// 移除变量
	void removeProperty(String key);

	// 获取所有变量
	Map<String, T> getProperties();

	// 初始化变量
	void setProperties(Map<String, T> properties);

	// 移除所有变量
	void removeAllProperties();
}

