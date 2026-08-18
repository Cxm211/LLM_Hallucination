// com/fasterxml/jackson/databind/deser/jdk/MapDeserializerCachingTest.java
public void testMapWithCustomDeserializer() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"map\":{\"key1\":\"value1\",\"key2\":\"value2\"}}";
        // First deserialize with non-annotated map holder to cache deserializer
        PlainMapHolder ignored = mapper.readValue(json, PlainMapHolder.class);
        // Then deserialize with annotated holder
        CustomMapHolder custom = mapper.readValue(json, CustomMapHolder.class);
        // Custom deserializer should have transformed keys to uppercase
        if (!custom.map.containsKey(\"KEY1\") || !custom.map.containsKey(\"KEY2\")) {
            fail(\"Custom map deserializer not used: \" + custom.map);
        }
    }

    static class PlainMapHolder {
        public Map<String, String> map;
    }

    static class CustomMapHolder {
        @JsonDeserialize(using = UpperCaseKeysMapDeserializer.class)
        public Map<String, String> map;
    }

    static class UpperCaseKeysMapDeserializer extends JsonDeserializer<Map<String, String>> {
        @Override
        public Map<String, String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            Map<String, String> result = new HashMap<>();
            p.nextToken(); // START_OBJECT
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String key = p.getCurrentName();
                p.nextToken();
                String value = p.getText();
                result.put(key.toUpperCase(), value);
            }
            return result;
        }
    }
