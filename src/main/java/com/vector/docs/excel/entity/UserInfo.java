package com.vector.docs.excel.entity;

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

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("真实姓名")
    private String reallyName;

    @ExcelProperty("电话号码")
    private String phone;

    @ExcelProperty("身份证号码")
    private String IdCard;

    @ExcelProperty("角色")
    private String roles;
}
