// org/apache/commons/collections4/map/MultiValueMapTest.java
@Test
public void testSafeDeSerializationWithLinkedList() throws Exception {
    MultiValueMap map = MultiValueMap.multiValueMap(new HashMap(), LinkedList.class);
    byte[] bytes = serialize(map);
    Object result = deserialize(bytes);
    assertEquals(map, result);
}