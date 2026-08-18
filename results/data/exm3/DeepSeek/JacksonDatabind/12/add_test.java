// com/fasterxml/jackson/databind/deser/TestCustomDeserializers.java
public void testCustomMapKeyDeserializer735() throws Exception {
        String json = "{\"map\":{\"a\":1}}";
        TestMapKeyBean735 bean = MAPPER.readValue(json, TestMapKeyBean735.class);
        assertEquals(1, bean.map.get(\"A\").intValue());
    }
