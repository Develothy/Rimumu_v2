package gg.rimumu.cache;

import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private static final Map<String, Object> cacheMap = new HashMap<>();

    public static Object get(String key) {
        return cacheMap.get(key);
    }

    public static void put(String key, Object value) {
        cacheMap.put(key, value);
    }

    public static void remove(String key) {
        cacheMap.remove(key);
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 0시 0분 0초
    public static void removeAll() {
        cacheMap.clear();
    }

}
