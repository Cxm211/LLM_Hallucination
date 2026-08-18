// buggy code
        public Object setValue(Object value) {
            if (canRemove == false) {
                throw new IllegalStateException(AbstractHashedMap.SETVALUE_INVALID);
            }
            Object old = getValue();
            switch (nextIndex) {
                case 3: 
                    parent.value3 = value;
                case 2:
                    parent.value2 = value;
                case 1:
                    parent.value1 = value;
            }
            return old;
        }

        public Object setValue(Object value) {
            if (canRemove == false) {
                throw new IllegalStateException(AbstractHashedMap.SETVALUE_INVALID);
            }
            Object old = getValue();
            switch (nextIndex) {
                case 3: 
                    parent.value3 = value;
                case 2:
                    parent.value2 = value;
                case 1:
                    parent.value1 = value;
            }
            return old;
        }

// relevant test
// org.apache.commons.collections.map.TestFlat3Map::testEquals1
    public void testEquals1() {
        Flat3Map map1 = new Flat3Map();
        map1.put("a", "testA");
        map1.put("b", "testB");
        Flat3Map map2 = new Flat3Map();
        map2.put("a", "testB");
        map2.put("b", "testA");
        assertEquals(false, map1.equals(map2));
    }

// org.apache.commons.collections.map.TestFlat3Map::testEquals2
    public void testEquals2() {
        Flat3Map map1 = new Flat3Map();
        map1.put("a", "testA");
        map1.put("b", "testB");
        Flat3Map map2 = new Flat3Map();
        map2.put("a", "testB");
        map2.put("c", "testA");
        assertEquals(false, map1.equals(map2));
    }

// org.apache.commons.collections.map.TestFlat3Map::testClone2
    public void testClone2() {
        Flat3Map map = new Flat3Map();
        assertEquals(0, map.size());
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        assertEquals(2, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertSame(TEN, map.get(ONE));
        assertSame(TWENTY, map.get(TWO));

        
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(2, cloned.size());
        assertEquals(true, cloned.containsKey(ONE));
        assertEquals(true, cloned.containsKey(TWO));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
        
        
        map.put(TEN, ONE);
        map.put(TWENTY, TWO);
        assertEquals(4, map.size());
        assertEquals(2, cloned.size());
        assertEquals(true, cloned.containsKey(ONE));
        assertEquals(true, cloned.containsKey(TWO));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
    }

// org.apache.commons.collections.map.TestFlat3Map::testClone4
    public void testClone4() {
        Flat3Map map = new Flat3Map();
        assertEquals(0, map.size());
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(TEN, ONE);
        map.put(TWENTY, TWO);
        
        
        Flat3Map cloned = (Flat3Map) map.clone();
        assertEquals(4, map.size());
        assertEquals(4, cloned.size());
        assertEquals(true, cloned.containsKey(ONE));
        assertEquals(true, cloned.containsKey(TWO));
        assertEquals(true, cloned.containsKey(TEN));
        assertEquals(true, cloned.containsKey(TWENTY));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
        assertSame(ONE, cloned.get(TEN));
        assertSame(TWO, cloned.get(TWENTY));
        
        
        map.clear();
        assertEquals(0, map.size());
        assertEquals(4, cloned.size());
        assertEquals(true, cloned.containsKey(ONE));
        assertEquals(true, cloned.containsKey(TWO));
        assertEquals(true, cloned.containsKey(TEN));
        assertEquals(true, cloned.containsKey(TWENTY));
        assertSame(TEN, cloned.get(ONE));
        assertSame(TWENTY, cloned.get(TWO));
        assertSame(ONE, cloned.get(TEN));
        assertSame(TWO, cloned.get(TWENTY));
    }

// org.apache.commons.collections.map.TestFlat3Map::testSerialisation0
    public void testSerialisation0() throws Exception {
        Flat3Map map = new Flat3Map();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(map);
        byte[] bytes = bout.toByteArray();
        out.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bin);
        Flat3Map ser = (Flat3Map) in.readObject();
        in.close();
        assertEquals(0, map.size());
        assertEquals(0, ser.size());
    }

