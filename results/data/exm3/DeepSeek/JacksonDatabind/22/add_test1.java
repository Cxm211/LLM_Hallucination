// com/fasterxml/jackson/databind/ser/TestJsonValue.java
public void testJsonValueWithCustomOverrideForMap() throws Exception
    {
        final MyMap INPUT = new MyMap();
        INPUT.put("key", "val");
        // by default, @JsonValue should be used
        assertEquals(quote("mapValue"), MAPPER.writeValueAsString(INPUT));
        // but custom serializer should override it
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule()
            .addSerializer(MyMap.class, new MyMapSerializer())
            );
        assertEquals("99", mapper.writeValueAsString(INPUT));
    }
    
    static class MyMap extends HashMap<String, String> {
        @JsonValue
        public String getMapValue() {
            return "mapValue";
        }
    }
    
    static class MyMapSerializer extends JsonSerializer<MyMap> {
        @Override
        public void serialize(MyMap value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(99);
        }
    }
