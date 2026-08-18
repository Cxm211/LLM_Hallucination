// com/fasterxml/jackson/databind/type/TestLocalType609.java
public void testNestedGenericTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Test nested generic type variables
        class Container<T> {
            public T item;
        }
        
        class Nested<U extends Container<U>> {
            public U nested;
        }
        
        TypeFactory tf = mapper.getTypeFactory();
        JavaType type = tf.constructType(Nested.class);
        assertNotNull(type);
    }