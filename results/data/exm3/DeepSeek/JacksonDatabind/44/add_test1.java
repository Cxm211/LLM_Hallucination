// com/fasterxml/jackson/databind/jsontype/TestSubtypes.java
public void testNarrowInterfaceToClass() throws Exception {
        com.fasterxml.jackson.databind.type.TypeFactory tf = MAPPER.getTypeFactory();
        com.fasterxml.jackson.databind.JavaType serializableType = tf.constructType(java.io.Serializable.class);
        com.fasterxml.jackson.databind.JavaType narrowed = serializableType.narrowBy(String.class);
        // Should not have superClass set to interface
        assertNotSame(serializableType, narrowed.getSuperClass());
        // superInterfaces should include Serializable
        com.fasterxml.jackson.databind.JavaType[] superInterfaces = narrowed.getSuperInterfaces();
        boolean found = false;
        for (com.fasterxml.jackson.databind.JavaType t : superInterfaces) {
            if (t.equals(serializableType)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
