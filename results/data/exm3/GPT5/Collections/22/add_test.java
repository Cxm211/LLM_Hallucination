// org/apache/commons/collections4/map/ListOrderedMapTest.java::testPutAll_existingNullThenNewAtEnd
public void testPutAll_existingNullThenNewAtEnd() {
        Object a = "a";
        Object b = "b";
        Object c = "c";

        ListOrderedMap<Object, Object> listMap = new ListOrderedMap<Object, Object>();
        listMap.put(a, null);
        listMap.put(b, 1);
        assertEquals(2, listMap.size());

        java.util.LinkedHashMap<Object, Object> lmap = new java.util.LinkedHashMap<Object, Object>();
        lmap.put(a, null); // existing key with null value
        lmap.put(c, 3);    // new key

        listMap.putAll(2, lmap); // start at end

        assertEquals(3, listMap.size());
        assertEquals(java.util.Arrays.asList(b, a, c), new java.util.ArrayList<Object>(listMap.keyList()));
    }