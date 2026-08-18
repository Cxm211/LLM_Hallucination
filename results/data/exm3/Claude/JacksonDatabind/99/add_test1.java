// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testCanonicalNamesAdditional2()
{
    TypeFactory tf = TypeFactory.defaultInstance();
    // Test with nested ReferenceType
    JavaType t = tf.constructType(new TypeReference<java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<String>>>() { });
    String can = t.toCanonical();
    assertEquals("java.util.concurrent.atomic.AtomicReference<java.util.concurrent.atomic.AtomicReference<java.lang.String>>", can);
    assertEquals(t, tf.constructFromCanonical(can));
}