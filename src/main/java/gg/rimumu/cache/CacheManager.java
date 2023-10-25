package gg.rimumu.cache;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private static final Map<String, Object> ITEM_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Object> CHAMPION_CACHE = new ConcurrentHashMap<>();

    public static Map<String, Object> findCacheMap(String prefix) {
        switch (prefix) {
            case "ITEM_" : return ITEM_CACHE;
            case "CHAMPION_" : return CHAMPION_CACHE;
        }
        return null;
    }

    public static Object get(String prefix) {
        return findCacheMap(prefix);
    }

    public static void put(String prefix, String key, Object value) {
        findCacheMap(prefix).put(key, value);
    }

    public static void remove(String prefix, String key) {
        findCacheMap(prefix).remove(key);
    }

    public static void removeAll(String prefix) {
        findCacheMap(prefix).clear();
    }

}
