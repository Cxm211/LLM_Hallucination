// com/fasterxml/jackson/databind/jsontype/TestSubtypes.java
public void testNarrowObjectToInteger() throws Exception {
        com.fasterxml.jackson.databind.type.TypeFactory tf = MAPPER.getTypeFactory();
        com.fasterxml.jackson.databind.JavaType objectType = tf.constructType(Object.class);
        com.fasterxml.jackson.databind.JavaType narrowed = objectType.narrowBy(Integer.class);
        // Assert superclass is not Object
        assertNotNull(narrowed.getSuperClass());
        assertNotSame(objectType, narrowed.getSuperClass());
        assertEquals(Number.class, narrowed.getSuperClass().getRawClass());
    }
