package com.mxr.docs.excel.entity;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @ExcelProperty("ID")
    private String id;

    @ExcelProperty("真实姓名")
    private String reallyName;

    @ExcelProperty("电话号码")
    private String phone;

    @ExcelProperty("身份证号码")
    private String identity;
}
