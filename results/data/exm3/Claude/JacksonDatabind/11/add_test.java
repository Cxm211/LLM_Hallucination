// com/fasterxml/jackson/databind/type/TestJavaType.java
public void testRecursiveTypeVariable() throws Exception
    {
        TypeFactory tf = TypeFactory.defaultInstance();
        
        // Test with a recursive type like Comparable<T>
        class MyComparable<T extends Comparable<T>> {
            public T getValue() { return null; }
        }
        
        Method m = MyComparable.class.getMethod("getValue");
        JavaType t = tf.constructType(m.getGenericReturnType());
        assertNotNull(t);
        // Should resolve to a bounded type, not fail or loop infinitely
        assertEquals(Comparable.class, t.getRawClass());
    }