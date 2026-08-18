// buggy function
    public JavaType constructType(Type type, Class<?> contextClass) {
        return constructType(type, constructType(contextClass));
    }

    public JavaType constructType(Type type, JavaType contextType) {
        return _fromAny(null, type, contextType.getBindings());
    }

// trigger testcase
// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java::testDeprecatedTypeResolution
public void testDeprecatedTypeResolution() throws Exception
    {
        TypeFactory tf = MAPPER.getTypeFactory();

        // first, with real (if irrelevant) context
        JavaType t = tf.constructType(Point.class, getClass());
        assertEquals(Point.class, t.getRawClass());

        // and then missing context
        JavaType t2 = tf.constructType(Point.class, (Class<?>) null);
        assertEquals(Point.class, t2.getRawClass());

        JavaType ctxt = tf.constructType(getClass());
        JavaType t3 = tf.constructType(Point.class, ctxt);
        assertEquals(Point.class, t3.getRawClass());
    }
