package com.vector.config;


import com.aspose.pdf.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * @author YuanJie
 * @ClassName AuthLincense
 * @description: Aspose PDF 授权许可配置类，用于加载和设置 Aspose PDF 组件的许可证文件
 * @date 2025/2/27 16:39
 */
@Slf4j
public class WordAuthLicense {

    /**
     * 设置 Aspose PDF 组件的授权许可
     * 从 classpath 路径下加载 License.xml 文件并设置授权
     */
    public static void setAuthLicense() {
        try {
            //根目录下的授权文件
            ClassPathResource resource = new ClassPathResource("License.xml");
            URL licenseFileNameURL = resource.getURL();
            String savePath = java.net.URLDecoder.decode(licenseFileNameURL.toString(), StandardCharsets.UTF_8);
            String licenseFileName = savePath.substring(6);
            if (new File(licenseFileName).exists()) {
                License license = new License();
                license.setLicense(licenseFileName);
            }
        } catch (Exception e) {
            log.error("aspose 授权异常");
        }
    }
}
