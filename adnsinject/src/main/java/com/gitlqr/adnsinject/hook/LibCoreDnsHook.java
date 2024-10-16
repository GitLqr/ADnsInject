package com.gitlqr.adnsinject.hook;

import com.gitlqr.adnsinject.logger.Logger;
import com.gitlqr.adnsinject.utils.ExceptionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.Map;

/**
 * 基于 LibCore 的 Hook 注入
 * <p>
 * https://cs.android.com/android/platform/superproject/+/android-4.4w_r1:libcore/luni/src/main/java/java/net/InetAddress.java
 * https://cs.android.com/android/platform/superproject/+/android-4.4w_r1:libcore/luni/src/main/java/libcore/io/Libcore.java
 * https://cs.android.com/android/platform/superproject/+/android-4.4w_r1:libcore/luni/src/main/java/libcore/io/Os.java
 * <p>
 * https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:libcore/ojluni/src/main/java/java/net/InetAddress.java
 * https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:libcore/ojluni/src/main/java/java/net/Inet6AddressImpl.java
 * https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:libcore/luni/src/main/java/libcore/io/Libcore.java
 * https://cs.android.com/android/platform/superproject/+/android14-qpr3-release:libcore/luni/src/main/java/libcore/io/Os.java
 *
 * @author LQR
 * @since 2024/10/15
 */
public class LibCoreDnsHook extends AbsDnsHook {

    private Class<?> LibcoreClass;
    private Class<?> OsClass;
    private Field osField;
    private Object oriOs;

    public LibCoreDnsHook() {
        init();
    }

    private void init() {
        try {
            LibcoreClass = Class.forName("libcore.io.Libcore");
            OsClass = Class.forName("libcore.io.Os");
            osField = LibcoreClass.getDeclaredField("os");
            osField.setAccessible(true);
            oriOs = osField.get(null);
            log("init success");
        } catch (Exception e) {
            log("init fail");
            e.printStackTrace();
        }
    }

    @Override
    public void hook() {
        if (oriOs == null) {
            log("hook fail, oriOs is null");
            return;
        }
        try {
            Object proxy = Proxy.newProxyInstance(
                    LibcoreClass.getClassLoader(),
                    new Class[]{OsClass},
                    new OsInvokeHandler(oriOs, hostMap));
            osField.set(null, proxy);
            log("hook success");
        } catch (Exception e) {
            log("hook fail, reason : " + ExceptionUtils.getReason(e));
            e.printStackTrace();
        }
    }

    @Override
    public void unhook() {
        if (oriOs == null) {
            log("unHook fail, oriOs is null");
            return;
        }
        try {
            osField.set(null, oriOs);
            log("hook success");
        } catch (IllegalAccessException e) {
            log("unHook fail, reason : " + ExceptionUtils.getReason(e));
            e.printStackTrace();
        }
    }

    private static class OsInvokeHandler implements InvocationHandler {

        private final Object oriOs;
        private final Map<String, String> hostMap;
        private Field aiFlagsField;

        public OsInvokeHandler(Object oriOs, Map<String, String> hostMap) {
            this.oriOs = oriOs;
            this.hostMap = hostMap;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            switch (methodName) {
                case "android_getaddrinfo":
                case "getaddrinfo":
                    // Android 5+：
                    // Inet6AddressImpl.lookupHostByName(String host, int netId) {
                    //      ...
                    //      InetAddress[] addresses = Libcore.os.android_getaddrinfo(host, hints, netId);
                    //      ...
                    // }
                    // public interface Os {
                    //      ...
                    //      public InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException;
                    //      ...
                    // }
                    // Android 4.x：
                    // InetAddress.lookupHostByName(String host) {
                    //      ...
                    //      InetAddress[] addresses = Libcore.os.getaddrinfo(host, hints);
                    //      ...
                    // }
                    // public interface Os {
                    //      ...
                    //      public InetAddress[] getaddrinfo(String node, StructAddrinfo hints) throws GaiException;
                    //      ...
                    // }
                    Object host = args[0];
                    Object hints = args[1];
                    if (host instanceof String && isValidAddress(hints) && hostMap.containsKey(host)) {
                        String ip = hostMap.get(host);
                        // byte[] address = InetAddress.getByName(ip).getAddress(); // [127,0,0,1]
                        // InetAddress byAddress = InetAddress.getByAddress((String) host, address);
                        // return new InetAddress[]{byAddress};
                        return InetAddress.getAllByName(ip);
                    }
                    break;
            }

            try {
                return method.invoke(oriOs, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private boolean isValidAddress(Object hints) {
            try {
                if (aiFlagsField == null) {
                    if (hints != null) {
                        aiFlagsField = hints.getClass().getDeclaredField("ai_flags");
                        aiFlagsField.setAccessible(true);
                    }
                }
            } catch (Exception e) {
                log("ai_flags get error, reason : " + ExceptionUtils.getReason(e));
                e.printStackTrace();
            }
            try {
                // Android 4.x 没有 OsConstants.AI_NUMERICHOST 这个常量，直接使用会报错：java.lang.NoClassDefFoundError: android.system.OsConstants
                final int AI_NUMERICHOST = 4; // OsConstants.AI_NUMERICHOST
                final Object aiFlags = aiFlagsField != null ? aiFlagsField.get(hints) : null;

                // Android 4.x 源码中，getaddrinfo() 会调用两次：
                // 第一次：是在调用链 getAllByName() -> getAllByNameImpl() -> parseNumericAddressNoThrow() -> disallowDeprecatedFormats() 过程中，
                // parseNumericAddressNoThrow() 会对 hints.ai_flags = AI_NUMERICHOST; 然后执行一次 Libcore.os.getaddrinfo(address, hints);
                // 这时我们不能对其进行篡改，否则会过不了后续的 disallowDeprecatedFormats() 校验，导致报错 "Deprecated IPv4 address format: "。
                // 第二次：是在调用链 getAllByName() -> getAllByNameImpl() -> lookupHostByName() 过程中，
                // lookupHostByName() 会对 hints.ai_flags = AI_ADDRCONFIG; 然后执行一次 Libcore.os.getaddrinfo(host, hints);
                // 这时我们就可以对其进行拦截，判断是否要篡改了。
                //
                // Android 5+ 源码中，android_getaddrinfo() 同理 也会调用两次。

                return aiFlags instanceof Integer && (int) aiFlags != AI_NUMERICHOST;
            } catch (Exception e) {
                log("isValidAddress error, reason : " + ExceptionUtils.getReason(e));
                e.printStackTrace();
                return false;
            }
        }
    }

    private static void log(String msg) {
        Logger.d("【LibCoreDnsHook】" + msg);
    }
}
