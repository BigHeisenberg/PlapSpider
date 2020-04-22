package com.javatop.cn.spider.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.javatop.cn.spider.entity.Plap;
import com.javatop.cn.spider.utils.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * 部队采购网
 *
 * @Author: Heisenberg
 * @create: 2020-03-17 22:18
 */
@Slf4j
public class PlapCrawlService {

    //集中采购-物资
    private static String CollectiveProcurementUrl = "https://www.plap.cn/index/selectIndexNewsByParId.html?id=3";  //&page=2
    //部队采购-物资
    private static String ArmyProcurementUrl = "https://www.plap.cn/index/selectIndexNewsByParId.html?id=24";

    public static void main(String[] args) throws IOException, InterruptedException {
        getCollectiveProcurementData();
    }

    public static void getCollectiveProcurementData() throws IOException, InterruptedException {
        Document parentDocument = JsoupDocumentService.getDocumentByJsoup(ArmyProcurementUrl);
        //标题、页面链接、采购方式
        Elements titleEles = parentDocument.getElementsByClass("col-md-7 col-sm-6 col-xs-12");
        //产品类别
        Elements productEles = parentDocument.getElementsByClass("col-md-2 col-sm-3 col-xs-6");
        //发布时间
        Elements dateEles = parentDocument.getElementsByClass("col-md-3 col-sm-3 col-xs-6 tc p0");

        List<Plap> plapList = Lists.newArrayList();

        for (int i = 0; i < titleEles.size(); i++) {
            Element element = titleEles.get(i);
            String titleTmp = element.text();
            plapList.add(Plap.builder()
                    .pageUrl("https://www.plap.cn/" + element.select("a").attr("href"))
                    .purchaseMode(titleTmp.substring(titleTmp.indexOf("【") + 1, titleTmp.indexOf("】")))
                    .title(titleTmp.substring(titleTmp.indexOf("】") + 1))
                    .productType(productEles.get(i).text())
                    .createTime(dateEles.get(i).text())
                    .build());
        }
        for (Plap plap : plapList) {
            Document childDocument = JsoupDocumentService.getDocumentByJsoup(plap.getPageUrl());
            Elements photoEles = childDocument.getElementsByClass("clear margin-top-20 new_content");
            String photoUrl = "https://www.plap.cn" + photoEles.select("img").attr("src");

            String fileName = ("【") + plap.getPurchaseMode() + ("】") + plap.getTitle().replace(".", "");
            fileName = fileName.replaceAll("[\\u005C/:\\u002A\\u003F\"<>\'\\u007C’\":?]", "");
            String pathName = "D:" + File.separator + "spider" + File.separator + fileName;
            File myPath = new File(pathName);
            if (!myPath.exists()) {
                myPath.mkdir();
            }
            File file = new File(pathName + File.separator + fileName + ".jpg");
            URL httpurl = new URL(photoUrl);
            FileUtils.copyURLToFile(httpurl, file);
            Thread.sleep(3000);
            String businessId = childDocument.getElementById("artice_file_show_downBsId").attr("value");
            String typeId = childDocument.getElementById("artice_file_show_downBstypeId").attr("value");
            String result = HttpClientUtils.postResult("https://www.plap.cn/file/fileExist.do",
                    "businessId=" + businessId + "&typeId=" + typeId + "&key=2&zipFileName=null&fileName=null");
            if (StringUtils.isBlank(result)) {
                continue;
            }
            JSONObject resultObj = JSONObject.parseObject(result);
            String fileIds = resultObj.get("fileIds").toString();
            if (StringUtils.isBlank(fileIds)) {
                continue;
            }
            HttpClientUtils.okHttpDownLoad("https://www.plap.cn/file/download.html", fileIds, pathName);
            Thread.sleep(2000);
        }
    }

}
