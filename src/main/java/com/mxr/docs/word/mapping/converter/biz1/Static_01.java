package com.mxr.docs.word.mapping.converter.biz1;

import com.mxr.utils.context.TtlContextHolderUtil;
import com.mxr.docs.word.mapping.AbstractStaticTemplate;
import com.mxr.docs.word.enums.EnumWordTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName Static_01
 * @description: 静态实例demo
 * @author YuanJie
 * @date 2025/8/11 15:18
 */
@Slf4j
@Component
public class Static_01 extends AbstractStaticTemplate {
    @Override
    public String getTemplateName() {
        return EnumWordTemplate.GG.getName();
    }
    @Override
    protected void placeholderMapping(ConcurrentHashMap<String, String> placeholderMap) {
        // 1.获取建设单位信息 和 获取项目基本信息
        /**
         * {@link GgGhxkController#input}
         */
        Object data = TtlContextHolderUtil.getContext().getProperty("data");
        log.info("接收到controller层数据: {}", data);
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            log.info("获取项目基本信息,threadName: {},threadId: {}", Thread.currentThread().getName(), Thread.currentThread().getId());

            placeholderMap.put("${JSDW}", Optional.ofNullable("建设单位").orElse(""));
            placeholderMap.put("${TYSHXYDM}", Optional.ofNullable("统一社会信用代码").orElse(""));
            placeholderMap.put("${LXR}", Optional.ofNullable("联系人").orElse(""));
            placeholderMap.put("${LXRDH}", Optional.ofNullable("联系人电话").orElse(""));
            placeholderMap.put("${DWDZ}", Optional.ofNullable("单位地址").orElse(""));
        });

        // 2.获取证书信息
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            log.info("获取证书信息,threadName: {},threadId: {}", Thread.currentThread().getName(), Thread.currentThread().getId());
            placeholderMap.put("${ZSBH}", Optional.ofNullable("证书编号").orElse(""));
            placeholderMap.put("${FZJG}", Optional.ofNullable("发证机关").orElse(""));
            placeholderMap.put("${FZRQ}", Optional.ofNullable("发证日期").orElse(""));
            placeholderMap.put("${ZSYXQ}", Optional.ofNullable("证书有效期").orElse(""));
            placeholderMap.put("${ZSFTFJ}", Optional.ofNullable("证书附件图").orElse(""));
        });
        String s = null;
        CompletableFuture.allOf(future1, future2).join();
        placeholderMap.put("${ZSYXQ}", Optional.ofNullable(s).orElse(""));

    }
}
