package com.mxr.utils.ik;

import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;

import java.util.List;

/**
 * @className: IKConfig
 * @author: WJH(yuanJie)
 * @date: 2025/11/12 12:39
 * @description: IK分词配置
 */
public class IKConfig extends DefaultConfig {
    private IKConfig() {
    }

    @Override
    public boolean useSmart() {
        return super.useSmart();
    }

    /**
     * 设置是否使用智能分词
     *
     * @param useSmart
     */
    @Override
    public void setUseSmart(boolean useSmart) {
        super.setUseSmart(useSmart);
    }

    /**
     * 获取扩展字典
     *
     * @return
     */
    @Override
    public List<String> getExtDictionarys() {
        return List.of("dic/ext_dict.dic");
    }

    /**
     * 获取扩展停止词字典
     *
     * @return
     */
    @Override
    public List<String> getExtStopWordDictionarys() {
        return List.of("dic/ext_stop_words.dic");
    }

    /**
     * 返回单例
     *
     * @return Configuration单例
     */
    public static Configuration getInstance() {
        return new IKConfig();
    }
}
