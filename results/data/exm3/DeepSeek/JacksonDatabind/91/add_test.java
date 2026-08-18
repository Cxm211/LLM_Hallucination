// com/fasterxml/jackson/databind/deser/jdk/MapDeserializerCachingTest.java
public void testListWithCustomDeserializer() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"list\":[\"a\",\"b\"]}";
        // First deserialize with non-annotated list holder to cache deserializer
        PlainListHolder ignored = mapper.readValue(json, PlainListHolder.class);
        // Then deserialize with annotated holder
        CustomListHolder custom = mapper.readValue(json, CustomListHolder.class);
        // Custom deserializer should have uppercased the strings
        if (!custom.list.get(0).equals(\"A\") || !custom.list.get(1).equals(\"B\")) {
            fail(\"Custom list deserializer not used: \" + custom.list);
        }
    }

    static class PlainListHolder {
        public List<String> list;
    }

    static class CustomListHolder {
        @JsonDeserialize(using = UpperCaseListDeserializer.class)
        public List<String> list;
    }

    static class UpperCaseListDeserializer extends JsonDeserializer<List<String>> {
        @Override
        public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            List<String> result = new ArrayList<>();
            p.nextToken(); // START_ARRAY
            while (p.nextToken() != JsonToken.END_ARRAY) {
                String str = p.getText();
                result.add(str.toUpperCase());
            }
            return result;
        }
    }
