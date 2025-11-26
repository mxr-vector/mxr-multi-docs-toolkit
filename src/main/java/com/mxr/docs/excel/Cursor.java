package com.mxr.docs.excel;

import java.io.Closeable;

/**
 * 请替换为 mybatisPlus对象实现！！
 * 请替换为 mybatisPlus对象实现！！
 * 请替换为 mybatisPlus对象实现！！
 * @param <T>
 */
public interface Cursor<T> extends Closeable, Iterable<T> {
    boolean isOpen();

    boolean isConsumed();

    int getCurrentIndex();
}
