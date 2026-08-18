// org/apache/commons/collections/map/TestFlat3Map.java
public void testEntryIteratorSetValue2Size2() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put(ONE, TEN);
        map.put(TWO, TWENTY);
        
        Iterator it = map.entrySet().iterator();
        it.next();
        Map.Entry entry = (Map.Entry) it.next();
        entry.setValue("NewValue");
        assertEquals(2, map.size());
        assertEquals(true, map.containsKey(ONE));
        assertEquals(true, map.containsKey(TWO));
        assertEquals(TEN, map.get(ONE));
        assertEquals("NewValue", map.get(TWO));
    }
