package shx.kimono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StateStore {
    private Map<String, Object> data = new ConcurrentHashMap<>();

    public Object get(String key) {
        return data.get(key);
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
