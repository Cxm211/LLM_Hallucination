// org/apache/commons/collections4/map/MultiValueMapTest.java
@Test(expected = UnsupportedOperationException.class)
public void testUnsafeDeSerializationWithInteger() throws Exception {
    MultiValueMap map = MultiValueMap.multiValueMap(new HashMap(), (Class) Integer.class);
    byte[] bytes = serialize(map);
    deserialize(bytes);
}