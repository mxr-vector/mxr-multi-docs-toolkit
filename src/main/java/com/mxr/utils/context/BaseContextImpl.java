package com.mxr.utils.context;

import lombok.ToString;

import java.io.Serial;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ToString
public class BaseContextImpl<T> implements BaseContext<T> {

	@Serial
	private static final long serialVersionUID = 8383356012441014698L;

	// 变量
	private Map<String, T> properties = new ConcurrentHashMap<>();

	public BaseContextImpl() {
	}

	@Override
	public T getProperty(String key) {
		return properties.get(key);
	}

	@Override
	public void addProperty(String key, T value) {
		if(!TtlContextHolderUtil.isRequestThread()){
			throw new UnsupportedOperationException("仅允许请求线程执行add操作");
		}
		properties.put(key, value);
	}

	@Override
	public void removeProperty(String key) {
		properties.remove(key);
	}

	@Override
	public Map<String, T> getProperties() {
		return properties;
	}

	@Override
	public void setProperties(Map<String, T> map) {
		if(!TtlContextHolderUtil.isRequestThread()){
			throw new UnsupportedOperationException("仅允许请求线程执行set操作");
		}
		this.properties = map;
	}

	@Override
	public void removeAllProperties() {
		properties.clear();
	}
}

