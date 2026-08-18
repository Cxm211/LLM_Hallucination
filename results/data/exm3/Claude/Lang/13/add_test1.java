// org/apache/commons/lang3/SerializationUtilsTest.java
public void testMixedPrimitiveAndObjectClassSerialization() {
    Class<?>[] mixedTypes = { int.class, String.class, double.class, Integer.class, boolean.class };

    for (Class<?> type : mixedTypes) {
        Class<?> clone = SerializationUtils.clone(type);
        assertEquals(type, clone);
    }
}