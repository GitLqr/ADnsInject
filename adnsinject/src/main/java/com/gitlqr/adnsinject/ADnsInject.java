package com.gitlqr.adnsinject;

import com.gitlqr.adnsinject.hook.AbsDnsHook;
import com.gitlqr.adnsinject.hook.LibCoreDnsHook;
import com.gitlqr.adnsinject.logger.Logger;

import java.util.Map;

/**
 * Android DNS 注入
 *
 * @author LQR
 * @since 2024/10/15
 */
public class ADnsInject {

    private static final class Holder {
        private static final ADnsInject INSTANCE = new ADnsInject();
    }

    public static ADnsInject get() {
        return Holder.INSTANCE;
    }

    private AbsDnsHook dnsHook;

    private ADnsInject() {
        // dnsHook = new AddressCacheDnsHook();
        dnsHook = new LibCoreDnsHook();
    }

    public void setLoggerEnable(boolean enable) {
        Logger.setEnable(enable);
    }

    public void start() {
        if (dnsHook != null) {
            dnsHook.hook();
        }
    }

    public void setHostMap(Map<String, String> map) {
        if (dnsHook != null) {
            dnsHook.setHostMap(map);
        }
    }

    public void stop() {
        if (dnsHook != null) {
            dnsHook.unhook();
        }
    }

}
