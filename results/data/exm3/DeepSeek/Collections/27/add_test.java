// org/apache/commons/collections4/map/MultiValueMapTest.java
public void testUnsafeDeSerializationWithStringBuilder() throws Exception {
        MultiValueMap map = MultiValueMap.multiValueMap(new HashMap(), (Class) StringBuilder.class);
        byte[] bytes = serialize(map);
        try {
            Object result = deserialize(bytes);
            fail("unsafe clazz accepted when de-serializing MultiValueMap");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }
