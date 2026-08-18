// buggy function
    public Object put(Object key, Object value) {
        boolean result = false;
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(1);  // might produce a non-empty collection
            result = coll.add(value);
            if (coll.size() > 0) {
                // only add if non-zero size to maintain class state
                getMap().put(key, coll);
                result = false;
            }
        } else {
            result = coll.add(value);
        }
        return (result ? value : null);
    }

    public boolean putAll(Object key, Collection values) {
        if (values == null || values.size() == 0) {
            return false;
        }
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(values.size());  // might produce a non-empty collection
            boolean result = coll.addAll(values);
            if (coll.size() > 0) {
                // only add if non-zero size to maintain class state
                getMap().put(key, coll);
                result = false;
            }
            return result;
        } else {
            return coll.addAll(values);
        }
    }

// trigger testcase
// org/apache/commons/collections/map/TestMultiValueMap.java::testPutAll_KeyCollection
public void testPutAll_KeyCollection() {
        MultiValueMap map = new MultiValueMap();
        Collection coll = Arrays.asList(new Object[] {"X", "Y", "Z"});
        
        assertEquals(true, map.putAll("A", coll));
        assertEquals(3, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));
        
        assertEquals(false, map.putAll("A", null));
        assertEquals(3, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));
        
        assertEquals(false, map.putAll("A", new ArrayList()));
        assertEquals(3, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));
        
        coll = Arrays.asList(new Object[] {"M"});
        assertEquals(true, map.putAll("A", coll));
        assertEquals(4, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));
        assertEquals(true, map.containsValue("A", "M"));
    }

// org/apache/commons/collections/map/TestMultiValueMap.java::testPutWithList
public void testPutWithList() {
        MultiValueMap test = MultiValueMap.decorate(new HashMap(), ArrayList.class);
        assertEquals("a", test.put("A", "a"));
        assertEquals("b", test.put("A", "b"));
        assertEquals(1, test.size());
        assertEquals(2, test.size("A"));
        assertEquals(2, test.totalSize());
    }

// org/apache/commons/collections/map/TestMultiValueMap.java::testPutWithSet
public void testPutWithSet() {
        MultiValueMap test = MultiValueMap.decorate(new HashMap(), HashSet.class);
        assertEquals("a", test.put("A", "a"));
        assertEquals("b", test.put("A", "b"));
        assertEquals(null, test.put("A", "a"));
        assertEquals(1, test.size());
        assertEquals(2, test.size("A"));
        assertEquals(2, test.totalSize());
    }
