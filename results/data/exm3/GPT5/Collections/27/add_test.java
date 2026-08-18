// org/apache/commons/collections4/map/MultiValueMapTest.java::testUnsafeDeSerialization
public void testUnsafeDeSerialization_RuntimeClass() throws Exception {
        MultiValueMap map = MultiValueMap.multiValueMap(new HashMap(), (Class) Runtime.class);
        byte[] bytes = serialize(map);
        try {
            Object result = deserialize(bytes);
            fail("unsafe clazz accepted when de-serializing MultiValueMap (Runtime.class)");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }