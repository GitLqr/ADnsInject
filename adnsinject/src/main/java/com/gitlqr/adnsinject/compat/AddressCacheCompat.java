package com.gitlqr.adnsinject.compat;

import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;

/**
 * AddressCache 兼容类
 * 注：不同的安卓版本，方法签名可能不同，需要单独适配兼容
 * https://cs.android.com/android/platform/superproject/+/android-4.1.1_r1.1:libcore/luni/src/main/java/java/net/AddressCache.java
 * https://cs.android.com/android/platform/superproject/+/android-7.1.2_r4:libcore/luni/src/main/java/java/net/AddressCache.java
 *
 * @author LQR
 * @since 2024/10/15
 */
public class AddressCacheCompat {

    private static final int NETID_UNSET = 0;

    private final Object obj;
    private final Class<?> addressCacheClass;
    private Method clearMethod;
    private Method getMethod;
    private Method putMethod;
    private Method putUnknownHostMethod;

    public AddressCacheCompat(Object addressCache) throws ClassNotFoundException {
        this.obj = addressCache;
        this.addressCacheClass = Class.forName("java.net.AddressCache");
    }

    public void clear() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (clearMethod == null) {
            clearMethod = this.addressCacheClass.getDeclaredMethod("clear");
        }
        clearMethod.invoke(obj);
    }

    /**
     * Android 5+：public Object get(String hostname, int netId)
     * Android 4.x：public Object get(String hostname)
     */
    public Object get(String hostname) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (getMethod == null) {
                getMethod = this.addressCacheClass.getDeclaredMethod("get", String.class, int.class);
            }
            return getMethod.invoke(obj, hostname, NETID_UNSET);
        } else {
            if (getMethod == null) {
                getMethod = this.addressCacheClass.getDeclaredMethod("get", String.class);
            }
            return getMethod.invoke(obj, hostname);
        }
    }

    /**
     * Android 5+：public void put(String hostname, int netId, InetAddress[] addresses)
     * Android 4.x：public void put(String hostname, InetAddress[] addresses)
     */
    public void put(String hostname, InetAddress[] addresses) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (putMethod == null) {
                putMethod = this.addressCacheClass.getDeclaredMethod("put", String.class, int.class, InetAddress[].class);
            }
            putMethod.invoke(obj, hostname, NETID_UNSET, addresses);
        } else {
            if (putMethod == null) {
                putMethod = this.addressCacheClass.getDeclaredMethod("put", String.class, InetAddress[].class);
            }
            putMethod.invoke(obj, hostname, addresses);
        }
    }

    /**
     * Android 5+：public void putUnknownHost(String hostname, int netId, String detailMessage)
     * Android 4.x：public void putUnknownHost(String hostname, String detailMessage)
     */
    public void putUnknownHost(String hostname, String detailMessage) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (putUnknownHostMethod == null) {
                putUnknownHostMethod = this.addressCacheClass.getDeclaredMethod("putUnknownHost", String.class, int.class, String.class);
            }
            putUnknownHostMethod.invoke(obj, hostname, NETID_UNSET, detailMessage);
        } else {
            if (putUnknownHostMethod == null) {
                putUnknownHostMethod = this.addressCacheClass.getDeclaredMethod("putUnknownHost", String.class, String.class);
            }
            putUnknownHostMethod.invoke(obj, hostname, detailMessage);
        }
    }
}
