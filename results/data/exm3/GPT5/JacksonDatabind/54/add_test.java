// com/fasterxml/jackson/databind/deser/TestJDKAtomicTypes.java
public void testEmpty1256TypeOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        BeanWithObjectAsAtomic b = new BeanWithObjectAsAtomic();
        String json = mapper.writeValueAsString(b);
        assertEquals("{}", json);
    }

    static class BeanWithObjectAsAtomic {
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(as = java.util.concurrent.atomic.AtomicReference.class)
        public Object value = new java.util.concurrent.atomic.AtomicReference<Object>(null);
    }