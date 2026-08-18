// org/apache/commons/collections4/map/MultiValueMapTest.java
@Test(expected = UnsupportedOperationException.class)
public void testUnsafeDeSerializationWithObject() throws Exception {
    MultiValueMap map = MultiValueMap.multiValueMap(new HashMap(), (Class) Object.class);
    byte[] bytes = serialize(map);
    deserialize(bytes);
}