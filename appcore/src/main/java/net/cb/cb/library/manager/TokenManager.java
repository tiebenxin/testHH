package net.cb.cb.library.manager;

/**
 * @author Liszt
 * @date 2019/11/27
 * Description token 管理类
 */
public class TokenManager {
    private static String TOKEN;
    public static final String TOKEN_KEY = "X-Access-Token";

    public static void initToken(String token) {
        TOKEN = token;
    }

    public static String getToken() {
        return TOKEN;
    }

}
