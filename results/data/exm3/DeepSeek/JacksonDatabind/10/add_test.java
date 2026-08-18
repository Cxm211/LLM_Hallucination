// com/fasterxml/jackson/databind/ser/TestAnyGetter.java
public void testAnyGetterWithCustomValueSerializer() throws Exception {
        static class CustomValue {
            public String data;
            public CustomValue(String d) { data = d; }
        }
        static class CustomValueSerializer extends JsonSerializer<CustomValue> {
            @Override
            public void serialize(CustomValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("custom:" + value.data);
            }
        }
        static class BeanWithAnyGetter {
            private Map<String, CustomValue> map = new HashMap<>();
            public BeanWithAnyGetter() {
                map.put("key1", new CustomValue("val1"));
                map.put("key2", new CustomValue("val2"));
            }
            @JsonAnyGetter
            public Map<String, CustomValue> getMap() { return map; }
        }
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(CustomValue.class, new CustomValueSerializer());
        mapper.registerModule(module);
        BeanWithAnyGetter bean = new BeanWithAnyGetter();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"key1\":\"custom:val1\",\"key2\":\"custom:val2\"}", json);
    }
