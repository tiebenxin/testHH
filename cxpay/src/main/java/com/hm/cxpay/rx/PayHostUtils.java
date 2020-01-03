package com.hm.cxpay.rx;

/**
 * @author Liszt
 * @date 2019/11/27
 * Description
 */
public class PayHostUtils {
    //    private static final String PORT_8888 = ":8888";//http端口
    private static final String PORT_9898 = ":9898";//http端口
    //    private static final String HOST = "192.168.10.112";//http路径  测试服地址
    private static final String HOST_TEST = "192.168.10.229";//http路径
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String HOST = "test.zhixun6.com";//https 路径

    //外网正式服  https
    public static String getHttpsUrl() {
        return HTTPS + HOST + PORT_9898;
    }

}
