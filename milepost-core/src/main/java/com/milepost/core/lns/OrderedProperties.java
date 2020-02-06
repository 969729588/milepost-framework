package com.milepost.core.lns;

import java.util.*;

/**
 * Created by Ruifu Hua on 2020/2/6.
 */
public class OrderedProperties extends Properties{
    private final Map<Object, Object> linkedMap = new LinkedHashMap();

    public OrderedProperties() {
    }

    public Object get(Object key) {
        return this.linkedMap.get(key);
    }

    public Object put(Object key, Object value) {
        return this.linkedMap.put(key, value);
    }

    public Object remove(Object key) {
        return this.linkedMap.remove(key);
    }

    public void clear() {
        this.linkedMap.clear();
    }

    public Enumeration<Object> keys() {
        return Collections.enumeration(this.linkedMap.keySet());
    }

    public Enumeration<Object> elements() {
        return Collections.enumeration(this.linkedMap.values());
    }

    public Set<Map.Entry<Object, Object>> entrySet() {
        return this.linkedMap.entrySet();
    }

    public int size() {
        return this.linkedMap.size();
    }

    public String getProperty(String key) {
        return (String)this.linkedMap.get(key);
    }

    public synchronized boolean containsKey(Object key) {
        return this.linkedMap.containsKey(key);
    }

    public String toString() {
        return this.linkedMap.toString();
    }
}
