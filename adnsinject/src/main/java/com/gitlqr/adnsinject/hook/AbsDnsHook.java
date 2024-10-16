package com.gitlqr.adnsinject.hook;

import android.net.Uri;
import android.text.TextUtils;

import com.gitlqr.adnsinject.logger.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Dns Hook 基类
 *
 * @author LQR
 * @since 2024/10/16
 */
public abstract class AbsDnsHook {

    protected final Map<String, String> hostMap = new HashMap<>();

    public abstract void hook();

    public abstract void unhook();

    public void setHostMap(Map<String, String> map) {
        hostMap.clear();
        map = normalizeHostMap(map);
        if (map != null) {
            hostMap.putAll(map);
        }
    }

    protected Map<String, String> normalizeHostMap(Map<String, String> hostMap) {
        if (hostMap == null) return null;
        Map<String, String> temp = new HashMap<>();
        for (Map.Entry<String, String> entry : hostMap.entrySet()) {
            String key = entry.getKey(); // http://www.baidu.com、https://www.baidu.com、www.baidu.com ...
            String value = entry.getValue(); // 127.0.0.1
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value) || !isIp(value)) {
                Logger.d("'" + key + ":" + value + "' is invalid, ignore");
                continue;
            }
            String hostname = key.contains(":") ? Uri.parse(key).getHost() : key; // www.baidu.com
            temp.put(hostname, value);
        }
        return temp;
    }

    private boolean isIp(String str) {
        String regex = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
        return str.matches(regex);
    }
}
