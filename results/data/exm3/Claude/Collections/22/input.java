// buggy function
    public void putAll(int index, final Map<? extends K, ? extends V> map) {
        for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            final V old = put(index, entry.getKey(), entry.getValue());
            if (old == null) {
            // The return value of put is null if the key did not exist OR the value was null
            // so it cannot be used to determine whether the key was added
                // if no key was replaced, increment the index
                index++;
            } else {
                // otherwise put the next item after the currently inserted key
                index = indexOf(entry.getKey()) + 1;
            }
        }
    }

// trigger testcase
// org/apache/commons/collections4/map/ListOrderedMapTest.java::testCOLLECTIONS_474_nullValues
public void testCOLLECTIONS_474_nullValues () {
        Object key1 = new Object();
        Object key2 = new Object();
        HashMap<Object, Object> hmap = new HashMap<Object, Object>();
        hmap.put(key1, null);
        hmap.put(key2, null);
        assertEquals("Should have two elements", 2, hmap.size());
        ListOrderedMap<Object, Object> listMap = new ListOrderedMap<Object, Object>();
        listMap.put(key1, null);
        listMap.put(key2, null);
        assertEquals("Should have two elements", 2, listMap.size());
        listMap.putAll(2, hmap);
    }
