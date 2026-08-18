// com/fasterxml/jackson/databind/type/TestTypeFactory.java
public void testPropertiesSubclass() throws Exception
{
    // Test that subclasses of Properties also get String/String typing
    class MyProperties extends java.util.Properties {}
    
    TypeFactory tf = TypeFactory.defaultInstance();
    JavaType t = tf.constructType(MyProperties.class);
    assertEquals(MapType.class, t.getClass());
    assertSame(MyProperties.class, t.getRawClass());
    
    // Subclass should inherit proper typing from Map, not special Properties handling
    // Since it's a subclass with no explicit type params, it should resolve to Object/Object
    assertSame(Object.class, ((MapType) t).getKeyType().getRawClass());
    assertSame(Object.class, ((MapType) t).getContentType().getRawClass());
}