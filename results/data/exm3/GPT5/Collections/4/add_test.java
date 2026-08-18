// org/apache/commons/collections/map/TestMultiValueMap.java::testPutWithSet
public void testPutAllWithSetDuplicates() {
        MultiValueMap test = MultiValueMap.decorate(new HashMap(), HashSet.class);
        Collection coll = Arrays.asList(new Object[]{"a", "a"});
        assertEquals(true, test.putAll("A", coll));
        assertEquals(1, test.size("A"));
    }