// org.apache.commons.collections.map.TestFlat3Map::testSerialisation2
    public void testSerialisation2() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(map);
        byte[] bytes = bout.toByteArray();
        out.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bin);
        Flat3Map ser = (Flat3Map) in.readObject();
        in.close();
        assertEquals(2, map.size());
        assertEquals(2, ser.size());
        assertEquals(true, ser.containsKey(ONE));
        assertEquals(true, ser.containsKey(TWO));
        assertEquals(TEN, ser.get(ONE));
        assertEquals(TWENTY, ser.get(TWO));
    }

// org.apache.commons.collections.map.TestFlat3Map::testSerialisation4
    public void testSerialisation4() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(TEN, ONE);
        map.put(TWENTY, TWO);
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(map);
        byte[] bytes = bout.toByteArray();
        out.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bin);
        Flat3Map ser = (Flat3Map) in.readObject();
        in.close();
        assertEquals(4, map.size());
        assertEquals(4, ser.size());
        assertEquals(true, ser.containsKey(ONE));
        assertEquals(true, ser.containsKey(TWO));
        assertEquals(true, ser.containsKey(TEN));
        assertEquals(true, ser.containsKey(TWENTY));
        assertEquals(TEN, ser.get(ONE));
        assertEquals(TWENTY, ser.get(TWO));
        assertEquals(ONE, ser.get(TEN));
        assertEquals(TWO, ser.get(TWENTY));
    }

// org.apache.commons.collections.map.TestFlat3Map::testEntryIteratorSetValue1
    public void testEntryIteratorSetValue1() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(THREE, THIRTY);
        
        Iterator it = map.entrySet().iterator();
        Map.Entry entry = (Map.Entry) it.next();
        entry.setValue("NewValue");
        assertEquals(3, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(true, map.containsKey(THREE));
        assertEquals("NewValue", map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

// org.apache.commons.collections.map.TestFlat3Map::testEntryIteratorSetValue2
    public void testEntryIteratorSetValue2() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(THREE, THIRTY);
        
        Iterator it = map.entrySet().iterator();
        it.next();
        Map.Entry entry = (Map.Entry) it.next();
        entry.setValue("NewValue");
        assertEquals(3, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(true, map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals("NewValue", map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

// org.apache.commons.collections.map.TestFlat3Map::testEntryIteratorSetValue3
    public void testEntryIteratorSetValue3() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(THREE, THIRTY);
        
        Iterator it = map.entrySet().iterator();
        it.next();
        it.next();
        Map.Entry entry = (Map.Entry) it.next();
        entry.setValue("NewValue");
        assertEquals(3, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(true, map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals("NewValue", map.get(THREE));
    }

// org.apache.commons.collections.map.TestFlat3Map::testMapIteratorSetValue1
    public void testMapIteratorSetValue1() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(THREE, THIRTY);
        
        MapIterator it = map.mapIterator();
        it.next();
        it.setValue("NewValue");
        assertEquals(3, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(true, map.containsKey(THREE));
        assertEquals("NewValue", map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

// org.apache.commons.collections.map.TestFlat3Map::testMapIteratorSetValue2
    public void testMapIteratorSetValue2() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(THREE, THIRTY);
        
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
        it.setValue("NewValue");
        assertEquals(3, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(true, map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals("NewValue", map.get(TWO));
        assertEquals(THIRTY, map.get(THREE));
    }

// org.apache.commons.collections.map.TestFlat3Map::testMapIteratorSetValue3
    public void testMapIteratorSetValue3() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        map.put(THREE, THIRTY);
        
        MapIterator it = map.mapIterator();
        it.next();
        it.next();
        it.next();
        it.setValue("NewValue");
        assertEquals(3, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(true, map.containsKey(THREE));
        assertEquals(TEN, map.get(ONE));
        assertEquals(TWENTY, map.get(TWO));
        assertEquals("NewValue", map.get(THREE));
    }
