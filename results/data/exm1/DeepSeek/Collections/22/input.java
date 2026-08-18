// buggy code
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

// relevant test
// org.apache.commons.collections4.iterators.UnmodifiableOrderedMapIteratorTest::testOrderedMapIterator
    public void testOrderedMapIterator() {
        assertTrue(makeEmptyIterator() instanceof Unmodifiable);
    }

// org.apache.commons.collections4.iterators.UnmodifiableOrderedMapIteratorTest::testDecorateFactory
    public void testDecorateFactory() {
        OrderedMapIterator<K, V> it = makeObject();
        assertSame(it, UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it));

        it = getMap().mapIterator() ;
        assertTrue(it != UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(it));

        try {
            UnmodifiableOrderedMapIterator.unmodifiableOrderedMapIterator(null);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }

// org.apache.commons.collections4.map.ListOrderedMap2Test::testGetByIndex
    public void testGetByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.get(0);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.get(-1);
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.get(-1);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.get(lom.size());
        } catch (final IndexOutOfBoundsException ex) {}

        int i = 0;
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext(); i++) {
            assertSame(it.next(), lom.get(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMap2Test::testGetValueByIndex
    public void testGetValueByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.getValue(0);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.getValue(-1);
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.getValue(-1);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.getValue(lom.size());
        } catch (final IndexOutOfBoundsException ex) {}

        int i = 0;
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext(); i++) {
            it.next();
            assertSame(it.getValue(), lom.getValue(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMap2Test::testIndexOf
    public void testIndexOf() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        assertEquals(-1, lom.indexOf(getOtherKeys()));

        resetFull();
        lom = getMap();
        final List<K> list = new ArrayList<K>();
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext();) {
            list.add(it.next());
        }
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i, lom.indexOf(list.get(i)));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMap2Test::testRemoveByIndex
    public void testRemoveByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.remove(0);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.remove(-1);
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.remove(-1);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.remove(lom.size());
        } catch (final IndexOutOfBoundsException ex) {}

        final List<K> list = new ArrayList<K>();
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext();) {
            list.add(it.next());
        }
        for (int i = 0; i < list.size(); i++) {
            final Object key = list.get(i);
            final Object value = lom.get(key);
            assertEquals(value, lom.remove(i));
            list.remove(i);
            assertEquals(false, lom.containsKey(key));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testGetByIndex
    public void testGetByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.get(0);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.get(-1);
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.get(-1);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.get(lom.size());
        } catch (final IndexOutOfBoundsException ex) {}

        int i = 0;
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext(); i++) {
            assertSame(it.next(), lom.get(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testGetValueByIndex
    public void testGetValueByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.getValue(0);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.getValue(-1);
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.getValue(-1);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.getValue(lom.size());
        } catch (final IndexOutOfBoundsException ex) {}

        int i = 0;
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext(); i++) {
            it.next();
            assertSame(it.getValue(), lom.getValue(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testIndexOf
    public void testIndexOf() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        assertEquals(-1, lom.indexOf(getOtherKeys()));

        resetFull();
        lom = getMap();
        final List<K> list = new ArrayList<K>();
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext();) {
            list.add(it.next());
        }
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i, lom.indexOf(list.get(i)));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testSetValueByIndex
    public void testSetValueByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.setValue(0, (V) "");
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.setValue(-1, (V) "");
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.setValue(-1, (V) "");
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.setValue(lom.size(), (V) "");
        } catch (final IndexOutOfBoundsException ex) {}

        for (int i = 0; i < lom.size(); i++) {
            final V value = lom.getValue(i);
            final Object input = Integer.valueOf(i);
            assertEquals(value, lom.setValue(i, (V) input));
            assertEquals(input, lom.getValue(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testRemoveByIndex
    public void testRemoveByIndex() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();
        try {
            lom.remove(0);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.remove(-1);
        } catch (final IndexOutOfBoundsException ex) {}

        resetFull();
        lom = getMap();
        try {
            lom.remove(-1);
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.remove(lom.size());
        } catch (final IndexOutOfBoundsException ex) {}

        final List<K> list = new ArrayList<K>();
        for (final MapIterator<K, V> it = lom.mapIterator(); it.hasNext();) {
            list.add(it.next());
        }
        for (int i = 0; i < list.size(); i++) {
            final Object key = list.get(i);
            final Object value = lom.get(key);
            assertEquals(value, lom.remove(i));
            list.remove(i);
            assertEquals(false, lom.containsKey(key));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testPut_intObjectObject
    public void testPut_intObjectObject() {
        resetEmpty();
        ListOrderedMap<K, V> lom = getMap();

        try {
            lom.put(1, (K) "testInsert1", (V) "testInsert1v");
            fail("should not be able to insert at pos 1 in empty Map");
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            lom.put(-1, (K) "testInsert-1", (V) "testInsert-1v");
            fail("should not be able to insert at pos -1 in empty Map");
        } catch (final IndexOutOfBoundsException ex) {}

        
        lom.put(0, (K) "testInsert1", (V) "testInsert1v");
        assertEquals("testInsert1v", lom.getValue(0));

        lom.put((K) "testInsertPut", (V) "testInsertPutv");
        assertEquals("testInsert1v", lom.getValue(0));
        assertEquals("testInsertPutv", lom.getValue(1));

        lom.put(0, (K) "testInsert0", (V) "testInsert0v");
        assertEquals("testInsert0v", lom.getValue(0));
        assertEquals("testInsert1v", lom.getValue(1));
        assertEquals("testInsertPutv", lom.getValue(2));

        lom.put(3, (K) "testInsert3", (V) "testInsert3v");
        assertEquals("testInsert0v", lom.getValue(0));
        assertEquals("testInsert1v", lom.getValue(1));
        assertEquals("testInsertPutv", lom.getValue(2));
        assertEquals("testInsert3v", lom.getValue(3));

        
        resetFull();
        lom = getMap();
        final ListOrderedMap<K, V> lom2 = new ListOrderedMap<K, V>();
        lom2.putAll(lom);

        lom2.put(0, (K) "testInsert0", (V) "testInsert0v");
        assertEquals("testInsert0v", lom2.getValue(0));
        for (int i = 0; i < lom.size(); i++) {
            assertEquals(lom2.getValue(i + 1), lom.getValue(i));
        }

        
        final Integer i1 = Integer.valueOf(1);
        final Integer i1b = Integer.valueOf(1);
        final Integer i2 = Integer.valueOf(2);
        final Integer i3 = Integer.valueOf(3);

        resetEmpty();
        lom = getMap();
        lom.put((K) i1, (V) "1");
        lom.put((K) i2, (V) "2");
        lom.put((K) i3, (V) "3");
        lom.put(0, (K) i1, (V) "One");
        assertEquals(3, lom.size());
        assertEquals(3, lom.map.size());
        assertEquals(3, lom.keyList().size());
        assertEquals("One", lom.getValue(0));
        assertSame(i1, lom.get(0));

        resetEmpty();
        lom = getMap();
        lom.put((K) i1, (V) "1");
        lom.put((K) i2, (V) "2");
        lom.put((K) i3, (V) "3");
        lom.put(0, (K) i1b, (V) "One");
        assertEquals(3, lom.size());
        assertEquals(3, lom.map.size());
        assertEquals(3, lom.keyList().size());
        assertEquals("One", lom.getValue(0));
        assertEquals("2", lom.getValue(1));
        assertEquals("3", lom.getValue(2));
        assertSame(i1b, lom.get(0));

        resetEmpty();
        lom = getMap();
        lom.put((K) i1, (V) "1");
        lom.put((K) i2, (V) "2");
        lom.put((K) i3, (V) "3");
        lom.put(1, (K) i1b, (V) "One");
        assertEquals(3, lom.size());
        assertEquals(3, lom.map.size());
        assertEquals(3, lom.keyList().size());
        assertEquals("One", lom.getValue(0));
        assertEquals("2", lom.getValue(1));
        assertEquals("3", lom.getValue(2));

        resetEmpty();
        lom = getMap();
        lom.put((K) i1, (V) "1");
        lom.put((K) i2, (V) "2");
        lom.put((K) i3, (V) "3");
        lom.put(2, (K) i1b, (V) "One");
        assertEquals(3, lom.size());
        assertEquals(3, lom.map.size());
        assertEquals(3, lom.keyList().size());
        assertEquals("2", lom.getValue(0));
        assertEquals("One", lom.getValue(1));
        assertEquals("3", lom.getValue(2));

        resetEmpty();
        lom = getMap();
        lom.put((K) i1, (V) "1");
        lom.put((K) i2, (V) "2");
        lom.put((K) i3, (V) "3");
        lom.put(3, (K) i1b, (V) "One");
        assertEquals(3, lom.size());
        assertEquals(3, lom.map.size());
        assertEquals(3, lom.keyList().size());
        assertEquals("2", lom.getValue(0));
        assertEquals("3", lom.getValue(1));
        assertEquals("One", lom.getValue(2));
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testPutAllWithIndex
    public void testPutAllWithIndex() {
        resetEmpty();
        final ListOrderedMap<String, String> lom = (ListOrderedMap<String, String>) map;

        
        lom.put("testInsert0", "testInsert0v");
        lom.put("testInsert1", "testInsert1v");
        lom.put("testInsert2", "testInsert2v");
        assertEquals("testInsert0v", lom.getValue(0));
        assertEquals("testInsert1v", lom.getValue(1));
        assertEquals("testInsert2v", lom.getValue(2));

        
        final Map<String, String> values = new ListOrderedMap<String, String>();
        values.put("NewInsert0", "NewInsert0v");
        values.put("NewInsert1", "NewInsert1v");
        lom.putAll(1, values);

        
        assertEquals("testInsert0v", lom.getValue(0));
        assertEquals("NewInsert0v", lom.getValue(1));
        assertEquals("NewInsert1v", lom.getValue(2));
        assertEquals("testInsert1v", lom.getValue(3));
        assertEquals("testInsert2v", lom.getValue(4));
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testPutAllWithIndexBug441
    public void testPutAllWithIndexBug441() {
        
        resetEmpty();
        final ListOrderedMap<K, V> lom = getMap();

        final int size = 5;
        for (int i = 0; i < size; i++) {
            lom.put((K) Integer.valueOf(i), (V) Boolean.TRUE);
        }

        final Map<K, V> map = new TreeMap<K, V>();
        for (int i = 0; i < size; i++) {
            map.put((K) Integer.valueOf(i), (V) Boolean.FALSE);
        }

        lom.putAll(3, map);

        final List<K> orderedList = lom.asList();
        for (int i = 0; i < size; i++) {
            assertEquals(Integer.valueOf(i), orderedList.get(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testValueList_getByIndex
    public void testValueList_getByIndex() {
        resetFull();
        final ListOrderedMap<K, V> lom = getMap();
        for (int i = 0; i < lom.size(); i++) {
            final V expected = lom.getValue(i);
            assertEquals(expected, lom.valueList().get(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testValueList_setByIndex
    public void testValueList_setByIndex() {
        resetFull();
        final ListOrderedMap<K, V> lom = getMap();
        for (int i = 0; i < lom.size(); i++) {
            final Object input = Integer.valueOf(i);
            final V expected = lom.getValue(i);
            assertEquals(expected, lom.valueList().set(i, (V) input));
            assertEquals(input, lom.getValue(i));
            assertEquals(input, lom.valueList().get(i));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testValueList_removeByIndex
    public void testValueList_removeByIndex() {
        resetFull();
        final ListOrderedMap<K, V> lom = getMap();
        while (lom.size() > 1) {
            final V expected = lom.getValue(1);
            assertEquals(expected, lom.valueList().remove(1));
        }
    }

// org.apache.commons.collections4.map.ListOrderedMapTest::testCOLLECTIONS_474_nullValues
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

// org.apache.commons.collections4.map.ListOrderedMapTest::testCOLLECTIONS_474_nonNullValues
    public void testCOLLECTIONS_474_nonNullValues () {
        Object key1 = new Object();
        Object key2 = new Object();
        HashMap<Object, Object> hmap = new HashMap<Object, Object>();
        hmap.put(key1, "1");
        hmap.put(key2, "2");
        assertEquals("Should have two elements", 2, hmap.size());
        ListOrderedMap<Object, Object> listMap = new ListOrderedMap<Object, Object>();
        listMap.put(key1, "3");
        listMap.put(key2, "4");
        assertEquals("Should have two elements", 2, listMap.size());
        listMap.putAll(2, hmap);
    }

// org.apache.commons.collections4.map.SingletonMapTest::testClone
    public void testClone() {
        final SingletonMap<K, V> map = makeFullMap();
        assertEquals(1, map.size());
        final SingletonMap<K, V> cloned = map.clone();
        assertEquals(1, cloned.size());
        assertEquals(true, cloned.containsKey(ONE));
        assertEquals(true, cloned.containsValue(TWO));
    }

// org.apache.commons.collections4.map.SingletonMapTest::testKeyValue
    public void testKeyValue() {
        final SingletonMap<K, V> map = makeFullMap();
        assertEquals(1, map.size());
        assertEquals(ONE, map.getKey());
        assertEquals(TWO, map.getValue());
        assertTrue(map instanceof KeyValue);
    }

// org.apache.commons.collections4.map.SingletonMapTest::testBoundedMap
    public void testBoundedMap() {
        final SingletonMap<K, V> map = makeFullMap();
        assertEquals(1, map.size());
        assertEquals(true, map.isFull());
        assertEquals(1, map.maxSize());
        assertTrue(map instanceof BoundedMap);
    }

// org.apache.commons.collections4.map.UnmodifiableOrderedMapTest::testUnmodifiable
    public void testUnmodifiable() {
        assertTrue(makeObject() instanceof Unmodifiable);
        assertTrue(makeFullMap() instanceof Unmodifiable);
    }

// org.apache.commons.collections4.map.UnmodifiableOrderedMapTest::testDecorateFactory
    public void testDecorateFactory() {
        final OrderedMap<K, V> map = makeFullMap();
        assertSame(map, UnmodifiableOrderedMap.unmodifiableOrderedMap(map));

        try {
            UnmodifiableOrderedMap.unmodifiableOrderedMap(null);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }
