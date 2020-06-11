package com.wayue.tracer.core.reporter.stat.model;

import java.util.HashMap;
import java.util.Map;

/**
 * StatMapKey
 *
 * @author jinming.xiao
 * @since 2020/06/01
 */
public class StatMapKey extends StatKey {

    /**
     * Key for statistic
     */
    private Map<String, String> keyMap = new HashMap<String, String>();

    public Map<String, String> getKeyMap() {
        return keyMap;
    }

    public void addKey(String key, String value) {
        if (key != null && value != null) {
            keyMap.put(key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StatMapKey)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        StatMapKey that = (StatMapKey) o;

        return getKeyMap().equals(that.getKeyMap());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getKeyMap().hashCode();
        return result;
    }
}
