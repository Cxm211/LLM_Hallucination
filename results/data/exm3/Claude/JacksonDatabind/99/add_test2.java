// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testCanonicalNamesAdditional3()
{
    TypeFactory tf = TypeFactory.defaultInstance();
    // Test with WeakReference which is another common ReferenceType
    JavaType t = tf.constructType(new TypeReference<java.lang.ref.WeakReference<Double>>() { });
    String can = t.toCanonical();
    assertEquals("java.lang.ref.WeakReference<java.lang.Double>", can);
    assertEquals(t, tf.constructFromCanonical(can));
}