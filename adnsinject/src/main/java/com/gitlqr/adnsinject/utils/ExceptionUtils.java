package com.gitlqr.adnsinject.utils;

/**
 * 异常工具类
 *
 * @author LQR
 * @since 2024/10/15
 */
public class ExceptionUtils {

    private ExceptionUtils() {
    }

    public static String getReason(Throwable e) {
        if (e == null) {
            return "unknown";
        }
        return e.getClass().getName() + " : " + e.getMessage();
    }
}
