// buggy code
	private void calculateHashCode(Object[] keys)
	{
		int total = 0;
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null) {
                total ^= keys[i].hashCode();
            }
        }
        hashCode = total;
	}

// relevant test
// org.apache.commons.collections.keyvalue.TestMultiKey::testConstructors
    public void testConstructors() throws Exception {
        MultiKey mk = null;
        mk = new MultiKey(ONE, TWO);
        Assert.assertTrue(Arrays.equals(new Object[] {ONE, TWO}, mk.getKeys()));

        mk = new MultiKey(ONE, TWO, THREE);
        Assert.assertTrue(Arrays.equals(new Object[] {ONE, TWO, THREE}, mk.getKeys()));

        mk = new MultiKey(ONE, TWO, THREE, FOUR);
        Assert.assertTrue(Arrays.equals(new Object[] {ONE, TWO, THREE, FOUR}, mk.getKeys()));

        mk = new MultiKey(ONE, TWO, THREE, FOUR, FIVE);
        Assert.assertTrue(Arrays.equals(new Object[] {ONE, TWO, THREE, FOUR, FIVE}, mk.getKeys()));

        mk = new MultiKey(new Object[] {THREE, FOUR, ONE, TWO}, false);
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, TWO}, mk.getKeys()));
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testConstructorsByArray
    public void testConstructorsByArray() throws Exception {
        MultiKey mk = null;
        Object[] keys = new Object[] {THREE, FOUR, ONE, TWO};
        mk = new MultiKey(keys);
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, TWO}, mk.getKeys()));
        keys[3] = FIVE;  
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, TWO}, mk.getKeys()));

        keys = new Object[] {};
        mk = new MultiKey(keys);
        Assert.assertTrue(Arrays.equals(new Object[] {}, mk.getKeys()));

        keys = new Object[] {THREE, FOUR, ONE, TWO};
        mk = new MultiKey(keys, true);
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, TWO}, mk.getKeys()));
        keys[3] = FIVE;  
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, TWO}, mk.getKeys()));

        keys = new Object[] {THREE, FOUR, ONE, TWO};
        mk = new MultiKey(keys, false);
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, TWO}, mk.getKeys()));
        
        
        keys[3] = FIVE;
        Assert.assertTrue(Arrays.equals(new Object[] {THREE, FOUR, ONE, FIVE}, mk.getKeys()));
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testConstructorsByArrayNull
    public void testConstructorsByArrayNull() throws Exception {
        Object[] keys = null;
        try {
            new MultiKey(keys);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MultiKey(keys, true);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            new MultiKey(keys, false);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testSize
    public void testSize() {
        Assert.assertEquals(2, new MultiKey(ONE, TWO).size());
        Assert.assertEquals(2, new MultiKey(null, null).size());
        Assert.assertEquals(3, new MultiKey(ONE, TWO, THREE).size());
        Assert.assertEquals(3, new MultiKey(null, null, null).size());
        Assert.assertEquals(4, new MultiKey(ONE, TWO, THREE, FOUR).size());
        Assert.assertEquals(4, new MultiKey(null, null, null, null).size());
        Assert.assertEquals(5, new MultiKey(ONE, TWO, THREE, FOUR, FIVE).size());
        Assert.assertEquals(5, new MultiKey(null, null, null, null, null).size());
        
        Assert.assertEquals(0, new MultiKey(new Object[] {}).size());
        Assert.assertEquals(1, new MultiKey(new Object[] {ONE}).size());
        Assert.assertEquals(2, new MultiKey(new Object[] {ONE, TWO}).size());
        Assert.assertEquals(7, new MultiKey(new Object[] {ONE, TWO, ONE, TWO, ONE, TWO, ONE}).size());
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testGetIndexed
    public void testGetIndexed() {
        MultiKey mk = new MultiKey(ONE, TWO);
        Assert.assertSame(ONE, mk.getKey(0));
        Assert.assertSame(TWO, mk.getKey(1));
        try {
            mk.getKey(-1);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
        try {
            mk.getKey(2);
            fail();
        } catch (IndexOutOfBoundsException ex) {}
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testGetKeysSimpleConstructor
    public void testGetKeysSimpleConstructor() {
        MultiKey mk = new MultiKey(ONE, TWO);
        Object[] array = mk.getKeys();
        Assert.assertSame(ONE, array[0]);
        Assert.assertSame(TWO, array[1]);
        Assert.assertEquals(2, array.length);
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testGetKeysArrayConstructorCloned
    public void testGetKeysArrayConstructorCloned() {
        Object[] keys = new Object[] {ONE, TWO};
        MultiKey mk = new MultiKey(keys, true);
        Object[] array = mk.getKeys();
        Assert.assertTrue(array != keys);
        Assert.assertTrue(Arrays.equals(array, keys));
        Assert.assertSame(ONE, array[0]);
        Assert.assertSame(TWO, array[1]);
        Assert.assertEquals(2, array.length);
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testGetKeysArrayConstructorNonCloned
    public void testGetKeysArrayConstructorNonCloned() {
        Object[] keys = new Object[] {ONE, TWO};
        MultiKey mk = new MultiKey(keys, false);
        Object[] array = mk.getKeys();
        Assert.assertTrue(array != keys);  
        Assert.assertTrue(Arrays.equals(array, keys));
        Assert.assertSame(ONE, array[0]);
        Assert.assertSame(TWO, array[1]);
        Assert.assertEquals(2, array.length);
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testHashCode
    public void testHashCode() {
        MultiKey mk1 = new MultiKey(ONE, TWO);
        MultiKey mk2 = new MultiKey(ONE, TWO);
        MultiKey mk3 = new MultiKey(ONE, "TWO");
        
        Assert.assertTrue(mk1.hashCode() == mk1.hashCode());
        Assert.assertTrue(mk1.hashCode() == mk2.hashCode());
        Assert.assertTrue(mk1.hashCode() != mk3.hashCode());
        
        int total = (0 ^ ONE.hashCode()) ^ TWO.hashCode();
        Assert.assertEquals(total, mk1.hashCode());
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testEquals
    public void testEquals() {
        MultiKey mk1 = new MultiKey(ONE, TWO);
        MultiKey mk2 = new MultiKey(ONE, TWO);
        MultiKey mk3 = new MultiKey(ONE, "TWO");
        
        Assert.assertEquals(mk1, mk1);
        Assert.assertEquals(mk1, mk2);
        Assert.assertTrue(mk1.equals(mk3) == false);
        Assert.assertTrue(mk1.equals("") == false);
        Assert.assertTrue(mk1.equals(null) == false);
    }

// org.apache.commons.collections.keyvalue.TestMultiKey::testEqualsAfterSerialization
    public void testEqualsAfterSerialization() throws IOException, ClassNotFoundException
	{
        SystemHashCodeSimulatingKey sysKey = new SystemHashCodeSimulatingKey("test");
		MultiKey mk = new MultiKey(ONE, sysKey);
        Map map = new HashMap();
        map.put(mk, TWO);

        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(sysKey);
        out.writeObject(map);
        out.close();

        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        sysKey = (SystemHashCodeSimulatingKey)in.readObject(); 
        Map map2 = (Map) in.readObject();
        in.close();

        assertEquals(2, sysKey.hashCode()); 

        MultiKey mk2 = new MultiKey(ONE, sysKey);
        assertEquals(TWO, map2.get(mk2));		
	}

// org.apache.commons.collections.map.TestMultiKeyMap::testNullHandling
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
        } catch (NullPointerException ex) {}
        assertEquals(null, map.put(new MultiKey(null, null), null));
        try {
            map.put(null, new Object());
            fail();
        } catch (NullPointerException ex) {}
    }

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyGet
    public void testMultiKeyGet() {
        resetFull();
        MultiKeyMap multimap = (MultiKeyMap) map;
        MultiKey[] keys = getMultiKeyKeys();
        Object[] values = getSampleValues();
        
        for (int i = 0; i < keys.length; i++) {
            MultiKey key = keys[i];
            Object value = values[i];
            
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

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyContainsKey
    public void testMultiKeyContainsKey() {
        resetFull();
        MultiKeyMap multimap = (MultiKeyMap) map;
        MultiKey[] keys = getMultiKeyKeys();
        Object[] values = getSampleValues();
        
        for (int i = 0; i < keys.length; i++) {
            MultiKey key = keys[i];
            Object value = values[i];
            
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

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyPut
    public void testMultiKeyPut() {
        MultiKey[] keys = getMultiKeyKeys();
        Object[] values = getSampleValues();
        
        for (int i = 0; i < keys.length; i++) {
            MultiKeyMap multimap = new MultiKeyMap();
            
            MultiKey key = keys[i];
            Object value = values[i];
            
            switch (key.size()) {
                case 2:
                assertEquals(null, multimap.put(key.getKey(0), key.getKey(1), value));
                assertEquals(1, multimap.size());
                assertEquals(value, multimap.get(key.getKey(0), key.getKey(1)));
                assertEquals(true, multimap.containsKey(key.getKey(0), key.getKey(1)));
                assertEquals(true, multimap.containsKey(new MultiKey(key.getKey(0), key.getKey(1))));
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
                assertEquals(true, multimap.containsKey(new MultiKey(key.getKey(0), key.getKey(1), key.getKey(2))));
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
                assertEquals(true, multimap.containsKey(new MultiKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3))));
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
                assertEquals(true, multimap.containsKey(new MultiKey(key.getKey(0), key.getKey(1), key.getKey(2), key.getKey(3), key.getKey(4))));
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

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyRemove
    public void testMultiKeyRemove() {
        MultiKey[] keys = getMultiKeyKeys();
        Object[] values = getSampleValues();
        
        for (int i = 0; i < keys.length; i++) {
            resetFull();
            MultiKeyMap multimap = (MultiKeyMap) map;
            int size = multimap.size();
            
            MultiKey key = keys[i];
            Object value = values[i];
            
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

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyRemoveAll1
    public void testMultiKeyRemoveAll1() {
        resetFull();
        MultiKeyMap multimap = (MultiKeyMap) map;
        assertEquals(12, multimap.size());
        
        multimap.removeAll(I1);
        assertEquals(8, multimap.size());
        for (MapIterator it = multimap.mapIterator(); it.hasNext();) {
            MultiKey key = (MultiKey) it.next();
            assertEquals(false, I1.equals(key.getKey(0)));
        }
    }

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyRemoveAll2
    public void testMultiKeyRemoveAll2() {
        resetFull();
        MultiKeyMap multimap = (MultiKeyMap) map;
        assertEquals(12, multimap.size());
        
        multimap.removeAll(I2, I3);
        assertEquals(9, multimap.size());
        for (MapIterator it = multimap.mapIterator(); it.hasNext();) {
            MultiKey key = (MultiKey) it.next();
            assertEquals(false, I2.equals(key.getKey(0)) && I3.equals(key.getKey(1)));
        }
    }

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyRemoveAll3
    public void testMultiKeyRemoveAll3() {
        resetFull();
        MultiKeyMap multimap = (MultiKeyMap) map;
        assertEquals(12, multimap.size());
        
        multimap.removeAll(I1, I1, I2);
        assertEquals(9, multimap.size());
        for (MapIterator it = multimap.mapIterator(); it.hasNext();) {
            MultiKey key = (MultiKey) it.next();
            assertEquals(false, I1.equals(key.getKey(0)) && I1.equals(key.getKey(1)) && I2.equals(key.getKey(2)));
        }
    }

// org.apache.commons.collections.map.TestMultiKeyMap::testMultiKeyRemoveAll4
    public void testMultiKeyRemoveAll4() {
        resetFull();
        MultiKeyMap multimap = (MultiKeyMap) map;
        assertEquals(12, multimap.size());
        
        multimap.removeAll(I1, I1, I2, I3);
        assertEquals(10, multimap.size());
        for (MapIterator it = multimap.mapIterator(); it.hasNext();) {
            MultiKey key = (MultiKey) it.next();
            assertEquals(false, I1.equals(key.getKey(0)) && I1.equals(key.getKey(1)) && I2.equals(key.getKey(2)) && key.size() >= 4 && I3.equals(key.getKey(3)));
        }
    }

// org.apache.commons.collections.map.TestMultiKeyMap::testClone
    public void testClone() {
        MultiKeyMap map = new MultiKeyMap();
        map.put(new MultiKey(I1, I2), "1-2");
        Map cloned = (Map) map.clone();
        assertEquals(map.size(), cloned.size());
        assertSame(map.get(new MultiKey(I1, I2)), cloned.get(new MultiKey(I1, I2)));
    }

// org.apache.commons.collections.map.TestMultiKeyMap::testLRUMultiKeyMap
    public void testLRUMultiKeyMap() {
        MultiKeyMap map = MultiKeyMap.decorate(new LRUMap(2));
        map.put(I1, I2, "1-2");
        map.put(I1, I3, "1-3");
        assertEquals(2, map.size());
        map.put(I1, I4, "1-4");
        assertEquals(2, map.size());
        assertEquals(true, map.containsKey(I1, I3));
        assertEquals(true, map.containsKey(I1, I4));
        assertEquals(false, map.containsKey(I1, I2));
        
        MultiKeyMap cloned = (MultiKeyMap) map.clone();
        assertEquals(2, map.size());
        assertEquals(true, cloned.containsKey(I1, I3));
        assertEquals(true, cloned.containsKey(I1, I4));
        assertEquals(false, cloned.containsKey(I1, I2));
        cloned.put(I1, I5, "1-5");
        assertEquals(2, cloned.size());
        assertEquals(true, cloned.containsKey(I1, I4));
        assertEquals(true, cloned.containsKey(I1, I5));
    }
