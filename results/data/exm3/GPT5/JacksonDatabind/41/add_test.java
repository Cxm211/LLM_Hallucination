// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java::testDeprecatedTypeResolutionWithNullJavaType
public void testDeprecatedTypeResolutionWithNullJavaType() throws Exception {
        TypeFactory tf = MAPPER.getTypeFactory();
        JavaType t = tf.constructType(Point.class, (JavaType) null);
        assertEquals(Point.class, t.getRawClass());
    }