// com/fasterxml/jackson/databind/type/TestTypeFactory.java::testCanonicalNames
public void testCanonicalNamesNestedReference() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType t = tf.constructType(new TypeReference<AtomicReference<AtomicReference<Long>>>() { });
        String can = t.toCanonical();
        assertEquals("java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.Long>>", can);
        assertEquals(t, tf.constructFromCanonical(can));
    }