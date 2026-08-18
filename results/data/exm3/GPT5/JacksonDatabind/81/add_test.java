// com/fasterxml/jackson/databind/introspect/TypeCoercion1592Test.java::testTypeCoercion1592
public void testTypeCoercionMapKeyPrimitiveWrapper() throws Exception {
        class BeanMapKey {
            @com.fasterxml.jackson.databind.annotation.JsonSerialize(keyAs=int.class)
            public java.util.Map<Integer,Integer> values = new java.util.HashMap<Integer,Integer>();
        }
        BeanMapKey b = new BeanMapKey();
        b.values.put(1, 2);
        String json = MAPPER.writeValueAsString(b);
        assertNotNull(json);
    }