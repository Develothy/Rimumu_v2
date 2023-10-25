package gg.rimumu.cache;

import com.google.gson.JsonObject;
import gg.rimumu.common.key.ChampionKey;
import gg.rimumu.exception.RimumuException;
import gg.rimumu.common.util.FileUtil;
import org.springframework.stereotype.Component;

@Component
public class CacheService {

    private static CacheManager cacheManager;

    private static final String PREFIX_ITEM = "ITEM_";
    private static final String PREFIX_CHAMPION = "CHAMPION_";
    private static final String CACHE_KEY_ITEM_ALL = "ITEM_ALL";

    public static JsonObject getItem(int itemNum) throws RimumuException {
        String key = String.valueOf(itemNum);
        JsonObject itemCached = (JsonObject) cacheManager.get(key);

        if (itemCached != null) {
            return itemCached;
        }

        JsonObject itemAllCached = getItemAllData();
        JsonObject itemData = itemAllCached.getAsJsonObject("data");
        JsonObject itemResult = itemData.getAsJsonObject(key);
        itemCached = itemResult;
        cacheManager.put(PREFIX_ITEM, key, itemCached);

        return itemCached;
    }

    public static JsonObject getItemAllData() throws RimumuException {

        JsonObject itemAllCached = (JsonObject) cacheManager.get(CACHE_KEY_ITEM_ALL);

        if (itemAllCached != null) {
            return itemAllCached;
        }

        itemAllCached = FileUtil.readJsonFile("/datadragon/item.json");
        cacheManager.put(PREFIX_ITEM, CACHE_KEY_ITEM_ALL, itemAllCached);

        return itemAllCached;
    }

    public static String getChampionName(int champNum) {
        String key = "K" + champNum;

        String champName = (String) cacheManager.get(key);

        if (champName != null) {
            return  champName;
        }

        champName = ChampionKey.valueOf("K" + champName).getLabel();
        cacheManager.put(PREFIX_CHAMPION, key, champName);
        return champName;
    }

}
