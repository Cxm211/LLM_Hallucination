// buggy code
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

// relevant test
// org.apache.commons.collections.map.TestMultiValueMap::testNoMappingReturnsNull
    public void testNoMappingReturnsNull() {
        final MultiValueMap map = createTestMap();
        assertNull(map.get("whatever"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testValueCollectionType
    public void testValueCollectionType() {
        final MultiValueMap map = createTestMap(LinkedList.class);
        assertTrue(map.get("one") instanceof LinkedList);
    }

// org.apache.commons.collections.map.TestMultiValueMap::testMultipleValues
    public void testMultipleValues() {
        final MultiValueMap map = createTestMap(HashSet.class);
        final HashSet expected = new HashSet();
        expected.add("uno");
        expected.add("un");
        assertEquals(expected, map.get("one"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testContainsValue
    public void testContainsValue() {
        final MultiValueMap map = createTestMap(HashSet.class);
        assertTrue(map.containsValue("uno"));
        assertTrue(map.containsValue("un"));
        assertTrue(map.containsValue("dos"));
        assertTrue(map.containsValue("deux"));
        assertTrue(map.containsValue("tres"));
        assertTrue(map.containsValue("trois"));
        assertFalse(map.containsValue("quatro"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testKeyContainsValue
    public void testKeyContainsValue() {
        final MultiValueMap map = createTestMap(HashSet.class);
        assertTrue(map.containsValue("one", "uno"));
        assertTrue(map.containsValue("one", "un"));
        assertTrue(map.containsValue("two", "dos"));
        assertTrue(map.containsValue("two", "deux"));
        assertTrue(map.containsValue("three", "tres"));
        assertTrue(map.containsValue("three", "trois"));
        assertFalse(map.containsValue("four", "quatro"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testValues
    public void testValues() {
        final MultiValueMap map = createTestMap(HashSet.class);
        final HashSet expected = new HashSet();
        expected.add("uno");
        expected.add("dos");
        expected.add("tres");
        expected.add("un");
        expected.add("deux");
        expected.add("trois");
        final Collection c = map.values();
        assertEquals(6, c.size());
        assertEquals(expected, new HashSet(c));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testKeyedIterator
    public void testKeyedIterator() {
        final MultiValueMap map = createTestMap();
        final ArrayList actual = new ArrayList(IteratorUtils.toList(map.iterator("one")));
        final ArrayList expected = new ArrayList(Arrays.asList(new String[]{"uno", "un"}));
        assertEquals(expected, actual);
    }

// org.apache.commons.collections.map.TestMultiValueMap::testRemoveAllViaIterator
    public void testRemoveAllViaIterator() {
        final MultiValueMap map = createTestMap();
        for(Iterator i = map.values().iterator(); i.hasNext();) {
            i.next();
            i.remove();
        }
        assertNull(map.get("one"));
        assertTrue(map.isEmpty());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testRemoveAllViaKeyedIterator
    public void testRemoveAllViaKeyedIterator() {
        final MultiValueMap map = createTestMap();
        for(Iterator i = map.iterator("one"); i.hasNext();) {
            i.next();
            i.remove();
        }
        assertNull(map.get("one"));
        assertEquals(4, map.totalSize());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testTotalSizeA
    public void testTotalSizeA() {
        assertEquals(6, createTestMap().totalSize());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testMapEquals
    public void testMapEquals() {
        MultiValueMap one = new MultiValueMap();
        Integer value = new Integer(1);
        one.put("One", value);
        one.removeMapping("One", value);
        
        MultiValueMap two = new MultiValueMap();
        assertEquals(two, one);
    }

// org.apache.commons.collections.map.TestMultiValueMap::testGetCollection
    public void testGetCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("A", "AA");
        assertSame(map.get("A"), map.getCollection("A"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testTotalSize
    public void testTotalSize() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.totalSize());
        map.put("A", "AA");
        assertEquals(1, map.totalSize());
        map.put("B", "BA");
        assertEquals(2, map.totalSize());
        map.put("B", "BB");
        assertEquals(3, map.totalSize());
        map.put("B", "BC");
        assertEquals(4, map.totalSize());
        map.remove("A");
        assertEquals(3, map.totalSize());
        map.removeMapping("B", "BC");
        assertEquals(2, map.totalSize());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testSize
    public void testSize() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.size());
        map.put("A", "AA");
        assertEquals(1, map.size());
        map.put("B", "BA");
        assertEquals(2, map.size());
        map.put("B", "BB");
        assertEquals(2, map.size());
        map.put("B", "BC");
        assertEquals(2, map.size());
        map.remove("A");
        assertEquals(1, map.size());
        map.removeMapping("B", "BC");
        assertEquals(1, map.size());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testSize_Key
    public void testSize_Key() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.size("A"));
        assertEquals(0, map.size("B"));
        map.put("A", "AA");
        assertEquals(1, map.size("A"));
        assertEquals(0, map.size("B"));
        map.put("B", "BA");
        assertEquals(1, map.size("A"));
        assertEquals(1, map.size("B"));
        map.put("B", "BB");
        assertEquals(1, map.size("A"));
        assertEquals(2, map.size("B"));
        map.put("B", "BC");
        assertEquals(1, map.size("A"));
        assertEquals(3, map.size("B"));
        map.remove("A");
        assertEquals(0, map.size("A"));
        assertEquals(3, map.size("B"));
        map.removeMapping("B", "BC");
        assertEquals(0, map.size("A"));
        assertEquals(2, map.size("B"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testIterator_Key
    public void testIterator_Key() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(false, map.iterator("A").hasNext());
        map.put("A", "AA");
        Iterator it = map.iterator("A");
        assertEquals(true, it.hasNext());
        it.next();
        assertEquals(false, it.hasNext());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testContainsValue_Key
    public void testContainsValue_Key() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(false, map.containsValue("A", "AA"));
        assertEquals(false, map.containsValue("B", "BB"));
        map.put("A", "AA");
        assertEquals(true, map.containsValue("A", "AA"));
        assertEquals(false, map.containsValue("A", "AB"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testPutWithList
    public void testPutWithList() {
        MultiValueMap test = MultiValueMap.decorate(new HashMap(), ArrayList.class);
        assertEquals("a", test.put("A", "a"));
        assertEquals("b", test.put("A", "b"));
        assertEquals(1, test.size());
        assertEquals(2, test.size("A"));
        assertEquals(2, test.totalSize());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testPutWithSet
    public void testPutWithSet() {
        MultiValueMap test = MultiValueMap.decorate(new HashMap(), HashSet.class);
        assertEquals("a", test.put("A", "a"));
        assertEquals("b", test.put("A", "b"));
        assertEquals(null, test.put("A", "a"));
        assertEquals(1, test.size());
        assertEquals(2, test.size("A"));
        assertEquals(2, test.totalSize());
    }

// org.apache.commons.collections.map.TestMultiValueMap::testPutAll_Map1
    public void testPutAll_Map1() {
        MultiMap original = new MultiValueMap();
        original.put("key", "object1");
        original.put("key", "object2");

        MultiValueMap test = new MultiValueMap();
        test.put("keyA", "objectA");
        test.put("key", "object0");
        test.putAll(original);

        assertEquals(2, test.size());
        assertEquals(4, test.totalSize());
        assertEquals(1, test.getCollection("keyA").size());
        assertEquals(3, test.getCollection("key").size());
        assertEquals(true, test.containsValue("objectA"));
        assertEquals(true, test.containsValue("object0"));
        assertEquals(true, test.containsValue("object1"));
        assertEquals(true, test.containsValue("object2"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testPutAll_Map2
    public void testPutAll_Map2() {
        Map original = new HashMap();
        original.put("keyX", "object1");
        original.put("keyY", "object2");

        MultiValueMap test = new MultiValueMap();
        test.put("keyA", "objectA");
        test.put("keyX", "object0");
        test.putAll(original);

        assertEquals(3, test.size());
        assertEquals(4, test.totalSize());
        assertEquals(1, test.getCollection("keyA").size());
        assertEquals(2, test.getCollection("keyX").size());
        assertEquals(1, test.getCollection("keyY").size());
        assertEquals(true, test.containsValue("objectA"));
        assertEquals(true, test.containsValue("object0"));
        assertEquals(true, test.containsValue("object1"));
        assertEquals(true, test.containsValue("object2"));
    }

// org.apache.commons.collections.map.TestMultiValueMap::testPutAll_KeyCollection
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

// org.apache.commons.collections.map.TestMultiValueMap::testRemove_KeyItem
    public void testRemove_KeyItem() {
        MultiValueMap map = new MultiValueMap();
        map.put("A", "AA");
        map.put("A", "AB");
        map.put("A", "AC");
        assertEquals(null, map.removeMapping("C", "CA"));
        assertEquals(null, map.removeMapping("A", "AD"));
        assertEquals("AC", map.removeMapping("A", "AC"));
        assertEquals("AB", map.removeMapping("A", "AB"));
        assertEquals("AA", map.removeMapping("A", "AA"));
        assertEquals(new MultiValueMap(), map);
    }
