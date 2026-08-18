// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testCanonicalNamesNested() {
    TypeFactory tf = TypeFactory.defaultInstance();
    JavaType t = tf.constructType(new TypeReference<AtomicReference<List<String>>>() { });
    String can = t.toCanonical();
    assertEquals("java.util.concurrent.atomic.AtomicReference<java.util.List<java.lang.String>>", can);
    assertEquals(t, tf.constructFromCanonical(can));
}
