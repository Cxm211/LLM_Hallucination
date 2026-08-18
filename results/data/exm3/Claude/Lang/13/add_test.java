// org/apache/commons/lang3/SerializationUtilsTest.java
public void testPrimitiveTypeArrayClassSerialization() {
    Class<?>[] primitiveArrayTypes = { byte[].class, short[].class, int[].class, long[].class, 
            float[].class, double[].class, boolean[].class, char[].class };

    for (Class<?> primitiveArrayType : primitiveArrayTypes) {
        Class<?> clone = SerializationUtils.clone(primitiveArrayType);
        assertEquals(primitiveArrayType, clone);
    }
}