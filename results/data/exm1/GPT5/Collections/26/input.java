// buggy code
    private Object readResolve() {
        calculateHashCode(keys);
        return this;
    }

// relevant test
// org.apache.commons.collections4.keyvalue.MultiKeyTest::testConstructors
    public void testConstructors() throws Exception {
        MultiKey<Integer> mk;
        mk = new MultiKey<Integer>(ONE, TWO);
        assertTrue(Arrays.equals(new Object[] { ONE, TWO }, mk.getKeys()));

        mk = new MultiKey<Integer>(ONE, TWO, THREE);
        assertTrue(Arrays.equals(new Object[] { ONE, TWO, THREE }, mk.getKeys()));

        mk = new MultiKey<Integer>(ONE, TWO, THREE, FOUR);
        assertTrue(Arrays.equals(new Object[] { ONE, TWO, THREE, FOUR }, mk.getKeys()));

        mk = new MultiKey<Integer>(ONE, TWO, THREE, FOUR, FIVE);
        assertTrue(Arrays.equals(new Object[] { ONE, TWO, THREE, FOUR, FIVE }, mk.getKeys()));

        mk = new MultiKey<Integer>(new Integer[] { THREE, FOUR, ONE, TWO }, false);
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, TWO }, mk.getKeys()));
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testConstructorsByArray
    public void testConstructorsByArray() throws Exception {
        MultiKey<Integer> mk;
        Integer[] keys = new Integer[] { THREE, FOUR, ONE, TWO };
        mk = new MultiKey<Integer>(keys);
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, TWO }, mk.getKeys()));
        keys[3] = FIVE;  
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, TWO }, mk.getKeys()));

        keys = new Integer[] {};
        mk = new MultiKey<Integer>(keys);
        assertTrue(Arrays.equals(new Object[] {}, mk.getKeys()));

        keys = new Integer[] { THREE, FOUR, ONE, TWO };
        mk = new MultiKey<Integer>(keys, true);
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, TWO }, mk.getKeys()));
        keys[3] = FIVE;  
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, TWO }, mk.getKeys()));

        keys = new Integer[] { THREE, FOUR, ONE, TWO };
        mk = new MultiKey<Integer>(keys, false);
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, TWO }, mk.getKeys()));
        
        
        keys[3] = FIVE;
        assertTrue(Arrays.equals(new Object[] { THREE, FOUR, ONE, FIVE }, mk.getKeys()));
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testConstructorsByArrayNull
    public void testConstructorsByArrayNull() throws Exception {
        final Integer[] keys = null;
        try {
            new MultiKey<Integer>(keys);
            fail();
        } catch (final IllegalArgumentException ex) {}
        try {
            new MultiKey<Integer>(keys, true);
            fail();
        } catch (final IllegalArgumentException ex) {}
        try {
            new MultiKey<Integer>(keys, false);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testSize
    public void testSize() {
        assertEquals(2, new MultiKey<Integer>(ONE, TWO).size());
        assertEquals(2, new MultiKey<Object>(null, null).size());
        assertEquals(3, new MultiKey<Integer>(ONE, TWO, THREE).size());
        assertEquals(3, new MultiKey<Object>(null, null, null).size());
        assertEquals(4, new MultiKey<Integer>(ONE, TWO, THREE, FOUR).size());
        assertEquals(4, new MultiKey<Object>(null, null, null, null).size());
        assertEquals(5, new MultiKey<Integer>(ONE, TWO, THREE, FOUR, FIVE).size());
        assertEquals(5, new MultiKey<Object>(null, null, null, null, null).size());

        assertEquals(0, new MultiKey<Object>(new Object[] {}).size());
        assertEquals(1, new MultiKey<Integer>(new Integer[] { ONE }).size());
        assertEquals(2, new MultiKey<Integer>(new Integer[] { ONE, TWO }).size());
        assertEquals(7, new MultiKey<Integer>(new Integer[] { ONE, TWO, ONE, TWO, ONE, TWO, ONE }).size());
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testGetIndexed
    public void testGetIndexed() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO);
        assertSame(ONE, mk.getKey(0));
        assertSame(TWO, mk.getKey(1));
        try {
            mk.getKey(-1);
            fail();
        } catch (final IndexOutOfBoundsException ex) {}
        try {
            mk.getKey(2);
            fail();
        } catch (final IndexOutOfBoundsException ex) {}
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testGetKeysSimpleConstructor
    public void testGetKeysSimpleConstructor() {
        final MultiKey<Integer> mk = new MultiKey<Integer>(ONE, TWO);
        final Object[] array = mk.getKeys();
        assertSame(ONE, array[0]);
        assertSame(TWO, array[1]);
        assertEquals(2, array.length);
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testGetKeysArrayConstructorCloned
    public void testGetKeysArrayConstructorCloned() {
        final Integer[] keys = new Integer[] { ONE, TWO };
        final MultiKey<Integer> mk = new MultiKey<Integer>(keys, true);
        final Object[] array = mk.getKeys();
        assertTrue(array != keys);
        assertTrue(Arrays.equals(array, keys));
        assertSame(ONE, array[0]);
        assertSame(TWO, array[1]);
        assertEquals(2, array.length);
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testGetKeysArrayConstructorNonCloned
    public void testGetKeysArrayConstructorNonCloned() {
        final Integer[] keys = new Integer[] { ONE, TWO };
        final MultiKey<Integer> mk = new MultiKey<Integer>(keys, false);
        final Object[] array = mk.getKeys();
        assertTrue(array != keys);  
        assertTrue(Arrays.equals(array, keys));
        assertSame(ONE, array[0]);
        assertSame(TWO, array[1]);
        assertEquals(2, array.length);
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testHashCode
    public void testHashCode() {
        final MultiKey<Integer> mk1 = new MultiKey<Integer>(ONE, TWO);
        final MultiKey<Integer> mk2 = new MultiKey<Integer>(ONE, TWO);
        final MultiKey<Object> mk3 = new MultiKey<Object>(ONE, "TWO");

        assertTrue(mk1.hashCode() == mk1.hashCode());
        assertTrue(mk1.hashCode() == mk2.hashCode());
        assertTrue(mk1.hashCode() != mk3.hashCode());

        final int total = (0 ^ ONE.hashCode()) ^ TWO.hashCode();
        assertEquals(total, mk1.hashCode());
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testEquals
    public void testEquals() {
        final MultiKey<Integer> mk1 = new MultiKey<Integer>(ONE, TWO);
        final MultiKey<Integer> mk2 = new MultiKey<Integer>(ONE, TWO);
        final MultiKey<Object> mk3 = new MultiKey<Object>(ONE, "TWO");

        assertEquals(mk1, mk1);
        assertEquals(mk1, mk2);
        assertFalse(mk1.equals(mk3));
        assertFalse(mk1.equals(""));
        assertFalse(mk1.equals(null));
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testEqualsAfterSerialization
    public void testEqualsAfterSerialization() throws IOException, ClassNotFoundException
    {
        SystemHashCodeSimulatingKey sysKey = new SystemHashCodeSimulatingKey("test");
        final MultiKey<?> mk = new MultiKey<Object>(ONE, sysKey);
        final Map<MultiKey<?>, Integer> map = new HashMap<MultiKey<?>, Integer>();
        map.put(mk, TWO);

        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(sysKey);
        out.writeObject(map);
        out.close();

        
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bais);
        sysKey = (SystemHashCodeSimulatingKey)in.readObject(); 
        final Map<?, ?> map2 = (Map<?, ?>) in.readObject();
        in.close();

        assertEquals(2, sysKey.hashCode()); 

        final MultiKey<?> mk2 = new MultiKey<Object>(ONE, sysKey);
        assertEquals(TWO, map2.get(mk2));
    }

// org.apache.commons.collections4.keyvalue.MultiKeyTest::testEqualsAfterSerializationOfDerivedClass
    public void testEqualsAfterSerializationOfDerivedClass() throws IOException, ClassNotFoundException
    {
        final DerivedMultiKey<?> mk = new DerivedMultiKey<String>("A", "B");

        
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(mk);
        out.close();

        
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bais);
        final DerivedMultiKey<?> mk2 = (DerivedMultiKey<?>)in.readObject();
        in.close();

        assertEquals(mk.hashCode(), mk2.hashCode());
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testNullHandling
    public void testNullHandling() {
        resetFull();
        assertEquals(null, map.get(null));
        assertEquals(false, map.containsKey(null));
        assertEquals(false, map.containsValue(null));
        assertEquals(null, map.remove(null));
        assertEquals(false, map.entrySet().contains(null));
        assertEquals(false, map.keySet().contains(null));
        assertEquals(false, map.values().contains(null));
        try {
            map.put(null, null);
            fail();
        } catch (final NullPointerException ex) {}
        assertEquals(null, map.put(new MultiKey<K>(null, null), null));
        try {
            map.put(null, (V) new Object());
            fail();
        } catch (final NullPointerException ex) {}
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyGet
    public void testMultiKeyGet() {
        resetFull();
        final MultiKeyMap<K, V> multimap = getMap();
        final MultiKey<K>[] keys = getMultiKeyKeys();
        final V[] values = getSampleValues();

        for (int i = 0; i < keys.length; i++) {
            final MultiKey<K> key = keys[i];
            final V value = values[i];

            switch (key.size()) {
                case 2:
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1)));
                assertEquals(null, multimap.get(null, key.getKey(1)));
                assertEquals(null, multimap.get(key.getKey(0), null));
                assertEquals(null, multimap.get(null, null));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), null));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), null, null));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), null, null, null));
                break;
                case 3:
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(null, multimap.get(null, key.getKey(1), key.getKey(2)));
                assertEquals(null, multimap.get(key.getKey(0), null, key.getKey(2)));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), null));
                assertEquals(null, multimap.get(null, null, null));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), null));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), null, null));
                break;
                case 4:
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(null, multimap.get(null, key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(null, multimap.get(key.getKey(0), null, key.getKey(2), key.getKey(3)));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), null, key.getKey(3)));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), null));
                assertEquals(null, multimap.get(null, null, null, null));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), null));
                break;
                case 5:
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(null, multimap.get(null, key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(null, multimap.get(key.getKey(0), null, key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), null, key.getKey(3), key.getKey(4)));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), null, key.getKey(4)));
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), null));
                assertEquals(null, multimap.get(null, null, null, null, null));
                break;
                default:
                fail("Invalid key size");
            }
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyContainsKey
    public void testMultiKeyContainsKey() {
        resetFull();
        final MultiKeyMap<K, V> multimap = getMap();
        final MultiKey<K>[] keys = getMultiKeyKeys();

        for (final MultiKey<K> key : keys) {
            switch (key.size()) {
                case 2:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1)));
                assertEquals(false, multimap.containsKey(null, key.getKey(1)));
                assertEquals(false, multimap.containsKey(key.getKey(0), null));
                assertEquals(false, multimap.containsKey(null, null));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), null));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), null, null));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), null, null, null));
                break;
                case 3:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(false, multimap.containsKey(null, key.getKey(1), key.getKey(2)));
                assertEquals(false, multimap.containsKey(key.getKey(0), null, key.getKey(2)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), null));
                assertEquals(false, multimap.containsKey(null, null, null));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), null));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), null, null));
                break;
                case 4:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(false, multimap.containsKey(null, key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(false, multimap.containsKey(key.getKey(0), null, key.getKey(2), key.getKey(3)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), null, key.getKey(3)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), null));
                assertEquals(false, multimap.containsKey(null, null, null, null));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), null));
                break;
                case 5:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(false, multimap.containsKey(null, key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(false, multimap.containsKey(key.getKey(0), null, key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), null, key.getKey(3), key.getKey(4)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), null, key.getKey(4)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), null));
                assertEquals(false, multimap.containsKey(null, null, null, null, null));
                break;
                default:
                fail("Invalid key size");
            }
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyPut
    public void testMultiKeyPut() {
        final MultiKey<K>[] keys = getMultiKeyKeys();
        final V[] values = getSampleValues();

        for (int i = 0; i < keys.length; i++) {
            final MultiKeyMap<K, V> multimap = new MultiKeyMap<K, V>();

            final MultiKey<K> key = keys[i];
            final V value = values[i];

            switch (key.size()) {
                case 2:
                assertEquals(null, multimap.put(key.getKey(0), key.getKey(1), value));
                assertEquals(1, multimap.size());
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1)));
                assertEquals(true, multimap.containsKey(new MultiKey<K>(key.getKey(0), key.getKey(1))));
                assertEquals(value, multimap.put(key.getKey(0), key.getKey(1), null));
                assertEquals(1, multimap.size());
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1)));
                break;
                case 3:
                assertEquals(null, multimap.put(key.getKey(0), key.getKey(1), key.getKey(2), value));
                assertEquals(1, multimap.size());
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(true, multimap.containsKey(new MultiKey<K>(key.getKey(0), key.getKey(1), key.getKey(2))));
                assertEquals(value, multimap.put(key.getKey(0), key.getKey(1), key.getKey(2), null));
                assertEquals(1, multimap.size());
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                break;
                case 4:
                assertEquals(null, multimap.put(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), value));
                assertEquals(1, multimap.size());
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(true, multimap.containsKey(new MultiKey<K>(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3))));
                assertEquals(value, multimap.put(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), null));
                assertEquals(1, multimap.size());
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                break;
                case 5:
                assertEquals(null, multimap.put(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4), value));
                assertEquals(1, multimap.size());
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(true, multimap.containsKey(new MultiKey<K>(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4))));
                assertEquals(value, multimap.put(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4), null));
                assertEquals(1, multimap.size());
                assertEquals(null, multimap.get(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                break;
                default:
                fail("Invalid key size");
            }
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyPutWithNullKey
    public void testMultiKeyPutWithNullKey() {
        final MultiKeyMap<String, String> map = new MultiKeyMap<String, String>();
        map.put("a", null, "value1");
        map.put("b", null, "value2");
        map.put("c", null, "value3");
        map.put("a", "z",  "value4");
        map.put("a", null, "value5");
        map.put(null, "a", "value6");
        map.put(null, null, "value7");
        
        assertEquals(6, map.size());
        assertEquals("value5", map.get("a", null));
        assertEquals("value4", map.get("a", "z"));
        assertEquals("value6", map.get(null, "a"));
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyRemove
    public void testMultiKeyRemove() {
        final MultiKey<K>[] keys = getMultiKeyKeys();
        final V[] values = getSampleValues();

        for (int i = 0; i < keys.length; i++) {
            resetFull();
            final MultiKeyMap<K, V> multimap = getMap();
            final int size = multimap.size();

            final MultiKey<K> key = keys[i];
            final V value = values[i];

            switch (key.size()) {
                case 2:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1)));
                assertEquals(value, multimap.removeMultiKey(key.getKey(0), key.getKey(1)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1)));
                assertEquals(size - 1, multimap.size());
                assertEquals(null, multimap.removeMultiKey(key.getKey(0), key.getKey(1)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1)));
                break;
                case 3:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(value, multimap.removeMultiKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(size - 1, multimap.size());
                assertEquals(null, multimap.removeMultiKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2)));
                break;
                case 4:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(value, multimap.removeMultiKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(size - 1, multimap.size());
                assertEquals(null, multimap.removeMultiKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3)));
                break;
                case 5:
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(value, multimap.removeMultiKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(size - 1, multimap.size());
                assertEquals(null, multimap.removeMultiKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                assertEquals(false, multimap.containsKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4)));
                break;
                default:
                fail("Invalid key size");
            }
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyRemoveAll1
    public void testMultiKeyRemoveAll1() {
        resetFull();
        final MultiKeyMap<K, V> multimap = getMap();
        assertEquals(12, multimap.size());

        multimap.removeAll(I1);
        assertEquals(8, multimap.size());
        for (final MapIterator<MultiKey<? extends K>, V> it = multimap.mapIterator(); it.hasNext();) {
            final MultiKey<? extends K> key = it.next();
            assertEquals(false, I1.equals(key.getKey(0)));
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyRemoveAll2
    public void testMultiKeyRemoveAll2() {
        resetFull();
        final MultiKeyMap<K, V> multimap = getMap();
        assertEquals(12, multimap.size());

        multimap.removeAll(I2, I3);
        assertEquals(9, multimap.size());
        for (final MapIterator<MultiKey<? extends K>, V> it = multimap.mapIterator(); it.hasNext();) {
            final MultiKey<? extends K> key = it.next();
            assertEquals(false, I2.equals(key.getKey(0)) && I3.equals(key.getKey(1)));
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyRemoveAll3
    public void testMultiKeyRemoveAll3() {
        resetFull();
        final MultiKeyMap<K, V> multimap = getMap();
        assertEquals(12, multimap.size());

        multimap.removeAll(I1, I1, I2);
        assertEquals(9, multimap.size());
        for (final MapIterator<MultiKey<? extends K>, V> it = multimap.mapIterator(); it.hasNext();) {
            final MultiKey<? extends K> key = it.next();
            assertEquals(false, I1.equals(key.getKey(0)) && I1.equals(key.getKey(1)) && I2.equals(key.getKey(2)));
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testMultiKeyRemoveAll4
    public void testMultiKeyRemoveAll4() {
        resetFull();
        final MultiKeyMap<K, V> multimap = getMap();
        assertEquals(12, multimap.size());

        multimap.removeAll(I1, I1, I2, I3);
        assertEquals(10, multimap.size());
        for (final MapIterator<MultiKey<? extends K>, V> it = multimap.mapIterator(); it.hasNext();) {
            final MultiKey<? extends K> key = it.next();
            assertEquals(false, I1.equals(key.getKey(0)) && I1.equals(key.getKey(1)) && I2.equals(key.getKey(2)) && key.size() >= 4 && I3.equals(key.getKey(3)));
        }
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testClone
    public void testClone() {
        final MultiKeyMap<K, V> map = new MultiKeyMap<K, V>();
        map.put(new MultiKey<K>((K) I1, (K) I2), (V) "1-2");
        final Map<MultiKey<? extends K>, V> cloned = map.clone();
        assertEquals(map.size(), cloned.size());
        assertSame(map.get(new MultiKey<K>((K) I1, (K) I2)), cloned.get(new MultiKey<K>((K) I1, (K) I2)));
    }

// org.apache.commons.collections4.map.MultiKeyMapTest::testLRUMultiKeyMap
    public void testLRUMultiKeyMap() {
        final MultiKeyMap<K, V> map = MultiKeyMap.multiKeyMap(new LRUMap<MultiKey<? extends K>, V>(2));
        map.put((K) I1, (K) I2, (V) "1-2");
        map.put((K) I1, (K) I3, (V) "1-1");
        assertEquals(2, map.size());
        map.put((K) I1, (K) I4, (V) "1-4");
        assertEquals(2, map.size());
        assertEquals(true, map.containsKey(I1, I3));
        assertEquals(true, map.containsKey(I1, I4));
        assertEquals(false, map.containsKey(I1, I2));

        final MultiKeyMap<K, V> cloned = map.clone();
        assertEquals(2, map.size());
        assertEquals(true, cloned.containsKey(I1, I3));
        assertEquals(true, cloned.containsKey(I1, I4));
        assertEquals(false, cloned.containsKey(I1, I2));
        cloned.put((K) I1, (K) I5, (V) "1-5");
        assertEquals(2, cloned.size());
        assertEquals(true, cloned.containsKey(I1, I4));
        assertEquals(true, cloned.containsKey(I1, I5));
    }
