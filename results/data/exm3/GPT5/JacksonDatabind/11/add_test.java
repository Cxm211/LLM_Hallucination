// com/fasterxml/jackson/databind/type/TestJavaType.java
public void testLocalTypeRecursiveBound() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        class IssueRecursive { public <T extends Comparable<T>> T method(T arg) { return arg; } }
        java.lang.reflect.Method m = IssueRecursive.class.getDeclaredMethod("method", Comparable.class);
        assertNotNull(m);
        JavaType t;
        // Return type: generic
        t = tf.constructType(m.getGenericReturnType());
        assertEquals(Comparable.class, t.getRawClass());
        // Parameter type: generic
        t = tf.constructType(m.getGenericParameterTypes()[0]);
        assertEquals(Comparable.class, t.getRawClass());
    }