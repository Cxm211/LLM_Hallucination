// org/apache/commons/collections/map/TestMultiValueMap.java
public void testPutAllWithSet() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        Collection coll = Arrays.asList(new Object[] {\"X\", \"X\", \"Y\"});
        assertEquals(true, map.putAll(\"A\", coll));
        assertEquals(2, map.size(\"A\"));
        assertEquals(true, map.containsValue(\"A\", \"X\"));
        assertEquals(true, map.containsValue(\"A\", \"Y\"));
        assertEquals(false, map.containsValue(\"A\", \"Z\"));
        Collection coll2 = Arrays.asList(new Object[] {\"Y\", \"Z\"});
        assertEquals(true, map.putAll(\"A\", coll2));
        assertEquals(3, map.size(\"A\"));
        assertEquals(true, map.containsValue(\"A\", \"Z\"));
    }
