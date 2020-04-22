package com.javatop.cn.spider.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: Heisenberg
 * @create: 2020-03-17 22:12
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 军队采购网
 */
public class Plap {
    //标题
    private String title;
    //页面链接
    private String pageUrl;
    //采购方式
    private String purchaseMode;
    //产品类别
    private String productType;
    //发布时间
    private String createTime;
}
