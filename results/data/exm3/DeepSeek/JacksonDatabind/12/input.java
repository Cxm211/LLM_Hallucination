// buggy function
    public boolean isCachable() {
        /* As per [databind#735], existence of value or key deserializer (only passed
         * if annotated to use non-standard one) should also prevent caching.
         */
        return (_valueTypeDeserializer == null)
                && (_ignorableProperties == null);
    }

// trigger testcase
// com/fasterxml/jackson/databind/deser/TestCustomDeserializers.java::testCustomMapValueDeser735
public void testCustomMapValueDeser735() throws Exception {
        String json = "{\"map1\":{\"a\":1},\"map2\":{\"a\":1}}";
        TestMapBean735 bean = MAPPER.readValue(json, TestMapBean735.class);

        assertEquals(100, bean.map1.get("a").intValue());
        assertEquals(1, bean.map2.get("a").intValue());
    }
