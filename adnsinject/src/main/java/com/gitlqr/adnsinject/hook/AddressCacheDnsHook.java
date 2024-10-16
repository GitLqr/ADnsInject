package com.gitlqr.adnsinject.hook;

import android.os.Build;

import com.gitlqr.adnsinject.compat.AddressCacheCompat;
import com.gitlqr.adnsinject.logger.Logger;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

/**
 * 基于 AddressCache 的 Hook 注入
 * 问题：
 * 1、在高版本安卓中，会有 9.0 非公开 API 限制
 * 2、该方案是一次性的，当网络请求报错时，cache 会自动清除！
 * 3、AddressCache 默认最大缓存 16 个，当有大量域名存在时，之前注入的会被清除！
 *
 * @author LQR
 * @since 2024/10/15
 */
public class AddressCacheDnsHook extends AbsDnsHook {

    private Object addressCache;
    private AddressCacheCompat addressCacheCompat;

    public AddressCacheDnsHook() {
        init();
    }

    private void init() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 获取 impl 字段，去除 final 修饰
                Field implField = InetAddress.class.getDeclaredField("impl");
                implField.setAccessible(true);
                // removeFinalModifier(implField);
                // 获取 impl，其类型为 Inet6AddressImpl
                Object impl = implField.get(null);
                // 获取 addressCache 字段，去除 final 修饰
                Class<?> inet6AddressImplClass = Class.forName("java.net.Inet6AddressImpl");
                Field addressCacheField = inet6AddressImplClass.getDeclaredField("addressCache");
                addressCacheField.setAccessible(true);
                // removeFinalModifier(addressCacheField);
                // 获取 addressCache
                Object addressCache = addressCacheField.get(impl);
                this.addressCache = addressCache;
            } else {
                // 获取 addressCache 字段，去除 final 修饰
                Field addressCacheField = InetAddress.class.getDeclaredField("addressCache");
                addressCacheField.setAccessible(true);
                // removeFinalModifier(addressCacheField);
                // 获取 addressCache
                Object addressCache = addressCacheField.get(null);
                this.addressCache = addressCache;
            }
            if (addressCache != null) {
                this.addressCacheCompat = new AddressCacheCompat(addressCache);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hook() {
    }

    @Override
    public void unhook() {
        clear();
    }

    private void clear() {
        try {
            if (addressCacheCompat != null) {
                addressCacheCompat.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setHostMap(Map<String, String> map) {
        super.setHostMap(map);
        clear();
        if (addressCacheCompat != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String host = entry.getKey(); // www.baidu.com
                String ip = entry.getValue(); // 127.0.0.1
                try {
                    byte[] address = InetAddress.getByName(ip).getAddress(); // [127,0,0,1]
                    log("'" + host + ":" + ip + "' --> " + host + ":" + Arrays.toString(address));
                    InetAddress byAddress = InetAddress.getByAddress(host, address);
                    addressCacheCompat.put(host, new InetAddress[]{byAddress});
                } catch (Exception e) {
                    final String reason = e != null ? e.getClass().getName() + ": " + e.getMessage() : "unknown";
                    log("'" + host + ":" + ip + "' is invalid, reason : " + reason);
                    e.printStackTrace();
                }
            }
        }
    }

    private static void log(String msg) {
        Logger.d("【AddressCacheDnsHook】" + msg);
    }
}
