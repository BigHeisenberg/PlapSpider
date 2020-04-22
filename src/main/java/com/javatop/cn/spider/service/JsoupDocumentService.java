package com.javatop.cn.spider.service;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.HttpHostConnectException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class JsoupDocumentService {

    private static final ThreadLocalRandom RANDOM =
            ThreadLocalRandom.current();


    private static SSLContext context;

    //生成万能认证证书
    static {
        try {
            // 重置HttpsURLConnection的DefaultHostnameVerifier，使其对任意站点进行验证时都返回true
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            // 创建随机证书生成工厂
            context = SSLContext.getInstance("TLSv1.2");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get 请求获取相关信息
     *
     * @param url
     * @return
     */
    public static Document getRandomDocumentByWebClient(String url) {
        Map<String, String> map = getRandomMap();
        Document document = null;
        //设置关闭HtmlUnit日志输出
        org.apache.log4j.Logger.getLogger("com.gargoylesoftware").setLevel(org.apache.log4j.Level.OFF);
        org.apache.log4j.Logger.getLogger("org.apache.http.client").setLevel(org.apache.log4j.Level.OFF);
        org.apache.log4j.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(org.apache.log4j.Level.OFF);
        ProxyConfig pc = new ProxyConfig();//新建代理配置
        pc.setSocksProxy(false); //如果是https类型，设置true，http则设置为false
        pc.setProxyHost(map.get("ip"));
        pc.setProxyPort(Integer.valueOf(map.get("port")));

        WebClient webClient = new WebClient(BrowserVersion.CHROME);// 创建HtmlUnit 模拟浏览器
        webClient.getOptions().setProxyConfig(pc); //设置代理
        webClient.getOptions().setJavaScriptEnabled(false);              // 启用JS解释器，默认为true
        webClient.getOptions().setCssEnabled(false);                    // 禁用css支持
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());//设置ajax
        webClient.setCssErrorHandler(new SilentCssErrorHandler());// 设置webClient的相关参数
        webClient.getOptions().setThrowExceptionOnScriptError(false);   // js运行错误时，是否抛出异常
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setTimeout(30 * 1000);                   // 设置连接超时时间
        webClient.addRequestHeader("User-Agent", map.get("userAgent"));
        webClient.waitForBackgroundJavaScript(30 * 1000);               // 等待js后台执行30秒
        String pageAsXml = null;
        try {
            HtmlPage page = webClient.getPage(url);
            pageAsXml = page.asXml();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (HttpHostConnectException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pageAsXml != null) {
            document = Jsoup.parseBodyFragment(pageAsXml);
            webClient.close();
        }
        return document;
    }

    /**
     * 使用Jsoup配置IP代理访问目标网站返回Document对象
     *
     * @param url
     * @return
     */
    public static Document getDocumentByJsoup(String url) {
        Map<String, String> map = getRandomMap();
        Connection connection = Jsoup.connect(url);
        Document document = null;
//        InetSocketAddress addr = new InetSocketAddress(map.get("ip"), Integer.parseInt(map.get("port")));
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr); // http proxy

        try {
            document = connection
                    .ignoreContentType(true)
                    .timeout(30000)
                    .sslSocketFactory(context.getSocketFactory())//设置访问验证通过HTTPS网站
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
//                    .proxy(proxy)
                    .userAgent(map.get("userAgent"))//设置请求头
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    /**
     * 生成随机请求头和代理IP信息
     *
     * @return
     */
    public static Map<String, String> getRandomMap() {
        Map<String, String> map = new HashMap<>();
        String[] ua = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0",
                "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586",
                "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1) Gecko/20070803 Firefox/1.5.0.12",
                "Opera/9.27 (Windows NT 5.2; U; zh-cn)",
                "Mozilla/5.0 (Macintosh; PPC Mac OS X; U; en) Opera 8.0",
                "Opera/8.0 (Macintosh; PPC Mac OS X; U; en)",
                "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.12) Gecko/20080219 Firefox/2.0.0.12 Navigator/9.0.0.6",
                "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
                "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0)",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; rv:11.0) like Gecko)",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2; .NET4.0C; .NET4.0E; QQBrowser/7.3.9825.400)",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
                "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2; .NET4.0C; .NET4.0E)",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 UBrowser/5.6.12150.8 Safari/537.36",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 TheWorld 7",
                "Mozilla/5.0 (Windows NT 6.1; W…) Gecko/20100101 Firefox/60.0"};
        map.put("userAgent", ua[ThreadLocalRandom.current().nextInt(ua.length)]);//设置随机请求头
        return map;
    }
}
