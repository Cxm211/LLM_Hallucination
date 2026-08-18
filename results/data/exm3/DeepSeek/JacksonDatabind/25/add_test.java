// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testModifyTypeByAnnotationWithNullKeyDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithMap bean = mapper.readValue("{\"map\":{\"key\":\"value\"}}", BeanWithMap.class);
        assertNotNull(bean.map);
        assertEquals("value", bean.map.get("key"));
    }
    static class BeanWithMap {
        public Map<String, String> map;
    }
