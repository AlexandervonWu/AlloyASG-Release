package is.fivefivefive.alloyasg.etc;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
/*
 * This class is used to store the mapping between two objects.
 * It is used to store the mapping between the node and its ID in the adjacency matrix.
 */
public class DoubleMap<K, V> {
    private Map<K, V> lmap;
    private Map<V, K> rmap;
    public DoubleMap() {
        lmap = new HashMap<K, V>();
        rmap = new HashMap<V, K>();
    }

    public DoubleMap(Map<K, V> lmap) {
        this.lmap = lmap;
        rmap = new HashMap<V, K>();
        for (K key : lmap.keySet()) {
            rmap.put(lmap.get(key), key);
        }
    }

    public List<K> keys() {
        return new ArrayList<K>(lmap.keySet());
    }

    public List<V> values() {
        return new ArrayList<V>(rmap.keySet());
    }

    public V get(K key) {
        return lmap.get(key);
    }

    public void put(K key, V value) {
        if (key == null || value == null) {
            return;
        }
        lmap.put(key, value);
        rmap.put(value, key);
    }

    public K rget(V value) {
        return rmap.get(value);
    }

    public void remove(K key) {
        V value = lmap.get(key);
        lmap.remove(key);
        rmap.remove(value);
    }

    public void rremove(V value) {
        K key = rmap.get(value);
        lmap.remove(key);
        rmap.remove(value);
    }

    public int size() {
        return lmap.size();
    }

    public boolean containsKey(K key) {
        return lmap.containsKey(key);
    }
    
    public boolean containsValue(V value) {
        return rmap.containsKey(value);
    }
}
