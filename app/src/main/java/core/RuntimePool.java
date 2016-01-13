package core;

import android.support.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sphynx on 2016/1/13.
 */
public final class RuntimePool {
    private static ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();

    public static Object getValue(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return null;
    }

    public static void setValue(String key, @Nullable Object object) {
        if (map.containsKey(key)) {
            map.remove(key);
        }
        if (object != null) {
            map.put(key, object);
        }
    }

    public static <T> void setValue(Class<T> cls, T object) {
        setValue(cls.getName(), object);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Class<T> cls) {
        return (T)getValue(cls.getName());
    }
}
